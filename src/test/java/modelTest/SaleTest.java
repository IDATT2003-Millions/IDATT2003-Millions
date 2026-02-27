package modelTest;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaleTest {

    private Share share;
    private Sale sale;
    private Player player;

    @BeforeEach
    void setUp() {
        Stock stock = new Stock("TSLA", "Tesla", new BigDecimal("150"));
        share = new Share(stock, new BigDecimal("2"), new BigDecimal("100"));
        sale = new Sale(share, 1);
        player = new Player("Alice", new BigDecimal("1000"));
        player.getPortfolio().addShare(share);
    }

    @Test
    void constructor_validInput_setsInheritedFields() {
        assertEquals(share, sale.getShare());
        assertEquals(1, sale.getWeek());
        assertFalse(sale.isCommitted());
    }

    @Test
    void commit_validSale_updatesPlayerState() {
        BigDecimal startingMoney = player.getMoney();
        BigDecimal totalProceeds = sale.getCalculator().calculateTotal();

        sale.commit(player);

        assertTrue(sale.isCommitted());
        assertEquals(0, player.getMoney().compareTo(startingMoney.add(totalProceeds)));
        assertFalse(player.getPortfolio().contains(share));
        assertFalse(player.getTransactionArchive().isEmpty());
        assertEquals(1, player.getTransactionArchive().getSales(1).size());
    }

    @Test
    void commit_nullPlayer_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> sale.commit(null));
    }

    @Test
    void commit_twice_throwsIllegalArgumentException() {
        sale.commit(player);

        assertThrows(IllegalArgumentException.class, () -> sale.commit(player));
    }

    @Test
    void commit_withoutOwnedShare_throwsIllegalArgumentException() {
        Player otherPlayer = new Player("Bob", new BigDecimal("1000"));

        assertThrows(IllegalArgumentException.class, () -> sale.commit(otherPlayer));
    }
}
