package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiIcebergAggregationTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
        System.clearProperty("lse.aggregate");
    }

    @Test
    void twoIcebergsAtSamePriceProduceTwo5TGMessages() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        // Two iceberg buy orders at same price 100. Use totals and peaks such that
        // the incoming sell cycles through the price level and hits both A and B multiple times.
        proc.processOrderLine("A,B,100,30000,10000"); // A: total 30000, peak 10000
        proc.processOrderLine("B,B,100,30000,10000"); // B: total 30000, peak 10000

        // Incoming sell that will consume 60k -> will cycle and hit A and B multiple times
        proc.processOrderLine("Sx,S,100,60000");

        // Enable aggregation
        System.setProperty("lse.aggregate", "true");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            ob.printTrades(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String out = baos.toString();
        // Expect two 5TG messages (one for A and one for B)
        assertTrue(out.contains("5TG A:"), "Should contain 5TG A message");
        assertTrue(out.contains("5TG B:"), "Should contain 5TG B message");
    }
}


