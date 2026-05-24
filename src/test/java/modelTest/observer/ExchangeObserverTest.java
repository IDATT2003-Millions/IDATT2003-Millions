package modelTest.observer;

import edu.ntnu.idi.idatt2003.model.core.Exchange;
import edu.ntnu.idi.idatt2003.model.core.Stock;
import edu.ntnu.idi.idatt2003.model.observer.ExchangeObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ExchangeObserverTest {

    private Exchange exchange;

    @BeforeEach
    void setUp() {
        Stock stock = new Stock("TSLA", "Tesla Inc", new BigDecimal("100.00"));
        exchange = new Exchange("NASDAQ", List.of(stock));
    }

    @Test
    void addObserver_singleObserver_isNotifiedOnAdvance() {
        List<Exchange> received = new ArrayList<>();
        ExchangeObserver observer = received::add;

        exchange.addObserver(observer);
        exchange.advance();

        assertEquals(1, received.size());
        assertSame(exchange, received.getFirst());
    }

    @Test
    void addObserver_multipleObservers_allReceiveNotificationOnAdvance() {
        List<Exchange> first = new ArrayList<>();
        List<Exchange> second = new ArrayList<>();

        exchange.addObserver(first::add);
        exchange.addObserver(second::add);
        exchange.advance();

        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @Test
    void removeObserver_registeredObserver_isNoLongerNotified() {
        List<Exchange> received = new ArrayList<>();
        ExchangeObserver observer = received::add;

        exchange.addObserver(observer);
        exchange.removeObserver(observer);
        exchange.advance();

        assertEquals(0, received.size());
    }

    @Test
    void removeObserver_unregisteredObserver_doesNotAffectOtherObservers() {
        List<Exchange> received = new ArrayList<>();
        ExchangeObserver registered = received::add;
        ExchangeObserver unregistered = e -> {};

        exchange.addObserver(registered);
        exchange.removeObserver(unregistered);
        exchange.advance();

        assertEquals(1, received.size());
    }

    @Test
    void advance_calledMultipleTimes_observerNotifiedEachTime() {
        List<Exchange> received = new ArrayList<>();
        exchange.addObserver(received::add);

        exchange.advance();
        exchange.advance();
        exchange.advance();

        assertEquals(3, received.size());
    }

    @Test
    void refresh_withRegisteredObserver_triggersNotification() {
        List<Exchange> received = new ArrayList<>();
        exchange.addObserver(received::add);

        exchange.refresh();

        assertEquals(1, received.size());
        assertSame(exchange, received.getFirst());
    }

    @Test
    void refresh_withNoObservers_doesNotThrow() {
        exchange.refresh();
    }

    @Test
    void addObserver_noObservers_advanceDoesNotThrow() {
        exchange.advance();
    }

    @Test
    void onExchangeUpdated_receivesCorrectExchangeReference() {
        List<Exchange> received = new ArrayList<>();
        exchange.addObserver(received::add);

        exchange.refresh();

        assertSame(exchange, received.getFirst());
    }
}
