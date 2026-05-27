package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.core.Share;

/**
 * Factory for creating {@link Purchase} and {@link Sale} transaction instances.
 *
 * <p>This class is not instantiable; use its static factory methods.
 */
public class TransactionFactory {

  private TransactionFactory() {}

  /**
   * Creates a new purchase transaction for the given share and week.
   *
   * @param share the share being purchased
   * @param week the week of the purchase
   * @return a new uncommitted {@link Purchase}
   */
  public static Purchase createPurchase(Share share, int week) {
    return new Purchase(share, week);
  }

  /**
   * Creates a new sale transaction for the given share and week.
   *
   * @param share the share being sold
   * @param week the week of the sale
   * @return a new uncommitted {@link Sale}
   */
  public static Sale createSale(Share share, int week) {
    return new Sale(share, week);
  }
}
