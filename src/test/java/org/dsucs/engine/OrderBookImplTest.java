package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderBookImplTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void testSimpleMatchProducesSingleTrade() {
        OrderBookImpl ob = OrderBookImpl.getInstance();
        ob.deleteAll();

        Order sell = new Order("S1", Side.SELL, 100, 10, 0);
        ob.processOrder(sell);

        Order buy = new Order("B1", Side.BUY, 100, 10, 0);
        ob.processOrder(buy);

        // capture stdout
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            ob.printTrades(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String out = baos.toString().trim();
        assertEquals("trade B1,S1,100,10", out);
    }

    @Test
    void testIcebergReplenishmentProducesMultipleTradesSummingToVolume() {
        OrderBookImpl ob = OrderBookImpl.getInstance();
        ob.deleteAll();

        // Iceberg sell: total volume 25, peak 10 (visible 10 -> replenish twice)
        Order sell = new Order("S2", Side.SELL, 100, 25, 10);
        ob.processOrder(sell);

        // Aggressive buy to consume all
        Order buy = new Order("B2", Side.BUY, 100, 25, 0);
        ob.processOrder(buy);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            ob.printTrades(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String out = baos.toString().trim();
        String[] lines = out.split("\\r?\\n");
        // Expect multiple trade lines (10,10,5)
        int sum = 0;
        for (String line : lines) {
            assertTrue(line.startsWith("trade "), "line starts with trade");
            String[] parts = line.split(",");
            int qty = Integer.parseInt(parts[3]);
            sum += qty;
            // ensure buy/sell ids are in expected order
            assertEquals("B2", parts[0].split(" ")[1]);
            assertEquals("S2", parts[1]);
            assertEquals("100", parts[2]);
        }

        assertEquals(25, sum);
    }
}

