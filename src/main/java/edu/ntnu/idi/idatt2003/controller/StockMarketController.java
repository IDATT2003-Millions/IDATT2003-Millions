package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.view.StockMarketView;
import javafx.scene.Node;

import java.math.BigDecimal;

/**
 * Handles all logic for the stock market screen.
 * The view only reports what the user did — this class decides what happens.
 */
public class StockMarketController {

  private final Exchange exchange;
  private final Player player;
  private final StockMarketView view;

  public StockMarketController(Exchange exchange, Player player, javafx.stage.Stage stage) {
    this.exchange = exchange;
    this.player = player;
    this.view = new StockMarketView(player, exchange, stage);
  }

  public Node buildContent() {
    return view.buildContent(
        this::advanceWeek,
        this::buy,
        this::sell
    );
  }

  private void advanceWeek() {
    exchange.advance();
    player.snapshotNetWorth();
  }

  private void buy(Stock stock, BigDecimal quantity) {
    exchange.buy(stock.getSymbol(), quantity, player).commit(player);
    exchange.refresh();
  }

  private void sell(Share share) {
    exchange.sell(share, player).commit(player);
    exchange.refresh();
  }
}
