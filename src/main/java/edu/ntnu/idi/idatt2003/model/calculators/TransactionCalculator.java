package edu.ntnu.idi.idatt2003.model.calculators;

import java.math.BigDecimal;

/**
 * Defines the methodes to calculate values related to transactions.
 * Implements calculate gross value, tax, commision and total amount
 depending on the type of transaction.
 * 
 */
public interface TransactionCalculator {

  /**
   * Calculates the gross value of the transaction before fees and taxes.
   *
   * @return the gross value as {@code BigDecimal}
   */
  BigDecimal calculateGross();
    
  /**
   * Calculates the commission fee associated with the transaction.
   *
   * @return the commission as {@code BigDecimal}
   */
  BigDecimal calculateCommission();

  /**
   * Calculates the tax associated with the transaction.
   *
   * @return the tax as {@code BigDecimal}
   */
  BigDecimal calculateTax();

  /**
   * Calculates the total value of the transaction after fees and taxes.
   *
   * @return the total as {@code BigDeimal}
   */
  BigDecimal calculateTotal();
    
} 