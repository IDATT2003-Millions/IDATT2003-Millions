package edu.ntnu.idi.idatt2003.model.calculators;

import edu.ntnu.idi.idatt2003.model.Share;

import java.math.BigDecimal;

import java.util.Objects;

public class PurchaseCalculator implements TransactionCalculator {

    private static final BigDecimal commission_rate = new BigDecimal("0.005");
    private static final BigDecimal zero = BigDecimal.ZERO;

    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;

    public PurchaseCalculator(Share share) {
        Objects.requireNonNull(share, "share must not be null");
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
    }

    public BigDecimal calculateGross() {
        return purchasePrice.multiply(quantity);
    }

    public BigDecimal calculateCommission() {
        return calculateGross().multiply(commission_rate);
    }
    
    public BigDecimal calculateTax() {
        return zero;
    }

    public BigDecimal calculateTotal() {
        return calculateGross().add(calculateCommission()).add(calculateTax());
    }
}
