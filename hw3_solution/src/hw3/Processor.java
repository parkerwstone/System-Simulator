package hw3;

public class Processor {
    private final SimpleServer server;
    private final Double servTime;
    private final String name;
    private final ServiceTimeTable serviceTimeTable;

    private Double cumulTq = 0.0;
    private Double cumulTw = 0.0;
    private Double busyTime = 0.0;
    private int processedReqs = 0;

    private Request request;

    Processor(SimpleServer server, Double servTime, String name) {
        this.servTime = servTime;
        this.name = name;
        this.server = server;
        this.serviceTimeTable = null;
    }

    Processor(SimpleServer server, ServiceTimeTable serviceTimeTable, String name) {
        this.servTime = null;
        this.name = name;
        this.server = server;
        this.serviceTimeTable = serviceTimeTable;
    }

    Event start(double startTime, Request request) {
        this.request = request;
        double serviceTime = serviceTimeTable == null
                ? Exp.getExp(1.0 / this.servTime)
                : serviceTimeTable.getServiceTime();
        Event nextEvent =
                new Event(
                        EventType.DEATH, request, startTime + serviceTime, server);

        request.recordServiceStart(startTime);
        cumulTw += request.getServiceStart() - request.getArrival();

        /* Print the occurrence of this event */
        System.out.println(
                request
                        + " START "
                        + this
                        + ": "
                        + startTime);

        return nextEvent;
    }

    void end(double time) {
        request.recordDeparture(time);

        /* Update busyTime */
        busyTime += request.getDeparture() - request.getServiceStart();

        /* Update cumulative response time at this server */
        cumulTq += request.getDeparture() - request.getArrival();

        /* Update number of served requests */
        processedReqs++;

        System.out.println(request + " DONE " + this + ": " + time);

        request = null;
    }

    boolean isBusy() {
        return request != null;
    }

    public Double cumWaitTime() {
        return cumulTw;
    }

    public Double getCumulTq() {
        return cumulTq;
    }

    public Double getBusyTime() {
        return busyTime;
    }

    public void printUtilStat(Double time) {
        System.out.println(this + " UTIL: " + busyTime / time);
    }

    public int getProcessedReqs() {
        return processedReqs;
    }

    public boolean hasRequest(Request request) {
        return request.equals(this.request);
    }

    @Override
    public String toString() {
        return name;
    }
}
