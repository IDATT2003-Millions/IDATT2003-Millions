package edu.ntnu.idi.idatt2003;

import edu.ntnu.idi.idatt2003.controller.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX entry point for the Millions desktop application. */
public class MainApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    SceneController sceneController = new SceneController(primaryStage);
    sceneController.showLaunchPage();
  }

  /**
   * Application entry point.
   *
   * @param args command-line arguments passed to the JavaFX launcher
   */
  public static void main(String[] args) {
    launch(args);
  }
}
