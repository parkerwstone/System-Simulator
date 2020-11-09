package hw3;

import java.util.*;

/***************************************************/
/* CS-350 Fall 2020 - Homework 2 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a single-processor server with an infinite    */
/*   request queue and exponentially distributed   */
/*   service times, i.e. a x/M/1 server.           */
/*                                                 */

/***************************************************/

class SimpleServer extends EventGenerator {
    static final int MAX_Q_SIZE = -1;

    private final Queue<Request> theQueue = new LinkedList<>();
  private final Double servTime;
  private final List<Processor> processors = new ArrayList<>();
  private final int maxQSize;
  private final ServiceTimeTable serviceTimeTable;
  private final Random rand = new Random(System.nanoTime());

  /* Statistics of this server --- to construct rolling averages */
  private Double cumulQ = 0.0;
  private Double cumulW = 0.0;
  private int snapCount = 0;
  private int numDrops = 0;

    public SimpleServer(String name, Timeline timeline, Double servTime, int numProcessors) {
        super(timeline, name);
        this.maxQSize = MAX_Q_SIZE;

        this.servTime = servTime;
        this.serviceTimeTable = null;
        createProcessors(name, servTime, numProcessors);
    }

    public SimpleServer(String name,
                        Timeline timeline,
                        ServiceTimeTable serviceTimeTable,
                        int numProcessors,
                        int maxQSize) {
        super(timeline, name);
        this.maxQSize = maxQSize;

        this.serviceTimeTable = serviceTimeTable;
        this.servTime = serviceTimeTable.getMinServiceTime();


        createProcessors(name, serviceTimeTable, numProcessors);
    }

    public SimpleServer(String name,
                        Timeline timeline,
                        Double servTime,
                        int numProcessors,
                        int maxQSize) {
        super(timeline, name);
        this.maxQSize = maxQSize;

        this.servTime = servTime;
        this.serviceTimeTable = null;

        createProcessors(name, servTime, numProcessors);
    }

    public SimpleServer(String name,
                        Timeline timeline,
                        ServiceTimeTable serviceTimeTable,
                        int numProcessors) {
        super(timeline, name);
        this.maxQSize = MAX_Q_SIZE;

        this.serviceTimeTable = serviceTimeTable;
        this.servTime = serviceTimeTable.getMinServiceTime();


        createProcessors(name, serviceTimeTable, numProcessors);
    }

    @Override
    void receiveRequest(Event evt) {
        super.receiveRequest(evt);

        Request curRequest = evt.getRequest();

    curRequest.recordArrival(evt.getTimestamp());

    /* Upon receiving the request, check the queue size and act
     * accordingly */
    if (theQueue.isEmpty() && getFirstAvailableProcessor() != null) {
		super.timeline.addEvent(getFirstAvailableProcessor().start(evt.getTimestamp(), curRequest));
    } else {
        addToQueue(curRequest);
    }
  }

    private void addToQueue(Request curRequest) {
      if (maxQSize != MAX_Q_SIZE && (theQueue.size() - getNumActiveRequests()) >= maxQSize) {
          super.timeline.addEvent(new Event(EventType.DROPPED, curRequest, curRequest.getArrival(), this));
          numDrops++;
          System.out.println(curRequest + " DROP " + this + ": " + curRequest.getArrival());
          return;
      }
        theQueue.add(curRequest);
    }

    private int getNumActiveRequests() {
      int numRequests = 0;
        for (Processor processor : processors) {
            if (processor.isBusy()) {
                numRequests++;
            }
        }
        return numRequests;
    }

    @Override
    void releaseRequest(Event evt) {
	/* What request we are talking about? */
        Request curRequest = evt.getRequest();

    Processor processor = getProcessorWithRequest(curRequest);
    processor.end(evt.getTimestamp());

    assert super.next != null;
    super.next.receiveRequest(evt);

    /* Any new request to put into service?  */
    if (!theQueue.isEmpty()) {
		Processor availableProcessor;
    	while ((availableProcessor = getFirstAvailableProcessor()) != null) {
    		if (!availableProcessor.isBusy()) {
				super.timeline.addEvent(getFirstAvailableProcessor().start(evt.getTimestamp(), theQueue.poll()));
			}
		}
    }
  }

  private Processor getFirstAvailableProcessor() {
        if (processors.size() == 1) {
            return processors.get(0).isBusy() ? null : processors.get(0);
      }
      List<Processor> availableProcessors = new ArrayList<>();
  	for (Processor processor : processors) {
  		if (!processor.isBusy()) {
  			availableProcessors.add(processor);
		}
	}
	if (availableProcessors.isEmpty()) {
  	    return null;
    }
	if (availableProcessors.size() == 1) {
  	    return availableProcessors.get(0);
    }
    int idx = rand.nextInt(availableProcessors.size());
  	return availableProcessors.get(idx);
  }

  private Processor getProcessorWithRequest(Request request) {
  	for (Processor processor : processors) {
  		if (processor.hasRequest(request)) {
  			return processor;
		}
	}
	return null;
  }

  @Override
  Double getRate() {
    return 1.0 / this.servTime;
  }

  @Override
  void executeSnapshot() {
        int numRequests = theQueue.size() + getNumActiveRequests();
    snapCount++;
    cumulQ += numRequests;
    cumulW += Math.max(theQueue.size(), 0);
  }

  @Override
  void printStats(Double time) {
      System.out.println();
  	Double cumulTq = 0.0;
  	Double servedReqs = 0.0;
  	for (Processor processor : processors) {
  		cumulTq += processor.getCumulTq();
  		servedReqs += processor.getProcessedReqs();
  		processor.printUtilStat(time);
	}
      System.out.println(this + " QLEN: " + cumulQ / (double) snapCount);
      System.out.println(this + " TRESP: " + cumulTq / servedReqs);
      if (maxQSize != MAX_Q_SIZE) {
          System.out.println(this + " DROPPED: " + numDrops);
      }
  }

    @Override
    public String toString() {
        return (this.name != null ? this.name : "");
    }

    private void createProcessors(String name, Double servTime, int numProcessors) {
        for (int i = 0; i < numProcessors; i++) {
            processors.add(new Processor(this, servTime, name + (numProcessors > 1 ? "," + (i + 1) : "")));
        }
    }

    private void createProcessors(String name, ServiceTimeTable servTime, int numProcessors) {
        for (int i = 0; i < numProcessors; i++) {
            processors.add(new Processor(this, servTime, name + (numProcessors > 1 ? "," + (i + 1) : "")));
        }
    }

    public int getSnapCount() {
        return snapCount;
    }

    public double getNumRequestsBeingHandled() {
        return cumulQ;
    }

    public double getTotal() {
        return ((double) cumulQ / (double) snapCount);
    }
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
