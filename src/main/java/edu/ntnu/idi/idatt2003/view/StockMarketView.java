package edu.ntnu.idi.idatt2003.view;


import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

    public StockMarketView(Player player, Exchange exchange, Stage stage) {
        this.player = player;
        this.exchange = exchange;
        this.stage = stage;
        exchange.addObserver(this);
    }

    public Scene createScene(Runnable onPortfolio) {
        Sidebar sidebar = new Sidebar(player, exchange);

        TextField searchField = new TextField();
        searchField.setPromptText("Search stocks…");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, text) -> refreshStockList(text));

        Button nextWeekBtn = new Button("Next Week ▶");
        nextWeekBtn.getStyleClass().addAll("action-button", "primary-button");
        nextWeekBtn.setOnAction(e -> exchange.advance());

        Button portfolioBtn = new Button("My Portfolio");
        portfolioBtn.getStyleClass().addAll("action-button", "secondary-button");
        portfolioBtn.setOnAction(e -> onPortfolio.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(12, searchField, spacer, nextWeekBtn, portfolioBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        topBar.getStyleClass().add("top-bar");

        table = buildTable();
        refreshStockList("");

        gainersBox = buildMiniPanel("Top Gainers");
        losersBox = buildMiniPanel("Top Losers");
        refreshGainersLosers();

        HBox bottomPanel = new HBox(16, gainersBox, losersBox);
        bottomPanel.getStyleClass().add("bottom-panel");
        HBox.setHgrow(gainersBox, Priority.ALWAYS);
        HBox.setHgrow(losersBox, Priority.ALWAYS);

        VBox center = new VBox(12, topBar, table, bottomPanel);
        center.getStyleClass().add("market-center");
        VBox.setVgrow(table, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(center);
        root.getStyleClass().add("market-root");

        return new Scene(root, 1200, 700);
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

        tv.getColumns().addAll(List.of(symbolCol, companyCol, priceCol, changeCol));

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
        repopulatePanel(losersBox, exchange.getLosers(5), false);
    }

    private void repopulatePanel(VBox panel, List<Stock> stocks, boolean gainer) {
        panel.getChildren().subList(1, panel.getChildren().size()).clear();
        for (Stock s : stocks) {
            BigDecimal ch = s.getLatestPriceChange();
            String sign = ch.signum() >= 0 ? "+" : "";

            Label sym = new Label(s.getSymbol());
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

// ── Buy / Sell dialog ────────────────────────────────────────────────────

    private void showBuySellDialog(Stock stock) {
        List<Share> owned = player.getPortfolio().getShares(stock.getSymbol());
        String ownedText = owned.isEmpty()
                ? "You own: 0 shares"
                : "You own: " + owned.stream()
                .map(s -> s.getQuantity().toPlainString())
                .reduce("0", (a, b) -> new BigDecimal(a).add(new BigDecimal(b)).toPlainString())
                + " shares";

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.setTitle(stock.getSymbol() + "  —  " + stock.getCompany());
        dialog.setHeaderText("Current price: " + fmt(stock.getSalesPrice()));

        Label cashInfo = new Label("Your cash: " + fmt(player.getMoney()));
        cashInfo.getStyleClass().add("dialog-info");

        Label ownedInfo = new Label(ownedText);
        ownedInfo.getStyleClass().add("dialog-info");

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity of shares");

        VBox content = new VBox(10, cashInfo, ownedInfo, qtyField);
        content.setMinWidth(300);
        dialog.getDialogPane().setContent(content);

        ButtonType buyType = new ButtonType("Buy", ButtonBar.ButtonData.OK_DONE);
        ButtonType sellType = new ButtonType("Sell", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(buyType, sellType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.CANCEL) return;
            String qtyText = qtyField.getText().trim();
            if (qtyText.isEmpty()) return;

            try {
                BigDecimal qty = new BigDecimal(qtyText);
                if (qty.signum() <= 0) throw new NumberFormatException("Must be positive");

                if (result == buyType) {
                    exchange.buy(stock.getSymbol(), qty, player).commit(player);
                    exchange.refresh();
                } else if (result == sellType) {
                    if (owned.isEmpty()) {
                        showError("You don't own any shares of " + stock.getSymbol() + ".");
                        return;
                    }
                    exchange.sell(owned.get(0), player).commit(player);
                    exchange.refresh();
                }
            } catch (NumberFormatException ex) {
                showError("Please enter a valid positive number for quantity.");
            } catch (IllegalStateException | IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

// ── Observer callback ────────────────────────────────────────────────────

    @Override
    public void onExchangeUpdated(Exchange exchange) {
        String currentFilter = "";
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
        alert.showAndWait();
    }

    private String fmt(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }
}