package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;
import edu.ntnu.idi.idatt2003.model.core.Share;

/**
 * Represents a sale transaction in the stock market.
 *
 * <p>A sale is created with a specific share and week number. The transaction is completed when
 * {@link #commit(Player)} is called.
 *
 * <p>When committed, the total proceeds are added to the player's balance, the share is removed
 * from the player's portfolio, and the transaction is stored in the player's transaction archive.
 */
public class Sale extends Transaction {

  /**
   * Creates a new sale transaction.
   *
   * <p>The transaction uses a {@link SaleCalculator} to calculate costs
   *
   * @param share the share to sell
   * @param week the week of the sale
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
  }

  @Override
  public void commit(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null.");
    }

    if (isCommitted()) {
      throw new IllegalStateException("This sale has already been committed.");
    }

    if (!player.getPortfolio().contains(getShare())) {
      throw new IllegalArgumentException("Player does not own the share attempted to be sold");
    }

    java.math.BigDecimal totalProceeds = getCalculator().calculateTotal();

    player.addMoney(totalProceeds);
    player.getPortfolio().removeShare(getShare());
    setCommitted();
    player.getTransactionArchive().add(this);
  }
}
