package edu.ntnu.idi.idatt2003.modelTest.calculators;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SaleCalculatorTest {

  private SaleCalculator sc;

  private Share createValidShareWithSalesPrice(
      BigDecimal salesPrice, BigDecimal quantity, BigDecimal purchasePrice) {
    Stock st = new Stock("TEST", "Test", salesPrice);
    return new Share(st, quantity, purchasePrice);
  }

  @BeforeEach
  void setUp() {
    Share share =
        createValidShareWithSalesPrice(
            new BigDecimal("150"), new BigDecimal("10"), new BigDecimal("100"));
    sc = new SaleCalculator(share);
  }

  @Test
  void calculateGross_shouldReturnCorrectValue() {
    assertEquals(new BigDecimal("1500"), sc.calculateGross());
  }

  @Test
  void calculateCommission_shouldReturnCorrectCommission() {

    assertEquals(0, sc.calculateCommission().compareTo(new BigDecimal("15")));
  }

  @Test
  void calculateTax_shouldReturnCorrectTaxWhenProfitIsPositive() {
    assertEquals(0, sc.calculateTax().compareTo(new BigDecimal("145.5")));
  }

  @Test
  void calculateTax_shouldReturnZeroWhenProfitIsNegative() {
    Share share =
        createValidShareWithSalesPrice(
            new BigDecimal("150"),
            new BigDecimal("10"),
            new BigDecimal("200")
            );
    SaleCalculator scLoss = new SaleCalculator(share);

    assertEquals(0, scLoss.calculateTax().compareTo(BigDecimal.ZERO));
  }

  @Test
  void calculateTotal_shouldReturnGrossMinusCommissionAndTax() {
    assertEquals(0, sc.calculateTotal().compareTo(new BigDecimal("1339.5")));
  }

  @Test
  void constructor_shouldThrowExceptionIfShareIsNull() {
    assertThrows(NullPointerException.class, () -> new SaleCalculator(null));
  }

  @Test
  void calculateTotal_shouldBeLessThanGross() {
    assertTrue(sc.calculateTotal().compareTo(sc.calculateGross()) < 0);
  }
}
