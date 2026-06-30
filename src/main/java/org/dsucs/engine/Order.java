package org.dsucs.engine;

import java.time.Instant;

public class Order {
    final String id;
    final Side side;
    final int price;
    int volume;  // Total remaining quantity (visible + hidden)
    int visibleVolume;    // Iceberg  case : Currently visible quantity on the book
    int peakSize;   // Iceberg  case : Max volume to show per replenishment
    Instant timestamp; // Iceberg  case : Added for Price-Time priority

    public Order(String id, Side side, int price, int volume, int peakSize) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.visibleVolume = volume;
        // If peakSize is null, it's a standard order; otherwise, it's an iceberg
        this.peakSize = peakSize;
        this.timestamp = Instant.now();
        replenish();
    }

    // Iceberg  case : evaluate currently visible quantity
    public void replenish() {
        if (isIceberg()) {
            if (this.peakSize > 0 && this.volume < this.peakSize) {
                this.peakSize = volume;
            }
            this.visibleVolume = Math.min(volume, peakSize);
            this.timestamp = Instant.now();
        }
    }

    public void reduceVolume(int amount) {
        this.volume -= amount;
        this.visibleVolume -= amount;
    }

    public boolean isIceberg() {
        return peakSize > 0;
    }
}