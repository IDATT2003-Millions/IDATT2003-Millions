package edu.ntnu.idi.idatt2003.modelTest.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.OrderBook;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderBookTest {

  private OrderBook orderBook;
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100"));
    exchange = new Exchange("TestExchange", List.of(stock));
    player = new Player("Alice", new BigDecimal("10000"));
    orderBook = new OrderBook();
  }


  @Test
  void placeOrder_addsToPendingOrders() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);

    orderBook.placeOrder(order);

    assertTrue(orderBook.getPendingOrders().contains(order));
    assertTrue(orderBook.getCompletedOrders().isEmpty());
  }

 
  @Test
  void cancelOrder_movesOrderFromPendingToCompleted() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.cancelOrder(order);

    assertFalse(orderBook.getPendingOrders().contains(order));
    assertTrue(orderBook.getCompletedOrders().contains(order));
    assertTrue(order.isCancelled());
  }

  @Test
  void cancelOrder_orderNotInPending_doesNothing() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);

    orderBook.cancelOrder(order);
    assertTrue(orderBook.getPendingOrders().isEmpty());
    assertTrue(orderBook.getCompletedOrders().isEmpty());
  }

  
  @Test
  void executeOrders_buyOrder_priceAtOrBelowTarget_executesOrder() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertTrue(order.isExecuted());
    assertFalse(order.isCancelled());
    assertTrue(orderBook.getPendingOrders().isEmpty());
    assertTrue(orderBook.getCompletedOrders().contains(order));
  }

  @Test
  void executeOrders_buyOrder_priceAboveTarget_orderStaysPending() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("50"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertFalse(order.isExecuted());
    assertFalse(order.isCancelled());
    assertTrue(orderBook.getPendingOrders().contains(order));
  }

  @Test
  void executeOrders_buyOrder_insufficientFunds_cancelsOrder() {
    Player poorPlayer = new Player("Bob", new BigDecimal("1"));
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, poorPlayer);

    assertFalse(order.isExecuted());
    assertTrue(order.isCancelled());
  }

  @Test
  void executeOrders_buyOrder_playerReceivesSharesAfterExecution() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertFalse(player.getPortfolio().getShares("AAPL").isEmpty());
  }

  
  @Test
  void executeOrders_sellOrder_priceAtOrAboveTarget_executesOrder() {

    Share share = new Share(exchange.getStock("AAPL"), new BigDecimal("5"), new BigDecimal("80"));
    player.getPortfolio().addShare(share);

    LimitOrder order = LimitOrder.sell("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("50"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertTrue(order.isExecuted());
    assertFalse(order.isCancelled());
    assertTrue(orderBook.getPendingOrders().isEmpty());
  }

  @Test
  void executeOrders_sellOrder_priceBelowTarget_orderStaysPending() {
    Share share = new Share(exchange.getStock("AAPL"), new BigDecimal("5"), new BigDecimal("80"));
    player.getPortfolio().addShare(share);

   
    LimitOrder order = LimitOrder.sell("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertFalse(order.isExecuted());
    assertFalse(order.isCancelled());
    assertTrue(orderBook.getPendingOrders().contains(order));
  }

  @Test
  void executeOrders_sellOrder_noSharesOwned_cancelsOrder() {
   
    LimitOrder order = LimitOrder.sell("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("50"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertFalse(order.isExecuted());
    assertTrue(order.isCancelled());
  }


  @Test
  void executeOrders_unknownSymbol_cancelsOrder() {
    LimitOrder order = LimitOrder.buy("UNKNOWN", "Unknown Corp", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertFalse(order.isExecuted());
    assertTrue(order.isCancelled());
  }

  @Test
  void executeOrders_executedOrder_weekResolvedMatchesExchangeWeek() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    orderBook.executeOrders(exchange, player);

    assertEquals(exchange.getWeek(), order.getWeekResolved());
  }


  @Test
  void getPendingOrders_returnsUnmodifiableList() {
    LimitOrder order = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("5"),
        new BigDecimal("200"), 1);
    orderBook.placeOrder(order);

    assertFalse(orderBook.getPendingOrders().isEmpty());
    org.junit.jupiter.api.Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> orderBook.getPendingOrders().clear());
  }
}
