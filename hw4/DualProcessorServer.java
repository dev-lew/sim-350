package hw4;

import java.util.LinkedList;

class DualProcessorServer extends SimpleServer {
    public boolean isBusy = false;
    private Request processing;

    /* The queue and some statistics are now shared */
    private static LinkedList<Request> theQueue = new LinkedList<>();

    private static double cumulQ = 0;
    private static double cumulTq = 0;
    private static int snapCount = 0;
    private static double servedReqs = 0;


    public DualProcessorServer(Timeline timeline, Double servTime) {
        super(timeline, servTime);
    }


    // To prevent the servers from scheduling a death event for the same requests
    // remove the head of the queue and store it
    public void __startService(Event evt, Request curRequest) {
        this.isBusy = true;
        this.processing = curRequest;

        assert curRequest == this.processing;
        Event nextEvent = new Event(EventType.DEATH, curRequest, evt.getTimestamp() + Exp.getExp(1 / this.servTime),
                this);

        curRequest.recordServiceStart(evt.getTimestamp());
        cumulTw += curRequest.getServiceStart() - curRequest.getArrival();

        /* Print the occurrence of this event */
        System.out.println(
                curRequest + " START" + (this.name != null ? "S" + this.name : "") + ": " + evt.getTimestamp());

        super.getParentTimeline().addEvent(nextEvent);
    }

    @Override
    void releaseRequest(Event evt) {
        this.isBusy = false;

        /* What request we are talking about? */
        Request curRequest = evt.getRequest();

        assert curRequest == this.processing;
        System.out.println(curRequest + " DONE" + (this.name != null ? "S" + this.name : "") + ": " + evt.getTimestamp());

        curRequest.recordDeparture(evt.getTimestamp());
	
        /* Update busyTime */
        this.busyTime += curRequest.getDeparture() - curRequest.getServiceStart();

        /* Update cumulative response time at this server */
        cumulTq += curRequest.getDeparture() - curRequest.getArrival();
	
        /* Update number of served requests */
        servedReqs++;

        assert super.getNext() != null;
        System.out.println(evt.getRequest() + " FROM S" + evt.getSource().toString().charAt(0) + " TO S"
                           + super.getNext() + ": " + evt.getTimestamp());

        super.getNext().receiveRequest(evt);
	
        /* Any new request to put into service?  */
        if(!theQueue.isEmpty()) {
            // Remove the request so the second processor doesn't get confused
            Request nextRequest = theQueue.removeFirst();
            this.isBusy = true;
            this.processing = nextRequest;

            if (dist == null)
                __startService(evt, nextRequest);
            else
                __startService(evt, nextRequest, dist);
        }
        this.processing = null;
    }

    @Override
    void receiveRequest(Event evt) {
        // Shadow EventGenerator
        Request r = evt.getRequest();
        r.moveTo(this);

        Request curRequest = evt.getRequest();

        curRequest.recordArrival(evt.getTimestamp());

        /*
         * Upon receiving the re quest, check the queue size and act accordingly
         */
        if (!this.isBusy ) {
            if (dist == null)
                __startService(evt, curRequest);
            else
                __startService(evt, curRequest, dist);
        }

        theQueue.add(curRequest);
    }

    @Override
    void printStats(Double time) {
        System.out.println("S" + this.name + " UTIL: " + this.busyTime / time);

        if (this.name.equals("1,2")) {
            System.out.println("S1 QLEN:" + reportQlen());
            System.out.println("S1 TRESP:" + reportTresp());
        }
    }

    double reportQlen() {
        return cumulQ / snapCount;
    }

    double reportTresp() {
        return cumulTq / servedReqs;
    }

}

