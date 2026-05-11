package edu.ntnu.idi.idatt2003.view;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Setup screen shown before a new game begins.
 * Collects username, starting money, and a stock CSV file.
 */
public class NewGameView {

    public record NewGameData(String username, BigDecimal money, Path csvPath) {}

    public Scene createScene(Stage stage, Consumer<NewGameData> onStartGame) {
        Label title = new Label("New Game");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Configure your session");
        subtitle.getStyleClass().add("subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().addAll("input-bar");
        usernameField.setMaxWidth(360);

        TextField moneyField = new TextField();
        moneyField.setPromptText("Starting money  (e.g. 10000)");
        moneyField.getStyleClass().addAll("input-bar");
        moneyField.setMaxWidth(360);

        final File[] selectedFile = {null};

        Label fileLabel = new Label("No file selected");
        fileLabel.getStyleClass().add("file-label");

        Button browseButton = new Button("Browse…");
        browseButton.getStyleClass().addAll("action-button", "secondary-button");
        browseButton.setPrefWidth(110);
        browseButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Stock CSV File");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            File docs = new File(System.getProperty("user.home") + "/Documents");
            if (docs.exists()) {
                chooser.setInitialDirectory(docs);
            }
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
            }
        });

        HBox fileBar = new HBox(12, fileLabel, new Region(), browseButton);
        HBox.setHgrow(fileLabel, Priority.ALWAYS);
        fileBar.getStyleClass().add("file-bar");
        fileBar.setMaxWidth(360);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().addAll("action-button", "primary-button");
        startButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String moneyText = moneyField.getText().trim();

            if (username.isEmpty() || moneyText.isEmpty() || selectedFile[0] == null) {
                showWarning(stage, "Please fill in all fields and select a CSV file.");
                return;
            }

            BigDecimal money;
            try {
                money = new BigDecimal(moneyText);
                if (money.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showWarning(stage, "Starting money must be a positive number.");
                return;
            }

            onStartGame.accept(new NewGameData(username, money, selectedFile[0].toPath()));
        });

        VBox content = new VBox(16, title, subtitle, usernameField, moneyField, fileBar, startButton);
        content.getStyleClass().add("new-game-root");

        return new Scene(content, 1100, 700);
    }

    private void showWarning(Stage stage, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(stage);
        alert.setHeaderText("Missing or invalid input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}