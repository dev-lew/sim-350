package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Server {
    // State of the queue, response, and busy times
    class State {
        private int queueLength;
        private double totalResponseTime;
        private double totalBusyTime;
        private int totalQueueLength = 0;

        private ArrayDeque<Request> requestQueue = new ArrayDeque<>();

        void addRequest(Request r) {
            requestQueue.add(r);
        }

        Request dispatchRequest() {
            return requestQueue.poll();
        }

        Request peek() {
            return requestQueue.peek();
        }


    }

    // Consider only integer based server names for now
    private int name;
    private State serverState;
    private Timeline eventTimeline;
    private double avgArrivalRate;
    private double avgServiceTime;
    private int numMonitors = 0;
    private double time = 0;

    Server(double avgServiceTime, int name) {
        this.avgServiceTime = avgServiceTime;
        this.name = name;
    }

    void updateStateVariables() {
        // Updates queue length
        serverState.totalQueueLength += serverState.getQueueLength();
    }

    void updateStateVariablesUponDeath(double responseTime,
                                       double busyTime) {
        serverState.totalResponseTime += responseTime;
        serverState.totalBusyTime += busyTime;
        Request.incrementCompletedRequests();
    }

    void printResult(String type, Request r) {
        double timestamp;

        switch(type) {
        case "ARR":
            timestamp = r.getArrivalTime();
            break;
        case "START":
            timestamp = r.getStartTime();
            break;
        case "DONE":
            timestamp = r.getFinishTime();
            break;
        default:
            String error = "Error when processing request.";
            throw new IllegalArgumentException(error);
        }

        DecimalFormat fmt = new DecimalFormat("#.000");
        System.out.println("R" + Integer.toString(r.getRequestID()) +
                           " " + type + ": " + fmt.format(timestamp));
    }

    void printStats() {
        double utilization = serverState.totalBusyTime() / time;
        double avgQueueLength = serverState.totalQueueLength() /
            Request.getNumCompletedMonitor();
        double avgResponseTime = serverState.totalResponseTime() /
            Request.getCompletedRequests();

        DecimalFormat fmt = new DecimalFormat("#.000");
        System.out.println("UTIL: " + fmt.format(utilization));
        System.out.println("QLEN: " + fmt.format(avgQueueLength));
        System.out.println("TRESP: " + fmt.format(avgResponseTime));
    }

    Event generateBirth() {
        return new Event(BIRTH, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateMonitor() {
        numMonitors++;
        return new Event(MONITOR, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateDeath() {
        return new Event(DEATH, time +
                         Exp.getExp(1 / avgServiceTime));
    }

    // Used by the router
    void generateBirthAndAddToTimeline() {
        eventTimeline.addToTimeline(generateBirth());
    }

    double computeResponseTime(Request r) {
        fTime = r.getFinishTime();
        aTime = r.getArrivalTime();

        if (fTime <= 0 || aTime <= 0 || aTime >= fTime)
            throw new IllegalArgumentException("Invalid request times");

        return fTime - aTime;
    }

    double computeBusyTime(Request r) {
        fTime = r.getFinishTime();
        sTime = r.getStartTime();

        if (fTime <= 0 || sTime <= 0 || sTime >= fTime)
            throw new IllegalArgumentException("Invalid request times");

        return fTime - sTime;
    }

    // If the server is in a pipeline, it may need to generate (return)
    // an event at a different server
    Event executeEvent(Event e) {
        // If the Optional is empty, there is nothing to execute
        switch (e.getType()) {
        case BIRTH:
            Request r = new Request();
            r.setArrivalTime(time);
            printResult("ARR", r);
            serverState.addRequest(r);

            if (serverState.getQueueLength() <= 1) {
                r.setStartTime(time);
                printResult("START", r);
                eventTimeline.addToTimeline(generateDeath());
            }

            eventTimeline.addToTimeline(generateBirth());
            break;
        case MONITOR:
            updateStateVariables();
            eventTimeline.addToTimeline(generateMonitor());
            break;
        case DEATH:
            Request done = serverState.dispatchRequest();
            done.setFinishTime(time);
            printResult("DONE", done);

            double rT = computeResponseTime(done);
            double bT = computeBusyTime(done);
            updateStateVariablesUponDeath(rT, bT);

            if (serverState.getQueueLength() > 0) {
                Request head = serverState.peek();
                // Begin Processing
                head.setStartTime(time);
                printResult("START", head);
                eventTimeline.addToTimeline(generateDeath());
            }
            break;
        }
    }
}
