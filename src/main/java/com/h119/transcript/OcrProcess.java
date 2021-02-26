package com.h119.transcript;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import com.h119.transcript.util.LanguageCodes;
import static com.h119.transcript.util.LanguageCodes.Language;

class OcrProcess extends Task<Void> {

	private final File pdfFile;
	private final Language documentLanguage;
	private final ArrayList<String> imageFiles;
	private final TextArea textArea;
	private final Button openFileButton;
	private final Button cancelButton;

	public OcrProcess(
		File pdfFile, Language documentLanguage, ArrayList<String> imageFiles,
		TextArea textArea, Button openFileButton, Button cancelButton
	) {
		this.pdfFile = pdfFile;
		this.documentLanguage = documentLanguage;
		this.imageFiles = imageFiles;
		this.textArea = textArea;
		this.openFileButton = openFileButton;
		this.cancelButton = cancelButton;
	}

	@Override
	public Void call() throws InterruptedException {
		try {
			String languageCode = documentLanguage.getAlpha3();
			var documentLines = new ArrayList<String>();
			String pdfFilePath = pdfFile.getCanonicalPath();
			String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));

			int documentPages = imageFiles.size();

			updateProgress(300, 1000);

			BytePointer outText;

			TessBaseAPI api = new TessBaseAPI();
			int errorCode = api.Init("tessdata", languageCode);
			if (errorCode != 0) {
				throw new RuntimeException(
					String.format("Could not initialize tesseract -- error code: %d", errorCode)
				);
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

