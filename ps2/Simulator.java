package ps2;

import static ps2.Event.Type.*;

import java.util.ArrayDeque;

class Simulator {
    class State {
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


    State initState() {
        return new State();
    }

    Timeline initTimeline() {
        Timeline tl =  new Timeline();
        tl.addToTimeline(new Event(BIRTH, 0.0));
        tl.addToTimeline(new Event(MONITOR, 0.0));
        return tl;
    }

    void executeEvent(Event e) {
    }

    void simulate(double simDuration) {
        State s = initState();
        Timeline t = initTimeline();
        double time = 0;

        while (time < simDuration) {
            Event e = t.popNext();
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
