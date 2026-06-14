package com.example.cep;

import com.example.cep.core.ImbalanceProcessor;
import com.example.cep.core.ImbalanceProcessor.AlertSink;
import com.example.cep.model.QuoteUpdate;
import com.example.cep.model.Side;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ImbalanceProcessorTest {

    @Test
    void triggersAndClearsWithSustainAndHysteresis() {
        AtomicBoolean alerted = new AtomicBoolean(false);
        AtomicBoolean cleared = new AtomicBoolean(false);

        AlertSink sink = new AlertSink() {
            @Override public void onImbalanceAlert(String symbol, double imbalance, long bid, long ask, long ts) {
                alerted.set(true);
            }
            @Override public void onImbalanceCleared(String symbol, long ts) {
                cleared.set(true);
            }
        };

        ImbalanceProcessor proc = new ImbalanceProcessor(0.6, 0.55, 1000, 50, sink);
        String sym = "XS1";
        long t0 = System.nanoTime();

        // Balanced
        proc.onQuote(new QuoteUpdate(sym, Side.BID, 600, t0));
        proc.onQuote(new QuoteUpdate(sym, Side.ASK, 650, t0));

        // Cross threshold
        proc.onQuote(new QuoteUpdate(sym, Side.BID, 5000, t0 + 10_000_000)); // 10ms later
        // sustain not met yet
        assertFalse(alerted.get());

        // After sustain
        proc.onQuote(new QuoteUpdate(sym, Side.ASK, 1200, t0 + 70_000_000)); // +70ms
        assertTrue(alerted.get());

        // Clear with small imbalance
        proc.onQuote(new QuoteUpdate(sym, Side.BID, 1400, t0 + 120_000_000));
        assertTrue(cleared.get());
    }

    @Test
    void ignoresOutOfOrderPerSide() {
        AtomicInteger count = new AtomicInteger();
        ImbalanceProcessor proc = new ImbalanceProcessor(0.6, 0.55, 1000, 0, (s, i, b, a, ts) -> count.incrementAndGet());
        String sym = "XS2";
        long t0 = System.nanoTime();

        proc.onQuote(new QuoteUpdate(sym, Side.BID, 10_000, t0 + 1_000));
        proc.onQuote(new QuoteUpdate(sym, Side.ASK, 100, t0 + 1_100)); // alert

        // out-of-order BID (older ts) should be ignored
        proc.onQuote(new QuoteUpdate(sym, Side.BID, 1, t0 + 500));

        // still in alert once; no extra alerts expected from out-of-order
        assertEquals(1, count.get());
    }
}
