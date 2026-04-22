package edu.ntnu.idi.idatt2003.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Builds the startup screen shown when the app launches.
 */
public class LaunchGameView {

  public Scene createScene(Runnable onNewGame, Runnable onLoadGame, Runnable onExit) {
    Label title = new Label("Millions");
    title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 54));

    Label subtitle = new Label("Stock market board game");
    subtitle.setFont(Font.font("Verdana", 18));

    Button newGameButton = createActionButton("New Game", onNewGame);
    Button loadGameButton = createActionButton("Load Game", onLoadGame);
    Button exitButton = createActionButton("Exit", onExit);

    VBox content = new VBox(14, title, subtitle, newGameButton, loadGameButton, exitButton);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(40));
    content.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f8ff, #dae4ff);");

    return new Scene(content, 1100, 700);
  }

  private Button createActionButton(String label, Runnable action) {
    Button button = new Button(label);
    button.setPrefWidth(220);
    button.setPrefHeight(48);
    button.setFont(Font.font("Verdana", FontWeight.SEMI_BOLD, 16));
    button.setOnAction(event -> action.run());
    return button;
  }
}
