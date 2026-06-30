package org.dsucs.engine;

public interface OrderBook {
    void processOrder(Order incoming);

    void printOrders(PrintOption opt);

    void printTrades(PrintOption opt);

    void deleteAll();
}
