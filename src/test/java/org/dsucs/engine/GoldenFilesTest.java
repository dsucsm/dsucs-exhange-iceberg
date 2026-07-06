package org.dsucs.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoldenFilesTest {

    @AfterEach
    void tearDown() {
        OrderBookImpl.getInstance().deleteAll();
        System.clearProperty("lse.aggregate");
    }

    @Test
    void example1_matches_golden_file() throws Exception {
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

        Path p1 = Path.of("src/test/resources/golden/example1.golden");
        String expected1 = Files.readString(p1, StandardCharsets.UTF_8);
        assertEquals(expected1, actual);
    }

    @Test
    void example2_matches_golden_file() throws Exception {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,105,20000");
        proc.processOrderLine("10001,S,100,500");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("10004,S,103,100");
        proc.processOrderLine("10006,B,105,16000");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            // printOrders already prints trades to avoid duplication
            ob.printOrders(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String actual = baos.toString();

        Path p2 = Path.of("src/test/resources/golden/example2.golden");
        String expected2 = Files.readString(p2, StandardCharsets.UTF_8);
        assertEquals(expected2, actual);
    }

    @Test
    void iceberg_example_matches_golden_file() throws Exception {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();

        proc.processOrderLine("10000,B,98,25500");
        proc.processOrderLine("10005,S,101,20000");
        proc.processOrderLine("10002,S,100,10000");
        proc.processOrderLine("10001,S,100,7500");
        proc.processOrderLine("10003,B,99,50000");
        proc.processOrderLine("ice1,B,100,100000,10000");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            // printOrders already prints trades to avoid duplication
            ob.printOrders(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        String actual = baos.toString();

        Path p3 = Path.of("src/test/resources/golden/iceberg.golden");
        String expected3 = Files.readString(p3, StandardCharsets.UTF_8);
        assertEquals(expected3, actual);
    }
}






