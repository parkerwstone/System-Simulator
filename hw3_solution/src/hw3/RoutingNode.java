package hw3;

import java.util.*;

class RoutingNode extends EventGenerator {

    private HashMap<EventGenerator, Double> routingTable = new HashMap<EventGenerator, Double>();

    public RoutingNode(Timeline timeline, String name) {
        super(timeline, name);
    }

    @Override
    public void routeTo(EventGenerator next) {
        routeTo(next, new Double(1));
    }

    public void routeTo(EventGenerator next, Double probability) {
    /* Always assume that the same destination does not exist
	 * twice in the routing table */
        assert !routingTable.containsKey(next);

	/* Add destination to routing table */
        routingTable.put(next, probability);

	/* Perform a sanity check that the total probability has not
	 * exceeded 1 */
        Double totalP = new Double(0);

        for (Map.Entry<EventGenerator, Double> entry : routingTable.entrySet()) {
            totalP += entry.getValue();
        }

        assert totalP <= 1;
    }

    @Override
    void receiveRequest(Event evt) {
        Request curRequest = evt.getRequest();

	/* Find out where to route to with a dice roll */
        Double dice = Math.random();

	/* Identify the destination with CDF calculation */
        Double cumulP = new Double(0);

        EventGenerator nextHop = null;

        for (Map.Entry<EventGenerator, Double> entry : routingTable.entrySet()) {
            cumulP += entry.getValue();

            if (dice < cumulP) {
                nextHop = entry.getKey();
                break;
            }
        }

	/* Print the occurrence of this event */
//        if (!nextHop.toString().equals("")) {
            System.out.println(evt.getRequest() + " FROM " + this + " TO " + nextHop + ": " + evt.getTimestamp());
//        }

        assert nextHop != null;

        nextHop.receiveRequest(evt);
    }

    @Override
    public String toString() {
        return name;
    }
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
