package edu.ntnu.idi.idatt2003.model.observer;

public interface Subject {
  void addObserver(ExchangeObserver observer);
  void removeObserver(ExchangeObserver observer);
}