package edu.ntnu.idi.idatt2003.model.observer;

/**
 * Observable subject in the Observer pattern.
 *
 * <p>Implementors maintain a list of {@link ExchangeObserver}s and notify them whenever relevant
 * state changes occur.
 */
public interface Subject {

  /**
   * Registers an observer to receive future state-change notifications.
   *
   * @param observer the observer to add
   */
  void addObserver(ExchangeObserver observer);

  /**
   * Removes a previously registered observer.
   *
   * @param observer the observer to remove
   */
  void removeObserver(ExchangeObserver observer);
}
