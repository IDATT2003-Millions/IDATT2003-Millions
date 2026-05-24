package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Reusable left-hand sidebar that displays live player and exchange state.
 * Register it with the exchange once; it self-updates via ExchangeObserver.
 */
public class Sidebar extends VBox implements ExchangeObserver {

  private final Player player;
  private final Label cashLabel;
  private final Label netWorthLabel;
  private final Label weekLabel;
  private final Label statusLabel;

  /**
   * Creates the sidebar with navigation callbacks and a sell-all-and-quit action.
   *
   * @param player            the active player
   * @param exchange          the exchange to observe
   * @param onStockMarket     navigates to the stock market page
   * @param onPortfolio       navigates to the portfolio page
   * @param onTransactions    navigates to the transactions page
   * @param onStatistics      navigates to the statistics page
   * @param onSellAllAndQuit  sells all shares and exits the application
   */
  public Sidebar(Player player, Exchange exchange,
                 Runnable onStockMarket,
                 Runnable onPortfolio,
                 Runnable onTransactions,
                 Runnable onStatistics,
                 Runnable onSellAllAndQuit) {
    this.player = player;
    getStyleClass().add("sidebar");

    Label appTitle = new Label("Millions");
    appTitle.getStyleClass().add("sidebar-app-title");

    Label playerSection = new Label("PLAYER");
    playerSection.getStyleClass().add("sidebar-section-label");

    Label playerName = new Label(player.getName());
    playerName.getStyleClass().add("sidebar-value-primary");

    Label cashSection = new Label("CASH");
    cashSection.getStyleClass().add("sidebar-section-label");

    cashLabel = new Label(formatMoney(player.getMoney()));
    cashLabel.getStyleClass().add("sidebar-value");

    Label netWorthSection = new Label("NET WORTH");
    netWorthSection.getStyleClass().add("sidebar-section-label");

    netWorthLabel = new Label(formatMoney(player.getNetWorth()));
    netWorthLabel.getStyleClass().add("sidebar-value");

    Label weekSection = new Label("WEEK");
    weekSection.getStyleClass().add("sidebar-section-label");

    weekLabel = new Label(String.valueOf(exchange.getWeek()));
    weekLabel.getStyleClass().add("sidebar-value-week");

    Label statusSection = new Label("STATUS");
    statusSection.getStyleClass().add("sidebar-section-label");

    statusLabel = new Label(player.getStatus().name());
    statusLabel.getStyleClass().add("sidebar-value");

    Button btnMarket       = new Button("Stock Market");
    Button btnPortfolio    = new Button("My Portfolio");
    Button btnTransactions = new Button("Transactions");
    Button btnStatistics   = new Button("Statistics");
    Button btnSellAll      = new Button("Sell All & Quit");

    btnMarket.setMaxWidth(Double.MAX_VALUE);
    btnPortfolio.setMaxWidth(Double.MAX_VALUE);
    btnTransactions.setMaxWidth(Double.MAX_VALUE);
    btnStatistics.setMaxWidth(Double.MAX_VALUE);
    btnSellAll.setMaxWidth(Double.MAX_VALUE);
    btnSellAll.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");

    btnMarket.setOnAction(e       -> onStockMarket.run());
    btnPortfolio.setOnAction(e    -> onPortfolio.run());
    btnTransactions.setOnAction(e -> onTransactions.run());
    btnStatistics.setOnAction(e   -> onStatistics.run());
    btnSellAll.setOnAction(e -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Sell All & Quit");
      confirm.setHeaderText("Are you sure?");
      confirm.setContentText("This will sell all your shares and exit the game.");
      Optional<ButtonType> result = confirm.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        onSellAllAndQuit.run();
      }
    });

    getChildren().addAll(
        appTitle,
        new Separator(),
        playerSection, playerName,
        new Separator(),
        cashSection, cashLabel,
        netWorthSection, netWorthLabel,
        statusSection, statusLabel,
        new Separator(),
        weekSection, weekLabel,
        new Separator(),
        btnMarket, btnPortfolio, btnTransactions, btnStatistics,
        new Separator(),
        btnSellAll
    );

    exchange.addObserver(this);
  }

  @Override
  public void onExchangeUpdated(Exchange exchange) {
    cashLabel.setText(formatMoney(player.getMoney()));
    netWorthLabel.setText(formatMoney(player.getNetWorth()));
    weekLabel.setText(String.valueOf(exchange.getWeek()));
    statusLabel.setText(player.getStatus().name());
  }

  private String formatMoney(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
