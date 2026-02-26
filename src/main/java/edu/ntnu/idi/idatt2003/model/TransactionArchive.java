package edu.ntnu.idi.idatt2003.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

public class TransactionArchive {
    private List<Transaction> transactions;
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
                .collect(Collectors.toList());
    }

    public List<Purchase> getPurchases(int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("Week must not be 0 or a negative number.");
        }
        return transactions.stream()
                .filter(transaction -> transaction instanceof Purchase && transaction.getWeek() == week)
                .map(transaction -> (Purchase) transaction)
                .collect(Collectors.toList());
    }

    public List<Sale> getSales(int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("Week must not be 0 or a negative number.");
        }
        return transactions.stream()
                .filter(transaction -> transaction instanceof Sale && transaction.getWeek() == week)
                .map(transaction -> (Sale) transaction)
                .collect(Collectors.toList());
    }

    public int countDistinctWeeks() {
    }
}
