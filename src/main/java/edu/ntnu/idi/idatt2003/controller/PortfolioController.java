package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.view.MyPortfolioView;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles all logic for the portfolio screen.
 * The view only reports what the user did — this class decides what happens.
 */
public class PortfolioController {

  private final Exchange exchange;
  private final Player player;
  private final MyPortfolioView view;

  public PortfolioController(Exchange exchange, Player player, Stage stage) {
    this.exchange = exchange;
    this.player = player;
    this.view = new MyPortfolioView(player, exchange, stage);
  }

  public Node buildContent() {
    return view.buildContent(this::sellQuantity, this::sellAll, this::advanceWeek, this::placeLimitOrder);
  }

  private void advanceWeek() {
    exchange.advance();
    player.getOrderBook().executeOrders(exchange, player);
    player.snapshotNetWorth();
    exchange.refresh();
  }

  private void sellQuantity(String symbol, BigDecimal quantity) {
    exchange.sellQuantity(symbol, quantity, player);
    exchange.refresh();
  }

  private void sellAll() {
    List<Share> shares = List.copyOf(player.getPortfolio().getShares());
    for (Share share : shares) {
      exchange.sell(share, player).commit(player);
    }
    exchange.refresh();
  }

  private void placeLimitOrder(LimitOrder order) {
    player.getOrderBook().placeOrder(order);
  }
}