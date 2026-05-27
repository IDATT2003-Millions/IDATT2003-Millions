package edu.ntnu.idi.idatt2003.modelTest.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.ntnu.idi.idatt2003.model.transactions.LimitOrder;
import edu.ntnu.idi.idatt2003.model.transactions.OrderType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LimitOrderTest {

  private LimitOrder buyOrder;
  private LimitOrder sellOrder;

  @BeforeEach
  void setUp() {
    buyOrder = LimitOrder.buy("AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150"), 1);
    sellOrder = LimitOrder.sell("MSFT", "Microsoft", new BigDecimal("5"), new BigDecimal("200"), 2);
  }

  @Test
  void buyFactory_setsAllFieldsCorrectly() {
    assertEquals(OrderType.BUY, buyOrder.getType());
    assertEquals("AAPL", buyOrder.getSymbol());
    assertEquals("Apple Inc.", buyOrder.getCompany());
    assertEquals(new BigDecimal("10"), buyOrder.getQuantity());
    assertEquals(new BigDecimal("150"), buyOrder.getTargetPrice());
    assertEquals(1, buyOrder.getWeekPlaced());
  }

  @Test
  void sellFactory_setsAllFieldsCorrectly() {
    assertEquals(OrderType.SELL, sellOrder.getType());
    assertEquals("MSFT", sellOrder.getSymbol());
    assertEquals("Microsoft", sellOrder.getCompany());
    assertEquals(new BigDecimal("5"), sellOrder.getQuantity());
    assertEquals(new BigDecimal("200"), sellOrder.getTargetPrice());
    assertEquals(2, sellOrder.getWeekPlaced());
  }

  @Test
  void newBuyOrder_isNotExecutedOrCancelled() {
    assertFalse(buyOrder.isExecuted());
    assertFalse(buyOrder.isCancelled());
  }

  @Test
  void newSellOrder_isNotExecutedOrCancelled() {
    assertFalse(sellOrder.isExecuted());
    assertFalse(sellOrder.isCancelled());
  }

  @Test
  void buyFactory_shareIsNull() {
    assertNull(buyOrder.getShare());
  }

  @Test
  void sellFactory_shareIsNull() {
    assertNull(sellOrder.getShare());
  }
}
