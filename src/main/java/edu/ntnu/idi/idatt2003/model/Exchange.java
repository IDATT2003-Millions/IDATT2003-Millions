package edu.ntnu.idi.idatt2003.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Exchange {

    private final String name;
    private final int week;
    private final Map<String,Stock> stockMap;
    private final Random random;

    public Exchange(String name, List<Stock> stocks) {

        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(stocks, "stocks must not be null");

    }

    public String getName() {
        return name;
    }

    public int getWeek() {
        return week;
    }

    public boolean hasStock(String symbol) {

    }

    public Stock getStock(String symbol) {

    }

    public List<Stock> findStocks(String searchTerm) {

    }

    public Transaciton buy(String symbol, BigDecimal quantity, Player player) {

    }

    public Transaction sell(Share share, Player player) {

    }

    public void advance() {

    }
}
