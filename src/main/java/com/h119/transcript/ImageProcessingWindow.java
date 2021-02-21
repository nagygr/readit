package com.h119.transcript;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

class ImageProcessingWindow {
	private final ArrayList<String> imageFiles;
	private Stage window;

	private final static int MARGIN = 10;

	public ImageProcessingWindow(Transcript.ThemeState themeState, ArrayList<String> imageFiles) {
		this.imageFiles = imageFiles;

		window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Image processing");

        Label label = new Label();
        label.setText("Image processing placeholder");

        Button closeButton = new Button("Close this window");
        closeButton.setOnAction(e -> window.close());

        VBox layout = new VBox();
        layout.getChildren().addAll(label, closeButton);
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
}
