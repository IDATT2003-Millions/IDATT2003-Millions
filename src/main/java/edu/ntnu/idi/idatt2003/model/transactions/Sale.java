package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.Share;
import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;

public class Sale extends Transaction {
    public Sale(Share share, int week) {
        super(share, week, new SaleCalculator(share));
    }

    @Override
    public void commit(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        if (isCommitted()) {
            throw new IllegalArgumentException("This sale has already been committed.");
        }
        if (!player.getPortfolio().contains(getShare())) {
            throw new IllegalArgumentException("Player does not own the share attempted to be sold");
        }

        java.math.BigDecimal totalProceeds = getCalculator().calculateTotal();

        player.addMoney(totalProceeds);
        player.getPortfolio().removeShare(getShare());
        setCommitted();
        player.getTransactionArchive().add(this);
    }
}
