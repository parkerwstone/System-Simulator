import java.io.*;
import java.util.*;

public class Hw4Checker {
    private final Map<String, Request> requestMap = new TreeMap<>(Comparator.comparingInt(o -> Integer.parseInt(o.substring(1))));
    private final List<Request> q0 = new ArrayList<>(1000);
    private final List<Request> q1 = new ArrayList<>(1000);
    private int numSendbacks = 0;
    private int numRequestsSentToQ1 = 0;
    private Double expectedStart0 = null;
    private Double expectedStart1 = null;
    private Double p0BusyUntil = 0.0;
    private Double p1BusyUntil = 0.0;
    private Double accArrivalDelta = 0.0;
    private Double lastArrivalTime = 0.0;
    private int q0Size = 0;
    private int q1Size = 0;
    private Request processing0;
    private Request processing1;
    private int s1Count = 0;
    private int s2Count = 0;
    private int s3ToS1Count = 0;
    private int s3ToS2Count = 0;
    private int s3ToOutCount = 0;


    private void process(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("ERROR: File not found: " + filename);
            return;
        }

        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            processLine(line);
        }
    }

    private void processLine(String line) {
        StringTokenizer token = new StringTokenizer(line, " ");
        String requestName = token.nextToken();
        String opStr = token.nextToken();
        while (!opStr.endsWith(":")) {
            opStr += (" " + token.nextToken());
        }
        double timestamp = Double.parseDouble(token.nextToken());
        Request request = requestMap.get(requestName);
        if (request == null && !opStr.contains("ARR")) {
            System.err.println(String.format("ERROR: %s: Received an operation (%s) for a request that has not arrived",
                    requestName, opStr));
            return;
        }
        if (opStr.contains("ARR")) {
            request = new Request(requestName, timestamp);
            requestMap.put(requestName, request);
        }
        request.addEvent(new Event(opStr, timestamp));

        if (opStr.contains("FROM S0 TO S1")) {
            s1Count++;
        } else if (opStr.contains("FROM S0 TO S2")) {
            s2Count++;
        } else if (opStr.contains("FROM S3 TO S1")) {
            s3ToS1Count++;
        } else if (opStr.contains("FROM S3 TO S2")) {
            s3ToS2Count++;
        } else if (opStr.contains("FROM S3 TO OUT")) {
            s3ToOutCount++;
        }

/*

        switch (operation) {
            case ARRIVAL:
                if (request != null) {
                    System.err.println(String.format("%s: Found a duplicate ARRival: %f and %f",
                            requestName, timestamp, request.getArrival()));
                    return;
                } else {
                    accArrivalDelta += (timestamp - lastArrivalTime);
                    lastArrivalTime = timestamp;
                    request = new Request(requestName, timestamp);
                    requestMap.put(requestName, request);
                    addToQ(q0, 0, request, timestamp);
                }
                break;

            case START0:
                if (request.hasStart0()) {
                    if (!request.hasNext0()) {
                        System.err.println(String.format("ERROR: %s: A Start0 was received but already has a start0 and no next0 found at time %f",
                                request.getName(), timestamp));
                    }
                    request.setSendbackStart(timestamp);
                } else {
                    request.setStart0(timestamp);
                }
                removeFromQ(q0, 0, request, timestamp);
                startProcessingRequest(0, request, timestamp);
                break;

            case START1:
                request.setStart1(timestamp);
                removeFromQ(q1, 1, request, timestamp);
                startProcessingRequest(1, request, timestamp);
                break;

            case NEXT0:
                request.setNext0(timestamp);
                addToQ(q0, 0, request, timestamp);
                numSendbacks++;
                p1BusyUntil = Math.max(p0BusyUntil, timestamp);
                endProcessingRequest(1, request, timestamp);
                break;

            case NEXT1:
                request.setNext1(timestamp);
                addToQ(q1, 1, request, timestamp);
                numRequestsSentToQ1++;
                p0BusyUntil = Math.max(p1BusyUntil, timestamp);
                endProcessingRequest(0, request, timestamp);
                break;

            case DONE0:
                request.setDone0(timestamp);
                p0BusyUntil = Math.max(p0BusyUntil, timestamp);
                endProcessingRequest(0, request, timestamp);
                break;

            case DONE1:
                request.setDone1(timestamp);
                p1BusyUntil = Math.max(p1BusyUntil, timestamp);
                endProcessingRequest(1, request, timestamp);
                break;

            default:
                System.err.println("Invalid operation");
        }
*/
    }

    private void printS0ToOtherServerStats() {
        System.out.println("\nS0 request path stats:");
        System.out.println(String.format("To S1: %d (%f)", s1Count, (double) s1Count / (double) requestMap.size()));
        System.out.println(String.format("To S2: %d (%f)", s2Count, (double) s2Count / (double) requestMap.size()));
    }

    private void printS3ToOtherServerStats() {
        System.out.println("\nS3 request path stats:");
        int totalReqs = s3ToS1Count + s3ToS2Count + s3ToOutCount;
        System.out.println(String.format("To S1: %d (%f)", s3ToS1Count, (double) s3ToS1Count / (double) totalReqs));
        System.out.println(String.format("To S2: %d (%f)", s3ToS2Count, (double) s3ToS2Count / (double) totalReqs));
        System.out.println(String.format("To Out: %d (%f)", s3ToOutCount, (double) s3ToOutCount / (double) totalReqs));
    }

    private void startProcessingRequest(int qid, Request request, double time) {
        Request currentlyProcessing = (qid == 0 ? processing0 : processing1);
        if (currentlyProcessing != null) {
            System.err.println(String.format("ERROR: %s: was starting while another request %s was processing on process %d at time %f",
                    request.getName(), currentlyProcessing.getName(), qid, time));
        }
        if (qid == 0) {
            processing0 = request;
        } else {
            processing1 = request;
        }
    }

    private void endProcessingRequest(int qid, Request request, double time) {
        Request currentlyProcessing = (qid == 0 ? processing0 : processing1);
        if (currentlyProcessing == null) {
            System.err.println(String.format("ERROR: %s: was ending but it wasn't being processed on process %d at time %f",
                    request.getName(), qid, time));
        }
        if (qid == 0) {
            processing0 = null;
        } else {
            processing1 = null;
        }
    }

    private void removeFromQ(List<Request> q, int qid, Request request, double time) {
        if (!q.contains(request)) {
            System.err.println(String.format("ERROR: %s: Ask to be removed from q %d, but the request is not in the q at time %f",
                    request.getName(), qid, time));
        }
        if (qid == 0) {
            if (q.size() == 1 && expectedStart0 != null && time != expectedStart0 && expectedStart0 > p0BusyUntil) {
                System.err.println(String.format("ERROR: %s: Should have started right away from q %d at %f, but is starting at: %f",
                        request.getName(), qid, time, expectedStart0));
            }
            expectedStart0 = null;
        } else {
            if (q.size() == 1 && expectedStart1 != null && time != expectedStart1 &&  expectedStart1 > p1BusyUntil) {
                System.err.println(String.format("ERROR: %s: Should have started right away from q %d at %f, but is starting at: %f",
                        request.getName(), qid, time, expectedStart1));
            }
            expectedStart1 = null;
        }
        q.remove(request);
//        System.out.println(String.format("%s: Removed from q %d at time %f, request q wait time was: %f",
//                request.getName(), qid, time, qid == 0 ? request.calcQ0Time() : request.calcQ1Time()));
    }

    private void addToQ(List<Request> q, int qid, Request request, double time) {
        if (q.contains(request)) {
            System.err.println(String.format("ERROR: %s: Asked to re-add this to q %d, but it is already in q at time %f",
                    request.getName(), qid, time));
            return;
        }
        System.out.println(String.format("%s: Adding to q %d at time %f %s",
                request.getName(), qid, time, q.isEmpty() ? ", q is empty, this should start right away" : (", q size after add: " + (q.size() + 1))));
        if (q.isEmpty()) {
            if (qid == 0) {
                expectedStart0 = time;
            } else {
                expectedStart1 = time;
            }
        }
        q.add(request);
    }

    private void printRequests() {
        int[] requestsPerServer = new int[4];
        int numDone = 0;
        int dropped = 0;
        for (Request request : requestMap.values()) {
            System.out.println(request);
            requestsPerServer[1] += request.timesAtServer[1];
            requestsPerServer[2] += request.timesAtServer[2];
            requestsPerServer[3] += request.timesAtServer[3];
            numDone += (request.done == null ? 0 : 1);
            if (request.dropped != null) {
                dropped++;
            }
        }
        System.out.println("\nNumber of requests per server:");
        System.out.println(String.format("1) %d (%f)", requestsPerServer[1], (double) requestsPerServer[1] / (double) requestMap.size()));
        System.out.println(String.format("2) %d (%f)", requestsPerServer[2], (double) requestsPerServer[2] / (double) requestMap.size()));
        System.out.println(String.format("3) %d (%f)", requestsPerServer[3], (double) requestsPerServer[3] / (double) requestMap.size()));
        System.out.println(String.format("Num done: %d (%f)", numDone, (double) numDone / (double) requestMap.size()));
        System.out.println(String.format("Num dropped: %d (%f)", dropped, (double) dropped / (double) s2Count));
    }

    /*

    private void printRequests() {
        for (Request request : requestMap.values()) {
            if (!request.isComplete()) continue;
            System.out.println(request);
            if (!request.doesServiceAndQWaitTimeMatchRespTime()) {
                System.err.println(String.format("ERROR: %s: calc error: %f",
                        request.getName(), request.calcResponseTime() - request.calcTotalServiceTime() - request.calcTotalQTime()));

                double a = request.calcResponseTime();
                double b = request.calcTotalServiceTime();
                double c = request.calcTotalQTime();

                int i = 0;
            }
        }
    }

    private void printRequestQWaitTimes() {
        double accWaitTime = 0.0;
        for (Request request : requestMap.values()) {
            if (!request.isComplete()) continue;
            double requestWaitTime = request.calcTotalQTime();
            System.out.println(String.format("%s: %f / %f", request.getName(), requestWaitTime, accWaitTime));
            accWaitTime += requestWaitTime;
        }
    }

    private void printRequestQLongWaitTimes() {
        double accWaitTime = 0.0;
        int count = 0;
        for (Request request : requestMap.values()) {
            if (!request.isComplete()) continue;
            double requestWaitTime = request.calcTotalQTime();
            if (requestWaitTime > 2.0) {
                System.out.println(
                String.format("%s: %f / %f", request.getName(), requestWaitTime, accWaitTime));
                accWaitTime += requestWaitTime;
                count++;
            }
        }
        System.out.println(String.format("Total long wait time: %f over %d requests (%f)",
                accWaitTime, count, accWaitTime / (double) count));
    }

    private void printResults() {
        double accQ0WaitTime = calcQ0Wait();
        double accQ1WaitTime = calcQ1Wait();
        double accSendbackQTime = calcSendbackQWait();
        double accService0Time = calcService0Time();
        double accService1Time = calcService1Time();
        double accSendbackServiceTime = calcSendBackServiceTime();
        double accResponseTime = calcResponseTime();

        int numRequests = requestMap.size();
        System.out.println("Number of requests: " + numRequests);
        System.out.println(String.format("Avg delta between arrivals: %f", accArrivalDelta / (double) numRequests));
        System.out.println(String.format("Number of sendbacks: %d (%f)",
                numSendbacks, (double) numSendbacks / (double) numRequests));
        System.out.println(String.format("Number requests sent to Q1: %d (%f)",
                numRequestsSentToQ1, (double) numRequestsSentToQ1 / (double) numRequests));
        System.out.println(String.format("Q0 wait time: %f (%f)", accQ0WaitTime, accQ0WaitTime / (double) (numRequests + numSendbacks)));
        System.out.println(String.format("Q1 wait time: %f (%f)", accQ1WaitTime, accQ1WaitTime / (double) numRequestsSentToQ1));
        System.out.println(String.format("Sendback Q0 wait time: %f (%f)", accSendbackQTime, accSendbackQTime / (double) numSendbacks));
        System.out.println(String.format("Total Q wait time: %f (%f)",
                (accQ0WaitTime + accQ1WaitTime + accSendbackQTime), (accQ0WaitTime + accQ1WaitTime + accSendbackQTime) / (double) numRequests));
        System.out.println(String.format("Service time for 0: %f (%f)", accService0Time, accService0Time / (double) (numRequests + numSendbacks)));
        System.out.println(String.format("Service time for 1: %f (%f)", accService1Time, accService1Time / (double) numRequestsSentToQ1));
        System.out.println(String.format("Service time for Sendback: %f (%f)", accSendbackServiceTime, accSendbackServiceTime / (double) numRequests));
        System.out.println(String.format("Total service time: %f (%f)",
                accService0Time + accService1Time + accSendbackServiceTime, (accService0Time + accService1Time + accSendbackServiceTime) / (double) numRequests));
        System.out.println(String.format("Total response time: %f (%f)", accResponseTime, accResponseTime / (double) numRequests));
        System.out.println(String.format("%d left in q0 and %d left in q1", q0.size(), q1.size()));
    }
*/

/*
    private double calcResponseTime() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcResponseTime();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcSendBackServiceTime() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcSendbackServiceTime();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcService1Time() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcService1Time();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcService0Time() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcService0Time();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcSendbackQWait() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcSendbackQTime();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcQ1Wait() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcQ1Time();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }

    private double calcQ0Wait() {
        double ret = 0.0;
        for (Request request : requestMap.values()) {
            Double time = request.calcQ0Time();
            ret += (time == null ? 0.0 : time);
        }
        return ret;
    }
*/

    private class Request {
        private final String name;
        private final Double arrival;
        private Double done = null;
        private final int[] timesAtServer = new int[4];
        private Boolean dropped = null;
        private Event lastEvent = null;

        private final List<Event> events = new ArrayList<>(100000);


        public Request(String name, double timestamp) {
            this.name = name;
            arrival = timestamp;
        }

        public String getName() {
            return name;
        }

        public Double getArrival() {
            return arrival;
        }

        public void addEvent(Event evt) {
            if (lastEvent != null && evt.time < lastEvent.time) {
                System.err.println(String.format("ERROR: %s received an out of order event at %f (prev event (%s))", name, evt.time, lastEvent));
            }
            if (evt.operation.contains("OUT")) {
                if (done != null) {
                    System.err.println(String.format("ERROR: %s already has a done time at %f but now we are resetting it to %f", name, done, evt.time));
                }
                done = evt.time;
            }

            if (evt.operation.contains(" TO S0")) {
                System.err.println(String.format("ERROR: %s was sent back to S0 at %f", name, evt.time));
            }
            if (evt.operation.contains(" TO S1")) {
                timesAtServer[1]++;
            } else if (evt.operation.contains(" TO S2")) {
                timesAtServer[2]++;
            } else if (evt.operation.contains(" TO S3")) {
                timesAtServer[3]++;
            } else if (evt.operation.contains("DROP")) {
                if (dropped != null) {
                    System.err.println(String.format("ERROR: %s is being dropped twice at %f", name, evt.time));
                }
                dropped = true;
            }

            events.add(evt);
            lastEvent = evt;
        }

        @Override
        public String toString() {
            return String.format("%s: events: %s ||| RESP: %f, Num times at S1: %d, S2: %d, S3: %d",
                    name,
                    eventsToString(),
                    (done == null ? -1.0 : done - arrival),
                    timesAtServer[1],
                    timesAtServer[2],
                    timesAtServer[3]);
        }

        public String eventsToString() {
            StringBuilder builder = new StringBuilder();
            Double priorTime = 0.0;
            for (Event evt : events) {
                if (builder.length() > 0) {
                    builder.append(" => (")
                            .append(evt.time - priorTime)
                            .append(") => ");
                }
                builder.append(evt);
                priorTime = evt.time;
            }
            return builder.toString();
        }

        /*
        public Double getStart0() {
            return start0;
        }

        public boolean hasStart0() {
            return start0 != null;
        }

        public void setStart0(Double start0) {
            if (start0 < arrival) {
                System.err.println(String.format("%s: start0 (%f) comes before arrival (%f)", name, start0, arrival));
            }
            this.start0 = start0;
        }

        public Double getDone0() {
            return done0;
        }

        public boolean hasDone0() {
            return done0 != null;
        }

        public void setDone0(Double done0) {
            if (done0 < start0 && (sendbackStart == null || done0 < sendbackStart)) {
                System.err.println(String.format("%s: done0 (%f) is before %s (%f)",
                        name,
                        done0,
                        sendbackStart == null ? "start0" : "sendbackStart",
                        sendbackStart == null ? start0 : sendbackStart));
            }
            this.done0 = done0;
        }

        public Double getNext1() {
            return next1;
        }

        public boolean hasNext1() {
            return next1 != null;
        }

        public void setNext1(Double next1) {
            if (next1 < start0) {
                System.err.println(String.format("%s: next1 (%f) came before start0 (%f)", name, next1, start0));
            }
            this.next1 = next1;
        }

        public Double getStart1() {
            return start1;
        }

        public boolean hasStart1() {
            return start1 != null;
        }

        public void setStart1(Double start1) {
            if (start1 < next1) {
                System.err.println(String.format("%s: start1 (%f) came before next1 (%f)", name, start1, next1));
            }
            this.start1 = start1;
        }

        public Double getNext0() {
            return next0;
        }

        public boolean hasNext0() {
            return next0 != null;
        }

        public void setNext0(Double next0) {
            if (next0 < start1) {
                System.err.println(String.format("%s: next0 (%f) came before start1 (%f)", name, next0, start1));
            }
            this.next0 = next0;
        }

        public Double getDone1() {
            return done1;
        }

        public boolean hasDone1() {
            return done1 != null;
        }

        public void setDone1(Double done1) {
            if (done1 < start1) {
                System.err.println(String.format("%s: done1 (%f) came before start1 (%f)", name, done1, start1));
            }
            this.done1 = done1;
        }

        public Double getSendbackStart() {
            return sendbackStart;
        }

        public boolean hasSendbackStart() {
            return sendbackStart != null;
        }

        public void setSendbackStart(Double sendbackStart) {
            if (sendbackStart < next0) {
                System.err.println(String.format("%s: sendbackStart (%f) came before next0 (%f)", name, sendbackStart, next0));
            }
            this.sendbackStart = sendbackStart;
        }

        public Double calcQ0Time() {
            if (start0 == null) {
                return 0.0;
            }
            return start0 - arrival;
        }

        public Double calcQ1Time() {
            if (start1 == null || next1 == null) {
                return 0.0;
            }
            return start1 - next1;
        }

        public Double calcSendbackQTime() {
            if (sendbackStart == null || next0 == null) {
                return 0.0;
            }
            return sendbackStart - next0;
        }

        public double calcTotalQTime() {
            return nullToDouble(calcQ0Time()) + nullToDouble(calcQ1Time()) + nullToDouble(calcSendbackQTime());
        }

        public int calcNumProcesses() {
            int numProcesses = 1;
            if (next1 != null) {
                numProcesses++;
            }
            if (sendbackStart != null) {
                numProcesses++;
            }
            return numProcesses;
        }

        public Double calcService0Time() {
            if (start0 == null || (done0 == null && next1 == null)) {
                return 0.0;
            }
            return (done0 == null || sendbackStart != null ? next1 : done0) - start0;
        }

        public Double calcService1Time() {
            if (start1 == null || (done1 == null && next0 == null)) {
                return 0.0;
            }
            return (done1 == null ? next0 : done1) - start1;
        }

        public Double calcSendbackServiceTime() {
            if (sendbackStart == null || done0 == null) {
                return 0.0;
            }
            return done0 - sendbackStart;
        }

        public Double calcTotalServiceTime() {
            return calcService0Time() + calcService1Time() + calcSendbackServiceTime();
        }

        public Double calcResponseTime() {
            if (done0 == null && done1 == null) {
                return 0.0;
            }
            return (done0 == null ? done1 : done0) - arrival;
        }

        public boolean doesServiceAndQWaitTimeMatchRespTime() {
            double calc = calcResponseTime() - calcTotalServiceTime() - calcTotalQTime();
            return (calc < 0.01 && calc >= -0.00001);
        }

        public boolean isComplete() {
            return done0 != null || done1 != null;
        }

        @Override
        public String toString() {
            return name +
                    ": arrival=" + arrival +
                    (hasStart0() ? (" => " + (start0 - arrival) + " => start0=" + start0) : "") +
                    ((!hasDone0() || hasSendbackStart()) ? "" : " => " + (done0 - start0) + " => done0=" + done0) +
                    (hasNext1() ? (" => " + (next1 - start0) + " => next1=" + next1) : "") +
                    (hasStart1() ? (" => " + (start1 - next1) + " => start1=" + start1) : "") +
                    (hasNext0() ? (" => " + (next0 - start1) + " => next0=" + next0) : "") +
                    (hasDone1() ? (" => " + (done1 - start1) + " => done1=" + done1) : "") +
                    (hasSendbackStart() ? (" => " + (sendbackStart - next0) + " => sendbackStart=" + sendbackStart) : "") +
                    ((hasDone0() && hasSendbackStart()) ? (" => " + (done0 - sendbackStart) + " => done0=" + done0) : "" ) +
                    String.format(", q0 wait: %f, q1 wait: %f, sendback wait: %f, total q wait: %f",
                            nullToDouble(calcQ0Time()), nullToDouble(calcQ1Time()), nullToDouble(calcSendbackQTime()), nullToDouble(calcTotalQTime())) +
                    String.format(", service0: %f, service1: %f, sendbackService: %f, total service: %f",
                            nullToDouble(calcService0Time()), nullToDouble(calcService1Time()), nullToDouble(calcSendbackServiceTime()), nullToDouble(calcTotalServiceTime())) +
                    String.format(", response time: %f", nullToDouble(calcResponseTime()));


            // TODO: QWait, Service time, Response time
        }

        private Double nullToDouble(Double num) {
            return num == null ? 0.0 : num;
        }
*/
    }

    private class Event {
        private final String operation;
        private final double time;

        public Event(String operation, double time) {
            this.operation = operation;
            this.time = time;
        }

        @Override
        public String toString() {
            return String.format("%s @ %s", operation, time);
        }
    }

/*
    private enum Operation {
        ARRIVAL("ARR:"),
        START0("START 0:"),
        START1("START 1:"),
        DONE0("DONE 0:"),
        DONE1("DONE 1:"),
        NEXT0("NEXT 0:"),
        NEXT1("NEXT 1:");

        private String parseString;

        Operation(String parseString) {
            this.parseString = parseString;
        }

        public static Operation parse(String parseString) {
            for (Operation op: Operation.values()) {
                if (op.parseString.equals(parseString)) {
                    return op;
                }
            }
            return null;
        }
    }
*/

    public static final void main(String[] args) {
        if (args.length == 0) {
            System.err.println("ERROR: File name required.");
            System.exit(-1);
        }
        try {
            Hw4Checker checker = new Hw4Checker();
            checker.process(args[0]);
            checker.printRequests();
            checker.printS0ToOtherServerStats();
            checker.printS3ToOtherServerStats();
//            checker.printRequestQWaitTimes();
//            checker.printRequestQLongWaitTimes();
//            checker.printResults();
        } catch(Throwable ex) {
            System.err.println("ERROR: " + ex);
            ex.printStackTrace();
        }
    }
}
