package edu.ntnu.idi.idatt2003.model.file;

import edu.ntnu.idi.idatt2003.model.core.Stock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StockCsvRepository {

    public List<Stock> load(Path path) throws IOException {
        List<Stock> stocks = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
            String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IOException("Invalid CSV line: " + line);
                }
                String symbol = parts[0].trim();
                String company = parts[1].trim();
                BigDecimal price = new BigDecimal(parts[2].trim());

                stocks.add(new Stock(symbol, company, price));
            }
        }
        return stocks;
    }

    public void save(Path path, Collection<Stock> stocks) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for (Stock stock : stocks) {

                writer.write(
                        stock.getSymbol() + "," +
                                stock.getCompany() + "," +
                                stock.getSalesPrice()
                );

                writer.newLine();
            }
        }
    }
}
