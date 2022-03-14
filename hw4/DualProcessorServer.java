package hw4;

import java.util.LinkedList;

class DualProcessorServer extends SimpleServer {
    public boolean isBusy = false;

    /* The queue is now shared */
    public DualProcessorServer(Timeline timeline, Double servTime,
                               LinkedList<Request> theQueue) {
        super(timeline, servTime);
        this.theQueue = theQueue;
    }

    public void __startService(Event evt, Request curRequest) {
        this.isBusy = true;
        Event nextEvent = new Event(EventType.DEATH, curRequest, evt.getTimestamp() + Exp.getExp(1 / this.servTime),
                this);

        curRequest.recordServiceStart(evt.getTimestamp());
        cumulTw += curRequest.getServiceStart() - curRequest.getArrival();

        /* Print the occurrence of this event */
        System.out.println(
                curRequest + " START" + (this.name != null ? " " + this.name : "") + ": " + evt.getTimestamp());

        super.getParentTimeline().addEvent(nextEvent);
    }

    @Override
    void releaseRequest(Event evt) {
        this.isBusy = false;

        /* What request we are talking about? */
        Request curRequest = evt.getRequest();

        /* Remove the request from the server queue */
        Request queueHead = theQueue.removeFirst();

        /* If the following is not true, something is wrong */
        assert curRequest == queueHead;

        curRequest.recordDeparture(evt.getTimestamp());
	
        /* Update busyTime */
        busyTime += curRequest.getDeparture() - curRequest.getServiceStart();

        /* Update cumulative response time at this server */
        cumulTq += curRequest.getDeparture() - curRequest.getArrival();
	
        /* Update number of served requests */
        servedReqs++;
	
        assert super.getNext() != null;
        super.getNext().receiveRequest(evt);
	
        /* Any new request to put into service?  */
        if(!theQueue.isEmpty()) {
            Request nextRequest = theQueue.peekFirst();

            if (dist == null)
                __startService(evt, nextRequest);
            else
                __startService(evt, nextRequest, dist);
        }
	
    }

}

