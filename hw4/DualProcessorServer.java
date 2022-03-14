package hw4;

class DualProcessorServer extends SimpleServer {
    private int reqsProcessing = 0;

    public DualProcessorServer(Timeline timeline, Double servTime) {
        super(timeline, servTime);
    }

    @Override
    void receiveRequest(Event evt) {
        // Shadow EventGenerator
        Request r = evt.getRequest();
        r.moveTo(this);

        r.recordArrival(evt.getTimestamp());

        // With two processors, the queue can have at most 1 request processing
        if (this.theQueue.size() <= 1) {
            reqsProcessing++;
            assert reqsProcessing <= 2;

            __startService(evt, r);
        }

        theQueue.add(r);
    }

    @Override
    void releaseRequest(Event evt) {
        Request curRequest = evt.getRequest();

        // We may remove the penultimate member of the queue
        assert theQueue.remove(curRequest);

        curRequest.recordDeparture(evt.getTimestamp());

        /* Update busyTime */
        busyTime += curRequest.getDeparture() - curRequest.getServiceStart();

        /* Update cumulative response time at this server */
        cumulTq += curRequest.getDeparture() - curRequest.getArrival();

        /* Update number of served requests */
        servedReqs++;

        // Update currently processing requests
        assert --reqsProcessing <= 2;

        assert super.getNext() != null;
        super.getNext().receiveRequest(evt);

        /* Any new request to put into service? */
        Request nextRequest;
        switch (theQueue.size()) {
        case 1:
            assert ++reqsProcessing <= 2;

            nextRequest = theQueue.peekFirst();
            __startService(evt, nextRequest);
            break;
        case 2:
            // Process first 2 elements in the queue
            if (reqsProcessing == 0) {
                // new Request[] is passed in order to cast the returned Object array
                // to a Request[] array
                Request[] tempQueueArray = theQueue.toArray(new Request[0]);
                __startService(evt, tempQueueArray[0]);
                __startService(evt, tempQueueArray[1]);
            } else if (reqsProcessing == 1) {
                nextRequest = theQueue.peekFirst();
                __startService(evt, nextRequest);
            }
            break;
        }
    }
}

