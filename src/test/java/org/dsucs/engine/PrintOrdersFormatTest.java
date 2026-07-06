package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrintOrdersFormatTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
    }

    @Test
    void compactPrintProducesAlignedColumns() {
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

        String out = baos.toString();
        String[] lines = out.split("\\r?\\n");
        // header should be exact
        assertTrue(lines[0].equals("Buyers             | Sellers"), "Header must match README sample");
        // first data row should contain both a buyers volume/price and sellers price/volume
        assertTrue(lines[1].matches(".*\\d{1,3}[,\\d]*\\s+\\d{1,3}.*\\|\\s+\\d{1,3}\\s+\\d{1,3}[,\\d]*.*"));
    }
}



