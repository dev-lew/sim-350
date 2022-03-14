package hw4;

class KQueueServer extends SimpleServer {
    /* New statistics */
    private int maxQueueLength;
    private int droppedRequests = 0;

    public KQueueServer(Timeline timeline, Double servTime, int maxQueueLength) {
        super(timeline, servTime);

        this.maxQueueLength = maxQueueLength;
    }

    @Override
    void receiveRequest(Event evt) {

        if (this.theQueue.size() == this.maxQueueLength) {
            this.droppedRequests++;
            return;
        }

        // Shadow EventGenerator
        Request r = evt.getRequest();
        r.moveTo(this);

        Request curRequest = evt.getRequest();

        curRequest.recordArrival(evt.getTimestamp());

        /*
         * Upon receiving the request, check the queue size and act accordingly
         */
        if (theQueue.isEmpty()) {
            if (dist == null)
                __startService(evt, curRequest);
            else
                __startService(evt, curRequest, dist);
        }

        theQueue.add(curRequest);
    }

    @Override
    void printStats(Double time) {
        if (this.name == null) {
            System.out.println("UTIL: " + busyTime/time);
            System.out.println("QLEN: " + cumulQ/snapCount);
            System.out.println("TRESP: " + cumulTq/servedReqs);
        } else {
            System.out.println(this.name + "UTIL " + ": " + busyTime/time);
            System.out.println(this.name + "QLEN " + ": " + cumulQ/snapCount);
            System.out.println(this.name + "TRESP: " + cumulTq / servedReqs);
            System.out.println(this.name + "DROPPED: " + this.droppedRequests);
        }
}
