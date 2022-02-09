class Simulator {
    void simulate(double time) {

    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTime = Double.parseDouble(args[2]);
    }
}
