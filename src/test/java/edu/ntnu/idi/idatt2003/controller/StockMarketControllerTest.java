package controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt2003.controller.StockMarketController;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StockMarketControllerTest {

  private Exchange exchange;
  private Player player;
  private Stock stock;
  private StockMarketController controller;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple", new BigDecimal("50.00"));
    exchange = new Exchange("Test Exchange", List.of(stock));
    player = new Player("Alice", new BigDecimal("1000.00"));
    controller = new StockMarketController(exchange, player, null);
    detachControllerViewObserver(controller, exchange);
  }

  @Test
  void advanceWeek_callsExchangeAdvanceAndIncrementsWeek() throws Exception {
    invokePrivate(controller, "advanceWeek");
    assertEquals(2, exchange.getWeek());
  }

  @Test
  void buy_validInput_commitsPurchaseAndUpdatesPlayerState() throws Exception {
    invokePrivate(controller, "buy", Stock.class, BigDecimal.class, stock, new BigDecimal("2"));

    assertEquals(1, player.getPortfolio().getShares("AAPL").size());
    assertEquals(1, player.getTransactionArchive().getPurchases(1).size());
    assertTrue(player.getMoney().compareTo(new BigDecimal("900.00")) < 0);
  }

  @Test
  void sell_ownedShare_commitsSaleAndRemovesOwnedShare() throws Exception {
    exchange.buy("AAPL", BigDecimal.ONE, player).commit(player);
    Share owned = player.getPortfolio().getShares("AAPL").getFirst();

    invokePrivate(controller, "sell", Share.class, owned);

    assertTrue(player.getPortfolio().getShares().isEmpty());
    assertEquals(1, player.getTransactionArchive().getSales(1).size());
    assertTrue(player.getMoney().compareTo(new BigDecimal("1000.00")) < 0);
  }

  private static void invokePrivate(Object target, String methodName) throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(target);
  }

  private static void invokePrivate(
      Object target, String methodName, Class<?> paramType, Object arg) throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName, paramType);
    method.setAccessible(true);
    method.invoke(target, arg);
  }

  private static void invokePrivate(
      Object target, String methodName, Class<?> p1, Class<?> p2, Object a1, Object a2)
      throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName, p1, p2);
    method.setAccessible(true);
    method.invoke(target, a1, a2);
  }

  private static void detachControllerViewObserver(
      StockMarketController controller, Exchange exchange) {
    try {
      java.lang.reflect.Field viewField = controller.getClass().getDeclaredField("view");
      viewField.setAccessible(true);
      Object view = viewField.get(controller);
      exchange.removeObserver((ExchangeObserver) view);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
