package edu.ntnu.idi.idatt2003.model.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *Represents a stock with a unique symbol, company name,
 * and a history of recorded sales prices.
 *
 *<p>A stock must always have a symbol, a company name, and at least one
 *initial sales price. All prices must be positive values.
 *
 *<p>The class stores all historical sales prices in the order they were added.
 * The most recent price represents the current sales price.
 */
public class Stock {
  private final String symbol;
  private final String company;
  private final List<BigDecimal> prices;

  /**
   * Crates a new stock when given a symbol, company name and initial sales price.
   *
   * @param symbol the unique symbol of a stock
   * @param company the name of the company
   * @param salesPrice the sales price of a stock, must be {@code positive}
   * @throws IllegalArgumentException if symbol or company is {@code null/blank},
   or if salesPrice is {@code null} or {@code not positive}
   */
  public Stock(String symbol, String company, BigDecimal salesPrice) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be null or blank");
    }
    if (company == null || company.isBlank()) {
      throw new IllegalArgumentException("Company cannot be null or blank");
    }
    if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Sales price must be positive");
    }

    this.symbol = symbol;
    this.company = company;
    this.prices = new ArrayList<>();
    this.prices.add(salesPrice);
  }

  public String getSymbol() {
    return symbol;
  }

  public String getCompany() {
    return company;
  }

  public BigDecimal getSalesPrice() {
    return prices.getLast();
  }

  /**
   *Add new sales price to the stocks price history.
   *
   * @param price the new price sales price to add, must be {@code positive}
   * @throws IllegalArgumentException if price is {@code null} or {@code positive}
   */
  public void addNewSalesPrice(BigDecimal price) {
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    prices.add(price);
  }

  public List<BigDecimal> getHistoricalPrices() {
    return List.copyOf(prices);
  }

  public BigDecimal getHighestPrice() {
    return Collections.max(prices);
  }

  public BigDecimal getLowestPrice() {
    return Collections.min(prices);
  }

  /**
   * Gets and calculates the latest change in price.
   *
   * <p>If there ar no or one registered price the change will
   * just return zero</p>
   *
   * @return the difference in the two last prices
   */
  public BigDecimal getLatestPriceChange() {

    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }
    return getSalesPrice().subtract(prices.get(prices.size() - 2));
  }
}
