package modelTest.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PurchaseTest {

  private Share share;
  private Purchase purchase;
  private Player player;

  @BeforeEach
  void setUp() {
    share =
        new Share(
            new Stock("TSLA", "Tesla", new BigDecimal("100")),
            new BigDecimal("2"),
            new BigDecimal("100"));
    purchase = new Purchase(share, 1);
    player = new Player("Alice", new BigDecimal("1000"));
  }

  @Test
  void constructor_validInput_setsInheritedFields() {
    assertEquals(share, purchase.getShare());
    assertEquals(1, purchase.getWeek());
    assertFalse(purchase.isCommitted());
  }

  @Test
  void commit_validPurchase_updatesPlayerState() {
    BigDecimal startingMoney = player.getMoney();
    BigDecimal totalCost = purchase.getCalculator().calculateTotal();

    purchase.commit(player);

    assertTrue(purchase.isCommitted());
    assertEquals(0, player.getMoney().compareTo(startingMoney.subtract(totalCost)));
    assertTrue(player.getPortfolio().contains(share));
    assertFalse(player.getTransactionArchive().isEmpty());
    assertEquals(1, player.getTransactionArchive().getPurchases(1).size());
  }

  @Test
  void commit_nullPlayer_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> purchase.commit(null));
  }

  @Test
  void commit_twice_throwsIllegalStateException() {
    purchase.commit(player);

    assertThrows(IllegalStateException.class, () -> purchase.commit(player));
  }

  @Test
  void commit_insufficientFunds_throwsIllegalStateException() {
    Player poorPlayer = new Player("Bob", new BigDecimal("50"));

    assertThrows(IllegalStateException.class, () -> purchase.commit(poorPlayer));
  }
}
