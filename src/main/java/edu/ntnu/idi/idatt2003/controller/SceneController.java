package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.view.LaunchGameView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Owns scene switching. Start with launch page and expand as more views are added.
 */
public class SceneController {

  private static final String APP_TITLE = "Millions";
  private static final String GLOBAL_CSS = "/css_files/global.css";
  private final Stage stage;

  public SceneController(Stage stage) {
    this.stage = stage;
  }

  public void showLaunchPage() {
    Scene scene = new LaunchGameView().createScene(
        this::onNewGame,
        this::onLoadGame,
        Platform::exit
    );
    applyGlobalStyles(scene);

    stage.setTitle(APP_TITLE);
    stage.setMinWidth(900);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }

  private void applyGlobalStyles(Scene scene) {
    String stylesheet = getClass().getResource(GLOBAL_CSS).toExternalForm();
    scene.getStylesheets().add(stylesheet);
  }

  private void onNewGame() {
    showPlaceholder("New Game", "Hook this button to your game setup scene.");
  }

  private void onLoadGame() {
    showPlaceholder("Load Game", "Hook this button to your save/load flow.");
  }

  private void showPlaceholder(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.initOwner(stage);
    alert.setTitle(APP_TITLE);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
