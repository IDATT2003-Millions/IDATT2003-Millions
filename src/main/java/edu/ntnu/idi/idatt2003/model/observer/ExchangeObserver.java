package edu.ntnu.idi.idatt2003.model.observer;

import edu.ntnu.idi.idatt2003.model.core.Exchange;

/**
 * Observer interface for receiving notifications when an {@link Exchange} changes state.
 *
 * <p>Register implementations with {@link
 * edu.ntnu.idi.idatt2003.model.observer.Subject#addObserver(ExchangeObserver)} to receive updates
 * after each week advance or balance change.
 */
public interface ExchangeObserver {

  /**
   * Called whenever the exchange has been updated.
   *
   * @param exchange the exchange that was updated
   */
  void onExchangeUpdated(Exchange exchange);
}
