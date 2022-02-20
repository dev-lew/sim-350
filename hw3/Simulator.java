package hw3;
import static hw3.Event.Type.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;

class Simulator {
    class Router {
        /*
          The map describes with what probability a given
          request exiting the server will go elsewhere
          The server will have to provide its own request timeline
         */
        private HashMap<Server, Double> routingMap =
                new HashMap<>();

        private addRoute(Server s, double p) {
            this.routingMap.put(s, Double(p));
        }

        // Send request from event e with probability defined in map
        private void send(Event e, Server source, Server dest) {
            assertNotNull("Server not in router",
                          this.routingMap.get(source));

            double prob = this.routingMap.get(source);
            Random roll = (new Random()).nextDouble();
            Request r = e.getRequest();

            if (!r)
                throw new IllegalArgumentException("Bad event with no request");

            if (roll <= prob) {
                System.out.println(Integer.toString(r.getRequestID()) +
                                   "NEXT " + Integer.toString(dest.getName()) +
                                   ":" + Double.toString(e.getTimestamp()));

                dest.generateBirthAndAddToTimeline();
            }
        }
    }
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
