class Simulator {
    class State {
        private int queueLength;
        private double requestTime;
        private double busyTime;
    }

    void simulate(double time) {

    }

    State initState() {
        return new State();
    }

    Timeline initTimeline() {
        Timeline tl =  new Timeline();
        return tl;
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTime = Double.parseDouble(args[2]);
    }
}
