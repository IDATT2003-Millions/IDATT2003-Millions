package edu.ntnu.idi.idatt2003.model.observer;

import edu.ntnu.idi.idatt2003.model.core.Exchange;

public interface ExchangeObserver {
  void onExchangeUpdated(Exchange exchange);
}
