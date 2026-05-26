package edu.ntnu.idi.idatt2003.modelTest.transactions;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt2003.model.core.Share;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.core.TransactionArchive;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import edu.ntnu.idi.idatt2003.model.transactions.PlayerStatus;
import edu.ntnu.idi.idatt2003.model.transactions.Purchase;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerStatusTest {
  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("TestPlayer", new BigDecimal("10000.00"));
  }

  @Test
  void getNetWorth_noTrade_equalsStartingMoney() {
    assertEquals(0, new BigDecimal("10000.00").compareTo(player.getNetWorth()));
  }

  @Test
  void getNetWorth_afterWithdraw_decreases() {
    player.withdrawMoney(new BigDecimal("500.00"));
    assertEquals(0, new BigDecimal("9500.00").compareTo(player.getNetWorth()));
  }

  @Test
  void getStatus_newPlayer_isNovice() {
    assertEquals(PlayerStatus.NOVICE, player.getStatus());
  }

  @Test
  void getStatus_highNetWorthButFewWeeks_isNovice() {
    simulateWeeklyTrades(5);
    assertEquals(PlayerStatus.NOVICE, player.getStatus());
  }

  @Test
  void getStatusEnoughWeeksButLowGrowth_isNovice() {
    simulateWeeklyTrades(10);
    assertEquals(PlayerStatus.NOVICE, player.getStatus());
  }

  @Test
  void getStatus_tenWeeksAndTwentyPercentGrowth_isInvestor() {
    simulateWeeklyTrades(10);
    player.addMoney(new BigDecimal("2000.00"));
    assertEquals(PlayerStatus.INVESTOR, player.getStatus());
  }

  @Test
  void getStatusExactlyTenWeeksAndHighGrowth_isInvestorNotSpeculator() {
    simulateWeeklyTrades(10);
    player.addMoney(new BigDecimal("15000.00"));
    assertEquals(PlayerStatus.INVESTOR, player.getStatus());
  }

  @Test
  void getStatus_twentyWeeksAndDoubleNetWorth_isSpeculator() {
    simulateWeeklyTrades(20);
    player.addMoney(new BigDecimal("10000.00"));
    assertEquals(PlayerStatus.SPECULATOR, player.getStatus());
  }

  @Test
  void getStatus_twentyWeeksButNotDoubled_isInvestor() {
    simulateWeeklyTrades(20);
    player.addMoney(new BigDecimal("5000.00"));
    assertEquals(PlayerStatus.INVESTOR, player.getStatus());
  }

  private void simulateWeeklyTrades(int weeks) {
    Stock testStock = new Stock("test", "testStock", new BigDecimal("1.00"));
    TransactionArchive archive = player.getTransactionArchive();

    for (int week = 1; week <= weeks; week++) {
      Share share = new Share(testStock, new BigDecimal("1"), new BigDecimal("1.00"));
      Purchase purchase = new Purchase(share, week);
      archive.add(purchase);
    }
  }
}
