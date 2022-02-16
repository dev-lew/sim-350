package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Request {
    private double arrivalTime;
    private double startTime;
    private double finishTime;
    private int requestID;
    private static int numCompletedMonitor = 0;
    private static int numRequests = 0;
    private static int completedRequests = 0;

    Request() {
        this.requestID = numRequests++;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public static void setNumCompletedMonitor(int numCompletedMonitor) {
        Request.numCompletedMonitor = numCompletedMonitor;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public int getRequestID() {
        return requestID;
    }

    public static int getNumRequests() {
        return numRequests;
    }

    public static int getCompletedRequests() {
        return completedRequests;
    }

    public static int getNumCompletedMonitor() {
        return numCompletedMonitor;
    }

    public static void incrementCompletedRequests() {
        completedRequests++;
    }
}

class Simulator {
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

        Request queuePeek() {
            return requestQueue.peek();
        }

        public int getQueueLength() {
            queueLength = requestQueue.size();
            return queueLength;
        }

        public double getTotalResponseTime() {
            return totalResponseTime;
        }

        public double getTotalBusyTime() {
            return totalBusyTime;
        }

        public int getTotalQueueLength() {
            return totalQueueLength;
        }

        public void setTotalResponseTime(double totalResponseTime) {
            this.totalResponseTime = totalResponseTime;
        }

        public void setTotalBusyTime(double totalBusyTime) {
            this.totalBusyTime = totalBusyTime;
        }

        public void setTotalQueueLength(int totalQueueLength) {
            this.totalQueueLength = totalQueueLength;
        }
    }

    private State simState;
    private Timeline simTimeline;
    private double avgArrivalRate;
    private double avgServiceTime;
    private double simDuration;

    private double time = 0;

    //Has no real function as of now
    void initState() {
        simState = new State();
    }

    void initTimeline() {
        simTimeline = new Timeline();
        simTimeline.addToTimeline(new Event(BIRTH, 0.0));
        simTimeline.addToTimeline(new Event(MONITOR, 0.0));
    }

    void updateStateVariables() {
        // Updates queue length
        simState.setTotalQueueLength(simState.getTotalQueueLength() +
                                     simState.getQueueLength());
        Request.setNumCompletedMonitor(Request.getNumCompletedMonitor() + 1);
    }

    void updateStateVariablesUponDeath(double responseTime,
                                       double busyTime) {
        simState.setTotalResponseTime(simState.getTotalResponseTime() +
                                      responseTime);
        simState.setTotalBusyTime(simState.getTotalBusyTime() +
                                  busyTime);
        Request.incrementCompletedRequests();
    }

    void printResult(String type, Request r) {
        double timestamp;

        if (type.equals("ARR")) {
            timestamp = r.getArrivalTime();
        } else if (type.equals("START")) {
            timestamp = r.getStartTime();
        } else if (type.equals("DONE")) {
            timestamp = r.getFinishTime();
        } else {
            String error = "Error when processing request.";
            throw new IllegalArgumentException(error);
        }
        DecimalFormat fmt = new DecimalFormat("#.000");
        System.out.println("R" + Integer.toString(r.getRequestID()) +
                           " " + type + ": " + fmt.format(timestamp));
    }

    void printStats() {
        double utilization = simState.getTotalBusyTime() / time;
        // double avgQueueLength = simState.getTotalQueueLength() / Request.getNumCompletedMonitor();
        // We do a little M/M/1 calculation
        double avgQueueLength = utilization / (1 - utilization);
        double avgResponseTime = simState.getTotalResponseTime() /
            Request.getCompletedRequests();

        DecimalFormat fmt = new DecimalFormat("#.000");
        System.out.println("UTIL: " + fmt.format(utilization));
        System.out.println("QLEN: " + fmt.format(avgQueueLength));
        System.out.println("TRESP: " + fmt.format(avgResponseTime));
    }

    void executeEvent(Event e) {
        double prevTime = e.getTimestamp();
        switch (e.getType()) {
        case BIRTH:
            Request r = new Request();
            r.setArrivalTime(time);
            printResult("ARR", r);
            simState.addRequest(r);

            if (simState.getQueueLength() <= 1) {
                r.setStartTime(time);
                printResult("START", r);
                simTimeline.addToTimeline(new Event(DEATH, prevTime +
                                                    Exp.getExp(1 / avgServiceTime)));
            }

            Event newBirth = new Event(BIRTH, prevTime + Exp.getExp(avgArrivalRate));
            simTimeline.addToTimeline(newBirth);
            break;
        case MONITOR:
            updateStateVariables();
            simTimeline.addToTimeline(new Event(MONITOR, prevTime +
                                                Exp.getExp(avgArrivalRate)));
            break;
        case DEATH:
            Request done = simState.dispatchRequest();
            done.setFinishTime(time);
            printResult("DONE", done);

            double responseTime = done.getFinishTime() - done.getArrivalTime();
            double busyTime = done.getFinishTime() - done.getStartTime();
            updateStateVariablesUponDeath(responseTime, busyTime);

            if (simState.getQueueLength() > 0) {
                Request head = simState.queuePeek();
                // Begin Processing
                head.setStartTime(time);
                printResult("START", head);
                simTimeline.addToTimeline(new Event(DEATH, prevTime + Exp.getExp(1 / avgServiceTime)));
            }
            break;
        }
    }

    void simulate(double simDuration, double avgArrivalrate,
                  double avgServiceTime) {
        this.avgArrivalRate = avgArrivalrate;
        this.avgServiceTime = avgServiceTime;
        this.simDuration = simDuration;
        initState();
        initTimeline();

        while (true) {
            Event e = simTimeline.popNext();
            time = e.getTimestamp();

            if (time >= simDuration)
                break;
            executeEvent(e);
        }

        printStats();
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTime = Double.parseDouble(args[2]);

        Simulator sim = new Simulator();
        sim.simulate(simTime, avgArrivalRate, avgServiceTime);

    }
}
