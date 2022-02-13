package ps2;

import static ps2.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Simulator {
    class Request {
        private double arrivalTime;
        private double startTime;
        private double finishTime;
        private int requestID;
        private static int numRequests = 0;

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
    }

    class State {
        private int queueLength;
        private double totalResponseTime;
        private double totalBusyTime;

        private ArrayDeque<Request> requestQueue = new ArrayDeque<>();

        void addRequest(Request r) {
            requestQueue.add(r);
        }

        Request dispatchRequest() {
            return requestQueue.poll();
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

        public void setTotalResponseTime(double totalResponseTime) {
            this.totalResponseTime = totalResponseTime;
        }

        public void setTotalBusyTime(double totalBusyTime) {
            this.totalBusyTime = totalBusyTime;
        }
    }

    private State simState;
    private Timeline simTimeline;
    private double avgArrivalRate;
    private double avgServiceTime;

    private double time = 0;

    //Has no real function as of now
    void initState() {
        simState = new State();
    }

    void initTimeline() {
        simTimeline.addToTimeline(new Event(BIRTH, 0.0));
        simTimeline.addToTimeline(new Event(MONITOR, 0.0));
    }

    void updateStateVariables() {
        // Updates queue length
        simState.getQueueLength();
    }

    void updateStateVariablesUponDeath(double responseTime,
                                       double busyTime) {
        simState.setTotalResponseTime(simState.getTotalResponseTime() +
                                      responseTime);
        simState.setTotalBusyTime(simState.getTotalBusyTime() +
                                  busyTime);
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
                           type + ": " + fmt.format(timestamp));
    }

    void executeEvent(Event e) {
        switch (e.getType()) {
        case BIRTH:
            Request r = new Request();
            r.setArrivalTime(time);
            simState.addRequest(r);
            printResult("ARR", r);

            if (simState.getQueueLength() == 0) {
                printResult("START", r);
                r.setStartTime(time);
                simTimeline.addToTimeline(new Event(DEATH, time +
                                                    Exp.getExp(avgServiceTime)));
            }
            Event newBirth = new Event(BIRTH, time + Exp.getExp(avgArrivalRate));
            simTimeline.addToTimeline(newBirth);

            break;
        case MONITOR:
            updateStateVariables();
            simTimeline.addToTimeline(new Event(MONITOR, time +
                                                Exp.getExp(avgArrivalRate)));
            break;
        case DEATH:
            Request done = simState.dispatchRequest();
            done.setFinishTime(time);
            printResult("DONE", done);

            double responseTime = done.getFinishTime() - done.getArrivalTime();
            double busyTime = done.getFinishTime() - done.getStartTime();
            updateStateVariablesUponDeath(responseTime, busyTime);
            break;
        }
    }

    void simulate(double simDuration, double avgArrivalrate,
                  double avgServiceTime) {
        this.avgArrivalRate = avgArrivalrate;
        this.avgServiceTime = avgServiceTime;
        initState();
        initTimeline();

        while (true) {
            Event e = simTimeline.popNext();
            time += e.getTimestamp();

            if (time >= simDuration)
                break;
            executeEvent(e);
        }
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
