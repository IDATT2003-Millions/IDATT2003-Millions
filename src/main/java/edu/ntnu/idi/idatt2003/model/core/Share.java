package edu.ntnu.idi.idatt2003.model.core;

import java.math.BigDecimal;


/**
 *Represents a purchase of a specific stock.
 *
 * <p>A share contains information about which stock, the quantity and the price of the purchase</p>
 *
 * <p>A {@code Share} instance is immutable after creation.
 *  * All values must be positive, and the stock reference cannot be null.<\p>
 */

public class Share {
  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * Crates a new share representing a stock purchase.
   *
   * @param stock is the stock that was purchased, cannot be {@code null}
   * @param quantity the number of stocks that was purchased, cannot be {@code negative}
   * @param purchasePrice the price of the purchase, must be {@code positive}
   * @throws IllegalArgumentException if stock is {@code null}, or if quantity or
  purchase is {@code null} or {@code positive}
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {

    if (stock == null) {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Purchase price must be positive");
    }
    this.stock = stock;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  public Stock getStock() {
    return stock;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }
}
