package edu.ntnu.idi.idatt2003.model.file.dto;

import java.util.List;

/**
 * Data-transfer object for a stock and its full price history.
 * Used exclusively for JSON serialization/deserialization.
 */
public class StockDto {
  public String symbol;
  public String company;
  public List<String> prices; // BigDecimal stored as plain strings to avoid precision loss
}
