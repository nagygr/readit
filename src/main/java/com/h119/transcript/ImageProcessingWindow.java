package com.h119.transcript;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        Button closeButton = new Button("Close this window");
        closeButton.setOnAction(e -> window.close());

        VBox layout = new VBox();
        layout.getChildren().addAll(imagePath, currentImage, closeButton);
        layout.setAlignment(Pos.CENTER);
		layout.setSpacing(MARGIN);
		layout.setPadding(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

        Scene scene = new Scene(layout);

		if (themeState == Transcript.ThemeState.DARK)
			scene.getStylesheets().add("/modena-dark.css");

		window.setScene(scene);
	}

	public void showAndWait() {
        window.showAndWait();
	}

	private void setupImage(String imageFile) {
		var image = new Image("file://" + imageFile);

		imagePath.setText(imageFile);
		currentImage.setImage(image);

		if (image.getWidth() > image.getHeight())
			currentImage.setFitWidth(imageWidth);
		else
			currentImage.setFitHeight(imageHeight);

		currentImage.setPreserveRatio(true);
		currentImage.setSmooth(true);
	}
}
