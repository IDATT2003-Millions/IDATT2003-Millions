package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.file.StockCsvRepository;
import edu.ntnu.idi.idatt2003.model.transactions.Player;

import java.util.List;
import edu.ntnu.idi.idatt2003.view.LaunchGameView;
import edu.ntnu.idi.idatt2003.view.NewGameView;
import edu.ntnu.idi.idatt2003.view.Sidebar;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 Owns scene switching. Add showXxxPage() methods here as new views are created.
 */
public class SceneController {

  private static final String APP_TITLE = "Millions";
  private static final String GLOBAL_CSS = "/css_files/global.css";
  private final Stage stage;

  private BorderPane gameRoot;
  private Scene gameScene;

  private StockMarketController stockMarketController;
  private PortfolioController portfolioController;
  private TransactionsController transactionsController;
  private StatisticsController statisticsController;

  public SceneController(Stage stage) {
    this.stage = stage;
  }

  public void showLaunchPage() {
    Scene scene = new LaunchGameView().createScene(
        this::showNewGamePage,
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

  public void showNewGamePage() {
    Scene scene = new NewGameView().createScene(stage, data -> {
      try {
        var stocks = new StockCsvRepository().load(data.csvPath());
        if (stocks.isEmpty()) {
          showError("The selected CSV file contains no valid stocks.");
          return;
        }
        Player player = new Player(data.username(), data.money());
        Exchange exchange = new Exchange("Millions Exchange", stocks);
        showStockMarketPage(player, exchange);
      } catch (IOException e) {
        showError("Could not read CSV file:\n" + e.getMessage());
      } catch (IllegalArgumentException e) {
        showError("Invalid game setup:\n" + e.getMessage());
      }
    });
    applyGlobalStyles(scene);
    stage.setScene(scene);
  }

  public void showStockMarketPage(Player player, Exchange exchange) {
    initGameSceneIfNeeded(player, exchange);
    gameRoot.setCenter(stockMarketController.buildContent());
  }

  public void showPortfolioPage(Player player, Exchange exchange) {
    initGameSceneIfNeeded(player, exchange);
    gameRoot.setCenter(portfolioController.buildContent());
  }

  public void showTransactionsPage(Player player, Exchange exchange) {
    initGameSceneIfNeeded(player, exchange);
    gameRoot.setCenter(transactionsController.buildContent());
  }

  public void showStatisticsPage(Player player, Exchange exchange) {
    initGameSceneIfNeeded(player, exchange);
    gameRoot.setCenter(statisticsController.buildContent());
  }

  private void initGameSceneIfNeeded(Player player, Exchange exchange) {
    if (gameScene != null) return;

    stockMarketController  = new StockMarketController(exchange, player, stage);
    portfolioController    = new PortfolioController(exchange, player, stage);
    transactionsController = new TransactionsController(exchange, player);
    statisticsController   = new StatisticsController(exchange, player);

    gameRoot = new BorderPane();
    gameRoot.getStyleClass().add("market-root");
    Sidebar sidebar = new Sidebar(
        player, exchange,
        () -> showStockMarketPage(player, exchange),
        () -> showPortfolioPage(player, exchange),
        () -> showTransactionsPage(player, exchange),
        () -> showStatisticsPage(player, exchange),
        () -> sellAllAndQuit(player, exchange)
    );
    gameRoot.setLeft(sidebar);
    gameScene = new Scene(gameRoot);
    applyGlobalStyles(gameScene);
    stage.setScene(gameScene);
  }
  private void sellAllAndQuit(Player player, Exchange exchange) {
    List<Share> shares = List.copyOf(player.getPortfolio().getShares());
    for (Share share : shares) {
      exchange.sell(share, player).commit(player);
    }
    Platform.exit();
  }

  private void onLoadGame() {
    showPlaceholder("Load Game", "Hook this button to your save/load flow.");
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void applyGlobalStyles(Scene scene) {
    String stylesheet = getClass().getResource(GLOBAL_CSS).toExternalForm();
    scene.getStylesheets().add(stylesheet);
  }


  private void showPlaceholder(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.initOwner(stage);
    alert.setTitle(APP_TITLE);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.initOwner(stage);
    alert.setTitle(APP_TITLE);
    alert.setHeaderText("Error");
    alert.setContentText(message);
    alert.showAndWait();
  }
}
