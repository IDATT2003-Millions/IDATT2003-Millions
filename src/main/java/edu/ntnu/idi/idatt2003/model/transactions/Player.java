package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.core.Portfolio;
import edu.ntnu.idi.idatt2003.model.core.TransactionArchive;

import java.math.BigDecimal;

/**
 * Represents a player in the game Millions.
 *
 * <p>A player has a name, starting money, and current balance.
 * the starting money is the same and remains the same, the balance will change as the game
 * proceeds and performs transactions.
 * </p>
 *
 * <p>A player has a portfolio containing their shares and a transaction archive storing
 * transactions by the player</p>
 */
public class Player {
  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;

  /**
   * Creates a new player with a name and starting capital.
   *
   * @param name the players name
   * @param startingMoney the initial mount of money
   * @throws NullPointerException if arguments is {@code null}
   * @throws IllegalArgumentException if startingMoney is {@code negative}
   */
  public Player(String name, BigDecimal startingMoney) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Player name cannot be null or blank.");
    }
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Starting money cannot be null and must be 0 or greater");
    }

    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  /**
   * Adds money to the players balance.
   *
   * @param amount the amount to add
   * @throws IllegalArgumentException if added money is {@code null} or {@code negative}
   */
  public void addMoney(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Addable money must be positive number.");
    }
    this.money = this.money.add(amount);
  }

  /**
   * Withdraws money from the players balance.
   *
   * @param amount the amount to withdraw
   * @throws IllegalArgumentException if amount is {@code null} or {@code negative}
   */
  public void withdrawMoney(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Withdrawable money must be a positive number.");
    }
    if (this.money.compareTo(amount) < 0) {
      throw new IllegalArgumentException("Insufficient funds: cannot withdraw "
              + amount + ". Balance is currently" + money + ".");
    }
    this.money = this.money.subtract(amount);
  }

  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  public BigDecimal getNetWorth() {
    return money.add(portfolio.getNetWorth());
  }

  public PlayerStatus getStatus() {
    int weeksTraded = transactionArchive.countDistinctWeeks();
    BigDecimal netWorth = getNetWorth();
    BigDecimal doubleGrowth = startingMoney.multiply(new BigDecimal("2"));
    BigDecimal twentyPercentGrowth = startingMoney.multiply(new BigDecimal("1.2"));

    if (weeksTraded >= 20 && netWorth.compareTo(doubleGrowth) >= 0) {
      return PlayerStatus.SPECULATOR;
    }
    else if (weeksTraded >= 10 && netWorth.compareTo(twentyPercentGrowth) >= 0) {
      return PlayerStatus.INVESTOR;
    }
    else {
      return PlayerStatus.NOVICE;
    }
  }
}
