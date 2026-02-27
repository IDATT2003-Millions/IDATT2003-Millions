package modelTest;

import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertThrows(IllegalArgumentException.class,
                () -> exchange.buy("TSLA", BigDecimal.ZERO, player));
        assertThrows(IllegalArgumentException.class,
                () -> exchange.buy("TSLA", new BigDecimal("-1"), player));
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
                new Share(new Stock("MSFT", "Microsoft", new BigDecimal("300.00")), BigDecimal.ONE,
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
}
