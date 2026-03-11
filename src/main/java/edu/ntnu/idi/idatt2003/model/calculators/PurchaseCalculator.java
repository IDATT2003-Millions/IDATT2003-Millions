package edu.ntnu.idi.idatt2003.model.calculators;

import edu.ntnu.idi.idatt2003.model.core.Share;
import java.math.BigDecimal;
import java.util.Objects;


/**
 * Calculates financial values for purchasing transaction.
 *
 * <p>Commission rate for a purchase is 0.5% of the gross value.
 * There is no tax when purchasing a share.</p>
 */
public class PurchaseCalculator implements TransactionCalculator {

  private static final BigDecimal commission_rate = new BigDecimal("0.005");
  private static final BigDecimal zero = BigDecimal.ZERO;

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;

  /**
   * Constructs a purchase calculator for the given share.
   *
   * @param share the share that is involved in the purchase
   * @throws NullPointerException if share is {@code null}
   */
  public PurchaseCalculator(Share share) {
    Objects.requireNonNull(share, "share must not be null");
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  /**
   * Calculates the gross value of the purchase.
   *
   * <p>The gross value is calculated by quantity x purchase price</p>
   *
   * @return the gross value
   */
  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  /**
   * Calculates the commission fee.
   *
   *  <p>The fee is 0.5% of the gross value</p>
   *
   * @return the commission amount
   */
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(commission_rate);
  }
    
  /**
   * Calculates tax of the purchase.
   *
   *  <p>There is no tax for purchasing a share</p>
   *
   * @return {@code zero}
   */
  @Override
  public BigDecimal calculateTax() {
    return zero;
  }

  /**
   * Calculates the total purchase cost.
   *
   * <p>The total is gross value + commission fee + tax</p>
   *
   * @return the total purchase cost
   */
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateCommission()).add(calculateTax());
  }
}
