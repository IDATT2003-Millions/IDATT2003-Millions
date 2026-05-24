package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * View for displaying the player's transaction history with search/filter
 * and a detail dialog for each transaction.
 */
public class TransactionsView {

  private final ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
  private final FilteredList<Transaction> filteredList = new FilteredList<>(allTransactions, t -> true);

  /**
   * Builds and returns the transactions page content.
   *
   * @param transactions the full list of transactions to display
   * @return the root node for this page
   */
  public Node buildContent(List<Transaction> transactions) {
    allTransactions.setAll(transactions);

    Label title = new Label("Transactions");
    title.getStyleClass().add("page-title");

    TextField searchField = new TextField();
    searchField.setPromptText("Search by symbol or company…");
    searchField.getStyleClass().add("search-field");
    searchField.textProperty().addListener((obs, old, text) -> {
      String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
      filteredList.setPredicate(t -> {
        if (lower.isBlank()) return true;
        Stock stock = t.getShare().getStock();
        return stock.getSymbol().toLowerCase(Locale.ROOT).contains(lower)
            || stock.getCompany().toLowerCase(Locale.ROOT).contains(lower);
      });
    });

    SortedList<Transaction> sorted = new SortedList<>(filteredList);
    TableView<Transaction> table = buildTable(sorted);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.setMaxHeight(Double.MAX_VALUE);

    VBox center = new VBox(12, title, searchField, table);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(center, Priority.ALWAYS);

    return center;
  }

  private TableView<Transaction> buildTable(SortedList<Transaction> sorted) {
    TableView<Transaction> tv = new TableView<>(sorted);
    sorted.comparatorProperty().bind(tv.comparatorProperty());
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.setPlaceholder(new Label("No transactions yet."));
    tv.getStyleClass().add("stock-table");

    TableColumn<Transaction, String> weekCol = new TableColumn<>("Week");
    weekCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getWeek())));
    weekCol.setMaxWidth(70);

    TableColumn<Transaction, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(d -> {
      Stock stock = d.getValue().getShare().getStock();
      return new SimpleStringProperty(stock.getSymbol() + "\n" + stock.getCompany());
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

    TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(d ->
        new SimpleStringProperty(d.getValue() instanceof Purchase ? "BUY" : "SELL"));
    typeCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setGraphic(null); setText(null); return; }
        Label badge = new Label(item);
        badge.getStyleClass().addAll("type-badge",
            "BUY".equals(item) ? "badge-buy" : "badge-sell");
        HBox box = new HBox(badge);
        box.setAlignment(Pos.CENTER_LEFT);
        setGraphic(box);
        setText(null);
      }
    });
    typeCol.setMaxWidth(80);

    TableColumn<Transaction, String> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(d ->
        new SimpleStringProperty(d.getValue().getShare().getQuantity().toPlainString()));
    qtyCol.setMaxWidth(100);

    TableColumn<Transaction, String> priceCol = new TableColumn<>("Price / Share");
    priceCol.setCellValueFactory(d ->
        new SimpleStringProperty(fmt(d.getValue().getShare().getPurchasePrice())));
    priceCol.setMaxWidth(130);

    TableColumn<Transaction, String> totalCol = new TableColumn<>("Total");
    totalCol.setCellValueFactory(d ->
        new SimpleStringProperty(fmt(d.getValue().getCalculator().calculateTotal().abs())));
    totalCol.setMaxWidth(140);

    tv.getColumns().addAll(weekCol, companyCol, typeCol, qtyCol, priceCol, totalCol);

    tv.setRowFactory(tableView -> {
      TableRow<Transaction> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (!row.isEmpty() && event.getClickCount() == 2) {
          showDetailDialog(row.getItem());
        }
      });
      return row;
    });

    return tv;
  }

  private void showDetailDialog(Transaction t) {
    boolean isBuy = t instanceof Purchase;
    Share share = t.getShare();
    Stock stock = share.getStock();

    BigDecimal gross      = t.getCalculator().calculateGross();
    BigDecimal commission = t.getCalculator().calculateCommission();
    BigDecimal tax        = t.getCalculator().calculateTax();
    BigDecimal total      = t.getCalculator().calculateTotal();

    BigDecimal salePrice = isBuy
        ? share.getPurchasePrice()
        : gross.divide(share.getQuantity(), 2, RoundingMode.HALF_UP);

    Alert dialog = new Alert(Alert.AlertType.INFORMATION);
    dialog.setTitle("Transaction Details");
    dialog.setHeaderText((isBuy ? "BUY" : "SELL") + "  —  "
        + stock.getSymbol() + " · " + stock.getCompany());

    GridPane grid = new GridPane();
    grid.setHgap(24);
    grid.setVgap(6);
    grid.setPadding(new Insets(12));

    int row = 0;
    addRow(grid, row++, "Week",          String.valueOf(t.getWeek()));
    addRow(grid, row++, "Quantity",      share.getQuantity().toPlainString());
    if (isBuy) {
      addRow(grid, row++, "Purchase price", fmt(share.getPurchasePrice()));
    } else {
      addRow(grid, row++, "Purchase price", fmt(share.getPurchasePrice()));
      addRow(grid, row++, "Sale price",     fmt(salePrice));
    }
    addRow(grid, row++, "Gross value",   fmt(gross));
    addRow(grid, row++, "Commission",    fmt(commission));
    if (!isBuy) {
      addRow(grid, row++, "Tax (30% profit)", fmt(tax));
    }
    addRow(grid, row, isBuy ? "Total cost" : "You received", fmt(total.abs()));

    dialog.getDialogPane().setContent(grid);
    dialog.showAndWait();
  }

  private void addRow(GridPane grid, int row, String key, String value) {
    Label keyLabel = new Label(key + ":");
    keyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
    Label valueLabel = new Label(value);
    valueLabel.setStyle("-fx-font-weight: bold;");
    grid.add(keyLabel, 0, row);
    grid.add(valueLabel, 1, row);
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
