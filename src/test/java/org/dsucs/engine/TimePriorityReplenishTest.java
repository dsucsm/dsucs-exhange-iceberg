package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimePriorityReplenishTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void replenished_iceberg_loses_priority_and_moves_to_back_of_queue() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        // Create queue at price 100: S1 (non-iceberg), I1 (iceberg peak 500 total 1500), S2 (non-iceberg)
        proc.processOrderLine("S1,S,100,500");
        proc.processOrderLine("I1,S,100,1500,500");
        proc.processOrderLine("S2,S,100,300");

        // Incoming buy will consume S1 (500), I1 visible (500 -> replenish), S2 (300), then replenished I1 (500)
        proc.processOrderLine("B1,B,100,1800");

        // Check recent trades order: should reflect sells consumed in order S1, I1, S2, I1
        List<Trade> trades = ob.getRecentTrades();
        assertEquals("S1", trades.get(0).sellOrderId);
        assertEquals("I1", trades.get(1).sellOrderId);
        assertEquals("S2", trades.get(2).sellOrderId);
        assertEquals("I1", trades.get(3).sellOrderId);
    }
}

