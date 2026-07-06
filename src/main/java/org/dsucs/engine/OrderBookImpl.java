package org.dsucs.engine;

import java.util.*;

import static org.dsucs.Util.isNullEmpty;

public class OrderBookImpl implements OrderBook {
    // Bids: Sorted descending (highest price first)
    private final TreeMap<Integer, Queue<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    // Asks: Sorted ascending (lowest price first)
    private final TreeMap<Integer, Queue<Order>> asks = new TreeMap<>();
    private final List<Trade> recentTrades = new ArrayList<>(); // for printing the recent trades
    private final List<Trade> allTrades = new ArrayList<>(); // for auditing of all trades

    private OrderBookImpl() {
    }

    public static OrderBookImpl getInstance() {
        return OrderBookImpl.Holder.INSTANCE;
    }

    public void processOrder(Order incoming) {
        // clear/hide recent trades for audit/printing (Note: all trades will be available until explicitly deleted)
        recentTrades.clear();

        // 1. Try to Trade
        matchOrder(incoming);

        // 2. If volume remains, add to book
        if (incoming.volume > 0) {
            TreeMap<Integer, Queue<Order>> sideMap = (incoming.side == Side.BUY) ? bids : asks;
            sideMap.computeIfAbsent(incoming.price, k -> new LinkedList<>()).add(incoming);
        }
    }

    private void matchOrder(Order incoming) {
        TreeMap<Integer, Queue<Order>> oppositeSide = (incoming.side == Side.BUY) ? asks : bids;
        Iterator<Map.Entry<Integer, Queue<Order>>> it = oppositeSide.entrySet().iterator();
        while (it.hasNext() && incoming.volume > 0) {
            Map.Entry<Integer, Queue<Order>> entry = it.next();
            int bestPrice = entry.getKey();

            // Check price compatibility
            if (incoming.side == Side.BUY && incoming.price < bestPrice) break;
            if (incoming.side == Side.SELL && incoming.price > bestPrice) break;

            Queue<Order> queue = entry.getValue();
            while (!queue.isEmpty() && incoming.volume > 0) {
                Order resting = queue.peek();
                int tradeVol = Math.min(incoming.volume, resting.visibleVolume);
                incoming.reduceVolume(tradeVol);
                resting.reduceVolume(tradeVol);
                if (resting.visibleVolume == 0) {
                    queue.poll(); // Current peak is gone
                    if (resting.volume > 0) {
                        // If visible is depleted but total remains, replenish (Iceberg logic)
                        resting.replenish();
                        // LSE Rule: Replenished peak goes to the back of the price level queue
                        queue.add(resting);
                    }
                }

                // For printing /auditing of matched orders
                String buyOrderId = incoming.side == Side.BUY ? incoming.id : resting.id;
                String sellOrderId = incoming.side == Side.SELL ? incoming.id : resting.id;
                boolean aggressorIsBuy = incoming.side == Side.BUY;
                recentTrades.add(new Trade(buyOrderId, sellOrderId, bestPrice, tradeVol, aggressorIsBuy));
                allTrades.add(new Trade(buyOrderId, sellOrderId, bestPrice, tradeVol, aggressorIsBuy));
            }
            if (queue.isEmpty()) {
                it.remove();
            }
        }
        incoming.replenish();
    }

    /*
     * CAUTION : will cleanup all live data (orders + trades) from the order book
     * */
    @Override
    public void deleteAll() {
        bids.clear();
        asks.clear();
        recentTrades.clear();
        allTrades.clear();
    }

    // Package-visible accessors for testing / inspection
    List<Trade> getRecentTrades() {
        return Collections.unmodifiableList(recentTrades);
    }

    List<Trade> getAllTrades() {
        return Collections.unmodifiableList(allTrades);
    }

    /**
     * Returns a snapshot of the bids map: price -> list of orders (in queue order).
     * Package-private for testing/inspection.
     */
    java.util.Map<Integer, java.util.List<Order>> getBidsSnapshot() {
        java.util.Map<Integer, java.util.List<Order>> snap = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<Integer, Queue<Order>> e : bids.entrySet()) {
            snap.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return java.util.Collections.unmodifiableMap(snap);
    }

    /**
     * Returns a snapshot of the asks map: price -> list of orders (in queue order).
     * Package-private for testing/inspection.
     */
    java.util.Map<Integer, java.util.List<Order>> getAsksSnapshot() {
        java.util.Map<Integer, java.util.List<Order>> snap = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<Integer, Queue<Order>> e : asks.entrySet()) {
            snap.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return java.util.Collections.unmodifiableMap(snap);
    }

    // For printing/displaying orders
    private List<Order> listOrders(TreeMap<Integer, Queue<Order>> book) {
        List<Order> orders = new ArrayList<>();
        for (Queue<Order> q : book.values()) {
            orders.addAll(q);
        }
        return orders;
    }

    @Override
    public void printOrders(PrintOption opt) {
        List<Order> bidRows = listOrders(bids);
        List<Order> askRows = listOrders(asks);
        String iceChar = isNullEmpty(System.getProperty("iceChar")) ? "i" : System.getProperty("iceChar");
        int maxRows = Math.max(bidRows.size(), askRows.size());
        printTrades(opt);
        switch (opt) {
            case COMPACT:
                // Exact README compact formatting:
                // Header: "Buyers             | Sellers"
                System.out.println("Buyers             | Sellers");
                for (int i = 0; i < maxRows; i++) {
                    String left;
                    if (i < bidRows.size()) {
                        Order b = bidRows.get(i);
                        String vol = String.format("%,d", b.visibleVolume);
                        String ice = b.isIceberg() ? iceChar : "";
                        // left: volume (right aligned width 9 incl commas) + space + price (right aligned width 3) + optional ice
                        left = String.format("%9s %3d%s", vol, b.price, ice);
                    } else {
                        left = String.format("%13s", "");
                    }

                    String right;
                    if (i < askRows.size()) {
                        Order a = askRows.get(i);
                        String vol = String.format("%,d", a.visibleVolume);
                        String ice = a.isIceberg() ? iceChar : "";
                        // right: price (right aligned 3) + space + volume (right aligned 9 incl commas) + ice
                        right = String.format("%3d %9s%s", a.price, vol, ice);
                    } else {
                        right = "";
                    }

                    System.out.println(String.format("%13s | %s", left, right));
                }
                break;
            case COMPREHENSIVE:
                System.out.printf("%-10s %10s %10s %10s %8s %1s%30s | %-30s%1s %-8s %10s %10s %10s %10s\n", "", "", "", "", "", "", "Bids (Taker Buying)", "Asks (Maker Selling)", "", "", "", "", "", "");
                System.out.printf("%-30s %-10s %10s %10s %10s%-1s %8s | %-8s %10s%1s %10s %10s %10s %30s\n", "Time", "ID", "Peak", "VolAct", "Volume", "", "Price", "Price", "Volume", "", "VolAct", "Peak", "ID", "Time");
                for (int i = 0; i < maxRows; i++) {
                    String bidPart = (i < bidRows.size()) ? String.format("%-30s %-10s %,10d %,10d %,10d%-1s %8d", bidRows.get(i).timestamp, bidRows.get(i).id, bidRows.get(i).peakSize, bidRows.get(i).volume, bidRows.get(i).visibleVolume, bidRows.get(i).isIceberg() ? iceChar : " ", bidRows.get(i).price) : String.format("%-84s", "");
                    String askPart = (i < askRows.size()) ? String.format("%-8d %,10d%1s %,10d %,10d %10s %30s", askRows.get(i).price, askRows.get(i).visibleVolume, askRows.get(i).isIceberg() ? iceChar : " ", askRows.get(i).volume, askRows.get(i).peakSize, askRows.get(i).id, askRows.get(i).timestamp) : "";
                    System.out.println(bidPart + " | " + askPart);
                }
                break;
        }
    }

    @Override
    public void printTrades(PrintOption opt) {
        switch (opt) {
            case COMPACT: // Recent trades only
                if (recentTrades.isEmpty()) return;
                String aggFlag = System.getProperty("lse.aggregate");
                boolean aggregate = aggFlag != null && aggFlag.equalsIgnoreCase("true");
                if (aggregate) {
                    // Group by resting sell id and resting buy id to allow symmetric aggregation.
                    // Group trades by the resting participant id (the side that was NOT aggressor)
                    Map<String, List<Trade>> restingGroups = new LinkedHashMap<>();
                    for (Trade m : recentTrades) {
                        String restingId = m.aggressorIsBuy ? m.sellOrderId : m.buyOrderId;
                        restingGroups.computeIfAbsent(restingId, k -> new ArrayList<>()).add(m);
                    }

                    Set<String> printedComposite = new HashSet<>();

                    // Aggregate repeated resting participants using LSE-style 5TG single-message format
                    // Format: 5TG <restingId>: <buyOrderId>,<sellOrderId>,<price>,<qty>; <...>
                    for (Map.Entry<String, List<Trade>> e : restingGroups.entrySet()) {
                        List<Trade> list = e.getValue();
                        if (list.size() > 1) {
                            System.out.print("5TG " + e.getKey() + ": ");
                            for (int i = 0; i < list.size(); i++) {
                                Trade t = list.get(i);
                                System.out.print(String.format("%s,%s,%d,%d", t.buyOrderId, t.sellOrderId, t.price, t.quantity));
                                if (i < list.size() - 1) System.out.print("; ");
                                printedComposite.add(t.buyOrderId + ":" + t.sellOrderId + ":" + t.price + ":" + t.quantity);
                            }
                            System.out.print("\\n");
                        }
                    }

                    // Print remaining individual trades that were not part of any aggregated group
                    for (Trade t : recentTrades) {
                        String key = t.buyOrderId + ":" + t.sellOrderId + ":" + t.price + ":" + t.quantity;
                        if (!printedComposite.contains(key)) {
                            System.out.printf("trade %s,%s,%d,%d\n", t.buyOrderId, t.sellOrderId, t.price, t.quantity);
                        }
                    }
                } else {
                    for (Trade m : recentTrades) {
                        System.out.printf("trade %s,%s,%d,%d\n", m.buyOrderId, m.sellOrderId, m.price, m.quantity);
                    }
                }
                break;
            case COMPREHENSIVE: // All trades
                System.out.printf("%-30s ,%s,%s,%s,%s\n", "Time", "Placer", "Provider", "Price", "Volume");
                if (allTrades.isEmpty()) return;
                for (Trade m : allTrades) {
                    System.out.printf("%-30s ,%s,%s,%d,%d\n", m.timestamp, m.buyOrderId, m.sellOrderId, m.price, m.quantity);
                }
                break;
        }
    }

    private static class Holder {
        private static final OrderBookImpl INSTANCE = new OrderBookImpl();
    }
}
