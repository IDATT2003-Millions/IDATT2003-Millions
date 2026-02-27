package edu.ntnu.idi.idatt2003.model.core;

import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 *Stores and manages a players completed transactions.
 *
 * <p>The archive keeps track of all committed transactions
 * and allows filtering by week and transaction type.</p>
 */
public class TransactionArchive {
  private final List<Transaction> transactions;

  /**
   * Creates an empty transaction archive.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a transaction to the archive.
   *
   * @param transaction the transaction to add
   * @return true if the transaction was successfully added
   * @throws IllegalArgumentException if transaction is {@code null}
   */
  public boolean add(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction cant be null");
    }
    return transactions.add(transaction);
  }

  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  /**
   * Returns all transactions performed in the specific week.
   *
   * @param week the week number
   * @return a list of transactions from that week
   * @throws IllegalArgumentException if week is {@code null} or {@code negative}
   */
  public List<Transaction> getTransactions(int week) {
    if (week <= 0) {
      throw new IllegalArgumentException("Week must not be 0 or a negative number");
    }

    return transactions.stream()
            .filter(transaction -> transaction.getWeek() == week)
            .toList();
  }

  /**
   * Returns all purchase transactions from the specified week.
   *
   * @param week the week number
   * @return a list of purchases from that week
   * @throws IllegalArgumentException if week is less than or is {@code zero}
   */
  public List<Purchase> getPurchases(int week) {
    if (week <= 0) {
      throw new IllegalArgumentException("Week must not be 0 or a negative number.");
    }

    return transactions.stream()
          .filter(transaction -> transaction instanceof Purchase && transaction.getWeek() == week)
          .map(transaction -> (Purchase) transaction)
          .toList();
  }

  /**
   * Returns all sale transactions performed in the specified week.
   *
   * @param week the week number
   * @return a list of sales from that week
   * @throws IllegalArgumentException if week is less or equal to {@code zero}
   */
  public List<Sale> getSales(int week) {
    if (week <= 0) {
      throw new IllegalArgumentException("Week must not be 0 or a negative number.");
    }

    return transactions.stream()
                .filter(transaction -> transaction instanceof Sale && transaction.getWeek() == week)
                .map(transaction -> (Sale) transaction)
                .toList();
  }

  /**
   * Returns the number of distinct weeks represented
   * in the transaction archive.
   *
   * @return the number of unique transaction weeks
   */
  public int countDistinctWeeks() {
    return (int) transactions.stream()
                .map(Transaction::getWeek)
                .distinct()
                .count();
  }
}
