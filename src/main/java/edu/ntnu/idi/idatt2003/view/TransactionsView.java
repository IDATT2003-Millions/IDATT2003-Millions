package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionsView {

  private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

  public Node buildContent(List<Transaction> transactions) {
    transactionList.setAll(transactions);

    Label title = new Label("Transactions");
    title.getStyleClass().add("page-title");

    TableView<Transaction> table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);
    table.setMaxHeight(Double.MAX_VALUE);

    VBox center = new VBox(16, title, table);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(center, Priority.ALWAYS);

    return center;
  }

  private TableView<Transaction> buildTable() {
    TableView<Transaction> tv = new TableView<>(transactionList);
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tv.setPlaceholder(new Label("No transactions yet."));
    tv.getStyleClass().add("stock-table");

    TableColumn<Transaction, String> weekCol = new TableColumn<>("Week");
    weekCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getWeek())));
    weekCol.setMaxWidth(70);

    TableColumn<Transaction, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(d -> {
      var stock = d.getValue().getShare().getStock();
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
    totalCol.setCellValueFactory(d -> {
      var share = d.getValue().getShare();
      BigDecimal total = share.getPurchasePrice().multiply(share.getQuantity());
      return new SimpleStringProperty(fmt(total));
    });
    totalCol.setMaxWidth(140);

    tv.getColumns().addAll(weekCol, companyCol, typeCol, qtyCol, priceCol, totalCol);
    return tv;
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
