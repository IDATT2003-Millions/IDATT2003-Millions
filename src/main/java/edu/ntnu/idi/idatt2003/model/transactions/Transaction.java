package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.calculators.TransactionCalculator;

public abstract class Transaction {
    private final Share share;
    private final int week;
    private final TransactionCalculator calculator;
    private boolean committed;

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

    protected void setCommitted() {
        this.committed = true;
    }

    public abstract void commit(Player player);
}

