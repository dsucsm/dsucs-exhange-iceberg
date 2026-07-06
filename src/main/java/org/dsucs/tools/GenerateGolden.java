package org.dsucs.tools;

import org.dsucs.engine.OrderBookImpl;
import org.dsucs.engine.OrderProcessor;
import org.dsucs.engine.PrintOption;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class GenerateGolden {
    public static void main(String[] args) throws Exception {
        generateExample1();
        generateExample2();
        generateIceberg();
        System.out.println("Golden files generated under src/test/resources/golden");
    }

    private static void writeFile(String path, String content) throws Exception {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void generateExample1() throws Exception {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();
        ob.deleteAll();
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

        writeFile("src/test/resources/golden/example1.golden", baos.toString());
    }

    private static void generateExample2() throws Exception {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();
        ob.deleteAll();
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
            ob.printOrders(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        writeFile("src/test/resources/golden/example2.golden", baos.toString());
    }

    private static void generateIceberg() throws Exception {
        OrderProcessor proc = new OrderProcessor();
        OrderBookImpl ob = OrderBookImpl.getInstance();
        ob.deleteAll();
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
            ob.printOrders(PrintOption.COMPACT);
        } finally {
            System.setOut(oldOut);
        }

        writeFile("src/test/resources/golden/iceberg.golden", baos.toString());
    }
}

