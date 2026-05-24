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
    Summary summary = buildSummary();
    return view.buildContent(
        player.getStartingMoney(),
        player.getMoney(),
        player.getPortfolio().getNetWorth(),
        summary.sharesOwned(),
        summary.uniqueStocks(),
        exchange.getWeek(),
        summary.totalTransactions(),
        summary.totalBuys(),
        summary.totalSells(),
        summary.totalBought(),
        summary.totalSold()
    );
  }

  Summary buildSummary() {
    List<Transaction> all = new ArrayList<>();
    for (int w = 1; w <= exchange.getWeek(); w++) {
      all.addAll(player.getTransactionArchive().getTransactions(w));
    }

    int totalBuys = 0;
    int totalSells = 0;
    BigDecimal totalBought = BigDecimal.ZERO;
    BigDecimal totalSold = BigDecimal.ZERO;

    for (Transaction t : all) {
      BigDecimal gross = t.getCalculator().calculateGross();
      if (t instanceof Purchase) {
        totalBuys++;
        totalBought = totalBought.add(gross);
      } else {
        totalSells++;
        totalSold = totalSold.add(gross);
      }
    }

    List<Share> shares = player.getPortfolio().getShares();
    Set<String> uniqueSymbols = new HashSet<>();
    for (Share s : shares) {
      uniqueSymbols.add(s.getStock().getSymbol());
    }

    return new Summary(
        shares.size(),
        uniqueSymbols.size(),
        all.size(),
        totalBuys,
        totalSells,
        totalBought,
        totalSold
    );
  }

  record Summary(
      int sharesOwned,
      int uniqueStocks,
      int totalTransactions,
      int totalBuys,
      int totalSells,
      BigDecimal totalBought,
      BigDecimal totalSold
  ) {
  }
}
