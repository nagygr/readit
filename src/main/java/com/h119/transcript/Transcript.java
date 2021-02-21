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
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.concurrent.Worker.State;
import static javafx.scene.control.Alert.AlertType;
import static javafx.stage.FileChooser.ExtensionFilter;

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
	private ImageView lightThemeIcon;
	private ImageView darkThemeIcon;

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

		lightThemeIcon = new ImageView("/lightTheme.png");
		darkThemeIcon = new ImageView("/darkTheme.png");

		themeButton = new Button();
		themeButton.setGraphic(darkThemeIcon);
		themeButton.setTooltip(new Tooltip("Change between light/dark theme"));
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

			var imageFiles = new ArrayList<String>();

			currentTask = new ImageCreationProcess(pdfFile, imageFiles, textArea);

			cancelButton.setDisable(false);
			openFileButton.setDisable(true);

			progressBar.progressProperty().bind(currentTask.progressProperty());

			currentTask.messageProperty().addListener(
				(observableValue, oldValue, newValue) -> {
					textArea.appendText(newValue + "\n");
				}
			);

			currentTask.stateProperty().addListener(
				(observableValue, oldValue, newValue) -> {
					if (newValue == Worker.State.SUCCEEDED) {
						var alert = new Alert(AlertType.INFORMATION);
						alert.setContentText("Image saving has finished successfully.");

						if (themeState == ThemeState.DARK) {
							alert
								.getDialogPane()
								.getStylesheets()
								.add("/modena-dark.css");
						}

						alert.showAndWait();

						progressBar.progressProperty().unbind();

						currentTask = new OcrProcess(pdfFile, documentLanguage, imageFiles, textArea, openFileButton, cancelButton);

						progressBar.progressProperty().bind(currentTask.progressProperty());

						currentTask.messageProperty().addListener(
							(observableMessage, oldMessage, newMessage) -> {
								textArea.appendText(newMessage + "\n");
							}
						);

						new Thread(currentTask).start();
					}
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
			themeButton.setGraphic(lightThemeIcon);
			scene.getStylesheets().add("/modena-dark.css");
		}
		else {
			themeState = ThemeState.LIGHT;
			themeButton.setGraphic(darkThemeIcon);
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
