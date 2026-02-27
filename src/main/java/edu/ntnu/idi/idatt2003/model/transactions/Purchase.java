package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.calculators.PurchaseCalculator;

/**
 * Represents a purchase done on the market.
 *
 * <p>A purchase is created with a specific share and week number.
 * The transaction is completed when {@link #commit(Player)} is called.</p>
 *
 * <p>When committed, the total cost is withdrawn from the players balance,
 * the share is added to the players portfolio, and the transaction is stored
 * in the players transaction archive</p>
 */
public class Purchase extends Transaction {

  /**
   * Creates a new purchase transaction.
   *
   * <p>The transaction uses a {@link PurchaseCalculator}
   *to calculate costs.</p>
   *
   * @param share the share being purchased
   * @param week the week of the purchase
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }


  @Override
  public void commit(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    if (isCommitted()) {
      throw new IllegalStateException("This purchase has already been committed,");
    }

    java.math.BigDecimal totalCost = getCalculator().calculateTotal();

    if (player.getMoney().compareTo(totalCost) < 0) {
      throw new IllegalStateException("Insufficient funds");
    }

    player.withdrawMoney(totalCost);
    player.getPortfolio().addShare(getShare());
    setCommitted();
    player.getTransactionArchive().add(this);
  }
}
