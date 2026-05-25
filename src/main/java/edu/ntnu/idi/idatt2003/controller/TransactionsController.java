package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import edu.ntnu.idi.idatt2003.view.TransactionsView;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

public class TransactionsController {

  private final Exchange exchange;
  private final Player player;
  private final TransactionsView view;

  public TransactionsController(Exchange exchange, Player player) {
    this.exchange = exchange;
    this.player = player;
    this.view = new TransactionsView();
  }

  public Node buildContent() {
    return view.buildContent(
        collectAllTransactions(),
        player.getOrderBook().getPendingOrders(),
        player.getOrderBook().getCompletedOrders(),
        order -> player.getOrderBook().cancelOrder(order)
    );
  }

  List<Transaction> collectAllTransactions() {
    List<Transaction> all = new ArrayList<>();
    for (int w = 1; w <= exchange.getWeek(); w++) {
      all.addAll(player.getTransactionArchive().getTransactions(w));
    }
    return all;
  }
}