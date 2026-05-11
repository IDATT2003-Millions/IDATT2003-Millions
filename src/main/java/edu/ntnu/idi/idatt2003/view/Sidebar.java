package edu.ntnu.idi.idatt2003.view;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import edu.ntnu.idi.idatt2003.model.transactions.Player;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Reusable left-hand sidebar that displays live player and exchange state.
 * Register it with the exchange once; it self-updates via ExchangeObserver.
 */
public class Sidebar extends VBox implements ExchangeObserver {

    private final Player player;
    private final Label cashLabel;
    private final Label netWorthLabel;
    private final Label weekLabel;

    public Sidebar(Player player, Exchange exchange) {
        this.player = player;
        getStyleClass().add("sidebar");

        Label appTitle = new Label("Millions");
        appTitle.getStyleClass().add("sidebar-app-title");

        Label playerSection = new Label("PLAYER");
        playerSection.getStyleClass().add("sidebar-section-label");

        Label playerName = new Label(player.getName());
        playerName.getStyleClass().add("sidebar-value-primary");

        Label cashSection = new Label("CASH");
        cashSection.getStyleClass().add("sidebar-section-label");

        cashLabel = new Label(formatMoney(player.getMoney()));
        cashLabel.getStyleClass().add("sidebar-value");

        Label netWorthSection = new Label("NET WORTH");
        netWorthSection.getStyleClass().add("sidebar-section-label");

        netWorthLabel = new Label(formatMoney(player.getNetWorth()));
        netWorthLabel.getStyleClass().add("sidebar-value");

        Label weekSection = new Label("WEEK");
        weekSection.getStyleClass().add("sidebar-section-label");

        weekLabel = new Label(String.valueOf(exchange.getWeek()));
        weekLabel.getStyleClass().add("sidebar-value-week");

        getChildren().addAll(
                appTitle,
                new Separator(),
                playerSection, playerName,
                new Separator(),
                cashSection, cashLabel,
                netWorthSection, netWorthLabel,
                new Separator(),
                weekSection, weekLabel
        );

        exchange.addObserver(this);
    }

    @Override
    public void onExchangeUpdated(Exchange exchange) {
        cashLabel.setText(formatMoney(player.getMoney()));
        netWorthLabel.setText(formatMoney(player.getNetWorth()));
        weekLabel.setText(String.valueOf(exchange.getWeek()));
    }

    private String formatMoney(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }
}