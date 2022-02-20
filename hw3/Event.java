package hw3;

class Event {
    enum Type {
        BIRTH, DEATH, MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    // May be necessary later
    private String targetServer;
    // Associate each Event with a request
    private Request r;
    private static int numEvents = 0;

    Event(Type type, double timestamp, Request r) {
        this.type = type;
        this.timestamp = timestamp;
        this.r = r;
        eventID = numEvents++;
    }

    Event(Type type, double timestamp, String targetServer) {
        this.type = type;
        this.timestamp = timestamp;
        this.r = r;
        this.eventID = numEvents++;
        this.targetServer = targetServer;
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

    public String getTargetServer() {
        return targetServer;
    }
}
