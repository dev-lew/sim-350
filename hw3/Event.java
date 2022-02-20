package hw3;

import java.util.Optional;

class Event {
    enum Type {
        BIRTH, DEATH, MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    // May be necessary later
    private String targetServer;
    /* An event may have a request associated with it
       If it does, then the request has come from another
       server.
    */
    private Request request = null;
    private static int numEvents = 0;

    Event(Type type, double timestamp) {
        this.type = type;
        this.timestamp = timestamp;
        eventID = numEvents++;
    }

    Event (Type type, double timestamp, Request r) {
        this.type = type;
        this.timestamp = timestamp;
        this.request = r;
        eventID = numEvents++;
        
    }


    Type getType() {
        return type;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public int getEventID() {
        return eventID;
    }

    public static int getNumEvents() {
        return numEvents;
    }

    public Request getRequest() {
        return request;
    }

    public String getTargetServer() {
        return targetServer;
    }
}
