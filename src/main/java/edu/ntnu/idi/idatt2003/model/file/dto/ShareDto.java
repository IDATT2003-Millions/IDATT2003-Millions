package edu.ntnu.idi.idatt2003.model.file.dto;

/**
 * Data-transfer object for a portfolio share. Used exclusively for JSON
 * serialization/deserialization.
 */
public class ShareDto {
  public String symbol;
  public String company;
  public String quantity;
  public String purchasePrice;
}
