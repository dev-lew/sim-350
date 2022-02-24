package hw3;
import static hw3.Event.Type.*;

import java.util.HashMap;
import java.util.Random;
import java.text.DecimalFormat;

class Simulator {
    class Router {
        /*
          The map describes with what probability a given
          request exiting the server will go elsewhere
          The server will have to provide its own request timeline
         */
        private HashMap<Server, Double> routingMap =
                new HashMap<>();

        private void addRoute(Server s, double p) {
            this.routingMap.put(s, p);
        }

        // Send request from event e with probability defined in map
        private boolean send(Event e, Server source, Server dest) {
            double prob = this.routingMap.get(source);
            double roll = (new Random()).nextDouble();
            Request r = e.getRequest();

            if (r == null)
                throw new IllegalArgumentException("Bad event with no request");

            if (roll <= prob) {
                // If dest is null, we leave the system
                if (dest == null) {
                    return true;
                }
                printNext(e, dest);
                dest.getServerState().addRequest(r);
                dest.executeRequest(e);
                return true;
            }
            return false;
        }
    }
    private double time = 0;
    private Timeline simTimeline = new Timeline();

    void printStats() {
    }

    void printNext(Event e, Server s) {
        DecimalFormat fmt = new DecimalFormat("#.000");
        Request r = e.getRequest();
        System.out.println("R" + Integer.toString(r.getRequestID()) +
                           " NEXT " + Integer.toString(s.getName()) +
                           ": " + fmt.format(e.getTimestamp()));
    }

    void printDone(Event e, Server s) {
        DecimalFormat fmt = new DecimalFormat("#.000");
        Request r = e.getRequest();
        System.out.println("R" + Integer.toString(r.getRequestID()) +
                           " DONE " + Integer.toString(s.getName()) +
                           ": " + fmt.format(e.getTimestamp()));
    }

    void initTimeline() {
        simTimeline.addToTimeline(new Event(BIRTH, 0.0));
        simTimeline.addToTimeline(new Event (MONITOR, 0.0));
    }

    void simulate(double simDuration, double avgArrivalrate,
                  double avgServiceTimePrimary,
                  double avgServiceTimeSecondary) {
        Server primary = new Server(avgServiceTimePrimary,
                                    avgArrivalrate, simTimeline);
        Server secondary = new Server(avgServiceTimeSecondary,
                                      avgArrivalrate, simTimeline);
        Router primSecondary = new Router();
        primSecondary.addRoute(primary, 1.0);
        primSecondary.addRoute(secondary, 1.0);

        initTimeline();

        while (true) {
            Event e = simTimeline.popNext();
            // System.out.println(e);
            time = e.getTimestamp();

            if (time >= simDuration)
                break;

            if (e.getType() == DEATH2) {
                secondary.processEvent(e);
                continue;
            }

            Event ret =  primary.processEvent(e);
            // If the event is a death, ret will be non-null
            if  (ret != null) {
                primSecondary.send(ret, primary, secondary);
                primSecondary.send(ret, secondary, null);
            }
        }

        printStats();
    }

    public static void main(String[] args) {
        if (args.length != 4)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTimePrim = Double.parseDouble(args[2]);
        double avgServiceTimeSec = Double.parseDouble(args[3]);

        Simulator sim = new Simulator();
        sim.simulate(simTime, avgArrivalRate, avgServiceTimePrim,
                     avgServiceTimeSec);

    }
}
