package modelTest;

import edu.ntnu.idi.idatt2003.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockTest {

    @Test
    void constructor_validInput_setsFieldsAndInitialPrice() {
        Stock stock = new Stock("TEST", "Test", new BigDecimal("123.45"));

        assertEquals("TEST", stock.getSymbol());
        assertEquals("Test", stock.getCompany());
        assertEquals(new BigDecimal("123.45"), stock.getSalesPrice());
    }


  @Test
  void constructor_nullSymbol_throwsIllegalArgumentException() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> new Stock(null, "Test", new BigDecimal("10")));

    assertTrue(ex.getMessage().toLowerCase().contains("symbol"));
  }

  @Test
  void constructor_blankSymbol_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("   ", "Test", new BigDecimal("10")));
  }

  @Test
  void constructor_nullCompany_throwsIllegalArgumentException() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> new Stock("TEST", null, new BigDecimal("10")));

    assertTrue(ex.getMessage().toLowerCase().contains("company"));
  }

  @Test
  void constructor_blankCompany_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("TEST", "   ", new BigDecimal("10")));
  }

  @Test
  void constructor_nullSalesPrice_throwsIllegalArgumentException() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> new Stock("TEST", "Test", null));

    assertTrue(ex.getMessage().toLowerCase().contains("price"));
  }

  @Test
  void constructor_zeroSalesPrice_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("TEST", "Test", BigDecimal.ZERO));
  }

  @Test
  void constructor_negativeSalesPrice_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("TEST", "Test", new BigDecimal("-1")));
  }

  @Test
  void addNewSalesPrice_validPrice_updatesCurrentSalesPrice() {
    Stock stock = new Stock("TEST", "Test", new BigDecimal("100"));

    stock.addNewSalesPrice(new BigDecimal("150"));

    assertEquals(new BigDecimal("150"), stock.getSalesPrice());
  }

  @Test
  void addNewSalesPrice_multipleUpdates_lastPriceIsReturned() {
    Stock stock = new Stock("TEST", "Test", new BigDecimal("100"));

    stock.addNewSalesPrice(new BigDecimal("101"));
    stock.addNewSalesPrice(new BigDecimal("99.50"));
    stock.addNewSalesPrice(new BigDecimal("120"));

    assertEquals(new BigDecimal("120"), stock.getSalesPrice());
  }

  @Test
  void addNewSalesPrice_null_throwsIllegalArgumentException() {
    Stock stock = new Stock("TEST", "Test", new BigDecimal("100"));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> stock.addNewSalesPrice(null));

    assertTrue(ex.getMessage().toLowerCase().contains("price"));
  }

  @Test
  void addNewSalesPrice_zero_throwsIllegalArgumentException() {
    Stock stock = new Stock("TEST", "Test", new BigDecimal("100"));

    assertThrows(IllegalArgumentException.class,
        () -> stock.addNewSalesPrice(BigDecimal.ZERO));
  }

  @Test
  void addNewSalesPrice_negative_throwsIllegalArgumentException() {
    Stock stock = new Stock("TEST", "Test", new BigDecimal("100"));

    assertThrows(IllegalArgumentException.class,
        () -> stock.addNewSalesPrice(new BigDecimal("-0.01")));
  }

}
