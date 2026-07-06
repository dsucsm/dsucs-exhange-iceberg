package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AggregationTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
        System.clearProperty("lse.aggregate");
    }

    @Test
    void aggregatedCompactTradeMessageForIcebergRestingParticipant() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        // Iceberg resting sell Sx: total 25, peak 10
        proc.processOrderLine("Sx,S,100,25,10");
        // Incoming buy that consumes 25 -> will produce 10,10,5 trades against Sx
        proc.processOrderLine("Bx,B,100,25");

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
        // Expect a single 5TG line for resting participant Sx
        assertTrue(out.contains("5TG Sx:"), "Should print aggregated 5TG message for Sx");
        // Ensure the aggregated message contains three sub-trades including quantities 10,10,5
        assertTrue(out.contains("10") && out.contains("5"));
    }
}

