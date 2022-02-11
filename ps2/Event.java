package ps2;

class Event {
    enum Type {
        BIRTH,
        DEATH,
        MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    private static int numEvents = 0;

    Event(Type type, double timestamp) {
        this.type = type;
        this.timestamp = timestamp;
        eventID = numEvents++;
    }

    Type getType() {
        return type;
    }

    double getTimestamp() {
        return timestamp;
    }

    public int getEventID() {
        return eventID;
    }
}