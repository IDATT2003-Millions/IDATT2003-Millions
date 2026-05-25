package controllerTest;

import edu.ntnu.idi.idatt2003.controller.PortfolioController;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortfolioControllerTest {

  private Exchange exchange;
  private Player player;
  private Stock tesla;
  private PortfolioController controller;

  @BeforeEach
  void setUp() {
    tesla = new Stock("TSLA", "Tesla", new BigDecimal("100.00"));
    exchange = new Exchange("Test Exchange", List.of(tesla));
    player = new Player("Kristoffer", new BigDecimal("1000.00"));
    controller = new PortfolioController(exchange, player, null);
  }

  @Test
  void sell_ownedShare_commitsTransactionAndRefreshesPlayerState() throws Exception {
    exchange.buy("TSLA", BigDecimal.ONE, player).commit(player);

    invokePrivate(controller, "sellQuantity", String.class, BigDecimal.class, "TSLA", BigDecimal.ONE);

    assertTrue(player.getPortfolio().getShares().isEmpty());
    assertTrue(player.getMoney().compareTo(new BigDecimal("1000.00")) < 0);
    assertEquals(1, player.getTransactionArchive().getSales(1).size());
  }

  private static void invokePrivate(Object target, String methodName,
                                    Class<?> p1, Class<?> p2, Object a1, Object a2)
      throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName, p1, p2);
    method.setAccessible(true);
    method.invoke(target, a1, a2);
  }
}
