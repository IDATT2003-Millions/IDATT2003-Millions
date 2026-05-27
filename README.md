
# Millions — Stock Market Game

A JavaFX desktop application where players buy and sell stocks on a simulated stock exchange.
The player starts with a chosen amount of capital and trades stocks over multiple weeks,
with the goal of growing their net worth. The game supports limit orders, portfolio tracking,
transaction history, and save/load functionality.

---



## Project Structure

```
src/
├── main/
│   ├── java/edu/ntnu/idi/idatt2003/
│   │   ├── MainApp.java
│   │   ├── controller/
│   │   │   ├── SceneController.java
│   │   │   ├── StockMarketController.java
│   │   │   ├── PortfolioController.java
│   │   │   ├── TransactionsController.java
│   │   │   └── StatisticsController.java
│   │   ├── model/
│   │   │   ├── calculators/
│   │   │   │   ├── TransactionCalculator.java
│   │   │   │   ├── PurchaseCalculator.java
│   │   │   │   └── SaleCalculator.java
│   │   │   ├── core/
│   │   │   │   ├── Exchange.java
│   │   │   │   ├── Stock.java
│   │   │   │   ├── Share.java
│   │   │   │   ├── Portfolio.java
│   │   │   │   └── TransactionArchive.java
│   │   │   ├── file/
│   │   │   │   ├── GameStateSerializer.java
│   │   │   │   ├── StockCsvRepository.java
│   │   │   │   └── dto/
│   │   │   │       ├── GameStateDto.java
│   │   │   │       ├── ShareDto.java
│   │   │   │       ├── StockDto.java
│   │   │   │       └── TransactionDto.java
│   │   │   ├── observer/
│   │   │   │   ├── ExchangeObserver.java
│   │   │   │   └── Subject.java
│   │   │   └── transactions/
│   │   │       ├── Player.java
│   │   │       ├── PlayerStatus.java
│   │   │       ├── Transaction.java
│   │   │       ├── Purchase.java
│   │   │       ├── Sale.java
│   │   │       ├── TransactionFactory.java
│   │   │       ├── LimitOrder.java
│   │   │       ├── OrderBook.java
│   │   │       └── OrderType.java
│   │   └── view/
│   │       ├── LaunchGameView.java
│   │       ├── NewGameView.java
│   │       ├── Sidebar.java
│   │       ├── StockMarketView.java
│   │       ├── MyPortfolioView.java
│   │       ├── TransactionsView.java
│   │       ├── StatisticsView.java
│   │       └── PriceChart.java
│   └── resources/
│       └── css_files/
│           └── global.css
└── test/
    └── java/                             # Unit tests mirroring the main structure
```

---

## Running the Application

```bash
mvn javafx:run
```

This will compile and launch the application. On startup you can either start a new game
by entering a player name, starting capital, and selecting a CSV file with stock listings,
or load a previously saved game.

### Stock CSV format

The CSV file must have one stock per line in the following format:

```
SYMBOL,Company Name,InitialPrice
AAPL,Apple Inc,175.00
MSFT,Microsoft,420.00
```

Lines starting with `#` and blank lines are ignored.

---

## Running Tests

```bash
mvn test
```

To also generate a code coverage report:

```bash
mvn verify
```

The JaCoCo coverage report will be available at:

```
target/site/jacoco/index.html
```

---

## Key Features

- Buy and sell stocks at current market price
- Place limit buy/sell orders that execute automatically when the target price is reached
- Advance the market week by week with randomized price changes
- Track portfolio value and net worth over time with a chart
- View full transaction history and order book
- Save and load game state to/from a JSON file
- Player status progression: Novice → Investor → Speculator

## Link to repo

- https://github.com/IDATT2003-Millions/IDATT2003-Millions/tree/main

## Link to figma design

GUI design:
https://www.figma.com/design/pbQsd7pjBYZ5lfM6VLfjw8/GUI-design-prog-2?node-id=0-1&p=f&t=eJ8arDT48ZHQWaiL-0

