package modelTest.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.TransactionFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransactionFactoryTest {

  private Share share;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("200.00"));
    share = new Share(stock, new BigDecimal("3"), new BigDecimal("200.00"));
  }

  @Test
  void createPurchase_validInput_returnsPurchaseInstance() {
    Purchase purchase = TransactionFactory.createPurchase(share, 1);

    assertInstanceOf(Purchase.class, purchase);
  }

  @Test
  void createPurchase_validInput_setsCorrectShareAndWeek() {
    Purchase purchase = TransactionFactory.createPurchase(share, 2);

    assertSame(share, purchase.getShare());
    assertEquals(2, purchase.getWeek());
  }

  @Test
  void createPurchase_differentWeeks_eachPurchaseHasItsOwnWeek() {
    Purchase week1 = TransactionFactory.createPurchase(share, 1);
    Purchase week5 = TransactionFactory.createPurchase(share, 5);

    assertEquals(1, week1.getWeek());
    assertEquals(5, week5.getWeek());
  }

  @Test
  void createSale_validInput_returnsSaleInstance() {
    Sale sale = TransactionFactory.createSale(share, 1);

    assertInstanceOf(Sale.class, sale);
  }

  @Test
  void createSale_validInput_setsCorrectShareAndWeek() {
    Sale sale = TransactionFactory.createSale(share, 3);

    assertSame(share, sale.getShare());
    assertEquals(3, sale.getWeek());
  }

  @Test
  void createSale_differentWeeks_eachSaleHasItsOwnWeek() {
    Sale week2 = TransactionFactory.createSale(share, 2);
    Sale week4 = TransactionFactory.createSale(share, 4);

    assertEquals(2, week2.getWeek());
    assertEquals(4, week4.getWeek());
  }

  @Test
  void createPurchaseAndCreateSale_sameShare_produceDifferentTypes() {
    Purchase purchase = TransactionFactory.createPurchase(share, 1);
    Sale sale = TransactionFactory.createSale(share, 1);

    assertInstanceOf(Purchase.class, purchase);
    assertInstanceOf(Sale.class, sale);
  }
}
