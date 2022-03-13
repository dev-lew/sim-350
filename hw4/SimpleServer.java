package hw4;
import java.util.*; 

/***************************************************/
/* CS-350 Spring 2022 - Homework 3 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a single-processor server with an infinite    */
/*   request queue and exponentially distributed   */
/*   service times, i.e. a x/M/1 server.           */
/*                                                 */
/***************************************************/

class SimpleServer extends EventGenerator {

    public LinkedList<Request> theQueue = new LinkedList<Request>();
    public Double servTime;
    public String name = null;
    
    /* Statistics of this server --- to construct rolling averages */
    public Double cumulQ = new Double(0);
    public Double cumulW = new Double(0);
    public Double cumulTq = new Double(0);
    public Double cumulTw = new Double(0);
    public Double busyTime = new Double(0);
    public int snapCount = 0;
    public int servedReqs = 0;
    
    public SimpleServer (Timeline timeline, Double servTime) {
        super(timeline);

        /* Initialize the average service time of this server */
        this.servTime = servTime;
    }

    // Used for subclasses
    EventGenerator getNext() {
        return super.next;
    }

    /* Given a name to this server that will be used for the trace and
     * statistics */
    public void setName (String name) {
        this.name = name;
    }
    
    /* Internal method to be used to simulate the beginning of service
     * for a queued/arrived request. */
    public void __startService(Event evt, Request curRequest) {
        Event nextEvent = new Event(EventType.DEATH, curRequest,
                                    evt.getTimestamp() + Exp.getExp(1/this.servTime), this);

        curRequest.recordServiceStart(evt.getTimestamp());
        cumulTw += curRequest.getServiceStart() - curRequest.getArrival();
	    
        /* Print the occurrence of this event */
        System.out.println(curRequest + " START" +
                           (this.name != null ? " " + this.name : "")  +
                           ": " + evt.getTimestamp());
	    
        super.timeline.addEvent(nextEvent);	    	
    }
    
    @Override
    void receiveRequest(Event evt) {
        super.receiveRequest(evt);

        Request curRequest = evt.getRequest();

        curRequest.recordArrival(evt.getTimestamp());
	
        /* Upon receiving the request, check the queue size and act
         * accordingly */
        if(theQueue.isEmpty()) {
            __startService(evt, curRequest);
        }
	    
        theQueue.add(curRequest);	
    }

    @Override
    void releaseRequest(Event evt) {
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
	
        assert super.next != null;
        super.next.receiveRequest(evt);
	
        /* Any new request to put into service?  */
        if(!theQueue.isEmpty()) {
            Request nextRequest = theQueue.peekFirst();

            __startService(evt, nextRequest);	    
        }
	
    }

    @Override
    Double getRate() {
        return 1/this.servTime;
    }

    @Override
    void executeSnapshot() {
        snapCount++;
        cumulQ += theQueue.size();
        cumulW += Math.max(theQueue.size()-1, 0);
    }

    @Override
    void printStats(Double time) {
        if (this.name == null) {
            System.out.println("UTIL: " + busyTime/time);
            System.out.println("QLEN: " + cumulQ/snapCount);
            System.out.println("TRESP: " + cumulTq/servedReqs);
        } else {
            System.out.println("UTIL " + this.name + ": " + busyTime/time);
            System.out.println("QLEN " + this.name + ": " + cumulQ/snapCount);
        }
    }

    
    @Override
    public String toString() {
        return (this.name != null ? this.name : "");
    }

    
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
