package com.example.cep.bench;

import com.example.cep.core.ImbalanceProcessor;
import com.example.cep.model.QuoteUpdate;
import com.example.cep.model.Side;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ImbalanceBenchmark {

    ImbalanceProcessor proc;
    String sym = "XS-BENCH";
    AtomicLong ts = new AtomicLong();

    @Setup
    public void setup() {
        proc = new ImbalanceProcessor(0.6, 0.55, 1000, 0,
                (s, i, b, a, t) -> {});
    }

    @Benchmark
    public void processQuotes() {
        long now = System.nanoTime();
        proc.onQuote(new QuoteUpdate(sym, Side.BID, 5000, now));
        proc.onQuote(new QuoteUpdate(sym, Side.ASK, 3000, now));
    }
}
