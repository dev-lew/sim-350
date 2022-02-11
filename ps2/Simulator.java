package ps2;

import static ps2.Event.Type.*;

import java.util.ArrayDeque;

class Simulator {
    class Request {
        private double arrivalTime;
        private double startTime;
        private double finishTime;

        public void setArrivalTime(double arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public void setFinishTime(double finishTime) {
            this.finishTime = finishTime;
        }
    }

    class State {
        private int queueLength;
        private double requestTime;
        private double busyTime;

        private ArrayDeque<Request> requestQueue = new ArrayDeque<>();

        void addRequest(Request r) {
            requestQueue.add(r);
        }

        void dispatchRequest() {
            requestQueue.removeLast();
        }

        public int getQueueLength() {
            queueLength = requestQueue.size();
            return queueLength;
        }
    }

    State simState;
    Timeline simTimeline;

    double time = 0;

    //Has no real function as of now
    void initState() {
        simState = new State();
    }

    void initTimeline() {
        simTimeline.addToTimeline(new Event(BIRTH, 0.0));
        simTimeline.addToTimeline(new Event(MONITOR, 0.0));
    }

    void executeEvent(Event e) {
        switch (e.getType()) {
        case BIRTH:
            Request r = new Request();
            r.setArrivalTime(time);
            break;
        case DEATH:
        }
    }

    void simulate(double simDuration) {
        initState();
        initTimeline();

        while (time < simDuration) {
            Event e = simTimeline.popNext();
            executeEvent(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTime = Double.parseDouble(args[2]);
    }
}
