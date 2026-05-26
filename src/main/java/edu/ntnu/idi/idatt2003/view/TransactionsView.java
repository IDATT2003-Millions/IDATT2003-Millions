package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TransactionsView {

  private final ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
  private final FilteredList<Transaction> filteredList =
      new FilteredList<>(allTransactions, t -> true);

  public Node buildContent(
      List<Transaction> transactions,
      List<LimitOrder> pending,
      List<LimitOrder> completed,
      Consumer<LimitOrder> onCancelOrder) {
    allTransactions.setAll(transactions);

    Label title = new Label("Transactions");
    title.getStyleClass().add("page-title");

    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane
        .getTabs()
        .addAll(
            buildHistoryTab(),
            buildPendingTab(pending, onCancelOrder),
            buildCompletedTab(completed));
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    VBox center = new VBox(12, title, tabPane);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(center, Priority.ALWAYS);
    return center;
  }

  private Tab buildHistoryTab() {
    TextField searchField = new TextField();
    searchField.setPromptText("Search by symbol or company…");
    searchField.getStyleClass().add("search-field");
    searchField
        .textProperty()
        .addListener(
            (obs, old, text) -> {
              String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
              filteredList.setPredicate(
                  t -> {
                    if (lower.isBlank()) {
                      return true;
                    }
                    Stock stock = t.getShare().getStock();
                    return stock.getSymbol().toLowerCase(Locale.ROOT).contains(lower)
                        || stock.getCompany().toLowerCase(Locale.ROOT).contains(lower);
                  });
            });

    SortedList<Transaction> sorted = new SortedList<>(filteredList);
    TableView<Transaction> table = buildHistoryTable(sorted);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.setMaxHeight(Double.MAX_VALUE);

    VBox content = new VBox(8, searchField, table);
    VBox.setVgrow(content, Priority.ALWAYS);
    content.setPadding(new Insets(8, 0, 0, 0));

    Tab tab = new Tab("History", content);
    return tab;
  }

  private TableView<Transaction> buildHistoryTable(SortedList<Transaction> sorted) {
    TableView<Transaction> tv = new TableView<>(sorted);
    sorted.comparatorProperty().bind(tv.comparatorProperty());
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.setPlaceholder(new Label("No transactions yet."));
    tv.getStyleClass().add("stock-table");

    TableColumn<Transaction, String> weekCol = new TableColumn<>("Week");
    weekCol.setCellValueFactory(
        d -> new SimpleStringProperty(String.valueOf(d.getValue().getWeek())));
    weekCol.setMaxWidth(70);

    TableColumn<Transaction, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(
        d -> {
          Stock stock = d.getValue().getShare().getStock();
          return new SimpleStringProperty(stock.getSymbol() + "\n" + stock.getCompany());
        });
    companyCol.setCellFactory(
        col ->
            new TableCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  return;
                }
                String[] parts = item.split("\n");
                Label symbol = new Label(parts[0]);
                symbol.getStyleClass().add("mini-symbol");
                Label company = new Label(parts.length > 1 ? parts[1] : "");
                company.getStyleClass().add("sidebar-section-label");
                setGraphic(new VBox(2, symbol, company));
              }
            });

    TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(
        d -> new SimpleStringProperty(d.getValue() instanceof Purchase ? "BUY" : "SELL"));
    typeCol.setCellFactory(
        col ->
            new TableCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  setText(null);
                  return;
                }
                Label badge = new Label(item);
                badge
                    .getStyleClass()
                    .addAll("type-badge", "BUY".equals(item) ? "badge-buy" : "badge-sell");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
              }
            });
    typeCol.setMaxWidth(80);

    TableColumn<Transaction, String> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(
        d -> new SimpleStringProperty(d.getValue().getShare().getQuantity().toPlainString()));
    qtyCol.setMaxWidth(100);

    TableColumn<Transaction, String> priceCol = new TableColumn<>("Price / Share");
    priceCol.setCellValueFactory(
        d -> new SimpleStringProperty(fmt(d.getValue().getShare().getPurchasePrice())));
    priceCol.setMaxWidth(130);

    TableColumn<Transaction, String> totalCol = new TableColumn<>("Total");
    totalCol.setCellValueFactory(
        d -> new SimpleStringProperty(fmt(d.getValue().getCalculator().calculateTotal().abs())));
    totalCol.setMaxWidth(140);

    tv.getColumns().addAll(weekCol, companyCol, typeCol, qtyCol, priceCol, totalCol);

    tv.setRowFactory(
        tableView -> {
          TableRow<Transaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                  showDetailDialog(row.getItem());
                }
              });
          return row;
        });

    return tv;
  }

  private Tab buildPendingTab(List<LimitOrder> pending, Consumer<LimitOrder> onCancel) {
    ObservableList<LimitOrder> items = FXCollections.observableArrayList(pending);
    TableView<LimitOrder> tv =
        buildOrderTable(
            items,
            false,
            order -> {
              onCancel.accept(order);
              items.remove(order);
            });
    tv.setPlaceholder(new Label("No pending orders."));
    VBox content = new VBox(tv);
    VBox.setVgrow(tv, Priority.ALWAYS);
    content.setPadding(new Insets(8, 0, 0, 0));
    return new Tab("Pending Orders (" + pending.size() + ")", content);
  }

  private Tab buildCompletedTab(List<LimitOrder> completed) {
    TableView<LimitOrder> tv =
        buildOrderTable(FXCollections.observableArrayList(completed), true, null);
    tv.setPlaceholder(new Label("No completed orders yet."));
    VBox content = new VBox(tv);
    VBox.setVgrow(tv, Priority.ALWAYS);
    content.setPadding(new Insets(8, 0, 0, 0));
    return new Tab("Completed Orders (" + completed.size() + ")", content);
  }

  private TableView<LimitOrder> buildOrderTable(
      ObservableList<LimitOrder> items, boolean showStatus, Consumer<LimitOrder> onDelete) {

    TableView<LimitOrder> tv = new TableView<>(items);
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.getStyleClass().add("stock-table");
    tv.setMaxHeight(Double.MAX_VALUE);

    TableColumn<LimitOrder, String> weekCol = new TableColumn<>("Week");
    weekCol.setCellValueFactory(
        d -> new SimpleStringProperty(String.valueOf(d.getValue().getWeekPlaced())));
    weekCol.setMaxWidth(70);

    TableColumn<LimitOrder, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(
        d -> {
          LimitOrder o = d.getValue();
          return new SimpleStringProperty(o.getSymbol() + "\n" + o.getCompany());
        });
    companyCol.setCellFactory(
        col ->
            new TableCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  return;
                }
                String[] parts = item.split("\n");
                Label symbol = new Label(parts[0]);
                symbol.getStyleClass().add("mini-symbol");
                Label company = new Label(parts.length > 1 ? parts[1] : "");
                company.getStyleClass().add("sidebar-section-label");
                setGraphic(new VBox(2, symbol, company));
              }
            });

    TableColumn<LimitOrder, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().name()));
    typeCol.setCellFactory(
        col ->
            new TableCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  setText(null);
                  return;
                }
                Label badge = new Label(item);
                badge
                    .getStyleClass()
                    .addAll("type-badge", "BUY".equals(item) ? "badge-buy" : "badge-sell");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
              }
            });
    typeCol.setMaxWidth(80);

    TableColumn<LimitOrder, String> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(
        d -> new SimpleStringProperty(d.getValue().getQuantity().toPlainString()));
    qtyCol.setMaxWidth(100);

    TableColumn<LimitOrder, String> targetCol = new TableColumn<>("Target Price");
    targetCol.setCellValueFactory(
        d -> new SimpleStringProperty(fmt(d.getValue().getTargetPrice())));
    targetCol.setMaxWidth(130);

    tv.getColumns().addAll(weekCol, companyCol, typeCol, qtyCol, targetCol);

    if (showStatus) {
      TableColumn<LimitOrder, String> statusCol = new TableColumn<>("Status");
      statusCol.setCellValueFactory(
          d -> {
            LimitOrder o = d.getValue();
            String status;
            if (o.isExecuted()) {
              status = "Executed (week " + o.getWeekResolved() + ")";
            } else {
              status =
                  o.getWeekResolved() == 0
                      ? "Cancelled"
                      : "Cancelled (week " + o.getWeekResolved() + ")";
            }
            return new SimpleStringProperty(status);
          });
      statusCol.setCellFactory(
          col ->
              new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                  super.updateItem(item, empty);
                  setText(empty || item == null ? null : item);
                  if (!empty && item != null) {
                    getStyleClass().removeAll("positive-change", "negative-change");
                    getStyleClass()
                        .add(item.startsWith("Executed") ? "positive-change" : "negative-change");
                  }
                }
              });
      tv.getColumns().add(statusCol);
    }

    if (onDelete != null) {
      TableColumn<LimitOrder, Void> deleteCol = new TableColumn<>("");
      deleteCol.setMaxWidth(90);
      deleteCol.setCellFactory(
          col ->
              new TableCell<>() {
                private final Button btn = new Button("Delete");

                {
                  btn.getStyleClass().addAll("action-button", "exit-button");
                  btn.setStyle("-fx-pref-width: 70; -fx-pref-height: 32; -fx-font-size: 12px;");
                  btn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                  super.updateItem(item, empty);
                  setGraphic(empty ? null : btn);
                }
              });
      tv.getColumns().add(deleteCol);
    }

    return tv;
  }

  private void showDetailDialog(Transaction t) {
    boolean isBuy = t instanceof Purchase;
    Share share = t.getShare();
    Stock stock = share.getStock();

    BigDecimal gross = t.getCalculator().calculateGross();
    BigDecimal commission = t.getCalculator().calculateCommission();
    BigDecimal tax = t.getCalculator().calculateTax();
    BigDecimal total = t.getCalculator().calculateTotal();

    BigDecimal salePrice =
        isBuy
            ? share.getPurchasePrice()
            : gross.divide(share.getQuantity(), 2, RoundingMode.HALF_UP);

    Alert dialog = new Alert(Alert.AlertType.INFORMATION);
    dialog.setTitle("Transaction Details");
    dialog.setHeaderText(
        (isBuy ? "BUY" : "SELL") + "  —  " + stock.getSymbol() + " · " + stock.getCompany());

    GridPane grid = new GridPane();
    grid.setHgap(24);
    grid.setVgap(6);
    grid.setPadding(new Insets(12));

    int row = 0;
    addRow(grid, row++, "Week", String.valueOf(t.getWeek()));
    addRow(grid, row++, "Quantity", share.getQuantity().toPlainString());
    addRow(grid, row++, "Purchase price", fmt(share.getPurchasePrice()));
    if (!isBuy) {
      addRow(grid, row++, "Sale price", fmt(salePrice));
    }
    addRow(grid, row++, "Gross value", fmt(gross));
    addRow(grid, row++, "Commission", fmt(commission));
    if (!isBuy) {
      addRow(grid, row++, "Tax (30% profit)", fmt(tax));
    }
    addRow(grid, row, isBuy ? "Total cost" : "You received", fmt(total.abs()));

    dialog.getDialogPane().setContent(grid);
    dialog.showAndWait();
  }

  private void addRow(GridPane grid, int row, String key, String value) {
    Label keyLabel = new Label(key + ":");
    keyLabel.getStyleClass().add("info-key");
    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add("info-value");
    grid.add(keyLabel, 0, row);
    grid.add(valueLabel, 1, row);
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
