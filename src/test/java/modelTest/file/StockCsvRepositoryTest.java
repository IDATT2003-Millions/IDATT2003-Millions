package modelTest.file;

import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.file.StockCsvRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockCsvRepositoryTest {
    @Test
    void load_shouldReadStocksFromFile() throws IOException {

        StockCsvRepository repo = new StockCsvRepository();

        Path path = Path.of("src/test/resources/stocks.csv");

        List<Stock> stocks = repo.load(path);

        assertEquals(3, stocks.size());
    }
}
