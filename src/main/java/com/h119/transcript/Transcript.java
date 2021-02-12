package com.h119.transcript;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.stage.FileChooser.ExtensionFilter;

import org.bytedeco.javacpp.*;
import org.bytedeco.leptonica.*;
import org.bytedeco.tesseract.*;
import static org.bytedeco.leptonica.global.lept.*;
import static org.bytedeco.tesseract.global.tesseract.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import com.h119.transcript.util.LanguageCodes;
import static com.h119.transcript.util.LanguageCodes.Language;

public class Transcript extends Application {
	private ComboBox<Language> languageBox;
	private Button openFileButton;
	private TextArea textArea;
	private ProgressBar progressBar;

	private Stage mainStage;

	private static final int MARGIN = 10;

	public void start(final Stage stage) throws Exception {
		mainStage = stage;

		final var languageLabel = new Label("Language:");
		
		languageBox = new ComboBox<>();
		languageBox.getItems().addAll(getTrainedLanguages());
		languageBox.setValue(languageBox.getItems().get(0));

		openFileButton = new Button("Open PDF file");
		textArea = new TextArea();

		progressBar = new ProgressBar();
		progressBar.setProgress(0);

		openFileButton.setOnAction(this::openFilePressed);

		final var controlBox = new HBox(
			languageLabel,
			languageBox,
			openFileButton
		);

		controlBox.setSpacing(MARGIN);
		controlBox.setAlignment(Pos.CENTER_LEFT);

		final var layout = new VBox(
			controlBox,
			textArea,
			progressBar
		);

		layout.setSpacing(MARGIN);
		layout.setPadding(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

		VBox.setVgrow(controlBox, Priority.NEVER);
		VBox.setVgrow(textArea, Priority.ALWAYS);
		VBox.setVgrow(progressBar, Priority.NEVER);

		progressBar.setMinHeight(Double.NEGATIVE_INFINITY);
		progressBar.setMaxWidth(Double.MAX_VALUE);

		final var scene = new Scene(layout);
		stage.setScene(scene);
		stage.setTitle("Transcript");
		stage.centerOnScreen();
		stage.show();
	}

	private void openFilePressed(ActionEvent event) {
		try {
			var pdfFile = getFile(mainStage);
			var documentLanguage = languageBox.getValue();

			textArea.setText(
				String.format(
					"The selected document language is: %s\nOpening PDF file: %s\n",
					documentLanguage.getName(),
					pdfFile.getCanonicalPath()
				)
			);

			final var task = new OcrProcess(pdfFile, documentLanguage, textArea);

			progressBar.progressProperty().bind(task.progressProperty());

			task.messageProperty().addListener(
				(observableValue, oldValue, newValue) -> {
					textArea.appendText(newValue + "\n");
				}
			);

			new Thread(task).start();
		}
		catch (Exception exception) {
			textArea.appendText(
				String.format("Error: %s\n", exception)
			);
		}
	}

	public static class OcrProcess extends Task<Void> {

		private File pdfFile;
		private Language documentLanguage;
		private TextArea textArea;

		public OcrProcess(File pdfFile, Language documentLanguage, TextArea textArea) {
			this.pdfFile = pdfFile;
			this.documentLanguage = documentLanguage;
			this.textArea = textArea;
		}

		@Override
		public Void call() throws InterruptedException {
			try {
				var languageCode = documentLanguage.getAlpha3();
				String pdfFilePath = pdfFile.getCanonicalPath();
				String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
				var documentText = new StringBuilder();
				var documentLines = new ArrayList<String>();
				var imageFiles = new ArrayList<String>();

				PDDocument document = PDDocument.load(pdfFile);
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				var documentPages = document.getNumberOfPages();

				Platform.runLater(() -> {
					textArea.appendText(String.format("The document consists of %d pages\n", documentPages));
				});
				Platform.runLater(() -> {textArea.appendText("Saving the pages as PNG images...\n");});

				for (int page = 0; page < documentPages; ++page) { 
					BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

					String imageFileName = fileNoExtension + "-" + (page + 1) + ".png";

					Platform.runLater(() -> {textArea.appendText(String.format("Saving %s...\n", imageFileName));});

					imageFiles.add(imageFileName);
					ImageIOUtil.writeImage(bim, imageFileName, 300);

					if (isCancelled()) {
						Platform.runLater(() -> {textArea.appendText("Cancelled\n");});
						return null;
					}

					updateProgress(300 * (page + 1) / documentPages, 1000);
				}
				
				document.close();

				BytePointer outText;

				TessBaseAPI api = new TessBaseAPI();
				if (api.Init("tessdata", languageCode) != 0) {
					throw new RuntimeException("Could not initialize tesseract.");
				}

				Platform.runLater(() -> {textArea.appendText("Successfully initialized tesseract\n");});
				Platform.runLater(() -> {textArea.appendText("Starting OCR...\n");});

				int page = 0;
				for (var imageFile: imageFiles) {
					Platform.runLater(() -> {textArea.appendText(String.format("Performing OCR on %s\n", imageFile));});

					PIX image = pixRead(imageFile);
					api.SetImage(image);

					outText = api.GetUTF8Text();
					documentLines.addAll(
						Arrays.asList(
							new String(outText.getStringBytes(), StandardCharsets.UTF_8)
								.split("\n")
						)
					);

					outText.deallocate();
					pixDestroy(image);

					if (isCancelled()) {
						Platform.runLater(() -> {textArea.appendText("Cancelled\n");});
						return null;
					}

					updateProgress(300 + (300 * (page + 1) / documentPages), 1000);
					page += 1;
				}

				api.End();

				Platform.runLater(() -> {textArea.appendText("Saving the text as a Word document...\n");});

				WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
				MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

				int currentLine = 0;
				int lineNumber = documentLines.size();

				for (var line: documentLines) {
					mainDocumentPart.addParagraphOfText(line);

					if (isCancelled()) {
						updateMessage("Cancelled");
						return null;
					}

					updateProgress(600 + (400 * (currentLine + 1) / lineNumber), 1000);
					currentLine += 1;
				}
				
				File exportFile = new File(fileNoExtension + ".docx");
				wordPackage.save(exportFile);

				Platform.runLater(() -> {
					textArea.appendText(
						String.format("The Word file has been created: %s.docx\n", fileNoExtension)
					);
				});
			}
			catch (Exception e) {
				Platform.runLater(() -> {textArea.appendText(String.format("Error: %s\n", e));});
			}

			return null;
		}

		@Override
		protected void done() {
			super.done();
			Platform.runLater(() -> {textArea.appendText("Done\n");});
		}
	}

	private File getFile(Stage parent) throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open PDF File");
		fileChooser
			.getExtensionFilters()
			.addAll(
				new ExtensionFilter("PDF Files", "*.pdf")
			);

		File selectedFile = fileChooser.showOpenDialog(parent);

		if (selectedFile == null)
			throw new FileNotFoundException();

		return selectedFile;
	}

	private static List<Language> getTrainedLanguages() {
		try {
			return
				Files.list(Paths.get("tessdata"))
					.filter(file -> !Files.isDirectory(file))
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.endsWith(".traineddata"))
					.filter(name -> name.indexOf("_") == -1)
					.map(name -> name.substring(0,3))
					.map(name -> LanguageCodes.ofAlpha3(name).orElseThrow())
					.sorted(Comparator.comparing(Language::getName))
					.collect(Collectors.toList());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void bootstrap(String[] args) {
		launch(args);
	}

}
