package edu.ntnu.idi.idatt2003.model.file.dto;

import java.util.List;

/**
 * Root data-transfer object that represents a complete saved game state.
 * Used exclusively for JSON serialization/deserialization.
 */
public class GameStateDto {
  public String playerName;
  public String startingMoney;
  public String currentMoney;
  public List<ShareDto> portfolio;
  public List<TransactionDto> transactions;
  public String exchangeName;
  public int week;
  public List<StockDto> stocks;
}
