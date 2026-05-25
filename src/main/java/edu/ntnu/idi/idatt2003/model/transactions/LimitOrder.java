package edu.ntnu.idi.idatt2003.model.transactions;

import edu.ntnu.idi.idatt2003.model.core.Share;
import java.math.BigDecimal;

/**
 * Represents a limit order to buy or sell a stock at a target price.
 *
 * <p>A BUY order executes when the market price drops to or below the target price.
 * A SELL order executes when the market price rises to or above the target price.</p>
 *
 * <p>SELL orders store a reference to the specific portfolio Share; if that share is
 * sold manually before the order triggers, the order is auto-cancelled on the next
 * week advance.</p>
 */
public class LimitOrder {

  private final OrderType type;
  private final String symbol;
  private final String company;
  private final BigDecimal quantity;
  private final BigDecimal targetPrice;
  private final Share share;
  private final int weekPlaced;
  private boolean executed;
  private boolean cancelled;
  private int weekResolved;

  private LimitOrder(OrderType type, String symbol, String company,
                     BigDecimal quantity, BigDecimal targetPrice,
                     Share share, int weekPlaced) {
    this.type = type;
    this.symbol = symbol;
    this.company = company;
    this.quantity = quantity;
    this.targetPrice = targetPrice;
    this.share = share;
    this.weekPlaced = weekPlaced;
  }

  public static LimitOrder buy(String symbol, String company,
                               BigDecimal quantity, BigDecimal targetPrice,
                               int weekPlaced) {
    return new LimitOrder(OrderType.BUY, symbol, company, quantity, targetPrice, null, weekPlaced);
  }

  public static LimitOrder sell(String symbol, String company,
                                BigDecimal quantity, BigDecimal targetPrice,
                                int weekPlaced) {
    return new LimitOrder(OrderType.SELL, symbol, company, quantity, targetPrice, null, weekPlaced);
  }

  public OrderType getType()         { return type; }
  public String getSymbol()          { return symbol; }
  public String getCompany()         { return company; }
  public BigDecimal getQuantity()    { return quantity; }
  public BigDecimal getTargetPrice() { return targetPrice; }
  public Share getShare()            { return share; }
  public int getWeekPlaced()         { return weekPlaced; }
  public boolean isExecuted()        { return executed; }
  public boolean isCancelled()       { return cancelled; }
  public int getWeekResolved()       { return weekResolved; }

  void markExecuted(int week)  { this.executed  = true; this.weekResolved = week; }
  void markCancelled(int week) { this.cancelled = true; this.weekResolved = week; }
}