/*
  Delegates requests among a server with two processors
*/

package hw4;
import java.util.HashMap;
import java.util.Map;


class Delegator extends RoutingNode {
    private HashMap<DualProcessorServer, Double> routingTable = new HashMap<DualProcessorServer, Double>();

    public Delegator (Timeline timeline) {
        super(timeline);
    }

    // Field hiding messes the call up
    public void routeTo(DualProcessorServer next) {
        /*
         * Always assume that the same destination does not exist twice in the routing
         * table
         */
        assert !routingTable.containsKey(next);
        routeTo(next, new Double(.5));
    }

    public void routeTo(DualProcessorServer next, Double probability) {
        /*
         * Always assume that the same destination does not exist twice in the routing
         * table
         */
        assert !routingTable.containsKey(next);

        /* Add destination to routing table */
        routingTable.put(next, probability);

        /*
         * Perform a sanity check that the total probability has not exceeded 1
         */
        Double totalP = new Double(0);

        for (Map.Entry<DualProcessorServer, Double> entry : routingTable.entrySet()) {
            totalP += entry.getValue();
        }

        assert totalP <= 1;
    }

    @Override
    void receiveRequest(Event evt) {
        Request curRequest = evt.getRequest();


        // Always send to the processor (server) that is not busy, if possible
        for (Map.Entry<DualProcessorServer, Double> entry : routingTable.entrySet()) {
            DualProcessorServer server = entry.getKey();
            assert server != null;

            if (!server.isBusy) {
                System.out.println(evt.getRequest() + " FROM S" + evt.getSource().toString() + " TO S" +
                                   server.toString().charAt(0) + ": "
                                   + evt.getTimestamp());
                server.receiveRequest(evt);
                return;
            }
        }

        /* Find out where to route to with a dice roll */
        Double dice = Math.random();

        /* Identify the destination with CDF calculation */
        Double cumulP = new Double(0);

        EventGenerator nextHop = null;

        for (Map.Entry<DualProcessorServer, Double> entry : routingTable.entrySet()) {
            cumulP += entry.getValue();

            if (dice < cumulP) {
                nextHop = entry.getKey();
                break;
            }
        }


        assert nextHop != null;
        /* Print the occurrence of this event */
        if (!nextHop.toString().equals("")) {
            System.out.println(evt.getRequest() + " FROM S" + evt.getSource().toString() + " TO S" + nextHop + ": "
                    + evt.getTimestamp());
        }

        nextHop.receiveRequest(evt);
    }
}
