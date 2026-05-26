package modelTest.transactions;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt2003.model.core.Portfolio;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.core.TransactionArchive;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    assertThrows(IllegalArgumentException.class, () -> new Player("Alice", new BigDecimal("-1")));
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
    assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(new BigDecimal("-1")));
    assertThrows(
        IllegalArgumentException.class, () -> player.withdrawMoney(new BigDecimal("1001")));
  }

  @Test
  void getters_returnPortfolioAndTransactionArchiveInstances() {
    Portfolio portfolio = player.getPortfolio();
    TransactionArchive archive = player.getTransactionArchive();

    assertNotNull(portfolio);
    assertNotNull(archive);
  }

  public static class TransactionArchiveTest {

    private TransactionArchive archive;
    private Purchase purchaseWeek1;
    private Purchase purchaseWeek2;
    private Sale saleWeek1;

    private Share createShare(String symbol, String company, String price) {
      Stock stock = new Stock(symbol, company, new BigDecimal(price));
      return new Share(stock, new BigDecimal("2"), new BigDecimal(price));
    }

    @BeforeEach
    void setUp() {
      archive = new TransactionArchive();
      purchaseWeek1 = new Purchase(createShare("AAA", "Alpha", "100"), 1);
      purchaseWeek2 = new Purchase(createShare("BBB", "Beta", "200"), 2);
      saleWeek1 = new Sale(createShare("CCC", "Gamma", "300"), 1);
    }

    @Test
    void constructor_createsEmptyArchive() {
      assertTrue(archive.isEmpty());
    }

    @Test
    void add_validTransaction_returnsTrueAndArchiveIsNotEmpty() {
      assertTrue(archive.add(purchaseWeek1));
      assertFalse(archive.isEmpty());
    }

    @Test
    void add_nullTransaction_throwsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> archive.add(null));
    }

    @Test
    void getTransactions_returnsOnlyTransactionsForWeek() {
      archive.add(purchaseWeek1);
      archive.add(purchaseWeek2);
      archive.add(saleWeek1);

      List<Transaction> transactions = archive.getTransactions(1);

      assertEquals(2, transactions.size());
      assertTrue(transactions.contains(purchaseWeek1));
      assertTrue(transactions.contains(saleWeek1));
    }

    @Test
    void getTransactions_invalidWeek_throwsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> archive.getTransactions(0));
      assertThrows(IllegalArgumentException.class, () -> archive.getTransactions(-1));
    }

    @Test
    void getPurchases_returnsOnlyPurchasesForWeek() {
      archive.add(purchaseWeek1);
      archive.add(purchaseWeek2);
      archive.add(saleWeek1);

      List<Purchase> purchases = archive.getPurchases(1);

      assertEquals(1, purchases.size());
      assertEquals(purchaseWeek1, purchases.getFirst());
    }

    @Test
    void getPurchases_invalidWeek_throwsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> archive.getPurchases(0));
    }

    @Test
    void getSales_returnsOnlySalesForWeek() {
      archive.add(purchaseWeek1);
      archive.add(saleWeek1);

      List<Sale> sales = archive.getSales(1);

      assertEquals(1, sales.size());
      assertEquals(saleWeek1, sales.getFirst());
    }

    @Test
    void getSales_invalidWeek_throwsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> archive.getSales(0));
    }

    @Test
    void countDistinctWeeks_returnsNumberOfUniqueWeeks() {
      archive.add(purchaseWeek1);
      archive.add(saleWeek1);
      archive.add(purchaseWeek2);

      assertEquals(2, archive.countDistinctWeeks());
    }
  }
}
