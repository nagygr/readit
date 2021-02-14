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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.stage.FileChooser.ExtensionFilter;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

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

import com.h119.transcript.util.LanguageCodes;
import static com.h119.transcript.util.LanguageCodes.Language;

public class Transcript extends Application {
	private ComboBox<Language> languageBox;
	private Button openFileButton;
	private TextArea textArea;
	private ProgressBar progressBar;
	private Button cancelButton;
	private Button themeButton;

	private enum ThemeState {LIGHT, DARK};
	private ThemeState themeState = ThemeState.LIGHT;

	private Stage mainStage;
	private Scene scene;

	private Task<Void> currentTask = null;

	private static final int MARGIN = 10;

	public void start(final Stage stage) throws Exception {
		mainStage = stage;

		final var languageLabel = new Label("Language:");
		
		languageBox = new ComboBox<>();
		languageBox.getItems().addAll(getTrainedLanguages());
		languageBox.setValue(languageBox.getItems().get(0));

		openFileButton = new Button("Open PDF file");

		textArea = new TextArea();
		textArea.setEditable(false);

		/*
		 * The snippet below is a workaround for a JavaFX bug that makes
		 * the text in a TextArea blurred on Windows. For details, see:
		 * https://stackoverflow.com/questions/23728517/blurred-text-in-javafx-textarea
		 */
		Platform.runLater(() -> {
			textArea.setCache(false);
			ScrollPane sp = (ScrollPane)textArea.getChildrenUnmodifiable().get(0);
			sp.setCache(false);
			for (Node n : sp.getChildrenUnmodifiable()) {
				n.setCache(false);
			}
		});
		/* -- END OF WORKAROUND CODE SNIPPET -- */

		progressBar = new ProgressBar();
		progressBar.setProgress(0);

		openFileButton.setOnAction(this::openFilePressed);

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction(this::cancelPressed);
		cancelButton.setDisable(true);

		themeButton = new Button("Dark theme");
		themeButton.setOnAction(this::themePressed);

		final var spacer = new Region();

		final var controlBox = new HBox(
			languageLabel,
			languageBox,
			openFileButton,
			cancelButton,
			spacer,
			themeButton
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

		HBox.setHgrow(spacer, Priority.ALWAYS);

		VBox.setVgrow(controlBox, Priority.NEVER);
		VBox.setVgrow(textArea, Priority.ALWAYS);
		VBox.setVgrow(progressBar, Priority.NEVER);

		progressBar.setMinHeight(Double.NEGATIVE_INFINITY);
		progressBar.setMaxWidth(Double.MAX_VALUE);

		scene = new Scene(layout);

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

			currentTask = new OcrProcess(pdfFile, documentLanguage, textArea, openFileButton, cancelButton);

			cancelButton.setDisable(false);
			openFileButton.setDisable(true);

			progressBar.progressProperty().bind(currentTask.progressProperty());

			currentTask.messageProperty().addListener(
				(observableValue, oldValue, newValue) -> {
					textArea.appendText(newValue + "\n");
				}
			);

			new Thread(currentTask).start();
		}
		catch (Exception exception) {
			textArea.appendText(
				String.format("Error: %s\n", exception)
			);
		}
	}

	public static class OcrProcess extends Task<Void> {

		private final File pdfFile;
		private final Language documentLanguage;
		private final TextArea textArea;
		private final Button openFileButton;
		private final Button cancelButton;

		public OcrProcess(
			File pdfFile, Language documentLanguage,
			TextArea textArea, Button openFileButton, Button cancelButton
		) {
			this.pdfFile = pdfFile;
			this.documentLanguage = documentLanguage;
			this.textArea = textArea;
			this.openFileButton = openFileButton;
			this.cancelButton = cancelButton;
		}

		@Override
		public Void call() throws InterruptedException {
			try {
				String languageCode = documentLanguage.getAlpha3();
				String pdfFilePath = pdfFile.getCanonicalPath();
				String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
				var documentText = new StringBuilder();
				var documentLines = new ArrayList<String>();
				var imageFiles = new ArrayList<String>();

				PDDocument document = PDDocument.load(pdfFile);
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				int documentPages = document.getNumberOfPages();

				updateProgress(0, 1000);

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
						updateProgress(0, 1000);
						document.close();
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
						updateProgress(0, 1000);
						api.End();
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
						Platform.runLater(() -> {textArea.appendText("Cancelled\n");});
						updateProgress(0, 1000);
						return null;
					}

					updateProgress(600 + (360 * (currentLine + 1) / lineNumber), 1000);
					currentLine += 1;
				}
				
				File exportFile = new File(fileNoExtension + ".docx");
				wordPackage.save(exportFile);

				updateProgress(1000, 1000);

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
			Platform.runLater(() -> {
				textArea.appendText("Done\n");
				openFileButton.setDisable(false);
				cancelButton.setDisable(true);
			});
		}
	}

	private void cancelPressed(ActionEvent event) {
		if (currentTask != null) {
			currentTask.cancel();
			currentTask = null;
			cancelButton.setDisable(true);
			openFileButton.setDisable(false);
		}
	}

	private void themePressed(ActionEvent event) {
		if (themeState == ThemeState.LIGHT) {
			themeState = ThemeState.DARK;
			themeButton.setText("Light theme");
			scene.getStylesheets().add("/modena-dark.css");
		}
		else {
			themeState = ThemeState.LIGHT;
			themeButton.setText("Dark theme");
			ObservableList<String> styleSheets = scene.getStylesheets();
			styleSheets.remove(0, styleSheets.size());
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
