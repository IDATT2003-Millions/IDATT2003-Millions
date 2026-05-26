package edu.ntnu.idi.idatt2003.model.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.file.dto.GameStateDto;
import edu.ntnu.idi.idatt2003.model.file.dto.ShareDto;
import edu.ntnu.idi.idatt2003.model.file.dto.StockDto;
import edu.ntnu.idi.idatt2003.model.file.dto.TransactionDto;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads the full game state to and from a JSON file.
 *
 * <p>Monetary values are stored as plain strings to avoid floating-point precision loss. Price
 * history for each stock is preserved so that charts and statistics remain accurate after a load.
 *
 * <p>Transaction history is reconstructed with snapshot stocks whose price equals the price at the
 * time of the original transaction, so the Transactions view displays correct historical values.
 */
public class GameStateSerializer {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Holds the result of loading a saved game.
   *
   * @param player the restored player
   * @param exchange the restored exchange
   */
  public record LoadedGame(Player player, Exchange exchange) {}

  /**
   * Serializes the current game state to a JSON file.
   *
   * @param player the player to save
   * @param exchange the exchange to save
   * @param path the file to write (created or overwritten)
   * @throws IOException if the file cannot be written
   */
  public void save(Player player, Exchange exchange, Path path) throws IOException {
    GameStateDto dto = new GameStateDto();

    dto.playerName = player.getName();
    dto.startingMoney = player.getStartingMoney().toPlainString();
    dto.currentMoney = player.getMoney().toPlainString();
    dto.exchangeName = exchange.getName();
    dto.week = exchange.getWeek();

    dto.portfolio = new ArrayList<>();
    for (Share share : player.getPortfolio().getShares()) {
      ShareDto s = new ShareDto();
      s.symbol = share.getStock().getSymbol();
      s.company = share.getStock().getCompany();
      s.quantity = share.getQuantity().toPlainString();
      s.purchasePrice = share.getPurchasePrice().toPlainString();
      dto.portfolio.add(s);
    }

    dto.transactions = new ArrayList<>();
    for (Transaction t : player.getTransactionArchive().getAll()) {
      TransactionDto td = new TransactionDto();
      td.type = (t instanceof Purchase) ? "BUY" : "SELL";
      td.symbol = t.getShare().getStock().getSymbol();
      td.company = t.getShare().getStock().getCompany();
      td.quantity = t.getShare().getQuantity().toPlainString();
      td.purchasePrice = t.getShare().getPurchasePrice().toPlainString();
      td.week = t.getWeek();

      if (t instanceof Sale) {
        BigDecimal gross = t.getCalculator().calculateGross();
        BigDecimal quantity = t.getShare().getQuantity();
        BigDecimal salePrice = gross.divide(quantity, 10, RoundingMode.HALF_UP);
        td.salePrice = salePrice.toPlainString();
      }

      dto.transactions.add(td);
    }

    dto.stocks = new ArrayList<>();
    for (Stock stock : exchange.getAllStocks()) {
      StockDto sd = new StockDto();
      sd.symbol = stock.getSymbol();
      sd.company = stock.getCompany();
      sd.prices = new ArrayList<>();
      for (BigDecimal price : stock.getHistoricalPrices()) {
        sd.prices.add(price.toPlainString());
      }
      dto.stocks.add(sd);
    }

    Files.writeString(path, GSON.toJson(dto), StandardCharsets.UTF_8);
  }

  /**
   * Deserializes a saved game state from a JSON file.
   *
   * @param path the file to read
   * @return a record containing the restored player and exchange
   * @throws IOException if the file cannot be read
   * @throws IllegalArgumentException if the file content is invalid
   */
  public LoadedGame load(Path path) throws IOException {
    String json = Files.readString(path, StandardCharsets.UTF_8);
    GameStateDto dto = GSON.fromJson(json, GameStateDto.class);

    if (dto == null) {
      throw new IllegalArgumentException("Save file is empty or corrupt.");
    }

    List<Stock> stocks = new ArrayList<>();
    for (StockDto sd : dto.stocks) {
      if (sd.prices == null || sd.prices.isEmpty()) {
        throw new IllegalArgumentException("Stock " + sd.symbol + " has no price history.");
      }
      Stock stock = new Stock(sd.symbol, sd.company, new BigDecimal(sd.prices.get(0)));
      for (int i = 1; i < sd.prices.size(); i++) {
        stock.addNewSalesPrice(new BigDecimal(sd.prices.get(i)));
      }
      stocks.add(stock);
    }

    Exchange exchange = new Exchange(dto.exchangeName, stocks);
    setWeek(exchange, dto.week);

    Player player = new Player(dto.playerName, new BigDecimal(dto.startingMoney));
    setMoney(player, new BigDecimal(dto.currentMoney));

    java.util.Map<String, Stock> stockMap = new java.util.HashMap<>();
    for (Stock s : stocks) {
      stockMap.put(s.getSymbol(), s);
    }

    for (ShareDto sd : dto.portfolio) {
      Stock stock = stockMap.get(sd.symbol);
      if (stock == null) {
        throw new IllegalArgumentException("Portfolio references unknown stock: " + sd.symbol);
      }
      Share share = new Share(stock, new BigDecimal(sd.quantity), new BigDecimal(sd.purchasePrice));
      player.getPortfolio().addShare(share);
    }

    for (TransactionDto td : dto.transactions) {
      boolean isBuy = "BUY".equals(td.type);

      String snapshotPrice = isBuy ? td.purchasePrice : td.salePrice;
      Stock snapshotStock = new Stock(td.symbol, td.company, new BigDecimal(snapshotPrice));

      Share share =
          new Share(snapshotStock, new BigDecimal(td.quantity), new BigDecimal(td.purchasePrice));

      Transaction t = isBuy ? new Purchase(share, td.week) : new Sale(share, td.week);
      player.getTransactionArchive().add(t);
    }

    return new LoadedGame(player, exchange);
  }

  /**
   * Sets Player.money (non-final private) via reflection. Needed because the public API only
   * supports incremental add/withdraw.
   */
  private static void setMoney(Player player, BigDecimal amount) {
    try {
      Field f = Player.class.getDeclaredField("money");
      f.setAccessible(true);
      f.set(player, amount);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Could not restore player money", e);
    }
  }

  /**
   * Sets Exchange.week (private) via reflection. Needed because the public API only increments the
   * week.
   */
  private static void setWeek(Exchange exchange, int week) {
    try {
      Field f = Exchange.class.getDeclaredField("week");
      f.setAccessible(true);
      f.set(exchange, week);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Could not restore exchange week", e);
    }
  }
}
