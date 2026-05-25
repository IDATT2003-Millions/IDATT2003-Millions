package modelTest.file;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.file.GameStateSerializer;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import edu.ntnu.idi.idatt2003.model.transactions.Sale;
import edu.ntnu.idi.idatt2003.model.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateSerializerTest {

  @TempDir
  Path tempDir;

  private GameStateSerializer serializer;
  private Exchange exchange;
  private Player player;
  private Stock apple;
  private Stock microsoft;

  @BeforeEach
  void setUp() {
    serializer = new GameStateSerializer();
    apple     = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
    microsoft = new Stock("MSFT", "Microsoft",  new BigDecimal("300.00"));
    exchange  = new Exchange("Test Exchange", List.of(apple, microsoft));
    player    = new Player("Kristoffer", new BigDecimal("10000.00"));
  }

  // ── Save + load round-trips ───────────────────────────────────────────────

  @Test
  void saveAndLoad_restoresPlayerName() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals("Kristoffer", loaded.player().getName());
  }

  @Test
  void saveAndLoad_restoresPlayerMoney() throws IOException {
    exchange.buy("AAPL", new BigDecimal("5"), player)
        .commit(player); // spends some money

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals(0, player.getMoney().compareTo(loaded.player().getMoney()));
  }

  @Test
  void saveAndLoad_restoresStartingMoney() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals(0,
        new BigDecimal("10000.00").compareTo(loaded.player().getStartingMoney()));
  }

  @Test
  void saveAndLoad_restoresExchangeWeek() throws IOException {
    exchange.advance();
    exchange.advance();
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals(3, loaded.exchange().getWeek());
  }

  @Test
  void saveAndLoad_restoresExchangeName() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals("Test Exchange", loaded.exchange().getName());
  }

  @Test
  void saveAndLoad_restoresAllStocks() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertTrue(loaded.exchange().hasStock("AAPL"));
    assertTrue(loaded.exchange().hasStock("MSFT"));
  }

  @Test
  void saveAndLoad_restoresStockPriceHistory() throws IOException {
    apple.addNewSalesPrice(new BigDecimal("160.00"));
    apple.addNewSalesPrice(new BigDecimal("170.00"));

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    Stock restoredApple = loaded.exchange().getStock("AAPL");
    assertEquals(3, restoredApple.getHistoricalPrices().size());
    assertEquals(0,
        new BigDecimal("170.00").compareTo(restoredApple.getSalesPrice()));
  }

  @Test
  void saveAndLoad_restoresPortfolioShares() throws IOException {
    exchange.buy("AAPL", new BigDecimal("10"), player).commit(player);
    exchange.buy("MSFT", new BigDecimal("5"),  player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals(2, loaded.player().getPortfolio().getShares().size());
  }

  @Test
  void saveAndLoad_restoresShareQuantityAndPurchasePrice() throws IOException {
    exchange.buy("AAPL", new BigDecimal("7"), player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    Share restored = loaded.player().getPortfolio().getShares().get(0);
    assertEquals(0, new BigDecimal("7").compareTo(restored.getQuantity()));
    assertEquals(0, new BigDecimal("150.00").compareTo(restored.getPurchasePrice()));
  }

  @Test
  void saveAndLoad_restoresTransactionCount() throws IOException {
    exchange.buy("AAPL", new BigDecimal("5"), player).commit(player);
    exchange.advance();
    Share owned = player.getPortfolio().getShares().get(0);
    exchange.sell(owned, player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    assertEquals(2, loaded.player().getTransactionArchive().getAll().size());
  }

  @Test
  void saveAndLoad_restoresBuyTransactionType() throws IOException {
    exchange.buy("AAPL", new BigDecimal("3"), player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    Transaction t = loaded.player().getTransactionArchive().getAll().get(0);
    assertInstanceOf(Purchase.class, t);
  }

  @Test
  void saveAndLoad_restoresSellTransactionType() throws IOException {
    exchange.buy("AAPL", new BigDecimal("3"), player).commit(player);
    Share owned = player.getPortfolio().getShares().get(0);
    exchange.sell(owned, player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    Transaction sell = loaded.player().getTransactionArchive().getAll().get(1);
    assertInstanceOf(Sale.class, sell);
  }

  @Test
  void saveAndLoad_portfolioShareLinkedToExchangeStock() throws IOException {
    exchange.buy("AAPL", new BigDecimal("5"), player).commit(player);

    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    GameStateSerializer.LoadedGame loaded = serializer.load(file);

    Share share = loaded.player().getPortfolio().getShares().get(0);
    Stock liveStock = loaded.exchange().getStock("AAPL");

    assertSame(share.getStock(), liveStock);
  }

  // ── Error cases ───────────────────────────────────────────────────────────

  @Test
  void load_throwsIOException_whenFileDoesNotExist() {
    Path missing = tempDir.resolve("missing.json");
    assertThrows(IOException.class, () -> serializer.load(missing));
  }

  @Test
  void load_throwsIllegalArgumentException_whenFileIsEmpty() throws IOException {
    Path empty = tempDir.resolve("empty.json");
    Files.writeString(empty, "");
    assertThrows(IllegalArgumentException.class, () -> serializer.load(empty));
  }

  @Test
  void save_createsFileOnDisk() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);
    assertTrue(Files.exists(file));
  }

  @Test
  void save_writesValidJson() throws IOException {
    Path file = tempDir.resolve("save.json");
    serializer.save(player, exchange, file);

    String content = Files.readString(file);
    assertTrue(content.contains("\"playerName\""));
    assertTrue(content.contains("\"Kristoffer\""));
    assertTrue(content.contains("\"AAPL\""));
  }
}
