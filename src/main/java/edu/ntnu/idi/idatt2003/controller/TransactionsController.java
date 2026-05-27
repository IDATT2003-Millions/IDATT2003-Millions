package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import edu.ntnu.idi.idatt2003.view.TransactionsView;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;

/**
 * Handles all logic for the transaction history screen.
 *
 * <p>Collects all committed transactions from every week and passes them, together with pending and
 * completed limit orders, to the view for display.
 */
public class TransactionsController {

  private final Exchange exchange;
  private final Player player;
  private final TransactionsView view;

  /**
   * Creates the transactions controller and its associated view.
   *
   * @param exchange the active exchange
   * @param player the active player
   */
  public TransactionsController(Exchange exchange, Player player) {
    this.exchange = exchange;
    this.player = player;
    this.view = new TransactionsView();
  }

  /**
   * Builds and returns the transactions page content node.
   *
   * @return the rendered transactions page
   */
  public Node buildContent() {
    return view.buildContent(
        collectAllTransactions(),
        player.getOrderBook().getPendingOrders(),
        player.getOrderBook().getCompletedOrders(),
        order -> player.getOrderBook().cancelOrder(order));
  }

  /**
   * Collects all committed transactions across every week in the current game session.
   *
   * @return a list of all transactions in chronological order
   */
  List<Transaction> collectAllTransactions() {
    List<Transaction> all = new ArrayList<>();
    for (int w = 1; w <= exchange.getWeek(); w++) {
      all.addAll(player.getTransactionArchive().getTransactions(w));
    }
    return all;
  }
}
