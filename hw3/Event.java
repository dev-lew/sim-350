package hw3;

class Event {
    enum Type {
        BIRTH, DEATH, DEATH2, MONITOR
    }

    private Type type;
    private double timestamp;
    private int eventID;
    // May be necessary later
    private int targetServer;
    /* An event may have a request associated with it
       If it does, then the request has come from another
       server.
    */
    private Request request = null;
    private static int numEvents = 0;

    Event(Type type, double timestamp, int targetServer) {
        this.type = type;
        this.timestamp = timestamp;
        this.targetServer = targetServer;
        eventID = numEvents++;
    }

    //TODO: Add target server for process event
    // This will allow for better flow in simulate
    Event (Type type, double timestamp, Request r, int targetServer) {
        this.type = type;
        this.timestamp = timestamp;
        this.request = r;
        this.targetServer = targetServer;
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

    public int getTargetServer() {
        return targetServer;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setTargetServer(int targetServer) {
        this.targetServer = targetServer;
    }

    @Override
    public String toString() {
        return "Event [eventID=" + eventID + ", request=" + request +
            ", targetServer=" + targetServer + ", timestamp=" +
                 timestamp + ", type=" + type + "]";
    }

}
