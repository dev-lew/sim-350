package hw4;
import java.lang.*;
import java.util.*; 

/***************************************************/
/* CS-350 Spring 2022 - Homework 3 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a simulator where a single source of events   */
/*   is connected to a single exit point, with a   */
/*   single-processor server in the middle.        */
/*                                                 */
/***************************************************/

public class Simulator {

    /* These are the resources that we intend to monitor */
    private LinkedList<EventGenerator> resources = new LinkedList<EventGenerator>();

    /* Timeline of events */
    private Timeline timeline = new Timeline();

    /* Simulation time */    
    private Double now;
    
    public void addMonitoredResource (EventGenerator resource) {
	this.resources.add(resource);
    }

    /* This method creates a new monitor in the simulator. To collect
     * all the necessary statistics, we need at least one monitor. */
    private void addMonitor() {
	/* Scan the list of resources to understand the granularity of
	 * time scale to use */
	Double monRate = Double.POSITIVE_INFINITY;

	for (int i = 0; i < resources.size(); ++i) {
	    Double rate = resources.get(i).getRate();
	    if (monRate > rate) {
		monRate = rate;
	    }
	}

	/* If this fails, something is wrong with the way the
	 * resources have been instantiated */
	assert !monRate.equals(Double.POSITIVE_INFINITY);

	/* Create a new monitor for this simulation */
	Monitor monitor = new Monitor(timeline, monRate, resources);

    }
    
    public void simulate (Double simTime) {

	/* Rewind time */
	now = new Double(0);

	/* Add monitor to the system */
	addMonitor();
	
	/* Main simulation loop */
	while(now < simTime) {
	    /* Fetch event from timeline */
	    Event evt = timeline.popEvent();
	    
	    /* Fast-forward time */
	    now = evt.getTimestamp();
	    
	    /* Extract block responsible for this event */
	    EventGenerator block = evt.getSource();

	    /* Handle event */
	    block.processEvent(evt);
	    
	}

	/* Print all the statistics */
	for (int i = 0; i < resources.size(); ++i) {
	    resources.get(i).printStats(now);
	}
	
	
    }
    
    /* Entry point for the entire simulation  */
    public static void main (String[] args) {

	/* Parse the input parameters */
	double simTime = Double.valueOf(args[0]);
	double lambda = Double.valueOf(args[1]);
	double servTime_pri = Double.valueOf(args[2]);
	double servTime_sec = Double.valueOf(args[3]);
	double prob_pri_exit = Double.valueOf(args[4]);
	double prob_sec_pri = Double.valueOf(args[5]);
	
	/* Create a new simulator instance */
	Simulator sim = new Simulator();
	
	/* Create the traffic source */
	Source trafficSource = new Source(sim.timeline, lambda);
	    
	/* Create a new traffic sink */
	Sink trafficSink = new Sink(sim.timeline);

	/* Create new single-cpu processing server */
	SimpleServer serverPri = new SimpleServer(sim.timeline, servTime_pri);
	SimpleServer serverSec = new SimpleServer(sim.timeline, servTime_sec);

	/* Give some names to identify these servers when printing
	 * trace and statistics */
	serverPri.setName("0");
	serverSec.setName("1");
	
	/* Create two routing nodes */
	RoutingNode rnPri = new RoutingNode(sim.timeline);
	RoutingNode rnSec = new RoutingNode(sim.timeline);
	
	/* Establish routing */
	trafficSource.routeTo(serverPri);
	serverPri.routeTo(rnPri);
	rnPri.routeTo(trafficSink, prob_pri_exit);
	rnPri.routeTo(serverSec, 1 - prob_pri_exit);

	serverSec.routeTo(rnSec);
	rnSec.routeTo(trafficSink, 1 - prob_sec_pri);
	rnSec.routeTo(serverPri, prob_sec_pri);

	/* Add resources to be monitored */
	sim.addMonitoredResource(serverPri);
	sim.addMonitoredResource(serverSec);
	sim.addMonitoredResource(trafficSink);
	
	/* Kick off simulation */
	sim.simulate(simTime);	
    }
    
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
