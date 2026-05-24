package edu.ntnu.idi.idatt2003.model.calculators;

import edu.ntnu.idi.idatt2003.model.core.Share;
import java.math.BigDecimal;
import java.util.Objects;


/**
 *Calculates financial values for sale transactions.
 *
 *<p>Commission rate for a sale is 1% off the gross value.
 * Tax for a sale is at 30% on the profit that you get after gross value -
 * commission - sales price</p>
 */
public class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal tax = new BigDecimal("0.3");
  private static final BigDecimal commission = new BigDecimal("0.01");
  private static final BigDecimal zero = BigDecimal.ZERO;

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;
  private final BigDecimal salesPrice;

  /**
   *Constructs a sales calculator for the sale.
   *
   * @param share the share that is involved in the sale
   * @throws NullPointerException if share is {@code null}
   */
  public SaleCalculator(Share share) {
    Objects.requireNonNull(share);
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
    this.salesPrice = share.getStock().getSalesPrice();
  }

  /**
   *Calculates the gross value of the sale.
   *
   * <p>The gross value is calculated by salesPrice*quantity</p>
   *
   * @return the gross value
   */
  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity);
  }

  /**
   *Calculates the commission fee.
   *
   * <p>The fee is 1% of the gross value</p>
   *
   * @return the commission amount
   */
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(commission);
  }

  /**
   *Calculates tax of the sale.
   *
   * <p>The tax is calculated by gross value - commission - purchase price</p>
   *
   * <p>If the profit is less than zero the tax is zero.</p>
   *
   * @return the tax amount
   */
  @Override
  public BigDecimal calculateTax() {
    BigDecimal profit = calculateGross()
              .subtract(purchasePrice.multiply(quantity))
              .subtract(calculateCommission());

    if (profit.signum() <= 0) {
      return zero;
    } else {
      return profit.multiply(tax);
    }

  }

  /**
   *Calculates the net proceeds from the sale.
   *
   * <p>The proceeds are the gross value minus commission fee and tax.</p>
   *
   * @return the net amount the player receives after costs
   */
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
            .subtract(calculateCommission())
            .subtract(calculateTax());
  }
}
