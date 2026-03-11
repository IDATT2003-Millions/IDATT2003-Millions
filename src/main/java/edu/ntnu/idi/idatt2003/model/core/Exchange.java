package edu.ntnu.idi.idatt2003.model.core;

import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a stock exchange that tracks listed stocks and the current game week.
 *
 * <p>The exchange can look up and search stocks, create buy and sell transactions, and advance
 * to the next week by applying random price changes to all listed stocks.
 */
public class Exchange {

    private final String name;
    private int week;
    private final Map<String,Stock> stockMap;
    private final Random random;

    /**
     * Creates a new exchange with a name and an initial list of stocks.
     *
     * @param name the exchange name
     * @param stocks the stocks listed on the exchange
     * @throws NullPointerException if {@code name} or {@code stocks} is {@code null}
     */
    public Exchange(String name, List<Stock> stocks) {

        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(stocks, "stocks must not be null");

        this.name = name;
        this.week = 1;
        this.random = new Random();
        this.stockMap = new HashMap<>();

        for (Stock stock : stocks) {
            stockMap.put(stock.getSymbol(), stock);
        }
    }

   
    public String getName() {
        return name;
    }

    
    public int getWeek() {
        return week;
    }

    /**
     * Returns whether a stock symbol is listed on this exchange.
     *
     * @param symbol the stock symbol to check
     * @return {@code true} if the symbol is listed, otherwise {@code false}
     * @throws NullPointerException if {@code symbol} is {@code null}
     */
    public boolean hasStock(String symbol) {
        Objects.requireNonNull(symbol, "symbol must not be null");
        return stockMap.containsKey(symbol);
    }

    /**
     * Returns the listed stock for the given symbol.
     *
     * @param symbol the stock symbol
     * @return the matching stock
     * @throws NullPointerException if {@code symbol} is {@code null}
     * @throws NoSuchElementException if no stock with the symbol is listed
     */
    public Stock getStock(String symbol) {
    Objects.requireNonNull(symbol, "symbol must not be null");
    Stock s = stockMap.get(symbol);
    if (s == null) {
        throw new NoSuchElementException("Stock " + symbol + " does not exist");
    }
    return s;
    }

    /**
     * Finds stocks where symbol or company name contains the search term.
     *
     * <p>The match is case-insensitive.
     *
     * @param searchTerm the term to search for
     * @return a list of matching stocks
     * @throws NullPointerException if {@code searchTerm} is {@code null}
     */
    public List<Stock> findStocks(String searchTerm) {
        Objects.requireNonNull(searchTerm, "searchTerm cannot be null");
        String term = searchTerm.toLowerCase(Locale.ROOT);

        return stockMap.values().stream()
                .filter(s -> s.getSymbol().toLowerCase(Locale.ROOT).contains(term)
                        || s.getCompany().toLowerCase(Locale.ROOT).contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Creates a purchase transaction for a listed stock.
     *
     * <p>The transaction is created for the current week and is not committed by this method.
     *
     * @param symbol the symbol of the stock to buy
     * @param quantity the number of shares to buy
     * @param player the player creating the purchase
     * @return a new purchase transaction
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code quantity} is zero or negative
     * @throws NoSuchElementException if the symbol is not listed
     */
    public Transaction buy(String symbol, BigDecimal quantity, Player player) {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(player, "player cannot be null");

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        Stock stock = getStock(symbol);
        BigDecimal purchasePrice = stock.getSalesPrice();
        Share share = new Share(stock, quantity, purchasePrice);
        return new Purchase(share, week);
    }

    /**
     * Creates a sale transaction for a share.
     *
     * <p>The stock in the share must be listed on this exchange. The transaction is created for
     * the current week and is not committed by this method.
     *
     * @param share the share to sell
     * @param player the player creating the sale
     * @return a new sale transaction
     * @throws NullPointerException if {@code share} or {@code player} is {@code null}
     * @throws IllegalArgumentException if the share's stock is not listed
     */
    public Transaction sell(Share share, Player player) {
        Objects.requireNonNull(share);
        Objects.requireNonNull(player);

        if (!hasStock(share.getStock().getSymbol())) {
            throw new IllegalArgumentException("Stock not listed");
        }

        return new Sale(share, week);
    }

    /**
     * Advances the exchange to the next week and updates all stock prices.
     *
     * <p>Each stock receives a random price change in the range -5% to +5%, rounded to two
     * decimals, with a minimum price of 0.01.
     */
    public void advance() {
        week++;

        for (Stock stock : stockMap.values()) {
            BigDecimal current = stock.getSalesPrice();


            double changePercent = (random.nextDouble() * 0.10) - 0.05;
            BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(changePercent));

            BigDecimal newPrice = current.multiply(factor);


            if (newPrice.compareTo(new BigDecimal("0.01")) < 0) {
                newPrice = new BigDecimal("0.01");
            }


            newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);

            stock.addNewSalesPrice(newPrice);
        }
    }

  public List<Stock> getGainers(int limit) {
      if (limit < 1) {
          return List.of();
      }
    return stockMap.values().stream()
            .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Stock::getLatestPriceChange).reversed())
      .limit(limit)

      .toList();
  }

  public List<Stock> getLosers(int limit) {
    if  (limit < 1) {
        return List.of();
    }
     return stockMap.values().stream()
             .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) < 0)
             .sorted(Comparator.comparing(Stock::getLatestPriceChange))
             .limit(limit)
             .toList();
  }
}

