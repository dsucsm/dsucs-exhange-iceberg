package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoldenReadmeTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void example1_golden_output_matches_expected() {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,105,20000");
        proc.processOrderLine("10001,S,100,500");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("10004,S,103,100");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            ob.printOrders(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String actual = baos.toString();

        // Build expected output using the same formatting rules as the engine's compact print
        List<Order> bidRows = new ArrayList<>(OrderBookImpl.getInstance().getBidsSnapshot().values().stream().flatMap(List::stream).toList());
        List<Order> askRows = new ArrayList<>(OrderBookImpl.getInstance().getAsksSnapshot().values().stream().flatMap(List::stream).toList());
        int maxRows = Math.max(bidRows.size(), askRows.size());
        StringBuilder expected = new StringBuilder();
        expected.append("Buyers             | Sellers\n");
        for (int i = 0; i < maxRows; i++) {
            String left = "";
            if (i < bidRows.size()) {
                Order b = bidRows.get(i);
                String vol = String.format("%,d", b.visibleVolume);
                String ice = b.isIceberg() ? "i" : "";
                left = String.format("%9s %3d%s", vol, b.price, ice);
            } else {
                left = String.format("%13s", "");
            }

            String right = "";
            if (i < askRows.size()) {
                Order a = askRows.get(i);
                String vol = String.format("%,d", a.visibleVolume);
                String ice = a.isIceberg() ? "i" : "";
                right = String.format("%3d %9s%s", a.price, vol, ice);
            }
            expected.append(String.format("%13s | %s\n", left, right));
        }

        String normExpected = expected.toString().replace("\r\n", "\n");
        String normActual = actual.replace("\r\n", "\n");
        assertEquals(normExpected, normActual);
    }
}


