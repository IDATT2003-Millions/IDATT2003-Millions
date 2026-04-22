package edu.ntnu.idi.idatt2003.controller;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Millions desktop application.
 */
public class MainApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    SceneController sceneController = new SceneController(primaryStage);
    sceneController.showLaunchPage();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
