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

    public void routeTo(DualProcessorServer next) {
        /*
         * Always assume that the same destination does not exist twice in the routing
         * table
         */
        assert !routingTable.containsKey(next);
        routeTo(next, .5);
    }

    @Override
    void receiveRequest(Event evt) {
        Request curRequest = evt.getRequest();


        // Always send to the processor (server) that is not busy, if possible
        for (Map.Entry<DualProcessorServer, Double> entry : routingTable.entrySet()) {
            DualProcessorServer server = entry.getKey();

            if (!server.isBusy) {
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

        /* Print the occurrence of this event */
        if (!nextHop.toString().equals(""))
            System.out.println(evt.getRequest() + " NEXT " + nextHop + ": " + evt.getTimestamp());

        assert nextHop != null;

        nextHop.receiveRequest(evt);
    }
}
