package hw2;

class Event {
    enum Type {
        BIRTH, DEATH, MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    private static int numEvents = 0;
    private static int numMonitors = 0;

    Event(Type type, double timestamp) {
        this.type = type;

        if (this.type == Event.Type.MONITOR)
            ++numMonitors;

        this.timestamp = timestamp;
        eventID = numEvents++;
    }

    Type getType() {
        return type;
    }

    double getTimestamp() {
        return timestamp;
    }

    public static int getNumMonitors() {
        return numMonitors;
    }

    public int getEventID() {
        return eventID;
    }
}
