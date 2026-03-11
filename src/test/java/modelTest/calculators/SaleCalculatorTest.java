package modelTest.calculators;

import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SaleCalculatorTest {

    private SaleCalculator sc;

    private Share createValidShareWithSalesPrice(BigDecimal salesPrice,
                                                 BigDecimal quantity,
                                                 BigDecimal purchasePrice) {
        Stock st = new Stock("TEST", "Test", salesPrice);
        return new Share(st, quantity, purchasePrice);
    }

    @BeforeEach
    void setUp() {
        // salesPrice=150, quantity=10, purchasePrice=100
        // gross = 1500
        // commission = 1% av 1500 = 15
        // profit = 1500 - (100*10) - 15 = 485
        // tax = 0.3*485 = 145.5
        Share share = createValidShareWithSalesPrice(
                new BigDecimal("150"),
                new BigDecimal("10"),
                new BigDecimal("100")
        );
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
        // purchasePrice høyere -> tap
        Share share = createValidShareWithSalesPrice(
                new BigDecimal("150"),
                new BigDecimal("10"),
                new BigDecimal("200")  // purchaseCosts = 2000
        );
        SaleCalculator scLoss = new SaleCalculator(share);

        assertEquals(0, scLoss.calculateTax().compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculateTotal_shouldMatchImplementation() {
        // OBS: Tester koden din slik den er nå:
        // total = gross + commission + tax = 1500 + 15 + 145.5 = 1660.5
        assertEquals(0, sc.calculateTotal().compareTo(new BigDecimal("1660.5")));
    }

    @Test
    void constructor_shouldThrowExceptionIfShareIsNull() {
        assertThrows(NullPointerException.class, () -> new SaleCalculator(null));
    }

    @Test
    void calculateTotal_shouldBeGreaterThanGross_withCurrentImplementation() {
        // Gjelder fordi du legger til avgift+skatt i total()
        assertTrue(sc.calculateTotal().compareTo(sc.calculateGross()) > 0);
    }
}