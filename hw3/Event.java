package hw3;

class Event {
    enum Type {
        BIRTH, DEATH, MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    private String targetServer;
    private static int numEvents = 0;

    Event(Type type, double timestamp) {
        this.type = type;
        this.timestamp = timestamp;
        eventID = numEvents++;
    }

    Event(Type type, double timestamp, String targetServer) {
        this.type = type;
        this.timestamp = timestamp;
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
