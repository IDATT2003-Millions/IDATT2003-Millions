package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages pending and completed limit orders for a single player.
 *
 * <p>Call {@link #executeOrders(Exchange, Player)} after each week advance to trigger orders whose
 * price conditions are now satisfied.
 */
public class OrderBook {

  private final List<LimitOrder> pendingOrders = new ArrayList<>();
  private final List<LimitOrder> completedOrders = new ArrayList<>();

  /**
   * Adds a limit order to the pending queue.
   *
   * @param order the order to place
   */
  public void placeOrder(LimitOrder order) {
    pendingOrders.add(order);
  }

  /**
   * Checks every pending order against current market prices and executes those whose conditions
   * are met.
   *
   * <p>BUY — executes when currentPrice ≤ targetPrice (and player has sufficient funds).<br>
   * SELL — executes when currentPrice ≥ targetPrice (and the share is still in the portfolio). If
   * the share has already been sold, the order is auto-cancelled.
   */
  public void executeOrders(Exchange exchange, Player player) {
    int week = exchange.getWeek();
    List<LimitOrder> snapshot = List.copyOf(pendingOrders);

    for (LimitOrder order : snapshot) {
      if (!exchange.hasStock(order.getSymbol())) {
        resolve(order, false, week);
        continue;
      }

      BigDecimal currentPrice = exchange.getStock(order.getSymbol()).getSalesPrice();

      if (order.getType() == OrderType.BUY) {
        if (currentPrice.compareTo(order.getTargetPrice()) <= 0) {
          try {
            exchange.buy(order.getSymbol(), order.getQuantity(), player).commit(player);
            resolve(order, true, week);
          } catch (IllegalArgumentException e) {
            resolve(order, false, week);
          }
        }
      } else {
        BigDecimal owned =
            player.getPortfolio().getShares(order.getSymbol()).stream()
                .map(s -> s.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (owned.compareTo(BigDecimal.ZERO) <= 0) {
          resolve(order, false, week);
        } else if (currentPrice.compareTo(order.getTargetPrice()) >= 0) {
          exchange.sellQuantity(order.getSymbol(), order.getQuantity().min(owned), player);
          resolve(order, true, week);
        }
      }
    }
  }

  private void resolve(LimitOrder order, boolean executed, int week) {
    if (executed) {
      order.markExecuted(week);
    } else {
      order.markCancelled(week);
    }
    pendingOrders.remove(order);
    completedOrders.add(order);
  }

  /**
   * Cancels a pending order and moves it to the completed list.
   *
   * <p>If the order is not in the pending queue this method does nothing.
   *
   * @param order the order to cancel
   */
  public void cancelOrder(LimitOrder order) {
    if (pendingOrders.remove(order)) {
      order.markCancelled(0);
      completedOrders.add(order);
    }
  }

  public List<LimitOrder> getPendingOrders() {
    return Collections.unmodifiableList(pendingOrders);
  }

  public List<LimitOrder> getCompletedOrders() {
    return Collections.unmodifiableList(completedOrders);
  }
}
