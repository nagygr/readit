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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

class ImageCreationProcess extends Task<Void> {

	private final File pdfFile;
	private final ArrayList<String> imageFiles;
	private final TextArea textArea;

	public ImageCreationProcess(
		File pdfFile, ArrayList<String> imageFiles,
		TextArea textArea
	) {
		this.pdfFile = pdfFile;
		this.imageFiles = imageFiles;
		this.textArea = textArea;
	}

	@Override
	public Void call() throws InterruptedException {
		try {
			String pdfFilePath = pdfFile.getCanonicalPath();
			String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
			var documentLines = new ArrayList<String>();

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
		}
		catch (Exception e) {
			Platform.runLater(() -> {textArea.appendText(String.format("Error: %s\n", e));});
		}

		return null;
	}

	@Override
	protected void done() {
		super.done();
	}

}
