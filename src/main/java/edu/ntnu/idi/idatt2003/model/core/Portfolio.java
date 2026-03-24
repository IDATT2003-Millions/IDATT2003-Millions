package edu.ntnu.idi.idatt2003.model.core;

import edu.ntnu.idi.idatt2003.model.calculators.SaleCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *Represents a players portfolio of shares.
 *
 * <p>The portfolio stores all shares currently owned by a player.
 * Shares can be added, removed, and queried by stock symbol.</p>
 */
public class Portfolio {
  private List<Share> shares;

  /**
   * Creates an empty portfolio.
   */
  public Portfolio() {
    this.shares = new ArrayList<>();
  }

  /**
   * Adds a share to portfolio.
   *
   * @param share the share to add
   * @return true if the share was successfully added
   * @throws IllegalArgumentException if the share is {@code empty}
   */
  public boolean addShare(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be empty");
    }

    return shares.add(share);
  }

  /**
   * Removes a share from portfolio.
   *
   * @param share the share to remove
   * @return true if share is removed
   * @throws IllegalArgumentException if share is {@code null}
   */
  public boolean removeShare(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be empty");
    }

    return shares.remove(share);
  }

  public List<Share> getShares() {
    return List.copyOf(shares);
  }

  /**
   *Returns an immutable list of al shares in the portfolio.
   *
   * @param symbol the stock symbol to search for
   * @return a copy of the portfolios share
   * @throws IllegalArgumentException if symbol is {@code null} or {@code blank}
   */
  public List<Share> getShares(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be blank or null");
    }

    return shares.stream()
            .filter(s -> s.getStock().getSymbol().equalsIgnoreCase(symbol))
            .toList();
  }

  /**
   * Checks if the portfolio contains the given share.
   *
   * @param share the share to check
   * @return true if the share exists in the portfolio
   * @throws IllegalArgumentException if share is {@code null}
   */
  public boolean contains(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cant be null");
    }

    return shares.contains(share);
  }

  public BigDecimal getNetWorth() {
    return shares.stream()
            .map(share -> new SaleCalculator(share).calculateTotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
