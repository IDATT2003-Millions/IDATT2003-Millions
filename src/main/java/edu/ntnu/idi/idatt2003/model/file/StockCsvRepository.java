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

/**
 * Handles persistence of {@link Stock} data in CSV format.
 *
 * <p>Each non-comment line in the CSV must have the format
 * {@code symbol,company,salesPrice}. Blank lines and lines that start with
 * {@code #} are ignored when loading.
 */
public class StockCsvRepository {

    /**
     * Loads stocks from a CSV file.
     *
     * <p>The method skips blank lines and comment lines that start with {@code #}.</p>
     *
     * @param path the path to the CSV file
     * @return a list of stocks loaded from the file
     * @throws IOException if the file cannot be read, if a line has invalid CSV format,
     *         or if a sales price cannot be parsed
     */
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

                BigDecimal price;
                try {
                    price = new BigDecimal(parts[2].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid price in CSV line: " + line, e);
                }

                stocks.add(new Stock(symbol, company, price));
            }
        }

        return stocks;
    }

    /**
     * Saves stocks to a CSV file.
     *
     * <p>Each stock is written in the format {@code symbol,company,salesPrice}.</p>
     *
     * @param path the path to the target CSV file
     * @param stocks the stocks to write
     * @throws IOException if the file cannot be written
     */
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
