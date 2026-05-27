package edu.ntnu.idi.idatt2003.model.file.dto;

/**
 * Data-transfer object for a historical transaction. Used exclusively for JSON
 * serialization/deserialization.
 *
 * <p>{@code salePrice} is {@code null} for BUY transactions.
 */
public class TransactionDto {
  public String type; // "BUY" or "SELL"
  public String symbol;
  public String company;
  public String quantity;
  public String purchasePrice; // original purchase price per share
  public String salePrice; // price per share at time of sale (null for buys)
  public int week;
}
