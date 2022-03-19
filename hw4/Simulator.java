package hw4;
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

            // General service distribution
            if (rate < 0) {
                continue;
            }

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

    /*
      Generates service time distribution HashMap as required by
      __startService
      arrDist is an array of (serviceTime, probability) pair
     */

    private static HashMap<Double, Double> generateDist(double arrDist[]) {
        assert arrDist.length % 2 == 0 && arrDist.length != 0;

        HashMap<Double, Double> dist = new HashMap<>();

        for (int i = 0; i < arrDist.length; i += 2)
            dist.put(arrDist[i], arrDist[i + 1]);

        return dist;
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
        assert args.length == 17;

        /* Parse the input parameters */
        double simTime = Double.valueOf(args[0]);
        double lambda = Double.valueOf(args[1]);
        double servTime_pri = Double.valueOf(args[2]);
        double servTime_sec = Double.valueOf(args[3]);
        double servTime_ter = Double.valueOf(args[4]);
        double servTime_quad_one = Double.valueOf(args[5]);
        double prob_one = Double.valueOf(args[6]);
        double servTime_quad_two = Double.valueOf(args[7]);
        double prob_two = Double.valueOf(args[8]);
        double servTime_quad_three = Double.valueOf(args[9]);
        double prob_three = Double.valueOf(args[10]);
        int ter_queue_length = Integer.valueOf(args[11]);
        double prob_pri_to_sec = Double.valueOf(args[12]);
        double prob_pri_to_ter = Double.valueOf(args[13]);
        double prob_quad_to_exit = Double.valueOf(args[14]);
        double prob_quad_to_sec = Double.valueOf(args[15]);
        double prob_quad_to_ter = Double.valueOf(args[16]);

        /* Create a new simulator instance */
        Simulator sim = new Simulator();
	
        /* Create the traffic source */
        Source trafficSource = new Source(sim.timeline, lambda);
	    
        /* Create a new traffic sink */
        Sink trafficSink = new Sink(sim.timeline);

        /* Create servers */
        SimpleServer serverPri = new SimpleServer(sim.timeline, servTime_pri);
        DualProcessorServer serverSec_One = new DualProcessorServer(sim.timeline, servTime_sec);
        DualProcessorServer serverSec_Two = new DualProcessorServer(sim.timeline, servTime_sec);
        SimpleServer serverTer = new KQueueServer(sim.timeline, servTime_ter, ter_queue_length);

        // Create array for generateDist
        double dist[] = {servTime_quad_one, prob_one,
            servTime_quad_two, prob_two,
            servTime_quad_three, prob_three};

        SimpleServer serverQuad = new SimpleServer(sim.timeline, generateDist(dist));

        /* Give some names to identify these servers when printing
         * trace and statistics */
        serverPri.setName("0");
        serverSec_One.setName("1,1");
        serverSec_Two.setName("1,2");
        serverTer.setName("2");
        serverQuad.setName("3");
	
        /* Create routing nodes */
        RoutingNode rnPri = new RoutingNode(sim.timeline);
        Delegator rnSec = new Delegator(sim.timeline);
        RoutingNode rnTer = new RoutingNode(sim.timeline);
	
        /* Establish routing */
        trafficSource.routeTo(serverPri);
        serverPri.routeTo(rnPri);

        // Send requests to the delegator
        rnPri.routeTo(rnSec, prob_pri_to_sec);

        // Delegate
        rnSec.routeTo(serverSec_One);
        rnSec.routeTo(serverSec_Two);

        rnPri.routeTo(rnTer, prob_pri_to_ter);

        // Route sec and ter to quad
        serverSec_One.routeTo(serverQuad);
        serverSec_Two.routeTo(serverQuad);
        serverTer.routeTo(serverQuad);

        serverQuad.routeTo(rnTer);
        rnTer.routeTo(trafficSink, prob_quad_to_exit);
        rnTer.routeTo(rnSec, prob_quad_to_sec);
        rnTer.routeTo(serverTer, prob_quad_to_ter);

        /* Add resources to be monitored */
        sim.addMonitoredResource(serverPri);
        sim.addMonitoredResource(serverSec_One);
        sim.addMonitoredResource(serverSec_Two);
        sim.addMonitoredResource(serverTer);
        sim.addMonitoredResource(serverQuad);
        sim.addMonitoredResource(trafficSink);
	
        /* Kick off simulation */
        sim.simulate(simTime);	
    }
    
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
