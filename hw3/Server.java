package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Server {
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

    Event generateBirth() {
        return new Event(BIRTH, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateMonitor() {
        return new Event(MONITOR, time +
                         Exp.getExp(avgArrivalRate));
    }

    Event generateDeath() {
        return new Event(DEATH, time +
                         Exp.getExp(1 / avgServiceTime));
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

    void executeEvent(Event e) {
        switch (e.getType()) {
        case BIRTH:
            Request r = new Request();
            r.setArrivalTime(time);
            printResult("ARR", r);
            simState.addRequest(r);

            if (simState.getQueueLength() <= 1) {
                r.setStartTime(time);
                printResult("START", r);
                simTimeline.addToTimeline(generateDeath());
            }

            simTimeline.addToTimeline(generateBirth());
            break;
        case MONITOR:
            updateStateVariables();
            simTimeline.addToTimeline(generateMonitor());
            break;
        case DEATH:
            Request done = simState.dispatchRequest();
            done.setFinishTime(time);
            printResult("DONE", done);

            double rT = computeResponseTime(done);
            double bT = computeBusyTime(done);
            updateStateVariablesUponDeath(rT, bT);

            if (simState.getQueueLength() > 0) {
                Request head = simState.queuePeek();
                // Begin Processing
                head.setStartTime(time);
                printResult("START", head);
                simTimeline.addToTimeline(generateDeath());
            }
            break;
        }
    }
}
