package com.h119.transcript;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
		jumpButton.setOnAction(e -> jumpToNewPage());

		leftJump = new Button("<<");
		leftJump.setOnAction(this::jumpOne);
		rightJump = new Button(">>");
		rightJump.setOnAction(this::jumpOne);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

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

        var layout = new VBox();
        layout.getChildren().addAll
		(
			imagePath,
			jumpControlBox,
			scrollPane,
			closeButton
		);
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

	private void jumpToNewPage() {
		try {
			int newIndex = Integer.parseInt(imageIndexField.getText()) - 1;

			if (newIndex >= 0 && newIndex < imageFiles.size()) {
				var path = imageFiles.get(newIndex);
				setupImage(path);
				currentImageIndex = newIndex;
			}
		}
		catch (NumberFormatException nfe) {
		}

		imageIndexField.setText(String.format("%d", currentImageIndex + 1));
	}

	private void jumpOne(ActionEvent e) {
		try {
			int currentPage = Integer.parseInt(imageIndexField.getText());

			if (e.getSource() == leftJump) {
				imageIndexField.setText(String.format("%d", currentPage - 1));
			}
			else {
				imageIndexField.setText(String.format("%d", currentPage + 1));
			}

			jumpToNewPage();
		}
		catch (NumberFormatException nfe) {
		}

	}
}
