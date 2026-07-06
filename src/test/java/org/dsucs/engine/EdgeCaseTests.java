package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EdgeCaseTests {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void deleteAll_clears_book_and_trades() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("A,B,100,100");
        proc.processOrderLine("B,S,100,100");

        // ensure trades recorded
        assertEquals(false, ob.getRecentTrades().isEmpty());

        ob.deleteAll();

        assertEquals(true, ob.getRecentTrades().isEmpty());
        assertEquals(true, ob.getAllTrades().isEmpty());
        assertEquals(true, ob.getBidsSnapshot().isEmpty());
        assertEquals(true, ob.getAsksSnapshot().isEmpty());
    }

    @Test
    void partial_peak_replenish_behavior() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        // Iceberg sell with total 1500, peak 1000
        proc.processOrderLine("I1,S,100,1500,1000");

        // Aggressive buy of 800 should partially consume visible peak leaving visible 200
        proc.processOrderLine("B1,B,100,800");

        var asks = ob.getAsksSnapshot();
        // Find I1 in asks
        boolean found = asks.values().stream().flatMap(java.util.List::stream).anyMatch(o -> o.id.equals("I1"));
        assertEquals(true, found, "I1 should still be in asks after partial fill");

        // Check remaining total and visible volumes
        java.util.List<Order> list = asks.values().stream().flatMap(java.util.List::stream).filter(o -> o.id.equals("I1")).toList();
        Order i1 = list.get(0);
        // total remaining should be 700, visible should be 200
        assertEquals(700, i1.volume);
        assertEquals(200, i1.visibleVolume);
    }
}

