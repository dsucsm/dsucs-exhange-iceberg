package org.dsucs.engine;

import org.dsucs.Util;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dsucs.Util.isNullEmpty;

public class OrderProcessor {
    private OrderBook book = OrderBookImpl.getInstance();

    public void processOrderFile(String file) {
        if(!Util.isFilePathValid(file)){
            return;
        }

        // Use BufferedReader for lower memory overhead than Scanner
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                processOrderLine(line);
            }

            PrintOption opt = PrintOption.COMPACT;
            if(!isNullEmpty(System.getProperty("printOption")) && System.getProperty("printOption").toUpperCase().matches(PrintOption.COMPREHENSIVE.name())){
                opt = PrintOption.COMPREHENSIVE;
            }
            book.printOrders(opt);
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    public void processOrderLine(String line) {
        try {
            int start = 0;
            int end = line.indexOf(',', start);
            if (end == -1) return; // Malformed: No ID/Side separator

            // 1. Order ID
            String id = line.substring(start, end).trim();

            // 2. Side (B/S)
            start = end + 1;
            end = line.indexOf(',', start);
            if (end == -1) return; // Malformed: No Side/Price separator
            Side side = (line.charAt(start) == 'B' || line.charAt(start) == 'b') ? Side.BUY : Side.SELL;

            // 3. Price
            start = end + 1;
            end = line.indexOf(',', start);
            if (end == -1) return; // Malformed: No Price/Volume separator
            int price = parseIntFast(line, start, end);

            // 4. Volume & Peak Size
            start = end + 1;
            end = line.indexOf(',', start);
            int volume;
            int peakSize = 0;

            if (end == -1) {
                // Standard order (no 5th column)
                volume = parseIntFast(line, start, line.length());
            } else {
                // Iceberg order
                volume = parseIntFast(line, start, end);
                start = end + 1;
                peakSize = parseIntFast(line, start, line.length());
            }
            if (volume == 0) return;

            book.processOrder(new Order(id, side, price, volume, peakSize));

        } catch (Exception e) {
            // Log error but do not crash the engine
            System.err.println("Malformed line skipped: " + line);
        }
    }

    // Faster than Integer.parseInt as it avoids additional string validation/trimming
    private int parseIntFast(String s, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
            }
        }
        return result;
    }
}
