package modelTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.Stock;
import edu.ntnu.idi.idatt2003.model.calculators.PurchaseCalculator;

public class PurchaseCalculatorTest {
    
    private PurchaseCalculator pc;

    private Share createValidShare() {
        Stock st = new Stock("TEST", "Test", new BigDecimal("100"));
        return new Share(st, new BigDecimal("2"), new BigDecimal("100"));
    }

    

    @BeforeEach
    void setUp() {
        Share share = createValidShare();
        pc = new PurchaseCalculator(share);
    }

    @Test
    void calculateGross_shouldReturnCorrectValue() {
        assertEquals(new BigDecimal("200"), pc.calculateGross());
    }

    @Test
    void calculateCommission_shouldReturnCorrectCommission() {
        BigDecimal commission = pc.calculateCommission();
        assertEquals(0, commission.compareTo(new BigDecimal("1")));
    }

    @Test
    void calculateTax_shouldReturnZeroForPurchase() {
        BigDecimal tax = new BigDecimal("0");
        assertEquals(0, tax.compareTo(pc.calculateTax()));
    }

    @Test
    void calculateTotal_shouldReturnGrossPlusCommission() {
        BigDecimal total = new BigDecimal("201");
        assertEquals(0, total.compareTo(pc.calculateTotal()));
    }

    @Test 
    void constructor_shouldThrowExceptionIfShareIsNull() {

        assertThrows(NullPointerException.class, 
            () -> new PurchaseCalculator(null));
    }

    @Test
    void calculateTotal_shouldBeGreaterThanGross() {
    assertTrue(pc.calculateTotal()
            .compareTo(pc.calculateGross()) > 0);
    }


}
