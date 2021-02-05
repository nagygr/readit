package com.h119.readit;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;
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

public class ReadIt {
	private static class Language {
		private String displayName;
		private String languageCode;

		public Language(String displayName, String languageCode) {
			this.displayName = displayName;
			this.languageCode = languageCode;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getLanguageCode() {
			return languageCode;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private JFrame frame;
	private JComboBox<Language> languageBox;
	private JButton openImageButton;
	private JLabel imagePathLabel;
	private JTextArea textArea;

	private static final int MARGIN = 10;
	private static final Language[] languages;
	private static final FlatLightLaf lightTheme;

	static {
		languages = new Language[] {
			new Language("Hungarian", "hun"),
			new Language("English", "eng"),
			new Language("Swedish", "sve"),
			new Language("Danish", "dan"),
			new Language("German", "deu"),
			new Language("Hebrew", "heb")
		};

		lightTheme = new FlatLightLaf();
	}

	public ReadIt() {
		var languageLabel = new JLabel("Language:");
		languageBox = new JComboBox<>(languages);
		openImageButton = new JButton("Open PDF file");
		imagePathLabel = new JLabel("");

		textArea = new JTextArea(20, 70);
		var scroll = new JScrollPane(textArea);

		frame = new JFrame("Read it");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		openImageButton.addActionListener(this::imageSelectorPressed);

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

		controlPanel.add(openImageButton, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;

		controlPanel.add(imagePathLabel, gbc);

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

		frame.add(mainPanel);
		frame.pack();
		frame.setVisible(true);

		frame.setLocationRelativeTo(null);
	}

	private void imageSelectorPressed(ActionEvent e) {
		try {
			var language = ((Language)languageBox.getSelectedItem()).getLanguageCode();
			var pdfFile = getFile(frame);
			String pdfFilePath = pdfFile.getCanonicalPath();
			String fileNoExtension = pdfFilePath.substring(0, pdfFilePath.lastIndexOf("."));
			var documentText = new StringBuilder();
			var documentLines = new ArrayList<String>();
			
			imagePathLabel.setText(truncateLongPath(pdfFile.getCanonicalPath()));
			textArea.setText("Working...\n");

			// The OCR process is started on a new thread so that this function can
			// return and the path of the selected file can be displayed immediately.
			// The reason is that OCR can take some time and it looks weird that the
			// path appears with a delay as well.
			Timer timer = new Timer(1,
				(ActionEvent timerEvent) -> {
					try {
						var imageFiles = new ArrayList<String>();

						PDDocument document = PDDocument.load(pdfFile);
						PDFRenderer pdfRenderer = new PDFRenderer(document);
						for (int page = 0; page < document.getNumberOfPages(); ++page)
						{ 
							BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

							// suffix in filename will be used as the file format
							String imageFileName = fileNoExtension + "-" + (page+1) + ".png";

							// Below are two alternative ways to geenrate the images
							//*
							imageFiles.add(imageFileName);
							ImageIOUtil.writeImage(bim, imageFileName, 300);
							// */
							
							/*
							imageFiles.add(imageFileName);
							File tempFile = new File(imageFileName);
							ImageIO.write(bim, "png", tempFile);
							// */
						}
						document.close();

						BytePointer outText;

						TessBaseAPI api = new TessBaseAPI();
						if (api.Init("data", language) != 0) {
							throw new RuntimeException("Could not initialize tesseract.");
						}

						for (var imageFile: imageFiles) {
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
						}

						api.End();

						WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
						MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

						for (var line: documentLines) {
							mainDocumentPart.addParagraphOfText(line);
						}
						
						File exportFile = new File(fileNoExtension + ".docx");
						wordPackage.save(exportFile);

						textArea.setText(String.format("The Word file has been created: %s.docx\n", fileNoExtension));
					}
					catch (Exception te) {
						textArea.setText(te.toString());
					}
				}
			);
			timer.setRepeats(false);
			timer.start();

		}
		catch (Exception exception) {
			textArea.setText(exception.toString());
		}
	}

	private static String truncateLongPath(String path) {
		final int maxLength = 40;
		int length = path.length();
		if (length < maxLength) return path;
		return String.format("...%s", path.substring(length - (maxLength - 3)));
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

		javax.swing.SwingUtilities.invokeLater(() -> {new ReadIt();});
	}
}
