package modelTest.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt2003.model.core.Portfolio;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortfolioTest {

  private Portfolio portfolio;
  private Share teslaShare;
  private Share appleShare;

  @BeforeEach
  void setUp() {
    portfolio = new Portfolio();
    teslaShare =
        new Share(
            new Stock("TSLA", "Tesla", new BigDecimal("100")),
            new BigDecimal("2"),
            new BigDecimal("95"));
    appleShare =
        new Share(
            new Stock("AAPL", "Apple", new BigDecimal("50")),
            new BigDecimal("3"),
            new BigDecimal("48"));
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

  @Test
  void getNetWorth_emptyPortfolio_returnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth()));
  }

  @Test
  void getNetWorth_singleShare_returnSaleTotal() {
    portfolio.addShare(appleShare);
    BigDecimal netWorth = portfolio.getNetWorth();
    assertTrue(
        netWorth.compareTo(BigDecimal.ZERO) > 0,
        "Net worth should be positive with a share with value");
  }

  @Test
  void getNetWorth_multipleShares_returnsSum() {
    portfolio.addShare(appleShare);
    portfolio.addShare(teslaShare);

    BigDecimal withBoth = portfolio.getNetWorth();
    Portfolio singlePortfolio = new Portfolio();
    singlePortfolio.addShare(appleShare);
    BigDecimal withOne = singlePortfolio.getNetWorth();

    assertTrue(
        withBoth.compareTo(withOne) > 0,
        "Net worth with two shares should exceed net worth with one share");
  }

  @Test
  void getNetWorth_priceIncreases_netWorthIncreases() {
    portfolio.addShare(appleShare);
    BigDecimal beforeIncrease = portfolio.getNetWorth();

    appleShare.getStock().addNewSalesPrice(new BigDecimal("1000.00"));
    BigDecimal afterIncrease = portfolio.getNetWorth();

    assertTrue(
        afterIncrease.compareTo(beforeIncrease) > 0,
        "Net worth should increase after stock price rises");
  }
}
