package edu.ntnu.idi.idatt2003.view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Builds the startup screen shown when the app launches.
 */
public class LaunchGameView {

  public Scene createScene(Runnable onNewGame, Runnable onLoadGame, Runnable onExit) {
    Label title = new Label("Millions");
    title.getStyleClass().add("title");

    Label subtitle = new Label("Stock market board game");
    subtitle.getStyleClass().add("subtitle");

    Button newGameButton = createActionButton("New Game", "secondary-button", onNewGame);
    Button loadGameButton = createActionButton("Load Game", "secondary-button", onLoadGame);
    Button exitButton = createActionButton("Exit", "", onExit);

    VBox content = new VBox(14, title, subtitle, newGameButton, loadGameButton, exitButton);
    content.getStyleClass().add("launch-root");

    return new Scene(content, 1100, 700);
  }

  private Button createActionButton(String label, String variantClass, Runnable action) {
    Button button = new Button(label);
    button.getStyleClass().addAll("action-button", variantClass);
    button.setOnAction(event -> action.run());
    return button;
  }
}
