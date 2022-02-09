class Event {
    private String type;
    private double timestamp;

    Event(String type, double timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    String getType() {
        return type;
    }

    double getTimestamp() {
        return timestamp;
    }
}
