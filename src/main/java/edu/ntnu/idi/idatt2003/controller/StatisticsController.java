package edu.ntnu.idi.idatt2003.controller;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import edu.ntnu.idi.idatt2003.view.StatisticsView;
import javafx.scene.Node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatisticsController {

  private final Exchange exchange;
  private final Player player;
  private final StatisticsView view;

  public StatisticsController(Exchange exchange, Player player) {
    this.exchange = exchange;
    this.player = player;
    this.view = new StatisticsView();
  }

  public Node buildContent() {
    List<Transaction> all = new ArrayList<>();
    for (int w = 1; w <= exchange.getWeek(); w++) {
      all.addAll(player.getTransactionArchive().getTransactions(w));
    }

    int totalBuys = 0;
    int totalSells = 0;
    BigDecimal totalBought = BigDecimal.ZERO;
    BigDecimal totalSold = BigDecimal.ZERO;

    for (Transaction t : all) {
      Share share = t.getShare();
      BigDecimal total = share.getPurchasePrice().multiply(share.getQuantity());
      if (t instanceof Purchase) {
        totalBuys++;
        totalBought = totalBought.add(total);
      } else {
        totalSells++;
        totalSold = totalSold.add(total);
      }
    }

    List<Share> shares = player.getPortfolio().getShares();
    Set<String> uniqueSymbols = new HashSet<>();
    for (Share s : shares) {
      uniqueSymbols.add(s.getStock().getSymbol());
    }

    return view.buildContent(
        player.getStartingMoney(),
        player.getMoney(),
        player.getPortfolio().getNetWorth(),
        shares.size(),
        uniqueSymbols.size(),
        exchange.getWeek(),
        all.size(),
        totalBuys,
        totalSells,
        totalBought,
        totalSold
    );
  }
}
