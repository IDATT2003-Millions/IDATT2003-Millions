package modelTest.file;

import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.file.StockCsvRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StockCsvRepositoryTest {
    @Test
    void load_shouldReadValidStocksFromFile() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path path = Path.of("src/test/resources/stocks.csv");

        List<Stock> stocks = repository.load(path);

        assertEquals(3, stocks.size());
        assertEquals("AAPL", stocks.getFirst().getSymbol());
        assertEquals("Apple Inc.", stocks.getFirst().getCompany());
        assertEquals(0, stocks.getFirst().getSalesPrice().compareTo(new BigDecimal("200")));
    }

    @Test
    void load_shouldIgnoreCommentsAndBlankLines() throws Exception {

        StockCsvRepository repository = new StockCsvRepository();

        Path path = Path.of("src/test/resources/stock-with-comment.csv");

        List<Stock> stocks = repository.load(path);

        assertEquals(2, stocks.size());

        assertEquals("AAPL", stocks.get(0).getSymbol());
        assertEquals("MSFT", stocks.get(1).getSymbol());
    }

    @Test
    void load_shouldThrowException_whenCsvLineHasInvalidFormat() {
        StockCsvRepository repository = new StockCsvRepository();
        Path path = Path.of("src/test/resources/stocks-invalid-format.csv");

        assertThrows(IOException.class, () -> repository.load(path));
    }

    @Test
    void save_shouldWriteStocksToFile() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        List<Stock> stocks = List.of(
                new Stock("AAPL", "Apple Inc.", new BigDecimal("276.43")),
                new Stock("MSFT", "Microsoft", new BigDecimal("404.68"))
        );

        Path tempFile = Files.createTempFile("stocks-", ".csv");

        repository.save(tempFile, stocks);
        List<Stock> loadedStocks = repository.load(tempFile);

        assertEquals(2, loadedStocks.size());
        assertEquals("AAPL", loadedStocks.get(0).getSymbol());
        assertEquals("MSFT", loadedStocks.get(1).getSymbol());
    }
}


