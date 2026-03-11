package modelTest.core;

import edu.ntnu.idi.idatt2003.model.core.Portfolio;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortfolioTest {

    private Portfolio portfolio;
    private Share teslaShare;
    private Share appleShare;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        teslaShare = new Share(new Stock("TSLA", "Tesla", new BigDecimal("100")),
                new BigDecimal("2"), new BigDecimal("95"));
        appleShare = new Share(new Stock("AAPL", "Apple", new BigDecimal("50")),
                new BigDecimal("3"), new BigDecimal("48"));
    }

    @Test
    void addShare_validShare_returnsTrueAndStoresShare() {
        assertTrue(portfolio.addShare(teslaShare));
        assertTrue(portfolio.contains(teslaShare));
    }

    @Test
    void addShare_nullShare_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.addShare(null));
    }

    @Test
    void removeShare_existingShare_returnsTrue() {
        portfolio.addShare(teslaShare);

        assertTrue(portfolio.removeShare(teslaShare));
        assertFalse(portfolio.contains(teslaShare));
    }

    @Test
    void removeShare_nullShare_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.removeShare(null));
    }

    @Test
    void getShares_returnsImmutableCopyOfAllShares() {
        portfolio.addShare(teslaShare);

        List<Share> shares = portfolio.getShares();

        assertEquals(1, shares.size());
        assertThrows(UnsupportedOperationException.class, () -> shares.add(appleShare));
    }

    @Test
    void getShares_symbol_returnsMatchingSharesCaseInsensitive() {
        portfolio.addShare(teslaShare);
        portfolio.addShare(appleShare);

        List<Share> shares = portfolio.getShares("tsla");

        assertEquals(1, shares.size());
        assertEquals(teslaShare, shares.getFirst());
    }

    @Test
    void getShares_invalidSymbol_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.getShares(null));
        assertThrows(IllegalArgumentException.class, () -> portfolio.getShares("   "));
    }

    @Test
    void contains_nullShare_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.contains(null));
    }
}
