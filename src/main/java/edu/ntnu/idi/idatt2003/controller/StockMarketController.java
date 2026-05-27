package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.view.StockMarketView;
import java.math.BigDecimal;
import javafx.scene.Node;

/**
 * Handles all logic for the stock market screen. The view only reports what the user did — this
 * class decides what happens.
 */
public class StockMarketController {

  private final Exchange exchange;
  private final Player player;
  private final StockMarketView view;

  /**
   * Creates the stock market controller and its associated view.
   *
   * @param exchange the active exchange
   * @param player the active player
   * @param stage the primary stage (used for dialogs)
   */
  public StockMarketController(Exchange exchange, Player player, javafx.stage.Stage stage) {
    this.exchange = exchange;
    this.player = player;
    this.view = new StockMarketView(player, exchange, stage);
  }

  /**
   * Builds and returns the stock market page content node.
   *
   * @return the rendered stock market page
   */
  public Node buildContent() {
    return view.buildContent(this::advanceWeek, this::buy, this::sell, this::placeLimitOrder);
  }

  private void advanceWeek() {
    exchange.advance();
    player.getOrderBook().executeOrders(exchange, player);
    player.snapshotNetWorth();
    exchange.refresh();
  }

  private void buy(Stock stock, BigDecimal quantity) {
    exchange.buy(stock.getSymbol(), quantity, player).commit(player);
    exchange.refresh();
  }

  private void sell(Share share) {
    exchange.sell(share, player).commit(player);
    exchange.refresh();
  }

  private void placeLimitOrder(LimitOrder order) {
    player.getOrderBook().placeOrder(order);
  }
}
