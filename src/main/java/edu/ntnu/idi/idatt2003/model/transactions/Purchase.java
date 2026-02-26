package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.calculators.PurchaseCalculator;

public class Purchase extends Transaction{
    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    @Override
    public void commit(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (isCommitted()) {
            throw new IllegalStateException("This purchase has already been committed,");
        }

        java.math.BigDecimal totalCost = getCalculator().calculateTotal();

        if (player.getMoney().compareTo(totalCost) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        player.withdrawMoney(totalCost);
        player.getPortfolio().addShare(getShare());
        setCommitted();
        player.getTransactionArchive().add(this);
    }
}
