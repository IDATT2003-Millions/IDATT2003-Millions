package edu.ntnu.idi.idatt2003.model.transactions;

/**
 * Represents the direction of a limit order.
 *
 * <ul>
 *   <li>{@link #BUY} – an order to purchase shares when the price falls to the target
 *   <li>{@link #SELL} – an order to sell shares when the price rises to the target
 * </ul>
 */
public enum OrderType {
  BUY,
  SELL
}
