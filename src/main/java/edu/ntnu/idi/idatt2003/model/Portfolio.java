package edu.ntnu.idi.idatt2003.model;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Portfolio {
    private List<Share> shares;

    public Portfolio() {
        this.shares = new ArrayList<>();
    }

    public boolean addShare(Share share) {
        if (share == null) {
            throw new IllegalArgumentException("Share cannot be empty");
        }
        return shares.add(share);
    }

    public boolean removeShare(Share share) {
        if (share == null) {
            throw new IllegalArgumentException("Share cannot be empty");
        }
        return shares.remove(share);
    }

    public List<Share> getShares() {
        return List.copyOf(shares);
    }

    public List<Share> getShares(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank or null");
        }
        return shares.stream()
                .filter(s -> s.getStock().getSymbol().equalsIgnoreCase(symbol))
                .collect(Collectors.toList());
    }

    public boolean contains(Share share){
        if (share == null) {
            throw new IllegalArgumentException("Share cant be null");
        }
        return shares.contains(share);
    }
}
