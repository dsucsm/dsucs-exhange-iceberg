package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AggregationSymmetricTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
        System.clearProperty("lse.aggregate");
    }

    @Test
    void aggregatedCompactTradeMessageForBuySideRestingParticipant() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        // Iceberg resting buy Bx: total 25, peak 10
        proc.processOrderLine("Bx,B,100,25,10");
        // Incoming sell that consumes 25 -> will produce trades against Bx
        proc.processOrderLine("Sx,S,100,25");

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

        String out = baos.toString().trim();
        // Should contain aggregated 5TG message for Bx (resting buy)
        assertTrue(out.contains("5TG Bx:"), "Should print aggregated 5TG message for Bx");
    }
}


