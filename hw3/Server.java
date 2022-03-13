package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Random;

class Server {
    // State of the queue, response, and busy times
    static class State {
        private int queueLength;
        private double totalResponseTime;
        private double totalBusyTime;
        private int totalQueueLength = 0;
        static int completedRequests = 0;

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
            this.queueLength = this.requestQueue.size();
            return queueLength;
        }

        public int getTotalQueueLength() {
            return totalQueueLength;
        }

        public ArrayDeque<Request> getRequestQueue() {
            return requestQueue;
        }

        public static int getCompletedRequests() {
            return completedRequests;
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
    private static int numMonitors = 0;
    private double time = 0;
    private static int numServers = 0;
    private Router router = new Router();
    static HashMap<Integer, Server> ServerList = new HashMap<>();

    Server(double avgServiceTime, double avgArrivalRate, Timeline eT) {
        this.avgServiceTime = avgServiceTime;
        this.avgArrivalRate = avgArrivalRate;
        this.eventTimeline = eT;
        this.name = numServers++;
        ServerList.put(this.name, this);
    }

    public int getName() {
        return name;
    }

    public State getServerState() {
        return serverState;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    static void incrementNumMonitors() {
        numMonitors++;
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
        Request.incrementTotalResponseTime(responseTime);
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

    void printNext(Event e, int serverName) {
        DecimalFormat fmt = new DecimalFormat("#.000");
        Request r = e.getRequest();

        System.out.println("R" + r.getRequestID() + " NEXT " +
                           serverName + ": " + fmt.format(e.getTimestamp()));
    }

    void printDone(Event e) {
        DecimalFormat fmt = new DecimalFormat("#.000");
        Request r = e.getRequest();
        System.out.println("R" + Integer.toString(r.getRequestID()) + " DONE " + this.name + ": "
                + fmt.format(e.getTimestamp()));
    }

    double[] buildStats() {
        // System.out.println("Completed Requests: " + this.serverState.completedRequests);
        // System.out.println(numMonitors);
        // System.out.println(serverState.totalQueueLength);
        double utilization = serverState.totalBusyTime / time;
        double avgQueueLength = serverState.totalQueueLength /
                numMonitors;
        double avgResponseTime = serverState.totalResponseTime /
               serverState.completedRequests;

        // DecimalFormat fmt = new DecimalFormat("#.000");
        // System.out.println("UTIL: " + fmt.format(utilization));
        // System.out.println("QLEN: " + fmt.format(avgQueueLength));
        // System.out.println("TRESP: " + fmt.format(avgResponseTime));
        double[] stats = { utilization, avgQueueLength, avgResponseTime };
        return stats;
    }

    // All generators take in their previous event to
    // keep track of time
    Event generateBirth(Event e) {
        double time = e.getTimestamp();
        return new Event(BIRTH, time +
                         Exp.getExp(avgArrivalRate),
                         this.name);
    }

    Event generateMonitor(Event e) {
        double time = e.getTimestamp();
        Event m = new Event(MONITOR, time +
                            Exp.getExp(avgArrivalRate),
                            this.name);
            this.eventTimeline.addToTimeline(m);
            return m;
    }

    Event generateDeath(Event e) {
        double time = e.getTimestamp();
        Event b =  new Event(DEATH, time +
                             Exp.getExp(1 / avgServiceTime),
                             0);
        b.setRequest(e.getRequest());
        return b;
    }

    Event generateDeath2(Event e) {
        double time = e.getTimestamp();
        Event b =  new Event(DEATH2, time +
                             Exp.getExp(1 / avgServiceTime),
                             1);
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

        e.setTargetServer(this.getName());
        eventTimeline.addToTimeline(generateBirth(e));
    }

    void executeMonitor(Event e) {
        numMonitors++;
        updateStateVariables();
        eventTimeline.addToTimeline(generateMonitor(e));
    }

    // We return e in case the result needs to be sent to another server
    Event executeDeath(Event e) {
        // for (Request r : this.serverState.getRequestQueue()) {
        //     System.out.println(r);
        // }
        Request done = serverState.dispatchRequest();

        // serverState.completedRequests++;
        // System.out.println(done);

        done.setFinishTime(time);
        // printResult("DONE", done);

        double rT = computeResponseTime(done);
        double bT = computeBusyTime(done);
        updateStateVariablesUponDeath(rT, bT);

        send(e, done);

        // TODO: Here we can print START before next
        if (serverState.getQueueLength() > 0) {
            Request head = serverState.peek();
            e.setRequest(head);
            // Begin Processing
            head.setStartTime(time);
            Request.incrementTotalWaitingTime(head.getStartTime() - head.getArrivalTime());
            printResult("START", head);
            eventTimeline.addToTimeline(generateDeath(e));
        }

        e.setRequest(done);
        e.setTargetServer(this.getName());
        return e;
    }

    void executeDeath2(Event e) {
        Request done = serverState.dispatchRequest();

        // serverState.completedRequests++;
        done.setFinishTime(time);


        double rT = computeResponseTime(done);
        double bT = computeBusyTime(done);
        updateStateVariablesUponDeath(rT, bT);

        send(e, done);
        if (serverState.getQueueLength() > 0) {
            Request head = serverState.peek();
            // Begin Processing
            e.setRequest(head);
            head.setStartTime(time);
            Request.incrementTotalWaitingTime(head.getStartTime() - head.getArrivalTime());
            printResult("START", head);
            eventTimeline.addToTimeline(generateDeath2(e));
        }

        e.setTargetServer(this.getName());
        e.setRequest(done);
    }

    // When a request is added to current server request queue
    void executeRequest(Event e) {
        time = e.getTimestamp();

        if (serverState.getQueueLength() == 1) {
            Request head = serverState.peek();
            e.setRequest(head);
            // Begin Processing
            head.setStartTime(time);
            Request.incrementTotalWaitingTime(head.getStartTime()- head.getArrivalTime());
            printResult("START", head);

            if (this.name == 0) {
                e.setTargetServer(0);
                eventTimeline.addToTimeline(generateDeath(e));
            }
            else {
                e.setTargetServer(1);
                eventTimeline.addToTimeline(generateDeath2(e));
            }
        }

    }

    Event processEvent(Event e) {
        Event ret = null;
        time = e.getTimestamp();
        // System.out.println(e);

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

    // Send request from event e with probability defined in map
    void send(Event e, Request rToSend) {
        assert e.getRequest() == rToSend;
        Server dest;
        Double prob;

        // TODO: Only works when there is one server in the router
        dest = (Server) this.router.getDestinationServers().toArray()[0];
        prob = this.router.getProb(dest);

        double roll = (new Random()).nextDouble();
        Request r = e.getRequest();

        if (r == null)
            throw new IllegalArgumentException("Bad event with no request");

        if (roll <= prob) {
            // If dest is null, we leave the system
            printNext(e, dest.getName());
            dest.getServerState().addRequest(r);
            e.setTargetServer(dest.getName());
            // New arrival time at new queue
            r.setArrivalTime(e.getTimestamp());
            dest.executeRequest(e);
        } else
            printDone(e);
    }
    //     return false;
    // }
}
