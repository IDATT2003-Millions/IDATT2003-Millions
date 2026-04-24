package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.core.Share;

public class TransactionFactory {

  private TransactionFactory() {}

  public static Purchase createPurchase(Share share, int week) {
    return new Purchase(share, week);
  }

  public static Sale createSale(Share share, int week) {
    return new Sale(share, week);
  }
}
