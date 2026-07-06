package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadmeExamplesTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void example1_noTrades_finalBookMatchesReadme() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,105,20000");
        proc.processOrderLine("10001,S,100,500");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("10004,S,103,100");

        // Inspect internal book snapshots
        var bids = ob.getBidsSnapshot();
        var asks = ob.getAsksSnapshot();
        // key prices from README example 1
        assertTrue(bids.containsKey(99), "Should contain price 99 in bids");
        assertTrue(bids.containsKey(98), "Should contain price 98 in bids");
        assertTrue(asks.containsKey(100), "Should contain price 100 in asks");
        assertTrue(asks.containsKey(105), "Should contain price 105 in asks");
    }

    @Test
    void example2_aggressiveBuy_producesTradesAndFinalBook() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,105,20000");
        proc.processOrderLine("10001,S,100,500");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("10004,S,103,100");

        // aggressive buy
        proc.processOrderLine("10006,B,105,16000");

        // Inspect recent trades
        var trades = ob.getRecentTrades();
        int sum = trades.stream().filter(t -> "10006".equals(t.buyOrderId)).mapToInt(t -> t.quantity).sum();
        assertTrue(sum == 16000, "Aggressor 10006 should have traded total 16000");
        // Ensure individual expected matches exist
        assertTrue(trades.stream().anyMatch(t -> "10006".equals(t.buyOrderId) && "10001".equals(t.sellOrderId) && t.price == 100 && t.quantity == 500));
        assertTrue(trades.stream().anyMatch(t -> "10006".equals(t.buyOrderId) && "10002".equals(t.sellOrderId) && t.price == 100 && t.quantity == 10000));
        assertTrue(trades.stream().anyMatch(t -> "10006".equals(t.buyOrderId) && "10004".equals(t.sellOrderId) && t.price == 103 && t.quantity == 100));
        assertTrue(trades.stream().anyMatch(t -> "10006".equals(t.buyOrderId) && "10005".equals(t.sellOrderId) && t.price == 105 && t.quantity == 5400));

        var bids = ob.getBidsSnapshot();
        assertTrue(bids.containsKey(99));
        assertTrue(bids.containsKey(98));
    }

    @Test
    void icebergExample_behaviourMatchesReadme() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,101,20000");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10001,S,100,7500");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("ice1,B,100,100000,10000");

        var trades = ob.getRecentTrades();
        // ice1 is aggressor (BUY) so buyOrderId == "ice1"
        int sumIce = trades.stream().filter(t -> "ice1".equals(t.buyOrderId)).mapToInt(t -> t.quantity).sum();
        assertTrue(sumIce == 17500, "ice1 should have traded 10000 + 7500 = 17500");
        assertTrue(trades.stream().anyMatch(t -> "ice1".equals(t.buyOrderId) && "10002".equals(t.sellOrderId) && t.quantity == 10000));
        assertTrue(trades.stream().anyMatch(t -> "ice1".equals(t.buyOrderId) && "10001".equals(t.sellOrderId) && t.quantity == 7500));

        var asks = ob.getAsksSnapshot();
        assertTrue(asks.containsKey(101));
        var bids = ob.getBidsSnapshot();
        assertTrue(bids.containsKey(100));
    }
}


