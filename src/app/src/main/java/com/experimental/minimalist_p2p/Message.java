package com.experimental.minimalist_p2p;

public class Message {
    private String text;
    private long timestamp;

    public Message(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
