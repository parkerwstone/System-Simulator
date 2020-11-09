package hw3;

/***************************************************/
/* CS-350 Fall 2020 - Homework 2 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a traffic sink. Any request that arrives at   */
/*   the sink is effectively released from the     */
/*   system.                                       */
/*                                                 */

/***************************************************/

class Sink extends EventGenerator {

    private Double cumulRespTime = new Double(0);
    private Double cumulWaitTime = new Double(0);
    private Double cumulProcSteps = new Double(0);
    private int doneRequests = 0;

    public Sink(Timeline timeline, String name) {
        super(timeline, name);
    }

    @Override
    void receiveRequest(Event evt) {
        super.receiveRequest(evt);

        Request doneReq = evt.getRequest();

	/* Print the occurrence of this event */
//        System.out.println(evt.getRequest() + " " + this + " "
//                + doneReq.getLastServer()
//                + ": " + evt.getTimestamp());

	/* Update system stats */
        doneRequests++;

	/* Recover the time of entry in the system */
        EventGenerator entry = doneReq.getEntryPoint();
        Stats entryStats = doneReq.getStatsAtNode(entry);
        Double timeOfArrival = entryStats.arrival;

	/* Extract the total service and number of steps this request
	 * went through */
        Double totalService = doneReq.getTotalService();
        int totalSteps = doneReq.getTotalSteps();

        cumulRespTime += evt.getTimestamp() - timeOfArrival;
        cumulWaitTime += (evt.getTimestamp() - timeOfArrival) - totalService;
        cumulProcSteps += totalSteps;
    }

    @Override
    public void printStats(Double time) {
        System.out.println("TRESP: " + cumulRespTime / doneRequests);
    }

    @Override
    public String toString() {
        return name;
    }
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
