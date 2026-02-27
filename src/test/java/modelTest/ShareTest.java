package modelTest;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ShareTest {

    private static Stock createValidStock() {
        return new Stock("TEST", "Test", new BigDecimal("100"));
    }

    @Test
    void constructor_validInput_setsFields() {
        Stock stock = createValidStock();
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("123.45");

        Share share = new Share(stock, quantity, purchasePrice);

        assertSame(stock, share.getStock());
        assertEquals(quantity, share.getQuantity());
        assertEquals(purchasePrice, share.getPurchasePrice());
    }

    @Test
    void constructor_nullStock_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Share(null, new BigDecimal("1"), new BigDecimal("10")));
    }

    @Test
    void constructor_nullQuantity_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, null, new BigDecimal("10")));
    }

    @Test
    void constructor_zeroQuantity_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, BigDecimal.ZERO, new BigDecimal("10")));
    }

    @Test
    void constructor_negativeQuantity_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, new BigDecimal("-0.01"), new BigDecimal("10")));
    }

    @Test
    void constructor_nullPurchasePrice_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, new BigDecimal("1"), null));
    }

    @Test
    void constructor_zeroPurchasePrice_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, new BigDecimal("1"), BigDecimal.ZERO));
    }

    @Test
    void constructor_negativePurchasePrice_throwsIllegalArgumentException() {
        Stock stock = createValidStock();

        assertThrows(IllegalArgumentException.class,
                () -> new Share(stock, new BigDecimal("1"), new BigDecimal("-10")));
    }
}
