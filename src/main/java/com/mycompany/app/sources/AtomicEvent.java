package com.mycompany.app.sources;

public class AtomicEvent {
    private String type;

    public AtomicEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "type: " + this.type;
    }
}