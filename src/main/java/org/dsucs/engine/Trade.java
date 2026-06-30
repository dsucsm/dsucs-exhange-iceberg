package org.dsucs.engine;

import java.time.Instant;

public class Trade {
    final String buyOrderId;
    final String sellOrderId;
    final int price;
    final int quantity;
    final Instant timestamp;

    public Trade(String buyOrderId, String sellOrderId, int price, int quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }
}