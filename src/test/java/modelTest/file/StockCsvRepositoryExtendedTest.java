package modelTest.file;

import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.file.StockCsvRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockCsvRepositoryExtendedTest {

    @Test
    void load_emptyFile_returnsEmptyList() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path path = Path.of("src/test/resources/stocks-empty.csv");

        List<Stock> stocks = repository.load(path);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void load_nonExistentFile_throwsIOException() {
        StockCsvRepository repository = new StockCsvRepository();
        Path path = Path.of("src/test/resources/does-not-exist.csv");

        assertThrows(IOException.class, () -> repository.load(path));
    }

    @Test
    void load_lineWithTooManyColumns_throwsIOException() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path tempFile = Files.createTempFile("stocks-extra-columns-", ".csv");
        Files.writeString(tempFile, "AAPL,Apple Inc.,200,EXTRA\n");

        assertThrows(IOException.class, () -> repository.load(tempFile));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void load_lineWithNonNumericPrice_throwsIOException() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path tempFile = Files.createTempFile("stocks-bad-price-", ".csv");
        Files.writeString(tempFile, "AAPL,Apple Inc.,not-a-number\n");

        assertThrows(IOException.class, () -> repository.load(tempFile));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void save_emptyList_writesEmptyFile() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path tempFile = Files.createTempFile("stocks-save-empty-", ".csv");

        repository.save(tempFile, List.of());
        List<Stock> loaded = repository.load(tempFile);

        assertTrue(loaded.isEmpty());

        Files.deleteIfExists(tempFile);
    }

    @Test
    void save_andLoad_preservesPriceWithDecimals() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path tempFile = Files.createTempFile("stocks-decimal-", ".csv");
        List<Stock> original = List.of(
                new Stock("TSLA", "Tesla Inc", new BigDecimal("123.45"))
        );

        repository.save(tempFile, original);
        List<Stock> loaded = repository.load(tempFile);

        assertEquals(1, loaded.size());
        assertEquals(0, loaded.getFirst().getSalesPrice().compareTo(new BigDecimal("123.45")));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void save_andLoad_preservesSymbolAndCompanyExactly() throws IOException {
        StockCsvRepository repository = new StockCsvRepository();
        Path tempFile = Files.createTempFile("stocks-fields-", ".csv");
        List<Stock> original = List.of(
                new Stock("MSFT", "Microsoft Corporation", new BigDecimal("400.00"))
        );

        repository.save(tempFile, original);
        List<Stock> loaded = repository.load(tempFile);

        assertEquals("MSFT", loaded.getFirst().getSymbol());
        assertEquals("Microsoft Corporation", loaded.getFirst().getCompany());

        Files.deleteIfExists(tempFile);
    }
}
