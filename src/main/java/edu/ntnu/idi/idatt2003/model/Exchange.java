package edu.ntnu.idi.idatt2003.model;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Exchange {

    private final String name;
    private int week;
    private final Map<String,Stock> stockMap;
    private final Random random;

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

    public boolean hasStock(String symbol) {
        Objects.requireNonNull(symbol, "symbol must not be null");
        return stockMap.containsKey(symbol);
    }

    public Stock getStock(String symbol) {
    Objects.requireNonNull(symbol, "symbol must not be null");
    Stock s = stockMap.get(symbol);
    if (s == null) {
        throw new NoSuchElementException("Stock " + symbol + " does not exist");
    }
    return s;
    }

    public List<Stock> findStocks(String searchTerm) {
        Objects.requireNonNull(searchTerm, "searchTerm cannot be null");
        String term = searchTerm.toLowerCase(Locale.ROOT);

        return stockMap.values().stream()
                .filter(s -> s.getSymbol().toLowerCase(Locale.ROOT).contains(term)
                        || s.getCompany().toLowerCase(Locale.ROOT).contains(term))
                .collect(Collectors.toList());
    }

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

    public Transaction sell(Share share, Player player) {
        Objects.requireNonNull(share);
        Objects.requireNonNull(player);

        if (!hasStock(share.getStock().getSymbol())) {
            throw new IllegalArgumentException("Stock not listed");
        }

        return new Sale(share, week);
    }

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
}
