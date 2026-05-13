package edu.ntnu.idi.idatt2003.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsView {

  public Node buildContent(
      BigDecimal startingMoney,
      BigDecimal currentCash,
      BigDecimal portfolioValue,
      int sharesOwned,
      int uniqueStocks,
      int weeksPlayed,
      int totalTransactions,
      int totalBuys,
      int totalSells,
      BigDecimal totalBought,
      BigDecimal totalSold
  ) {
    Label title = new Label("Statistics");
    title.getStyleClass().add("page-title");

    BigDecimal netWorth = currentCash.add(portfolioValue);
    BigDecimal gain = netWorth.subtract(startingMoney);
    String gainSign = gain.signum() >= 0 ? "+" : "";
    BigDecimal gainPct = startingMoney.compareTo(BigDecimal.ZERO) == 0
        ? BigDecimal.ZERO
        : gain.divide(startingMoney, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

    VBox card1 = buildCard("Portfolio Overview",
        "Current Value",    fmt(portfolioValue),
        "Available Cash",   fmt(currentCash),
        "Net Worth",        fmt(netWorth),
        "Shares Held",      String.valueOf(sharesOwned),
        "Unique Stocks",    String.valueOf(uniqueStocks)
    );

    VBox card2 = buildCard("Performance",
        "Starting Capital", fmt(startingMoney),
        "Net Worth",        fmt(netWorth),
        "Gain / Loss",      gainSign + fmt(gain),
        "Return",           gainSign + gainPct.setScale(1, RoundingMode.HALF_UP) + "%"
    );

    VBox card3 = buildCard("Trading Activity",
        "Weeks Played",     String.valueOf(weeksPlayed),
        "Total Trades",     String.valueOf(totalTransactions),
        "Buys",             String.valueOf(totalBuys),
        "Sells",            String.valueOf(totalSells)
    );

    VBox card4 = buildCard("Transaction Volume",
        "Total Purchased",  fmt(totalBought),
        "Total Sold",       fmt(totalSold)
    );

    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);
    grid.add(card1, 0, 0);
    grid.add(card2, 1, 0);
    grid.add(card3, 0, 1);
    grid.add(card4, 1, 1);

    ColumnConstraints col = new ColumnConstraints();
    col.setPercentWidth(50);
    grid.getColumnConstraints().addAll(col, new ColumnConstraints() {{ setPercentWidth(50); }});

    VBox center = new VBox(16, title, grid);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(grid, Priority.ALWAYS);

    return center;
  }

  private VBox buildCard(String header, String... keyValues) {
    Label headerLabel = new Label(header);
    headerLabel.getStyleClass().add("stat-card-header");

    VBox card = new VBox(10, headerLabel);
    card.getStyleClass().add("stat-card");
    card.setMaxWidth(Double.MAX_VALUE);

    for (int i = 0; i < keyValues.length - 1; i += 2) {
      String key = keyValues[i];
      String value = keyValues[i + 1];

      Label keyLabel = new Label(key);
      keyLabel.getStyleClass().add("sidebar-section-label");

      Label valueLabel = new Label(value);
      valueLabel.getStyleClass().add("stat-card-value");

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      HBox row = new HBox(spacer, valueLabel);
      row.setAlignment(Pos.CENTER_LEFT);

      VBox entry = new VBox(2, keyLabel, row);
      card.getChildren().add(entry);
    }

    return card;
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
