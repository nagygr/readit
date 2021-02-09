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
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
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
		}
		catch (Exception exception) {
			textArea.appendText(
				String.format("Error: %s\n", exception)
			);
		}

		//// TODO
		//try {
		//	var language = ((Language)languageBox.getSelectedItem()).getAlpha3();
		//	var pdfFile = getFile(frame);
		//	String pdfFilePath = pdfFile.getCanonicalPath();
		//	String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
		//	var documentText = new StringBuilder();
		//	var documentLines = new ArrayList<String>();
		//	
		//	imagePathLabel.setText(truncateLongPath(pdfFile.getCanonicalPath()));
		//	textArea.setText("Working...\n");

		//	// The OCR process is started on a new thread so that this function can
		//	// return and the path of the selected file can be displayed immediately.
		//	// The reason is that OCR can take some time and it looks weird that the
		//	// path appears with a delay as well.
		//	Timer timer = new Timer(1,
		//		(ActionEvent timerEvent) -> {
		//			try {
		//				var imageFiles = new ArrayList<String>();

		//				PDDocument document = PDDocument.load(pdfFile);
		//				PDFRenderer pdfRenderer = new PDFRenderer(document);
		//				for (int page = 0; page < document.getNumberOfPages(); ++page)
		//				{ 
		//					BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

		//					// suffix in filename will be used as the file format
		//					String imageFileName = fileNoExtension + "-" + (page+1) + ".png";

		//					// Below are two alternative ways to geenrate the images
		//					//*
		//					imageFiles.add(imageFileName);
		//					ImageIOUtil.writeImage(bim, imageFileName, 300);
		//					// */
		//					
		//					/*
		//					imageFiles.add(imageFileName);
		//					File tempFile = new File(imageFileName);
		//					ImageIO.write(bim, "png", tempFile);
		//					// */
		//				}
		//				document.close();

		//				BytePointer outText;

		//				TessBaseAPI api = new TessBaseAPI();
		//				if (api.Init("tessdata", language) != 0) {
		//					throw new RuntimeException("Could not initialize tesseract.");
		//				}

		//				for (var imageFile: imageFiles) {
		//					PIX image = pixRead(imageFile);
		//					api.SetImage(image);

		//					outText = api.GetUTF8Text();
		//					documentLines.addAll(
		//						Arrays.asList(
		//							new String(outText.getStringBytes(), StandardCharsets.UTF_8)
		//								.split("\n")
		//						)
		//					);

		//					outText.deallocate();
		//					pixDestroy(image);
		//				}

		//				api.End();

		//				WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
		//				MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

		//				for (var line: documentLines) {
		//					mainDocumentPart.addParagraphOfText(line);
		//				}
		//				
		//				File exportFile = new File(fileNoExtension + ".docx");
		//				wordPackage.save(exportFile);

		//				textArea.setText(String.format("The Word file has been created: %s.docx\n", fileNoExtension));
		//			}
		//			catch (Exception te) {
		//				textArea.setText(te.toString());
		//			}
		//		}
		//	);
		//	timer.setRepeats(false);
		//	timer.start();

		//}
		//catch (Exception exception) {
		//	textArea.setText(exception.toString());
		//}
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
