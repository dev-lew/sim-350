package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Simulator {
    private double time = 0;

    void printStats() {
    }


    void simulate(double simDuration, double avgArrivalrate,
                  double avgServiceTimePrimary,
                  double avgServiceTimeSecondary) {
        Server primary = new Server(avgServiceTimePrimary);
        Server secondary = new Server(avgServiceTimeSecondary);

        while (true) {
            Event e = simTimeline.popNext();
            time = e.getTimestamp();

            if (time >= simDuration)
                break;

            // executeEvent() may generate a birth for the secondary server
            Optional<Event> passedEvent;

            if ((passedEvent = primary.executeEvent(e)).isPresent())
                secondary.executeEvent(passedEvent.get());

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
