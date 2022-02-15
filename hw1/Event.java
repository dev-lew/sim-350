class Event {
    private String type;
    private double timestamp;
    private int eventIDA;
    private int eventIDB;
    private static int eventCountA = 0;
    private static int eventCountB = 0;

    Event(String type, double timestamp) {
        this.type = type;
        this.timestamp = timestamp;

        if (this.type.equals("A"))
            eventIDA = eventCountA++;
        else
            eventIDB = eventCountB++;
    }

    String getType() {
        return type;
    }

    double getTimestamp() {
        return timestamp;
    }

    public int getEventID() {
        if (type.equals("A"))
            return eventIDA;
        else
            return eventIDB;
    }
}
