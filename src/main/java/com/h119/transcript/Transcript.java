package com.h119.transcript;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

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

import com.formdev.flatlaf.*;

import com.h119.transcript.util.LanguageCodes;
import static com.h119.transcript.util.LanguageCodes.Language;

public class Transcript {
	private JFrame frame;
	private JComboBox<Object> languageBox;
	private JButton openFileButton;
	private JButton cancelButton;
	private JTextArea textArea;
	private JProgressBar progressBar;

	private OcrWorker worker = null;

	private static final int MARGIN = 10;
	private static final FlatLightLaf lightTheme;

	static {
		lightTheme = new FlatLightLaf();
	}

	public Transcript() {
		var languageLabel = new JLabel("Language:");
		languageBox = new JComboBox<>(
			getTrainedLanguages()
		);
		openFileButton = new JButton("Open PDF file");

		textArea = new JTextArea(20, 70);
		textArea.setEditable(false);

		var scroll = new JScrollPane(textArea);

		frame = new JFrame("Transcript");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		progressBar = new JProgressBar(0, 1000);

		openFileButton.addActionListener(this::openFilePressed);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this::cancelPressed);
		cancelButton.setEnabled(false);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 0, MARGIN); // top, left, bottom, right

		controlPanel.add(languageLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;

		controlPanel.add(languageBox, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;

		controlPanel.add(openFileButton, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;

		controlPanel.add(cancelButton, gbc);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)); // top, left, bottom, right

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, MARGIN, 0); // top, left, bottom, right

		mainPanel.add(controlPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.BOTH;

		mainPanel.add(scroll, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(MARGIN, 0, 0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		mainPanel.add(progressBar, gbc);

		frame.add(mainPanel);
		frame.pack();
		frame.setVisible(true);

		frame.setLocationRelativeTo(null);
	}

	public static class StatusReport {
		public enum Type {
			MESSAGE,
			PROGRESS
		}

		public class InvalidValueAccess extends RuntimeException {
			private Type valueType;

			private static final long serialVersionUID = 0L;

			public InvalidValueAccess(Type valueType) {
				this.valueType = valueType;
			}

			@Override
			public String toString() {
				return String.format(
					"A StatusReport object's value was accessed through the wrong accessor (type: %s)",
					valueType
				);
			}
		}

		private final Type type;
		private final Object value;

		public StatusReport(String message) {
			this.type = Type.MESSAGE;
			this.value = message;
		}

		public StatusReport(Integer progress) {
			this.type = Type.PROGRESS;
			this.value = progress;
		}

		public Type getType() {
			return type;
		}

		public String getMessage() {
			if (type == Type.MESSAGE)
				return (String)value;

			throw new InvalidValueAccess(type);
		}

		public int getProgress() {
			if (type == Type.PROGRESS)
				return (Integer)value;

			throw new InvalidValueAccess(type);
		}
	}

	private void openFilePressed(ActionEvent e) {
		try {
			var documentLanguage = (Language)languageBox.getSelectedItem();
			var pdfFile = getFile(frame);

			worker = new OcrWorker(
				pdfFile, documentLanguage, textArea, progressBar,
				openFileButton, cancelButton
			);
			
			textArea.setText(
				String.format(
					"The selected document language is: %s\nOpening PDF file: %s\n",
					documentLanguage.getName(),
					pdfFile.getCanonicalPath()
				)
			);

			cancelButton.setEnabled(true);
			openFileButton.setEnabled(false);

			progressBar.setValue(0);

			worker.execute();
		}
		catch (Exception exception) {
			textArea.append(exception.toString());
		}
	}

	public static class OcrWorker extends SwingWorker<Void, StatusReport> {
		private File pdfFile;
		private Language documentLanguage;
		private JTextArea textArea;
		private JProgressBar progressBar;
		private JButton openFileButton;
		private JButton cancelButton;

		public OcrWorker(
			File pdfFile, Language documentLanguage,
			JTextArea textArea, JProgressBar progressBar,
			JButton openFileButton, JButton cancelButton
		) {
			this.pdfFile = pdfFile;
			this.documentLanguage = documentLanguage;
			this.textArea = textArea;
			this.progressBar = progressBar;
			this.openFileButton = openFileButton;
			this.cancelButton = cancelButton;
		}

		@Override
		protected Void doInBackground() throws Exception {
			try {
				String pdfFilePath = pdfFile.getCanonicalPath();
				String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
				var languageCode = documentLanguage.getAlpha3();
				var documentText = new StringBuilder();
				var documentLines = new ArrayList<String>();

				var imageFiles = new ArrayList<String>();

				PDDocument document = PDDocument.load(pdfFile);
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				int documentPages = document.getNumberOfPages();

				publish(
					new StatusReport(
						String.format(
							"The document consists of %d pages\n",
							documentPages
						)
					)
				);

				for (int page = 0; page < documentPages; ++page) {
					BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
					String imageFileName = fileNoExtension + "-" + (page+1) + ".png";

					publish(
						new StatusReport(
							String.format(
								"Saving %s...\n",
								imageFileName
							)
						)
					);

					imageFiles.add(imageFileName);
					ImageIOUtil.writeImage(bim, imageFileName, 300);

					if (isCancelled()) {
						publish(new StatusReport("Cancelled\n"));
						publish(new StatusReport(0));
						document.close();
						return null;
					}

					publish(new StatusReport(300 * (page + 1) / documentPages));
				}
				document.close();

				BytePointer outText;

				TessBaseAPI api = new TessBaseAPI();
				if (api.Init("tessdata", languageCode) != 0) {
					throw new RuntimeException("Could not initialize tesseract.");
				}

				publish(new StatusReport("Successfully initialized tesseract\n"));
				publish(new StatusReport("Starting OCR...\n"));

				int page = 0;
				for (var imageFile: imageFiles) {
					publish(new StatusReport(String.format("Performing OCR on %s\n", imageFile)));

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
						publish(new StatusReport("Cancelled\n"));
						publish(new StatusReport(0));
						api.End();
						return null;
					}
					
					publish(new StatusReport(300 + (300 * (page + 1) / documentPages)));
					page += 1;
				}

				api.End();


				publish(new StatusReport("Saving the text as a Word document...\n"));

				WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
				MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

				int currentLine = 0;
				int lineNumber = documentLines.size();

				for (var line: documentLines) {
					mainDocumentPart.addParagraphOfText(line);

					if (isCancelled()) {
						publish(new StatusReport("Cancelled\n"));
						publish(new StatusReport(0));
						return null;
					}
					
					publish(new StatusReport(600 + (400 * (currentLine + 1) / lineNumber)));
					currentLine += 1;
				}
				
				File exportFile = new File(fileNoExtension + ".docx");
				wordPackage.save(exportFile);

				publish(
					new StatusReport(
						String.format(
							"The Word file has been created: %s.docx\n",
							fileNoExtension
						)
					)
				);
			}
			catch (Exception te) {
				publish(new StatusReport(te.toString()));
			}

			return null;
		}

		@Override
		protected void process(List<StatusReport> chunks) {
			for (var status: chunks) {
				if (status.getType() == StatusReport.Type.PROGRESS) {
					progressBar.setValue(status.getProgress());
				}
				else {
					textArea.append(status.getMessage());
				}
			}
		}

		@Override
		protected void done() {
			try {
				get();
				textArea.append("Done");
				openFileButton.setEnabled(true);
				cancelButton.setEnabled(false);
			}
			catch (InterruptedException e) {
				textArea.append(e.toString() + "\n");
			}
			catch (ExecutionException e) {
				textArea.append(e.toString() + "\n");
			}
			catch (CancellationException e) {
				textArea.append("The cancelled task has finished executing.\n");
			}
		}
	};

	private void cancelPressed(ActionEvent e) {
		if (worker != null) {
			worker.cancel(true);
			worker = null;
			cancelButton.setEnabled(false);
			openFileButton.setEnabled(true);
		}
	}

	private File getFile(JFrame parent) throws FileNotFoundException {
		JFileChooser jfc = new JFileChooser(".");

		jfc.setDialogTitle("Open PDF file");
		
		jfc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF files", "pdf");
		jfc.addChoosableFileFilter(filter);

		int returnValue = jfc.showOpenDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();

			if (file == null) throw new FileNotFoundException();

			return file;
		}
		else throw new FileNotFoundException();
	}

	public static void main(String[] args) {
		try {
				UIManager.setLookAndFeel(lightTheme);
			}
		catch (UnsupportedLookAndFeelException flatlafException) {
			System.out.format("Couldn't set flatlaf: %s\n", flatlafException);
		}
		catch (Exception exception) {
			System.out.format("Error when setting look and feel: %s\n", exception);
		}

		javax.swing.SwingUtilities.invokeLater(() -> {new Transcript();});
	}

	private static Object[] getTrainedLanguages() {
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
					.collect(Collectors.toList())
					.toArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
