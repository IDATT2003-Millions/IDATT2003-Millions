package edu.ntnu.idi.idatt2003.model.calculators;

import edu.ntnu.idi.idatt2003.model.Share;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleCalculator implements TransactionCalculator{

    private static final BigDecimal tax = new BigDecimal("0.3");
    private static final BigDecimal commission = new BigDecimal("0.01");
    private static final BigDecimal zero = BigDecimal.ZERO;

    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;
    private final BigDecimal salesPrice;

    public SaleCalculator(Share share) {
        Objects.requireNonNull(share);
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
        this.salesPrice = share.getStock().getSalesPrice();
    }

    @Override
    public BigDecimal calculateGross() {
        return salesPrice.multiply(quantity);
    }

    @Override
    public BigDecimal calculateCommission() {
        return calculateGross().multiply(commission);
    }

    @Override
    public BigDecimal calculateTax() {
        BigDecimal profit =calculateGross()
                .subtract(purchasePrice.multiply(quantity))
                .subtract(calculateCommission());

        if (profit.signum() <= 0) {
            return zero;
        } else {return profit.multiply(tax);
        }

    }

    @Override
    public BigDecimal calculateTotal() {
        return calculateGross()
                .add(calculateCommission())
                .add(calculateTax());
    }
}
