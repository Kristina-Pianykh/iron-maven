package com.mycompany.app.sources;


import java.time.Instant;

public class AtomicEvent {
    private String type;
    private Long timestamp;


    public AtomicEvent(String type) {
        this.type = type;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString() {
        String time = Instant.ofEpochSecond(this.timestamp).toString();
        return "type: " + this.type + ", timestamp: " + time;
    }
}