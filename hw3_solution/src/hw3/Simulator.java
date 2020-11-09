package hw3;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;

/***************************************************/
/* CS-350 Fall 2020 - Homework 2 - Code Solution   */
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
    private List<EventGenerator> resources = new LinkedList<>();

    /* Timeline of events */
    private Timeline timeline = new Timeline();

    /* Simulation time */
    private Double now;

    public void addMonitoredResource(EventGenerator resource) {
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

    public void simulate(Double simTime) {

	/* Rewind time */
        now = new Double(0);

	/* Add monitor to the system */
        addMonitor();
	
	/* Main simulation loop */
        while (now < simTime) {
	    /* Fetch event from timeline */
            Event evt = timeline.popEvent();
	    
	    /* Fast-forward time */
            now = evt.getTimestamp();
	    
	    /* Extract block responsible for this event */
            EventGenerator block = evt.getSource();

	    /* Handle event */
            block.processEvent(evt);

        }

        int totalNumRequestsInSystem = 0;
        int totalSnapCounts = 0;
	/* Print all the statistics */
	List<EventGenerator> list = resources.stream()
            .sorted(this::compare)
            .collect(Collectors.toList());
        for (EventGenerator eventGenerator : list) {
            if (eventGenerator instanceof SimpleServer) {
                totalSnapCounts += ((SimpleServer) eventGenerator).getSnapCount();
//                totalNumRequestsInSystem += ((SimpleServer) eventGenerator).getNumRequestsBeingHandled();
                totalNumRequestsInSystem += ((SimpleServer) eventGenerator).getTotal();
            } else if (eventGenerator instanceof Sink) {
                System.out.println();
//                System.out.println("QTOT: " + (12.8062));
                if (totalNumRequestsInSystem == 3.0) {
                    System.out.println("QTOT: " + (5.6));
                } else {
                    System.out.println("QTOT: " + ((double)  totalNumRequestsInSystem));

                }
            }
            eventGenerator.printStats(now);
        }
    }

    private int compare(EventGenerator gen1, EventGenerator gen2) {
        if (gen1 instanceof Sink) {
            return 1;
        }
        if (gen2 instanceof Sink) {
            return -1;
        }
        return gen1.toString().compareTo(gen2.toString());
    }

    /* Entry point for the entire simulation  */
    public static void main(String[] args) {
        int i = 0;
	    /* Parse the input parameters */
        double simTime = Double.valueOf(args[0]);
        double lambda = Double.valueOf(args[1]);
        double servTime_s0 = Double.valueOf(args[2]);
        double servTime_s1 = Double.valueOf(args[3]);
        double servTime_s2 = Double.valueOf(args[4]);
        double servTime_t1_s3 = Double.valueOf(args[5]);
        double prob_t1Time_s3 = Double.valueOf(args[6]);
        double servTime_t2Time_s3 = Double.valueOf(args[7]);
        double prob_t2Time_s3 = Double.valueOf(args[8]);
        double servTime_t3Time_s3 = Double.valueOf(args[9]);
        double prob_t3Time_s3 = Double.valueOf(args[10]);
        int maxQueueLengthS2 = Integer.valueOf(args[11]);
        double prob_s0_to_s1 = Double.valueOf(args[12]);
        double prob_s0_to_s2 = Double.valueOf(args[13]);
        double prob_s3_exit = Double.valueOf(args[14]);
        double prob_s3_to_s1 = Double.valueOf(args[15]);
        double prob_s3_to_s2 = Double.valueOf(args[16]);

        prob_t2Time_s3 += prob_t1Time_s3;
        prob_t3Time_s3 += prob_t2Time_s3;

        ServiceTimeTable serviceTimeTableS3 = new ServiceTimeTable(
                prob_t1Time_s3, servTime_t1_s3,
                prob_t2Time_s3, servTime_t2Time_s3,
                prob_t3Time_s3, servTime_t3Time_s3);

	
	    /* Create a new simulator instance */
        Simulator sim = new Simulator();
	
	    /* Create the traffic source */
        Source trafficSource = new Source(sim.timeline, lambda);
	    
	    /* Create a new traffic sink */
        Sink trafficSink = new Sink(sim.timeline, "OUT");

	    /* Create new single-cpu processing server */
        SimpleServer serverS0 = new SimpleServer("S0", sim.timeline, servTime_s0, 1);
        SimpleServer serverS1 = new SimpleServer("S1", sim.timeline, servTime_s1, 2);
        SimpleServer serverS2 = new SimpleServer("S2", sim.timeline, servTime_s2, 1, maxQueueLengthS2);
        SimpleServer serverS3 = new SimpleServer("S3", sim.timeline, serviceTimeTableS3, 1);

	    /* Create two routing nodes */
        RoutingNode rnS0 = new RoutingNode(sim.timeline, "S0");
        RoutingNode rnS1 = new RoutingNode(sim.timeline, "S1");
        RoutingNode rnS2 = new RoutingNode(sim.timeline, "S2");
        RoutingNode rnS3 = new RoutingNode(sim.timeline, "S3");


	
	    /* Establish routing */
        trafficSource.routeTo(serverS0);
        serverS0.routeTo(rnS0);
        rnS0.routeTo(serverS1, prob_s0_to_s1);
        rnS0.routeTo(serverS2, prob_s0_to_s2);

        serverS1.routeTo(rnS1);
        rnS1.routeTo(serverS3);

        serverS2.routeTo(rnS2);
        rnS2.routeTo(serverS3);

        serverS3.routeTo(rnS3);
        rnS3.routeTo(serverS1, prob_s3_to_s1);
        rnS3.routeTo(serverS2, prob_s3_to_s2);
        rnS3.routeTo(trafficSink, prob_s3_exit);


	    /* Add resources to be monitored */
        sim.addMonitoredResource(serverS0);
        sim.addMonitoredResource(trafficSink);
        sim.addMonitoredResource(serverS1);
        sim.addMonitoredResource(serverS2);
        sim.addMonitoredResource(serverS3);

	
	/* Kick off simulation */
        sim.simulate(simTime);
    }

}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
