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
        private int completedRequests = 0;

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

        public int getQueueLength() {
            queueLength = this.requestQueue.size();
            return queueLength;
        }

        public int getTotalQueueLength() {
            return totalQueueLength;
        }

        public ArrayDeque<Request> getRequestQueue() {
            return requestQueue;
        }
    }

    // Consider only integer based server names for now
    private int name;
    // Next server to send to, null if there is none
    private Server next;
    private State serverState = new State();
    private Timeline eventTimeline;
    private double avgArrivalRate;
    private double avgServiceTime;
    private int numMonitors = 1;
    private double time = 0;
    private static int numServers = 0;

    Server(double avgServiceTime, double avgArrivalRate, Timeline eT) {
        this.avgServiceTime = avgServiceTime;
        this.avgArrivalRate = avgArrivalRate;
        this.eventTimeline = eT;
        this.name = numServers++;
    }

    public int getName() {
        return name;
    }

    public State getServerState() {
        return serverState;
    }

    void updateStateVariables() {
        // Updates queue length
        serverState.totalQueueLength += serverState.getQueueLength();
    }

    void updateStateVariablesUponDeath(double responseTime,
                                       double busyTime) {
        serverState.totalResponseTime += responseTime;
        serverState.totalBusyTime += busyTime;
        serverState.completedRequests++;
    }

    void printResult(String type, Request r) {
        double timestamp;
        DecimalFormat fmt = new DecimalFormat("#.000");

        switch(type) {
        case "ARR":
            timestamp = r.getArrivalTime();
            System.out.println("R" + Integer.toString(r.getRequestID()) + " " + type +
                               ": " + fmt.format(timestamp));
            break;
        case "START":
            timestamp = r.getStartTime();
            System.out.println("R" + Integer.toString(r.getRequestID()) + " " + type +
                                " " + Integer.toString(this.name) + ": " + fmt.format(timestamp));
            break;
        default:
            String error = "Error when processing request.";
            throw new IllegalArgumentException(error);
        }
    }

    void printDone(Event e) {
        DecimalFormat fmt = new DecimalFormat("#.000");
        Request r = e.getRequest();
        System.out.println("R" + Integer.toString(r.getRequestID()) + " DONE " + this.name + ": "
                + fmt.format(e.getTimestamp()));
    }

    double[] buildStats() {
        System.out.println("Completed Requests: " + this.serverState.completedRequests);
        System.out.println(time);
        System.out.println(this.numMonitors);
        double utilization = serverState.totalBusyTime / time;
        double avgQueueLength = serverState.totalQueueLength /
                this.numMonitors;
        double avgResponseTime = serverState.totalResponseTime /
                this.serverState.completedRequests;

        // DecimalFormat fmt = new DecimalFormat("#.000");
        // System.out.println("UTIL: " + fmt.format(utilization));
        // System.out.println("QLEN: " + fmt.format(avgQueueLength));
        // System.out.println("TRESP: " + fmt.format(avgResponseTime));
        double[] stats = { utilization, avgQueueLength };
        return stats;
    }

    // All generators take in their previous event to
    // keep track of time
    Event generateBirth(Event e) {
        double time = e.getTimestamp();
        return new Event(BIRTH, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateMonitor(Event e) {
        double time = e.getTimestamp();
        return new Event(MONITOR, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateDeath(Event e) {
        double time = e.getTimestamp();
        Event b =  new Event(DEATH, time +
                         Exp.getExp(1 / avgServiceTime));
        b.setRequest(e.getRequest());
        return b;
    }

    Event generateDeath2(Event e) {
        double time = e.getTimestamp();
        Event b =  new Event(DEATH2, time +
                         Exp.getExp(1 / avgServiceTime));
        b.setRequest(e.getRequest());
        return b;
    }

    double computeResponseTime(Request r) {
        double fTime = r.getFinishTime();
        double aTime = r.getArrivalTime();

        if (fTime < 0 || aTime < 0 || aTime > fTime) {
            System.out.println(r);
            throw new IllegalArgumentException("Invalid request times");
        }

        return fTime - aTime;
    }

    double computeBusyTime(Request r) {
        double fTime = r.getFinishTime();
        double sTime = r.getStartTime();

        if (fTime < 0 || sTime < 0 || sTime > fTime) {
            System.out.println(r);
            throw new IllegalArgumentException("Invalid request times");
        }

        return fTime - sTime;
    }

    // If the server is in a pipeline, it may need to generate (return)
    // an event at a different server
    // TODO: executeEvent does not translate well with multiple servers
    // factor some stuff out

    //TODO: cant have a server time, pass it in when you need it from Simulator
    // Generate new events based on the timestamps of previous ones (to replace time)
    // Figure out send, if this server has a next field, a death event should send 
    // without printing to the next server

    void executeBirth(Event e) {
        Request r = new Request();
        r.setArrivalTime(e.getTimestamp());
        e.setRequest(r);
        printResult("ARR", r);
        serverState.addRequest(r);

        if (serverState.getQueueLength() == 1) {
            r.setStartTime(e.getTimestamp());
            printResult("START", r);
            eventTimeline.addToTimeline(generateDeath(e));
        }

        eventTimeline.addToTimeline(generateBirth(e));
    }

    void executeMonitor(Event e) {
        this.numMonitors++;
        updateStateVariables();
        eventTimeline.addToTimeline(generateMonitor(e));
    }

    // We return e in case the result needs to be sent to another server
    Event executeDeath(Event e) {
        // for (Request r : this.serverState.getRequestQueue()) {
        //     System.out.println(r);
        // }
        Request done = serverState.dispatchRequest();

        serverState.completedRequests++;
        // System.out.println(done);

        done.setFinishTime(time);
        // printResult("DONE", done);

        double rT = computeResponseTime(done);
        double bT = computeBusyTime(done);
        updateStateVariablesUponDeath(rT, bT);

        // TODO: Here we can print START before next
        if (serverState.getQueueLength() > 0) {
            Request head = serverState.peek();
            // Begin Processing
            head.setStartTime(time);
            printResult("START", head);
            eventTimeline.addToTimeline(generateDeath(e));
        }

        e.setRequest(done);
        return e;
    }

    void executeDeath2(Event e) {
        Request done = serverState.dispatchRequest();

        serverState.completedRequests++;
        done.setFinishTime(time);
        printDone(e);

        double rT = computeResponseTime(done);
        double bT = computeBusyTime(done);
        updateStateVariablesUponDeath(rT, bT);

        if (serverState.getQueueLength() > 0) {
            Request head = serverState.peek();
            // Begin Processing
            head.setStartTime(time);
            printResult("START", head);
            eventTimeline.addToTimeline(generateDeath2(e));
        }

        e.setRequest(done);
    }

    // When a request is added to current server request queue
    void executeRequest(Event e) {
        time = e.getTimestamp();
        serverState.completedRequests++;

        if (serverState.getQueueLength() == 1) {
            Request head = serverState.peek();
            // Begin Processing
            head.setStartTime(time);
            printResult("START", head);
            if (this.name == 0)
                eventTimeline.addToTimeline(generateDeath(e));
            else
                eventTimeline.addToTimeline(generateDeath2(e));
        }

    }

    Event processEvent(Event e) {
        Event ret = null;
        time = e.getTimestamp();
        System.out.println(e);

        switch (e.getType()) {
        case BIRTH:
            executeBirth(e);
            break;
        case MONITOR:
            executeMonitor(e);
            break;
        case DEATH:
          ret = executeDeath(e);
          break;
        case DEATH2:
            executeDeath2(e);
            break;
        }
        return ret;
    }
}
