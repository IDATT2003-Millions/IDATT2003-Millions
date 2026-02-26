package edu.ntnu.idi.idatt2003.model;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;

import javax.sql.rowset.spi.TransactionalWriter;
import java.math.BigDecimal;

public class Player {
    private String name;
    private BigDecimal startingMoney;
    private BigDecimal money;
    private Portfolio portfolio;
    private TransactionArchive transactionArchive;

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

    public void addMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Addable money must be positive number.");
        }
        this.money = this.money.add(amount);
    }

    public void withdrawMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawable money must be a positive number.");
        }
        if (this.money.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds: cannot withdraw " + amount + ". Balance is currently" + money + ".");
        }
        this.money = this.money.subtract(amount);
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public TransactionArchive getTransactionArchive() {
        return transactionArchive;
    }
}
