package com.example.cep;

import com.example.cep.core.ImbalanceProcessor;
import com.example.cep.core.ImbalanceProcessor.AlertSink;
import com.example.cep.model.QuoteUpdate;
import com.example.cep.model.Side;
import com.example.cep.pipeline.DisruptorPipeline;
import com.example.cep.load.MultiSymbolLoadGen;

public class App {

    public static void main(String[] args) throws Exception {
        double threshold = 0.60;   // 60%
        double hysteresis = 0.55;  // 55%
        long minTotal = 1_000;
        long sustainMs = 50;

        AlertSink sink = new AlertSink() {
            @Override
            public void onImbalanceAlert(String symbol, double imbalance, long bid, long ask, long tsNanos) {
                System.out.printf("ALERT  %s  imbalance=%.2f%%  bid=%d ask=%d%n",
                    symbol, 100.0*imbalance, bid, ask);
            }
            @Override
            public void onImbalanceCleared(String symbol, long tsNanos) {
                System.out.printf("CLEAR  %s  back within band%n", symbol);
            }
        };

        ImbalanceProcessor processor = new ImbalanceProcessor(threshold, hysteresis, minTotal, sustainMs, sink);
        DisruptorPipeline pipeline = new DisruptorPipeline(processor);

        // Start Disruptor
        pipeline.start();

        // Load generator: 8 symbols, ~500k events/s total target (depends on CPU)
        MultiSymbolLoadGen gen = new MultiSymbolLoadGen(8, 100_000, pipeline::publish);
        gen.runForMillis(1500);

        // Graceful stop
        pipeline.stop();
        System.out.println("Done.");
    }
}
