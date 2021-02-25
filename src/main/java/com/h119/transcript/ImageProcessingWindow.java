package com.h119.transcript;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

class ImageProcessingWindow {
	private final ArrayList<String> imageFiles;
	private final int imageNumber;
	private int currentImageIndex;

	private Stage window;
	private Label imagePath;
	private ImageView currentImage;
	private TextField imageIndexField;
	private Button jumpButton;
	private Button leftJump;
	private Button rightJump;
	private Button rotateClockWise;
	private Button rotateCounterClockWise;
	private ImageView rotateClockWiseIconLight;
	private ImageView rotateClockWiseIconDark;
	private ImageView rotateCounterClockWiseIconLight;
	private ImageView rotateCounterClockWiseIconDark;

	private final static int MARGIN = 10;

	private static final Rectangle2D screenBounds;
	private static final double imageHeight;
	private static final double imageWidth;

	static {
		screenBounds = Screen.getPrimary().getBounds();
		imageHeight = screenBounds.getHeight() * 0.7;
		imageWidth = screenBounds.getWidth() * 0.4;
	}

	public ImageProcessingWindow(Transcript.ThemeState themeState, ArrayList<String> imageFiles) {
		this.imageFiles = imageFiles;
		this.imageNumber = imageFiles.size();

		currentImageIndex = 0;

		window = new Stage();

		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Image processing");

		imagePath = new Label();
		currentImage = new ImageView();

		var imageFile = imageFiles.get(currentImageIndex);

		setupImage(imageFile);

		imageIndexField = new TextField(String.format("%d", currentImageIndex + 1));

		jumpButton = new Button("Jump");
		jumpButton.setOnAction(this::jumpToNewPage);

		leftJump = new Button("<<");
		leftJump.setOnAction(this::jumpOne);
		rightJump = new Button(">>");
		rightJump.setOnAction(this::jumpOne);

		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> window.close());

		rotateClockWiseIconLight = new ImageView("/CWLight.png");
		rotateClockWiseIconDark = new ImageView("/CWDark.png");
		rotateCounterClockWiseIconLight = new ImageView("/CCWLight.png");
		rotateCounterClockWiseIconDark = new ImageView("/CCWDark.png");

		rotateClockWise = new Button();
		rotateClockWise.setOnAction(this::rotate);
		rotateCounterClockWise = new Button();
		rotateCounterClockWise.setOnAction(this::rotate);

		if (themeState == Transcript.ThemeState.DARK) {
			rotateClockWise.setGraphic(rotateClockWiseIconDark);
			rotateCounterClockWise.setGraphic(rotateCounterClockWiseIconDark);
		}
		else {
			rotateClockWise.setGraphic(rotateClockWiseIconLight);
			rotateCounterClockWise.setGraphic(rotateCounterClockWiseIconLight);
		}

		var leftSpacer = new Region();
		var rightSpacer = new Region();

		var jumpControlBox = new HBox();
		jumpControlBox.getChildren().addAll(
			leftJump,
			leftSpacer,
			imageIndexField,
			jumpButton,
			rightSpacer,
			rightJump
		);
		jumpControlBox.setAlignment(Pos.CENTER);
		jumpControlBox.setSpacing(MARGIN);
		jumpControlBox.setPadding(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

		HBox.setHgrow(leftSpacer, Priority.ALWAYS);
		HBox.setHgrow(rightSpacer, Priority.ALWAYS);

		var scrollPane = new ScrollPane();
		scrollPane.setContent(currentImage);

		/*
		 * The snippet below is a workaround for a JavaFX bug that makes
		 * the text in a TextArea blurred on Windows. For details, see:
		 * https://stackoverflow.com/questions/23728517/blurred-text-in-javafx-textarea
		 */
		Platform.runLater(() -> {
			scrollPane.setCache(false);
			for (Node n : scrollPane.getChildrenUnmodifiable()) {
				n.setCache(false);
			}
		});
		/* -- END OF WORKAROUND CODE SNIPPET -- */

		var rotationControl = new HBox();
		rotationControl.getChildren().addAll(
			rotateCounterClockWise,
			rotateClockWise
		);
		rotationControl.setAlignment(Pos.CENTER);
		rotationControl.setSpacing(MARGIN);
		rotationControl.setPadding(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

		var layout = new VBox();
		layout.getChildren().addAll(
			imagePath,
			jumpControlBox,
			scrollPane,
			rotationControl,
			closeButton
		);
		layout.setAlignment(Pos.CENTER);
		layout.setSpacing(MARGIN);
		layout.setPadding(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

		VBox.setVgrow(imagePath, Priority.NEVER);
		VBox.setVgrow(jumpControlBox, Priority.NEVER);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		VBox.setVgrow(rotationControl, Priority.NEVER);
		VBox.setVgrow(closeButton, Priority.NEVER);

		Scene scene = new Scene(layout);

		if (themeState == Transcript.ThemeState.DARK)
			scene.getStylesheets().add("/modena-dark.css");

		window.setScene(scene);
	}

	public void showAndWait() {
		window.showAndWait();
	}

	private void setupImage(String imageFile) {
		try {
			var image = new Image(new FileInputStream(imageFile));

			imagePath.setText(imageFile);
			currentImage.setImage(image);

			if (image.getWidth() > image.getHeight())
				currentImage.setFitWidth(imageWidth);
			else
				currentImage.setFitHeight(imageHeight);

			currentImage.setPreserveRatio(true);
			currentImage.setSmooth(true);
		}
		catch (FileNotFoundException exception) {
			System.err.format("Error loading file: %s\n", exception);
		}
	}

	private void jumpToNewPage(ActionEvent e) {
		try {
			jumpToIndex(
				Integer.parseInt(imageIndexField.getText()) - 1
			);
		}
		catch (NumberFormatException nfe) {
			imageIndexField.setText(String.format("%d", currentImageIndex + 1));
		}
	}

	private void jumpOne(ActionEvent e) {
		jumpToIndex(
			currentImageIndex + (e.getSource() == leftJump ? -1 : 1)
		);
	}

	private void jumpToIndex(int newIndex) {
		if (newIndex >= 0 && newIndex < imageFiles.size()) {
			var path = imageFiles.get(newIndex);
			setupImage(path);
			currentImageIndex = newIndex;
		}

		imageIndexField.setText(String.format("%d", currentImageIndex + 1));
	}

	private void rotate(ActionEvent e) {
		try {
			int angle = e.getSource() == rotateClockWise ? 90 : -90;
			String imageFileName = imageFiles.get(currentImageIndex);

			BufferedImage image = ImageIO.read(new File(imageFileName));
			final double rads = Math.toRadians(angle);
			final double sin = Math.abs(Math.sin(rads));
			final double cos = Math.abs(Math.cos(rads));
			final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
			final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
			final var rotatedImage = new BufferedImage(w, h, image.getType());
			final var at = new AffineTransform();
			at.translate(w / 2, h / 2);
			at.rotate(rads, 0, 0);
			at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
			final var rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			rotateOp.filter(image,rotatedImage);
			ImageIO.write(rotatedImage, "PNG", new File(imageFileName));

			setupImage(imageFileName);
		}
		catch (IOException ioe) {
			System.err.format("Exception thrown while trying to rotate image: %s\n", ioe);
			ioe.printStackTrace(System.err);
		}
	}
}
