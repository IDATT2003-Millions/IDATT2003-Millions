package modelTest;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.Stock;
import edu.ntnu.idi.idatt2003.model.calculators.TransactionCalculator;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {

    private Share share;
    private TestCalculator calculator;

    @BeforeEach
    void setUp() {
        share = new Share(new Stock("TEST", "Test", new BigDecimal("100")),
                new BigDecimal("2"), new BigDecimal("100"));
        calculator = new TestCalculator();
    }

    @Test
    void constructor_validInput_setsFields() {
        TestTransaction transaction = new TestTransaction(share, 1, calculator);

        assertEquals(share, transaction.getShare());
        assertEquals(1, transaction.getWeek());
        assertEquals(calculator, transaction.getCalculator());
        assertFalse(transaction.isCommitted());
    }

    @Test
    void constructor_invalidInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestTransaction(null, 1, calculator));
        assertThrows(IllegalArgumentException.class,
                () -> new TestTransaction(share, 0, calculator));
        assertThrows(IllegalArgumentException.class,
                () -> new TestTransaction(share, -1, calculator));
        assertThrows(IllegalArgumentException.class,
                () -> new TestTransaction(share, 1, null));
    }

    @Test
    void setCommitted_marksTransactionAsCommitted() {
        TestTransaction transaction = new TestTransaction(share, 1, calculator);

        transaction.markCommitted();

        assertTrue(transaction.isCommitted());
    }

    @Test
    void commit_testImplementationMarksTransactionAsCommitted() {
        TestTransaction transaction = new TestTransaction(share, 1, calculator);
        Player player = new Player("Alice", new BigDecimal("1000"));

        transaction.commit(player);

        assertTrue(transaction.isCommitted());
    }

    private static class TestTransaction extends Transaction {

        TestTransaction(Share share, int week, TransactionCalculator calculator) {
            super(share, week, calculator);
        }

        void markCommitted() {
            setCommitted();
        }

        @Override
        public void commit(Player player) {
            setCommitted();
        }
    }

    private static class TestCalculator implements TransactionCalculator {

        @Override
        public BigDecimal calculateGross() {
            return new BigDecimal("100");
        }

        @Override
        public BigDecimal calculateCommission() {
            return new BigDecimal("1");
        }

        @Override
        public BigDecimal calculateTax() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal calculateTotal() {
            return new BigDecimal("101");
        }
    }
}
