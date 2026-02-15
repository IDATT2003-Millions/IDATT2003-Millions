package edu.ntnu.idi.idatt2003.Modell;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Portfolio {
    private final List<Share> shares;

    public Portfolio() {
        this.shares = new ArrayList<>();
    }

    public boolean addShare(Share share) {
        //Adds a share to portfolio
        return shares.add(share);
    }

    public boolean removeShares(Share share) {
        //Removes a specific share from the portfolio
        return shares.remove(share);
    }

    public List<Share> getShares() {
        //Returns all shares in the portfolio
        return new ArrayList<>(shares);
    }

    public List<Share> getShares(String symbol) {
        //Returns a list of shares filtered by the stock symbol
        return shares.stream()
                .filter(share -> share.getStock().getSymbol().equalsIgnoreCase(symbol))
                .collect(Collectors.toList());
    }

    public boolean contains(Share share) {
        //Checks if a specific share exists in the portfolio
        return shares.contains(share);
    }

}
