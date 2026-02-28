package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.calculators.TransactionCalculator;
import edu.ntnu.idi.idatt2003.model.core.Share;

/**
 * Represents a financial transaction in the stock market.
 *
 * <p>A transaction contains a share, the week it was created
 * and a {@link TransactionCalculator} used to calculate financial values.
 * A transaction can only be committed once.
 * </p>
 *
 * <p>Subclasses such as {@code Purchase} and {@code Sale}
 * define how the transaction is completed through the
 * {@link #commit(Player)} method</p>
 */
public abstract class Transaction {
  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;
  protected boolean committed;

  /**
   * Creates a new transaction.
   *
   * @param share the share involved in the transaction
   * @param week the week the transaction is created
   * @param calculator the calculator used to compute financial values
   * @throws IllegalArgumentException if share or calculator is {@code null} or week is zero or less
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    if (share == null) {
      throw new IllegalArgumentException("Share cant be null.");
    }

    if (week <= 0) {
      throw new IllegalArgumentException("Week cannot be 0 or a negative number.");
    }

    if (calculator == null) {
      throw new IllegalArgumentException("Calculator cannot apply math to Null.");
    }

    this.share = share;
    this.week = week;
    this.calculator = calculator;
    this.committed = false;
  }

  public Share getShare() {
    return share;
  }

  public int getWeek() {
    return week;
  }

  public TransactionCalculator getCalculator() {
    return calculator;
  }

  public boolean isCommitted() {
    return committed;
  }

  /**
   * Marks the transaction as committed.
   *
   * <p>This method is intended to be called by subclasses
   * after a successful execution of {@link #commit(Player)}.</p>
   */
  protected void setCommitted() {
    this.committed = true;
  }

  /**
   * Executes the transaction for the player.
   *
   * <p>The specific implementation is defined by subclasses
   *  * such as {@code Purchase} and {@code Sale}.</p>
   *
   * @param player the player performing the transaction
   */
  public abstract void commit(Player player);
}

