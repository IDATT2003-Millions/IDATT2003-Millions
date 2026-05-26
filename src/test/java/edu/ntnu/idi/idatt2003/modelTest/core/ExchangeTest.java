package modelTest.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExchangeTest {

  private Stock tesla;
  private Stock apple;
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    tesla = new Stock("TSLA", "Tesla Inc", new BigDecimal("100.00"));
    apple = new Stock("AAPL", "Apple Inc", new BigDecimal("50.00"));
    exchange = new Exchange("NASDAQ", List.of(tesla, apple));
    player = new Player("Alice", new BigDecimal("10000"));
  }

  @Test
  void constructor_validInput_setsNameAndInitialWeek() {
    assertEquals("NASDAQ", exchange.getName());
    assertEquals(1, exchange.getWeek());
  }

  @Test
  void constructor_nullName_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Exchange(null, List.of(tesla)));
  }

  @Test
  void constructor_nullStocks_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new Exchange("NYSE", null));
  }

  @Test
  void hasStock_knownAndUnknownSymbol_returnsExpectedValue() {
    assertTrue(exchange.hasStock("TSLA"));
    assertFalse(exchange.hasStock("MSFT"));
  }

  @Test
  void hasStock_nullSymbol_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> exchange.hasStock(null));
  }

  @Test
  void getStock_existingSymbol_returnsStock() {
    assertEquals(tesla, exchange.getStock("TSLA"));
  }

  @Test
  void getStock_unknownSymbol_throwsNoSuchElementException() {
    assertThrows(NoSuchElementException.class, () -> exchange.getStock("MSFT"));
  }

  @Test
  void findStocks_symbolOrCompanyMatch_caseInsensitive() {
    List<Stock> bySymbol = exchange.findStocks("aapl");
    List<Stock> byCompany = exchange.findStocks("tesla");

    assertEquals(1, bySymbol.size());
    assertEquals("AAPL", bySymbol.getFirst().getSymbol());
    assertEquals(1, byCompany.size());
    assertEquals("TSLA", byCompany.getFirst().getSymbol());
  }

  @Test
  void findStocks_nullSearchTerm_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> exchange.findStocks(null));
  }

  @Test
  void buy_validInput_returnsPurchaseWithExpectedShareAndWeek() {
    Transaction transaction = exchange.buy("TSLA", new BigDecimal("2.5"), player);

    assertInstanceOf(Purchase.class, transaction);
    assertEquals(1, transaction.getWeek());
    assertEquals("TSLA", transaction.getShare().getStock().getSymbol());
    assertEquals(new BigDecimal("2.5"), transaction.getShare().getQuantity());
    assertEquals(new BigDecimal("100.00"), transaction.getShare().getPurchasePrice());
  }

  @Test
  void buy_invalidInput_throwsException() {
    assertThrows(NullPointerException.class, () -> exchange.buy(null, BigDecimal.ONE, player));
    assertThrows(NullPointerException.class, () -> exchange.buy("TSLA", null, player));
    assertThrows(NullPointerException.class, () -> exchange.buy("TSLA", BigDecimal.ONE, null));
    assertThrows(
        IllegalArgumentException.class, () -> exchange.buy("TSLA", BigDecimal.ZERO, player));
    assertThrows(
        IllegalArgumentException.class, () -> exchange.buy("TSLA", new BigDecimal("-1"), player));
    assertThrows(NoSuchElementException.class, () -> exchange.buy("MSFT", BigDecimal.ONE, player));
  }

  @Test
  void sell_validInputForListedStock_returnsSale() {
    Share share = new Share(tesla, BigDecimal.ONE, tesla.getSalesPrice());

    Transaction transaction = exchange.sell(share, player);

    assertInstanceOf(Sale.class, transaction);
    assertEquals(1, transaction.getWeek());
    assertEquals(share, transaction.getShare());
  }

  @Test
  void sell_invalidInput_throwsException() {
    Share listedShare = new Share(tesla, BigDecimal.ONE, tesla.getSalesPrice());
    Share unlistedShare =
        new Share(
            new Stock("MSFT", "Microsoft", new BigDecimal("300.00")),
            BigDecimal.ONE,
            new BigDecimal("300.00"));

    assertThrows(NullPointerException.class, () -> exchange.sell(null, player));
    assertThrows(NullPointerException.class, () -> exchange.sell(listedShare, null));
    assertThrows(IllegalArgumentException.class, () -> exchange.sell(unlistedShare, player));
  }

  @Test
  void advance_incrementsWeekAndUpdatesPricesWithinExpectedBounds() {
    BigDecimal teslaBefore = tesla.getSalesPrice();
    BigDecimal appleBefore = apple.getSalesPrice();

    exchange.advance();

    assertEquals(2, exchange.getWeek());

    BigDecimal teslaAfter = tesla.getSalesPrice();
    BigDecimal appleAfter = apple.getSalesPrice();

    assertTrue(teslaAfter.scale() <= 2);
    assertTrue(appleAfter.scale() <= 2);
    assertTrue(teslaAfter.compareTo(new BigDecimal("95.00")) >= 0);
    assertTrue(teslaAfter.compareTo(new BigDecimal("105.00")) <= 0);
    assertTrue(appleAfter.compareTo(new BigDecimal("47.50")) >= 0);
    assertTrue(appleAfter.compareTo(new BigDecimal("52.50")) <= 0);
    assertTrue(teslaAfter.compareTo(teslaBefore) != 0 || appleAfter.compareTo(appleBefore) != 0);
  }

  @Test
  void getGainers_returnsPositiveChangesInDescendingOrderWithLimit() {
    Stock winner = new Stock("WIN", "Winner Corp", new BigDecimal("100.00"));
    winner.addNewSalesPrice(new BigDecimal("112.00")); // +12.00
    Stock runnerUp = new Stock("RUN", "RunnerUp Corp", new BigDecimal("80.00"));
    runnerUp.addNewSalesPrice(new BigDecimal("85.00")); // +5.00
    Stock flat = new Stock("FLT", "Flat Corp", new BigDecimal("42.00"));
    flat.addNewSalesPrice(new BigDecimal("42.00")); // +0.00
    Stock loser = new Stock("LOS", "Loser Corp", new BigDecimal("60.00"));
    loser.addNewSalesPrice(new BigDecimal("55.00")); // -5.00

    Exchange localExchange = new Exchange("TEST", List.of(winner, runnerUp, flat, loser));

    List<Stock> gainers = localExchange.getGainers(2);

    assertEquals(2, gainers.size());
    assertEquals("WIN", gainers.get(0).getSymbol());
    assertEquals("RUN", gainers.get(1).getSymbol());
  }

  @Test
  void getLosers_returnsNegativeChangesInAscendingOrderWithLimit() {
    Stock biggestDrop = new Stock("BIGD", "Big Drop", new BigDecimal("200.00"));
    biggestDrop.addNewSalesPrice(new BigDecimal("180.00"));
    Stock smallerDrop = new Stock("SMAL", "Smaller Drop", new BigDecimal("90.00"));
    smallerDrop.addNewSalesPrice(new BigDecimal("85.00"));
    Stock gainer = new Stock("GAIN", "Gainer", new BigDecimal("30.00"));
    gainer.addNewSalesPrice(new BigDecimal("35.00"));

    Exchange localExchange = new Exchange("TEST", List.of(biggestDrop, smallerDrop, gainer));

    List<Stock> losers = localExchange.getLosers(2);

    assertEquals(2, losers.size());
    assertEquals("BIGD", losers.get(0).getSymbol());
    assertEquals("SMAL", losers.get(1).getSymbol());
  }

  @Test
  void getGainersAndLosers_nonPositiveLimit_returnsEmptyList() {
    assertTrue(exchange.getGainers(0).isEmpty());
    assertTrue(exchange.getGainers(-1).isEmpty());
    assertTrue(exchange.getLosers(0).isEmpty());
    assertTrue(exchange.getLosers(-1).isEmpty());
  }
}
