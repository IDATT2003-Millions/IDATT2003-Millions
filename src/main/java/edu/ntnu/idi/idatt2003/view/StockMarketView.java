package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.calculators.PurchaseCalculator;
import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Main stock market screen.
 * Layout: static Sidebar on the left, scrollable market content on the right.
 */
public class StockMarketView implements ExchangeObserver {

  private final Player player;
  private final Exchange exchange;
  private final Stage stage;

  private final ObservableList<Stock> stockList = FXCollections.observableArrayList();
  private TableView<Stock> table;
  private VBox gainersBox;
  private VBox losersBox;
  private TextField searchField;

  private static final String GLOBAL_CSS = "/css_files/global.css";

  private BiConsumer<Stock, BigDecimal> onBuy;
  private Consumer<Share> onSell;

  /**
   * Creates the stock market view and registers it as an observer on the exchange.
   *
   * @param player   the active player
   * @param exchange the exchange to display
   * @param stage    the primary stage (used as dialog owner)
   */
  public StockMarketView(Player player, Exchange exchange, Stage stage) {
    this.player = player;
    this.exchange = exchange;
    this.stage = stage;
    exchange.addObserver(this);
  }

  /**
   * Builds and returns the market content node.
   *
   * @param onAdvanceWeek advances the exchange by one week
   * @param onBuy         callback to buy a stock at the given quantity
   * @param onSell        callback to sell an owned share
   * @return the root node for this page
   */
  public Node buildContent(Runnable onAdvanceWeek,
                           BiConsumer<Stock, BigDecimal> onBuy,
                           Consumer<Share> onSell) {
    this.onBuy = onBuy;
    this.onSell = onSell;

    searchField = new TextField();
    searchField.setPromptText("Search stocks…");
    searchField.getStyleClass().add("search-field");
    searchField.textProperty().addListener((obs, old, text) -> refreshStockList(text));

    Button nextWeekBtn = new Button("Next Week ▶");
    nextWeekBtn.getStyleClass().addAll("action-button", "primary-button");
    nextWeekBtn.setOnAction(e -> onAdvanceWeek.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox topBar = new HBox(12, searchField, spacer, nextWeekBtn);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    topBar.getStyleClass().add("top-bar");

    table = buildTable();
    refreshStockList("");

    gainersBox = buildMiniPanel("Top Gainers");
    losersBox  = buildMiniPanel("Top Losers");
    refreshGainersLosers();

    HBox bottomPanel = new HBox(16, gainersBox, losersBox);
    bottomPanel.getStyleClass().add("bottom-panel");
    HBox.setHgrow(gainersBox, Priority.ALWAYS);
    HBox.setHgrow(losersBox, Priority.ALWAYS);

    VBox center = new VBox(12, topBar, table, bottomPanel);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(table, Priority.ALWAYS);

    return center;
  }

  // ── Table ────────────────────────────────────────────────────────────────

  private TableView<Stock> buildTable() {
    TableView<Stock> tv = new TableView<>(stockList);
    tv.getStyleClass().add("stock-table");
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.setPlaceholder(new Label("No stocks found"));

    TableColumn<Stock, String> symbolCol = new TableColumn<>("Symbol");
    symbolCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSymbol()));
    symbolCol.setMaxWidth(100);

    TableColumn<Stock, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompany()));

    TableColumn<Stock, String> priceCol = new TableColumn<>("Price");
    priceCol.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().getSalesPrice())));
    priceCol.setMaxWidth(130);

    TableColumn<Stock, String> changeCol = new TableColumn<>("Change");
    changeCol.setCellValueFactory(d -> {
      BigDecimal ch = d.getValue().getLatestPriceChange();
      String sign = ch.signum() >= 0 ? "+" : "";
      return new SimpleStringProperty(sign + fmt(ch));
    });
    changeCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("positive-change", "negative-change");
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item);
          getStyleClass().add(item.startsWith("+") ? "positive-change" : "negative-change");
        }
      }
    });
    changeCol.setMaxWidth(130);

    TableColumn<Stock, Void> infoCol = new TableColumn<>("");
    infoCol.setMaxWidth(70);
    infoCol.setCellFactory(col -> new TableCell<>() {
      private final Button btn = new Button("📊 Info");
      {
        btn.setStyle("-fx-font-size: 11px; -fx-padding: 2 6 2 6;");
        btn.setOnAction(e -> showStockInfoDialog(getTableView().getItems().get(getIndex())));
      }
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    });

    tv.getColumns().addAll(List.of(symbolCol, companyCol, priceCol, changeCol, infoCol));

    tv.setRowFactory(tableView -> {
      TableRow<Stock> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (!row.isEmpty() && event.getClickCount() == 2) {
          showBuySellDialog(row.getItem());
        }
      });
      return row;
    });

    return tv;
  }

  // ── Stock Info dialog (statistics) ───────────────────────────────────────

  private void showStockInfoDialog(Stock stock) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setTitle("Stock Details");
    dialog.setHeaderText(stock.getSymbol() + "  —  " + stock.getCompany());

    GridPane grid = new GridPane();
    grid.setHgap(24);
    grid.setVgap(8);
    grid.setPadding(new Insets(12));

    addInfoRow(grid, 0, "Current Price",  fmt(stock.getSalesPrice()));
    addInfoRow(grid, 1, "All-time High",  fmt(stock.getHighestPrice()));
    addInfoRow(grid, 2, "All-time Low",   fmt(stock.getLowestPrice()));
    addInfoRow(grid, 3, "Last Change",    signedFmt(stock.getLatestPriceChange()));

    Label historyTitle = new Label("Price History");
    historyTitle.getStyleClass().add("info-section-title");

    ListView<String> historyList = new ListView<>();
    historyList.getStyleClass().add("history-list");
    List<BigDecimal> prices = stock.getHistoricalPrices();
    for (int i = 0; i < prices.size(); i++) {
      historyList.getItems().add("Week " + (i + 1) + ":  " + fmt(prices.get(i)));
    }
    historyList.setPrefHeight(150);
    historyList.setMaxWidth(300);

    PriceChart chart = new PriceChart(stock.getHistoricalPrices(), 110);
    VBox content = new VBox(8, grid, chart, historyTitle, historyList);
    content.setMinWidth(320);
    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    applyTheme(dialog.getDialogPane());

    dialog.showAndWait();
  }

  private void addInfoRow(GridPane grid, int row, String key, String value) {
    Label keyLabel = new Label(key + ":");
    keyLabel.getStyleClass().add("info-key");
    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add("info-value");
    grid.add(keyLabel, 0, row);
    grid.add(valueLabel, 1, row);
  }

  // ── Buy / Sell dialog ────────────────────────────────────────────────────

  private void showBuySellDialog(Stock stock) {
    List<Share> owned = player.getPortfolio().getShares(stock.getSymbol());

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setTitle(stock.getSymbol() + "  —  " + stock.getCompany());
    dialog.setHeaderText("Current price: " + fmt(stock.getSalesPrice()));

    Label cashInfo = new Label("Your cash: " + fmt(player.getMoney()));
    cashInfo.getStyleClass().add("dialog-info");

    BigDecimal totalOwnedQty = owned.stream()
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Label ownedInfo = new Label(owned.isEmpty()
        ? "You own: 0 shares"
        : "You own: " + totalOwnedQty.toPlainString() + " shares");
    ownedInfo.getStyleClass().add("dialog-info");

    TextField qtyField = new TextField();
    qtyField.setPromptText("Quantity to buy");

    // ── Buy preview ──
    Label buyTitle = new Label("Buy cost estimate:");
    buyTitle.getStyleClass().add("info-value");
    Label buyGross      = new Label();
    Label buyCommission = new Label();
    Label buyTotal      = new Label();
    buyTotal.getStyleClass().add("info-value");
    VBox buyPreview = new VBox(3, buyTitle, buyGross, buyCommission, buyTotal);
    buyPreview.getStyleClass().add("buy-preview");

    // ── Sell preview (first owned lot) ──
    VBox sellPreview = new VBox(3);
    sellPreview.getStyleClass().add("sell-preview");
    if (!owned.isEmpty()) {
      Share firstLot = owned.getFirst();
      Label sellTitle      = new Label("Sell estimate (your " + firstLot.getQuantity().toPlainString() + " shares):");
      sellTitle.getStyleClass().add("info-value");
      SaleCalculator sellCalc = new SaleCalculator(firstLot);
      Label sellGross      = new Label("Market value:  " + fmt(sellCalc.calculateGross()));
      Label sellCommission = new Label("Commission (1%):  -" + fmt(sellCalc.calculateCommission()));
      Label sellTax        = new Label("Tax (30% profit):  -" + fmt(sellCalc.calculateTax()));
      Label sellTotal      = new Label("You receive:  " + fmt(sellCalc.calculateTotal()));
      sellTotal.getStyleClass().add("info-value");
      sellPreview.getChildren().addAll(sellTitle, sellGross, sellCommission, sellTax, sellTotal);
    }

    // Update buy preview as user types
    qtyField.textProperty().addListener((obs, old, text) -> {
      try {
        BigDecimal qty = new BigDecimal(text.trim());
        if (qty.signum() > 0) {
          Share tempShare = new Share(stock, qty, stock.getSalesPrice());
          PurchaseCalculator calc = new PurchaseCalculator(tempShare);
          buyGross.setText("Market value:  " + fmt(calc.calculateGross()));
          buyCommission.setText("Commission (0.5%):  " + fmt(calc.calculateCommission()));
          buyTotal.setText("Total cost:  " + fmt(calc.calculateTotal()));
        } else {
          clearBuyPreview(buyGross, buyCommission, buyTotal);
        }
      } catch (Exception e) {
        clearBuyPreview(buyGross, buyCommission, buyTotal);
      }
    });

    PriceChart chart = new PriceChart(stock.getHistoricalPrices(), 100);
    VBox content = new VBox(10, cashInfo, ownedInfo,
        new Separator(),
        chart,
        new Label("Quantity to buy:"), qtyField, buyPreview);
    if (!owned.isEmpty()) {
      content.getChildren().addAll(new Separator(), sellPreview);
    }
    content.setMinWidth(340);
    dialog.getDialogPane().setContent(content);
    applyTheme(dialog.getDialogPane());

    ButtonType buyType  = new ButtonType("Buy",  ButtonBar.ButtonData.OK_DONE);
    ButtonType sellType = new ButtonType("Sell", ButtonBar.ButtonData.OTHER);
    dialog.getDialogPane().getButtonTypes().addAll(buyType, ButtonType.CANCEL);
    if (!owned.isEmpty()) {
      dialog.getDialogPane().getButtonTypes().add(1, sellType);
    }

    dialog.showAndWait().ifPresent(result -> {
      if (result == ButtonType.CANCEL) return;
      try {
        if (result == buyType) {
          String qtyText = qtyField.getText().trim();
          if (qtyText.isEmpty()) return;
          BigDecimal qty = new BigDecimal(qtyText);
          if (qty.signum() <= 0) throw new NumberFormatException();
          Share tempShare = new Share(stock, qty, stock.getSalesPrice());
          PurchaseCalculator calc = new PurchaseCalculator(tempShare);
          onBuy.accept(stock, qty);
          showBuyReceipt(stock, qty, calc);
        } else if (result == sellType && !owned.isEmpty()) {
          Share firstLot = owned.getFirst();
          SaleCalculator calc = new SaleCalculator(firstLot);
          onSell.accept(firstLot);
          showSellReceipt(stock, firstLot, calc);
        }
      } catch (NumberFormatException ex) {
        showError("Please enter a valid positive number for quantity.");
      } catch (IllegalStateException | IllegalArgumentException ex) {
        showError(ex.getMessage());
      }
    });
  }

  private void clearBuyPreview(Label gross, Label commission, Label total) {
    gross.setText("");
    commission.setText("");
    total.setText("");
  }

  // ── Receipts ─────────────────────────────────────────────────────────────

  private void showBuyReceipt(Stock stock, BigDecimal qty, PurchaseCalculator calc) {
    Alert receipt = new Alert(Alert.AlertType.INFORMATION);
    receipt.initOwner(stage);
    receipt.setTitle("Purchase Confirmed");
    receipt.setHeaderText("✓  Bought " + qty.toPlainString() + " shares of " + stock.getSymbol());

    GridPane grid = buildReceiptGrid(
        "Stock",        stock.getSymbol() + " — " + stock.getCompany(),
        "Quantity",     qty.toPlainString(),
        "Price/share",  fmt(stock.getSalesPrice()),
        "Market value", fmt(calc.calculateGross()),
        "Commission",   fmt(calc.calculateCommission()),
        "Tax",          fmt(calc.calculateTax()),
        "Total paid",   fmt(calc.calculateTotal())
    );
    receipt.getDialogPane().setContent(grid);
    applyTheme(receipt.getDialogPane());
    receipt.showAndWait();
  }

  private void showSellReceipt(Stock stock, Share share, SaleCalculator calc) {
    Alert receipt = new Alert(Alert.AlertType.INFORMATION);
    receipt.initOwner(stage);
    receipt.setTitle("Sale Confirmed");
    receipt.setHeaderText("✓  Sold " + share.getQuantity().toPlainString()
        + " shares of " + stock.getSymbol());

    GridPane grid = buildReceiptGrid(
        "Stock",            stock.getSymbol() + " — " + stock.getCompany(),
        "Quantity",         share.getQuantity().toPlainString(),
        "Purchase price",   fmt(share.getPurchasePrice()),
        "Sale price",       fmt(stock.getSalesPrice()),
        "Market value",     fmt(calc.calculateGross()),
        "Commission (1%)",  fmt(calc.calculateCommission()),
        "Tax (30% profit)", fmt(calc.calculateTax()),
        "You received",     fmt(calc.calculateTotal())
    );
    receipt.getDialogPane().setContent(grid);
    applyTheme(receipt.getDialogPane());
    receipt.showAndWait();
  }

  private GridPane buildReceiptGrid(String... keyValues) {
    GridPane grid = new GridPane();
    grid.setHgap(24);
    grid.setVgap(6);
    grid.setPadding(new Insets(12));
    for (int i = 0; i < keyValues.length - 1; i += 2) {
      Label key = new Label(keyValues[i] + ":");
      key.getStyleClass().add("info-key");
      Label val = new Label(keyValues[i + 1]);
      val.getStyleClass().add("info-value");
      grid.add(key, 0, i / 2);
      grid.add(val, 1, i / 2);
    }
    return grid;
  }

  // ── Gainers / Losers panels ───────────────────────────────────────────────

  private VBox buildMiniPanel(String title) {
    Label header = new Label(title);
    header.getStyleClass().add("panel-header");
    VBox box = new VBox(6, header);
    box.getStyleClass().add("mini-panel");
    return box;
  }

  private void refreshGainersLosers() {
    repopulatePanel(gainersBox, exchange.getGainers(5), true);
    repopulatePanel(losersBox,  exchange.getLosers(5),  false);
  }

  private void repopulatePanel(VBox panel, List<Stock> stocks, boolean gainer) {
    panel.getChildren().subList(1, panel.getChildren().size()).clear();
    for (Stock s : stocks) {
      BigDecimal ch = s.getLatestPriceChange();
      String sign = ch.signum() >= 0 ? "+" : "";

      Label sym   = new Label(s.getSymbol());
      sym.getStyleClass().add("mini-symbol");

      Label price = new Label(fmt(s.getSalesPrice()));
      price.getStyleClass().add("mini-price");

      Label chg = new Label(sign + fmt(ch));
      chg.getStyleClass().add(gainer ? "positive-change" : "negative-change");

      Region sp = new Region();
      HBox.setHgrow(sp, Priority.ALWAYS);

      HBox row = new HBox(8, sym, sp, price, chg);
      row.getStyleClass().add("mini-row");
      panel.getChildren().add(row);
    }
  }

  // ── Observer callback ────────────────────────────────────────────────────

  @Override
  public void onExchangeUpdated(Exchange exchange) {
    String currentFilter = searchField != null ? searchField.getText() : "";
    if (table != null) {
      refreshStockList(currentFilter);
      table.refresh();
    }
    refreshGainersLosers();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void refreshStockList(String filter) {
    stockList.setAll(exchange.findStocks(filter == null ? "" : filter));
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.initOwner(stage);
    alert.setHeaderText("Error");
    alert.setContentText(message);
    applyTheme(alert.getDialogPane());
    alert.showAndWait();
  }

  private void applyTheme(javafx.scene.control.DialogPane pane) {
    pane.getStylesheets().add(getClass().getResource(GLOBAL_CSS).toExternalForm());
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }

  private String signedFmt(BigDecimal amount) {
    String sign = amount.signum() >= 0 ? "+" : "";
    return sign + fmt(amount);
  }
}
