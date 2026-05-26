package edu.ntnu.idi.idatt2003.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransactionsControllerLogicTest {

  private Exchange exchange;
  private Player player;
  private TransactionsController controller;

  @BeforeEach
  void setUp() {
    Stock apple = new Stock("AAPL", "Apple", new BigDecimal("50.00"));
    exchange = new Exchange("Test Exchange", List.of(apple));
    player = new Player("Alice", new BigDecimal("1000.00"));
    controller = new TransactionsController(exchange, player);
  }

  @Test
  void collectAllTransactions_multipleWeeks_returnsTransactionsInWeekOrder() {
    exchange.buy("AAPL", BigDecimal.ONE, player).commit(player);
    exchange.advance();
    Share owned = player.getPortfolio().getShares("AAPL").getFirst();
    exchange.sell(owned, player).commit(player);

    List<Transaction> all = controller.collectAllTransactions();

    assertEquals(2, all.size());
    assertEquals(1, all.get(0).getWeek());
    assertEquals(2, all.get(1).getWeek());
  }

  @Test
  void collectAllTransactions_noTransactions_returnsEmptyList() {
    List<Transaction> all = controller.collectAllTransactions();
    assertEquals(0, all.size());
  }
}
