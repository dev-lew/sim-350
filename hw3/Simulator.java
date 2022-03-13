package hw3;
import static hw3.Event.Type.*;


import hw3.Server.State;

import java.text.DecimalFormat;

//TODO: Change send to update target server
// refactor process event
class Simulator {
    private double time = 0;
    private Timeline simTimeline = new Timeline();

    void printStats(Server s, Server t) {
        double[] sStats = s.buildStats();
        double sUtil = sStats[0];
        double sQLen = sStats[1];

        double[] tStats = t.buildStats();
        double tUtil = tStats[0];
        double tQLen = tStats[1];

        double tresp = Request.getTotalResponseTime() / State.getCompletedRequests();
        double twait = Request.getTotalWaitingTime() / State.getCompletedRequests();

        DecimalFormat fmt = new DecimalFormat("#.000");
        System.out.println("UTIL 0: " + fmt.format(sUtil));
        System.out.println("UTIL 1: " + fmt.format(tUtil));
        System.out.println("QLEN 0: " + fmt.format(sQLen));
        System.out.println("QLEN 1: " + fmt.format(tQLen));
        System.out.println("TRESP: " +  tresp);
        System.out.println("TWAIT: " + twait);



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
        simTimeline.addToTimeline(new Event(BIRTH, 0.0, 0));
        simTimeline.addToTimeline(new Event (MONITOR, 0.0, 0));
    }

    Event delegate(Event e, Server p, Server s) {
        Event ret = null;
        if (e.getType() == MONITOR) {
            executeMonitor(p, s, e);
            return ret;
        }
        switch (e.getTargetServer()) {
        case 0:
            ret = p.processEvent(e);
            break;
        case 1:
            ret = s.processEvent(e);
            break;
        default:
            throw new IllegalArgumentException("Error in delegate");

        }
        return ret;

    }

    void executeMonitor(Server s, Server t, Event e) {
        s.updateStateVariables();
        t.updateStateVariables();
        Server.incrementNumMonitors();
        s.generateMonitor(e);
    }

    void simulate(double simDuration, double avgArrivalrate,
                  double avgServiceTimePrimary,
                  double avgServiceTimeSecondary,
                  double probPrimExit, double probSecondReturn) {
        Server primary = new Server(avgServiceTimePrimary,
                                    avgArrivalrate, simTimeline);
        Server secondary = new Server(avgServiceTimeSecondary,
                                      avgArrivalrate, simTimeline);
        Router primaryR = new Router();
        Router secR = new Router();

        primaryR.addRoute(secondary, 1 - probPrimExit);
        secR.addRoute(primary, probSecondReturn);

        primary.setRouter(primaryR);
        secondary.setRouter(secR);

        initTimeline();

        while (true) {
            Event e = simTimeline.popNext();
            // System.out.println(e);
            time = e.getTimestamp();

            if (time >= simDuration)
                break;

            delegate(e, primary, secondary);

            // // If the event is a death, ret will be non-null
            // if  (ret != null) {
            //     if (!primSecondary.send(ret, primary, secondary)) {
            //         // Exit server
            //         printDone(e, primary);
            //         primSecondary.send(ret, primary, null);
            //     }
            //     if (!primSecondary.send(ret, secondary, primary)) {
            //         // Exit server
            //         printDone(e, secondary);
            //         primSecondary.send(ret, secondary, null);
            //     }
            // }
        }

        printStats(primary, secondary);
    }

    public static void main(String[] args) {
        if (args.length != 6)
            throw new IllegalArgumentException("Invalid number of arguments");

        double simTime = Double.parseDouble(args[0]);
        double avgArrivalRate = Double.parseDouble(args[1]);
        double avgServiceTimePrim = Double.parseDouble(args[2]);
        double avgServiceTimeSec = Double.parseDouble(args[3]);
        double probPrimExit = Double.parseDouble(args[4]);
        double probSecondReturn = Double.parseDouble(args[5]);


        Simulator sim = new Simulator();
        sim.simulate(simTime, avgArrivalRate, avgServiceTimePrim,
                     avgServiceTimeSec, probPrimExit, probSecondReturn);

    }
}
