package com.akshatagarwal1265.test1;

/**
 * Created by Akshat on 20-09-2017.
 */

public class Message {

    private String message, type, from;
    private long timestamp;
    private boolean seen;

    public Message(){}

    public Message(String message, boolean seen, long timestamp, String type, String from) {
        this.message = message;
        this.seen = seen;
        this.timestamp = timestamp;
        this.type = type;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return message+type+from+timestamp+seen;
    }
}
