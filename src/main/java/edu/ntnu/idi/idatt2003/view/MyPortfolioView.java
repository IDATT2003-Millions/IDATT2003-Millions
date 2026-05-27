package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * View for the player's portfolio screen.
 *
 * <p>Displays all currently owned shares grouped by stock, and updates automatically
 * via {@link ExchangeObserver} whenever the exchange state changes.
 */
public class MyPortfolioView implements ExchangeObserver {

  private static final String GLOBAL_CSS = "/css_files/global.css";

  /** Aggregated view of one stock position (may span multiple purchase lots). */
  private record PortfolioPosition(
      Stock stock, BigDecimal totalQuantity, BigDecimal weightedAvgPrice, List<Share> lots) {}

  private final Player player;
  private final Exchange exchange;
  private final Stage stage;
  private final ObservableList<PortfolioPosition> positionList =
      FXCollections.observableArrayList();

  private BiConsumer<String, BigDecimal> onSellQuantity;
  private Consumer<LimitOrder> onPlaceSellOrder;
  private PriceChart portfolioChart;

  /**
   * Creates a new portfolio view for the given player and exchange.
   *
   * @param player the active player
   * @param exchange the active exchange
   * @param stage the primary stage (used for dialogs)
   */
  public MyPortfolioView(Player player, Exchange exchange, Stage stage) {
    this.player = player;
    this.exchange = exchange;
    this.stage = stage;
    exchange.addObserver(this);
  }

  /**
   * Builds and returns the portfolio page content node.
   *
   * @param onSellQuantity called with symbol and quantity when the player sells shares
   * @param onSellAll called when the player sells all shares at once
   * @param onAdvanceWeek called when the player advances to the next week
   * @param onPlaceSellOrder called when the player places a limit sell order
   * @return the rendered portfolio page
   */
  public Node buildContent(
      BiConsumer<String, BigDecimal> onSellQuantity,
      Runnable onSellAll,
      Runnable onAdvanceWeek,
      Consumer<LimitOrder> onPlaceSellOrder) {
    this.onSellQuantity = onSellQuantity;
    this.onPlaceSellOrder = onPlaceSellOrder;

    Label title = new Label("My Portfolio");
    title.getStyleClass().add("page-title");

    Button sellAllBtn = new Button("Sell All");
    sellAllBtn.getStyleClass().addAll("action-button", "exit-button");
    sellAllBtn.setOnAction(
        e -> {
          if (player.getPortfolio().getShares().isEmpty()) {
            return;
          }
          Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
          confirm.setTitle("Sell All Shares");
          confirm.setHeaderText("Are you sure?");
          confirm.setContentText(
              "This will sell all your shares immediately at the current market price.");
          Optional<ButtonType> result = confirm.showAndWait();
          if (result.isPresent() && result.get() == ButtonType.OK) {
            onSellAll.run();
          }
        });

    Button nextWeekBtn = new Button("Next Week ▶");
    nextWeekBtn.getStyleClass().addAll("action-button", "primary-button");
    nextWeekBtn.setOnAction(e -> onAdvanceWeek.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox topBar = new HBox(12, title, spacer, sellAllBtn, nextWeekBtn);
    topBar.setAlignment(Pos.CENTER_LEFT);

    TableView<PortfolioPosition> table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);
    table.setMaxHeight(Double.MAX_VALUE);

    portfolioChart = new PriceChart(buildPortfolioHistory(), 330);
    VBox.setMargin(portfolioChart, new Insets(110, 0, 0, 0));
    VBox center = new VBox(16, topBar, portfolioChart, table);
    center.getStyleClass().add("market-center");
    VBox.setVgrow(center, Priority.ALWAYS);

    positionList.setAll(buildPositions());
    return center;
  }

  private List<PortfolioPosition> buildPositions() {
    Map<String, List<Share>> grouped =
        player.getPortfolio().getShares().stream()
            .collect(Collectors.groupingBy(s -> s.getStock().getSymbol()));

    return grouped.values().stream()
        .map(
            lots -> {
              Stock stock = lots.getFirst().getStock();
              BigDecimal totalQty =
                  lots.stream().map(Share::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
              BigDecimal totalCost =
                  lots.stream()
                      .map(s -> s.getPurchasePrice().multiply(s.getQuantity()))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              BigDecimal avgPrice =
                  totalQty.compareTo(BigDecimal.ZERO) == 0
                      ? BigDecimal.ZERO
                      : totalCost.divide(totalQty, 2, RoundingMode.HALF_UP);
              return new PortfolioPosition(stock, totalQty, avgPrice, lots);
            })
        .toList();
  }

  private TableView<PortfolioPosition> buildTable() {
    TableView<PortfolioPosition> tv = new TableView<>(positionList);
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tv.setPlaceholder(new Label("You own no shares yet."));
    tv.getStyleClass().add("stock-table");

    TableColumn<PortfolioPosition, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(
        d -> {
          PortfolioPosition p = d.getValue();
          return new SimpleStringProperty(p.stock().getSymbol() + "\n" + p.stock().getCompany());
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

    TableColumn<PortfolioPosition, String> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(
        d -> new SimpleStringProperty(d.getValue().totalQuantity().toPlainString()));
    qtyCol.setMaxWidth(100);

    TableColumn<PortfolioPosition, String> buyPriceCol = new TableColumn<>("Avg Purchase Price");
    buyPriceCol.setCellValueFactory(
        d -> new SimpleStringProperty(fmt(d.getValue().weightedAvgPrice())));
    buyPriceCol.setMaxWidth(150);

    TableColumn<PortfolioPosition, String> currentCol = new TableColumn<>("Current Value");
    currentCol.setCellValueFactory(
        d -> {
          PortfolioPosition p = d.getValue();
          BigDecimal cv = p.stock().getSalesPrice().multiply(p.totalQuantity());
          return new SimpleStringProperty(fmt(cv));
        });
    currentCol.setMaxWidth(140);

    TableColumn<PortfolioPosition, String> gainCol = new TableColumn<>("Gain/Loss");
    gainCol.setCellValueFactory(
        d -> {
          PortfolioPosition p = d.getValue();
          BigDecimal currentValue = p.stock().getSalesPrice().multiply(p.totalQuantity());
          BigDecimal purchaseTotal = p.weightedAvgPrice().multiply(p.totalQuantity());
          BigDecimal gain = currentValue.subtract(purchaseTotal);
          BigDecimal pct =
              purchaseTotal.compareTo(BigDecimal.ZERO) == 0
                  ? BigDecimal.ZERO
                  : gain.divide(purchaseTotal, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100));
          String sign = gain.signum() >= 0 ? "+" : "";
          return new SimpleStringProperty(
              sign + fmt(gain) + "\n" + sign + pct.setScale(1, RoundingMode.HALF_UP) + "%");
        });
    gainCol.setCellFactory(
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

    TableColumn<PortfolioPosition, Void> sellCol = new TableColumn<>("");
    sellCol.setCellFactory(
        col ->
            new TableCell<>() {
              private final Button btn = new Button("SELL");

              {
                btn.getStyleClass().addAll("action-button", "primary-button");
                btn.setStyle("-fx-pref-width: 70; -fx-pref-height: 32; -fx-font-size: 12px;");
                btn.setOnAction(
                    e -> {
                      PortfolioPosition pos = getTableView().getItems().get(getIndex());
                      showSellDialog(pos);
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
              }
            });
    sellCol.setMaxWidth(90);

    tv.getColumns().addAll(List.of(companyCol, qtyCol, buyPriceCol, currentCol, gainCol, sellCol));
    return tv;
  }

  private void showSellDialog(PortfolioPosition pos) {
    Stock stock = pos.stock();
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setTitle("Sell  —  " + stock.getSymbol());
    dialog.setHeaderText("Current price: " + fmt(stock.getSalesPrice()));

    Label ownedInfo = new Label("You own: " + pos.totalQuantity().toPlainString() + " shares");
    ownedInfo.getStyleClass().add("dialog-info");

    TextField qtyField = new TextField(pos.totalQuantity().toPlainString());

    // Live sell estimate (mirrors buy preview pattern)
    Label sellGross = new Label();
    Label sellComm = new Label();
    Label sellTax = new Label();
    Label sellTotal = new Label();
    sellTotal.getStyleClass().add("info-value");
    Label sellTitle = new Label("Sell proceeds estimate:");
    sellTitle.getStyleClass().add("info-value");
    VBox sellPreview = new VBox(3, sellTitle, sellGross, sellComm, sellTax, sellTotal);
    sellPreview.getStyleClass().add("sell-preview");

    Runnable updatePreview =
        () -> {
          try {
            BigDecimal qty = new BigDecimal(qtyField.getText().trim());
            if (qty.signum() > 0 && qty.compareTo(pos.totalQuantity()) <= 0) {
              Share temp = new Share(stock, qty, pos.weightedAvgPrice());
              SaleCalculator calc = new SaleCalculator(temp);
              sellGross.setText("Market value:  " + fmt(calc.calculateGross()));
              sellComm.setText("Commission (1%):  -" + fmt(calc.calculateCommission()));
              sellTax.setText("Tax (30% profit):  -" + fmt(calc.calculateTax()));
              sellTotal.setText("You receive:  " + fmt(calc.calculateTotal()));
            } else {
              sellGross.setText("");
              sellComm.setText("");
              sellTax.setText("");
              sellTotal.setText("");
            }
          } catch (Exception ignored) {
            sellGross.setText("");
            sellComm.setText("");
            sellTax.setText("");
            sellTotal.setText("");
          }
        };
    qtyField.textProperty().addListener((obs, old, val) -> updatePreview.run());
    updatePreview.run();

    VBox mainContent =
        new VBox(
            10, ownedInfo, new Separator(), new Label("Quantity to sell:"), qtyField, sellPreview);
    mainContent.setMinWidth(320);
    dialog.getDialogPane().setContent(mainContent);
    applyTheme(dialog.getDialogPane());

    ButtonType sellNowType = new ButtonType("Sell Now", ButtonBar.ButtonData.OK_DONE);
    ButtonType placeSellType = new ButtonType("Place Sell Order", ButtonBar.ButtonData.OTHER);
    ButtonType confirmOrderType = new ButtonType("Confirm Order", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(sellNowType, placeSellType, ButtonType.CANCEL);

    TextField[] targetPriceRef = {null};
    TextField[] qtyAtSwitch = {null};

    Node placeSellBtn = dialog.getDialogPane().lookupButton(placeSellType);
    placeSellBtn.addEventFilter(
        ActionEvent.ACTION,
        evt -> {
          evt.consume();
          qtyAtSwitch[0] = new TextField(qtyField.getText());
          TextField targetPriceField = new TextField();
          targetPriceField.setPromptText("Target price");
          targetPriceRef[0] = targetPriceField;

          Label grossLabel = new Label();
          Label commLabel = new Label();
          Label taxLabel = new Label();
          Label receiveLabel = new Label();
          receiveLabel.getStyleClass().add("info-value");
          Label titleLabel = new Label("Sell proceeds estimate at target:");
          titleLabel.getStyleClass().add("info-value");
          VBox limitPreview =
              new VBox(3, titleLabel, grossLabel, commLabel, taxLabel, receiveLabel);
          limitPreview.getStyleClass().add("sell-preview");

          Runnable updateLimitPreview =
              () -> {
                try {
                  BigDecimal qty = new BigDecimal(qtyAtSwitch[0].getText().trim());
                  BigDecimal targetPrice = new BigDecimal(targetPriceField.getText().trim());
                  if (qty.signum() > 0
                      && targetPrice.signum() > 0
                      && qty.compareTo(pos.totalQuantity()) <= 0) {
                    BigDecimal gross = targetPrice.multiply(qty);
                    BigDecimal commission =
                        gross.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal costBasis = pos.weightedAvgPrice().multiply(qty);
                    BigDecimal profit = gross.subtract(costBasis).subtract(commission);
                    BigDecimal tax =
                        profit.signum() > 0
                            ? profit
                                .multiply(new BigDecimal("0.30"))
                                .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    BigDecimal receive = gross.subtract(commission).subtract(tax);
                    grossLabel.setText("Market value:  " + fmt(gross));
                    commLabel.setText("Commission (1%):  -" + fmt(commission));
                    taxLabel.setText("Tax (30% profit):  -" + fmt(tax));
                    receiveLabel.setText("You receive:  " + fmt(receive));
                  } else {
                    grossLabel.setText("");
                    commLabel.setText("");
                    taxLabel.setText("");
                    receiveLabel.setText("");
                  }
                } catch (Exception ignored) {
                  grossLabel.setText("");
                  commLabel.setText("");
                  taxLabel.setText("");
                  receiveLabel.setText("");
                }
              };
          qtyAtSwitch[0].textProperty().addListener((o, old, v) -> updateLimitPreview.run());
          targetPriceField.textProperty().addListener((o, old, v) -> updateLimitPreview.run());
          updateLimitPreview.run();

          VBox limitContent =
              new VBox(
                  10,
                  new Label("Quantity to sell:"),
                  qtyAtSwitch[0],
                  new Label("Target price:"),
                  targetPriceField,
                  limitPreview);
          limitContent.setMinWidth(320);
          final double currentWidth = dialog.getDialogPane().getWidth();
          dialog.setHeaderText("Place Limit Sell Order — " + stock.getSymbol());
          dialog.getDialogPane().setContent(limitContent);
          dialog.getDialogPane().setMinWidth(currentWidth);
          dialog.getDialogPane().getButtonTypes().setAll(confirmOrderType, ButtonType.CANCEL);
          Platform.runLater(() -> dialog.getDialogPane().getScene().getWindow().sizeToScene());
        });

    dialog
        .showAndWait()
        .ifPresent(
            result -> {
              if (result == ButtonType.CANCEL) {
                return;
              }
              try {
                if (result == sellNowType) {
                  BigDecimal qty = new BigDecimal(qtyField.getText().trim());
                  if (qty.signum() <= 0 || qty.compareTo(pos.totalQuantity()) > 0) {
                    throw new NumberFormatException();
                  }
                  onSellQuantity.accept(stock.getSymbol(), qty);
                } else if (result == confirmOrderType && targetPriceRef[0] != null) {
                  BigDecimal qty = new BigDecimal(qtyAtSwitch[0].getText().trim());
                  BigDecimal targetPrice = new BigDecimal(targetPriceRef[0].getText().trim());
                  if (qty.signum() <= 0 || targetPrice.signum() <= 0) {
                    throw new NumberFormatException();
                  }
                  onPlaceSellOrder.accept(
                      LimitOrder.sell(
                          stock.getSymbol(),
                          stock.getCompany(),
                          qty,
                          targetPrice,
                          exchange.getWeek()));
                }
              } catch (NumberFormatException ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.initOwner(stage);
                err.setHeaderText("Invalid input");
                err.setContentText("Enter a valid quantity (≤ owned) and positive price.");
                applyTheme(err.getDialogPane());
                err.showAndWait();
              }
            });
  }

  @Override
  public void onExchangeUpdated(Exchange exchange) {
    positionList.setAll(buildPositions());
    if (portfolioChart != null) {
      Platform.runLater(() -> portfolioChart.setData(buildPortfolioHistory()));
    }
  }

  private List<BigDecimal> buildPortfolioHistory() {
    return player.getNetWorthHistory();
  }

  private void applyTheme(DialogPane pane) {
    pane.getStylesheets().add(getClass().getResource(GLOBAL_CSS).toExternalForm());
  }

  private String fmt(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}
