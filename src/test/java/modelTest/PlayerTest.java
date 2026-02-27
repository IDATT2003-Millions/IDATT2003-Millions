package modelTest;

import edu.ntnu.idi.idatt2003.model.Portfolio;
import edu.ntnu.idi.idatt2003.model.TransactionArchive;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new BigDecimal("1000"));
    }

    @Test
    void constructor_validInput_setsFields() {
        assertEquals("Alice", player.getName());
        assertEquals(new BigDecimal("1000"), player.getStartingMoney());
        assertEquals(new BigDecimal("1000"), player.getMoney());
        assertNotNull(player.getPortfolio());
        assertNotNull(player.getTransactionArchive());
    }

    @Test
    void constructor_invalidInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Player(null, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Player("   ", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Player("Alice", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Player("Alice", new BigDecimal("-1")));
    }

    @Test
    void addMoney_validAmount_increasesBalance() {
        player.addMoney(new BigDecimal("250"));

        assertEquals(new BigDecimal("1250"), player.getMoney());
    }

    @Test
    void addMoney_invalidAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(null));
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(new BigDecimal("-1")));
    }

    @Test
    void withdrawMoney_validAmount_decreasesBalance() {
        player.withdrawMoney(new BigDecimal("400"));

        assertEquals(new BigDecimal("600"), player.getMoney());
    }

    @Test
    void withdrawMoney_invalidAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(null));
        assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> player.withdrawMoney(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> player.withdrawMoney(new BigDecimal("1001")));
    }

    @Test
    void getters_returnPortfolioAndTransactionArchiveInstances() {
        Portfolio portfolio = player.getPortfolio();
        TransactionArchive archive = player.getTransactionArchive();

        assertNotNull(portfolio);
        assertNotNull(archive);
    }
}
