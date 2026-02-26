package edu.ntnu.idi.idatt2003.model;

import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;


public class TransactionArchive {
    private final List<Transaction> transactions;
    public TransactionArchive() {
        this.transactions = new ArrayList<>();
    }

    public boolean add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cant be null");
        }
        return transactions.add(transaction);
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public List<Transaction> getTransactions(int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("Week must not be 0 or a negative number");
        }
        return transactions.stream()
                .filter(transaction -> transaction.getWeek() == week)
                .toList();
    }

    public List<Purchase> getPurchases(int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("Week must not be 0 or a negative number.");
        }
        return transactions.stream()
                .filter(transaction -> transaction instanceof Purchase && transaction.getWeek() == week)
                .map(transaction -> (Purchase) transaction)
                .toList();
    }

    public List<Sale> getSales(int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("Week must not be 0 or a negative number.");
        }
        return transactions.stream()
                .filter(transaction -> transaction instanceof Sale && transaction.getWeek() == week)
                .map(transaction -> (Sale) transaction)
                .toList();
    }

    public int countDistinctWeeks() {
        return (int) transactions.stream()
                .map(Transaction::getWeek)
                .distinct()
                .count();
    }
}
