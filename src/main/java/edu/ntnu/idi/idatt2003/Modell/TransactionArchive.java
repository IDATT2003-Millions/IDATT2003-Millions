package edu.ntnu.idi.idatt2003.Modell;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionArchive {
    private final List<Transaction> transactions;

    public TransactionArchive() {
        this.transactions = new ArrayList<>();
    }

    public boolean add(Transaction transaction) {
        return transaction.add(transaction);
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public List<Transaction> getTransactions(int week) {
        //Filters the list for all transactions in a specific week
        return transactions.stream()
                .filter(transaction -> transaction.getWeek() == week)
                .collect(Collectors.toList());
    }

    public List<Purchase> getPurchase(int week) {
        //Filters for only purchase transactions in a specific week
        return transactions.stream()
                .filter(transaction -> transaction instanceof Purchase && transaction.getWeek() == week)
                .map(transaction -> (Purchase) transaction)
                .collect(Collectors.toList());
    }

    public List<Sale> getSale(int week) {
        //Filters for only sale transactions in a specific week
        return transactions.stream()
                .filter(transaction -> transaction instanceof Sale && transaction.getWeek() == week)
                .map(transaction -> (Sale) transaction)
                .collect(Collectors.toList());
    }

    public int countDistinctWeeks() {
        //counts the amount of weeks that have had trading activity
        return (int) transactions.stream()
                .map(Transaction::getweek)
                .distinct()
                .count();
    }
}
