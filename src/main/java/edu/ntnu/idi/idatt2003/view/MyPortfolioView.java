package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

public class MyPortfolioView implements ExchangeObserver {

  private final Player player;
  private final Exchange exchange;
  private final Stage stage;
  private final ObservableList<Share> shareList = FXCollections.observableArrayList();
  private Consumer<Share> onSell;
  private Runnable onSellAll;

  public MyPortfolioView(Player player, Exchange exchange, Stage stage) {
    this.player = player;
    this.exchange = exchange;
    this.stage = stage;
    exchange.addObserver(this);
  }

  public Node buildContent(Consumer<Share> onSell, Runnable onSellAll) {
    this.onSell = onSell;
    this.onSellAll = onSellAll;

    Label title = new Label("My Portfolio");
    title.getStyleClass().add("page-title");

    Button sellAllBtn = new Button("Sell All");
    sellAllBtn.getStyleClass().addAll("action-button", "exit-button");
    sellAllBtn.setStyle("-fx-pref-height: 36; -fx-font-size: 13px;");
    sellAllBtn.setOnAction(e -> {
      if (player.getPortfolio().getShares().isEmpty()) return;
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Sell All Shares");
      confirm.setHeaderText("Are you sure?");
      confirm.setContentText("This will sell all your shares immediately at the current market price.");
      Optional<ButtonType> result = confirm.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        onSellAll.run();
      }
    });

    HBox topBar = new HBox(title, new javafx.scene.layout.Region(), sellAllBtn);
    HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
    topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    topBar.setSpacing(12);

    TableView<Share> table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);
    table.setMaxHeight(Double.MAX_VALUE);

    VBox center = new VBox(16, topBar, table);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(center, Priority.ALWAYS);

    shareList.setAll(player.getPortfolio().getShares());
    return center;
  }

  private TableView<Share> buildTable() {
    TableView<Share> tv = new TableView<>(shareList);
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.setPlaceholder(new Label("You own no shares yet."));
    tv.getStyleClass().add("stock-table");

    TableColumn<Share, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(d -> {
      Share s = d.getValue();
      return new SimpleStringProperty(s.getStock().getSymbol() + "\n" + s.getStock().getCompany());
    });
    companyCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setGraphic(null); return; }
        String[] parts = item.split("\n");
        Label symbol = new Label(parts[0]);
        symbol.getStyleClass().add("mini-symbol");
        Label company = new Label(parts.length > 1 ? parts[1] : "");
        company.getStyleClass().add("sidebar-section-label");
        setGraphic(new VBox(2, symbol, company));
      }
    });

    TableColumn<Share, String> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getQuantity().toPlainString()));
    qtyCol.setMaxWidth(100);

    TableColumn<Share, String> buyPriceCol = new TableColumn<>("Purchase Price");
    buyPriceCol.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().getPurchasePrice())));
    buyPriceCol.setMaxWidth(140);

    TableColumn<Share, String> currentCol = new TableColumn<>("Current Value");
    currentCol.setCellValueFactory(d -> {
      Share s = d.getValue();
      BigDecimal currentValue = s.getStock().getSalesPrice().multiply(s.getQuantity());
      return new SimpleStringProperty(fmt(currentValue));
    });
    currentCol.setMaxWidth(140);

    TableColumn<Share, String> gainCol = new TableColumn<>("Gain/Loss");
    gainCol.setCellValueFactory(d -> {
      Share s = d.getValue();
      BigDecimal currentValue = s.getStock().getSalesPrice().multiply(s.getQuantity());
      BigDecimal purchaseTotal = s.getPurchasePrice().multiply(s.getQuantity());
      BigDecimal gain = currentValue.subtract(purchaseTotal);
      BigDecimal pct = purchaseTotal.compareTo(BigDecimal.ZERO) == 0
          ? BigDecimal.ZERO
          : gain.divide(purchaseTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
      String sign = gain.signum() >= 0 ? "+" : "";
      return new SimpleStringProperty(sign + fmt(gain) + "\n" + sign + pct.setScale(1, RoundingMode.HALF_UP) + "%");
    });
    gainCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setGraphic(null); return; }
        String[] parts = item.split("\n");
        boolean positive = item.startsWith("+");
        Label amount = new Label(parts[0]);
        Label pct = new Label(parts.length > 1 ? parts[1] : "");
        String style = positive ? "-color-winner-text" : "-color-loser-text";
        amount.setStyle("-fx-text-fill: " + style + "; -fx-font-weight: bold;");
        pct.setStyle("-fx-text-fill: " + style + ";");
        VBox box = new VBox(2, amount, pct);
        box.setAlignment(Pos.CENTER_RIGHT);
        setGraphic(box);
        setText(null);
      }
    });
    gainCol.setMaxWidth(140);

    TableColumn<Share, Void> sellCol = new TableColumn<>("");
    sellCol.setCellFactory(col -> new TableCell<>() {
      private final Button btn = new Button("SELL");
      {
        btn.getStyleClass().addAll("action-button", "primary-button");
        btn.setStyle("-fx-pref-width: 70; -fx-pref-height: 32; -fx-font-size: 12px;");
        btn.setOnAction(e -> {
          Share share = getTableView().getItems().get(getIndex());
          onSell.accept(share);
        });
      }
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    });
    sellCol.setMaxWidth(90);

    tv.getColumns().addAll(companyCol, qtyCol, buyPriceCol, currentCol, gainCol, sellCol);
    return tv;
  }

  @Override
  public void onExchangeUpdated(Exchange exchange) {
    shareList.setAll(player.getPortfolio().getShares());
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
