package edu.ntnu.idi.idatt2003.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatisticsControllerLogicTest {

  private Exchange exchange;
  private Player player;
  private StatisticsController controller;

  @BeforeEach
  void setUp() {
    Stock apple = new Stock("AAPL", "Apple", new BigDecimal("50.00"));
    exchange = new Exchange("Test Exchange", List.of(apple));
    player = new Player("Alice", new BigDecimal("1000.00"));
    controller = new StatisticsController(exchange, player);
  }

  @Test
  void buildSummary_withBuyAndSell_returnsExpectedAggregates() {
    exchange.buy("AAPL", new BigDecimal("2"), player).commit(player);
    Share owned = player.getPortfolio().getShares("AAPL").getFirst();
    exchange.sell(owned, player).commit(player);

    StatisticsController.Summary summary = controller.buildSummary();

    assertEquals(0, summary.sharesOwned());
    assertEquals(0, summary.uniqueStocks());
    assertEquals(2, summary.totalTransactions());
    assertEquals(1, summary.totalBuys());
    assertEquals(1, summary.totalSells());
    assertEquals(0, summary.totalBought().compareTo(new BigDecimal("100.00")));
    assertEquals(0, summary.totalSold().compareTo(new BigDecimal("100.00")));
  }

  @Test
  void buildSummary_noTransactions_returnsZeroValues() {
    StatisticsController.Summary summary = controller.buildSummary();

    assertEquals(0, summary.sharesOwned());
    assertEquals(0, summary.uniqueStocks());
    assertEquals(0, summary.totalTransactions());
    assertEquals(0, summary.totalBuys());
    assertEquals(0, summary.totalSells());
    assertEquals(0, summary.totalBought().compareTo(BigDecimal.ZERO));
    assertEquals(0, summary.totalSold().compareTo(BigDecimal.ZERO));
  }
}
