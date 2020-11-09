package hw3;
import java.lang.*;
import java.util.*; 

/***************************************************/
/* CS-350 Fall 2020 - Homework 1 - Code Solution   */
/* Author: Renato Mancuso (BU)                     */
/*                                                 */
/* Description: This class implements the logic of */
/*   a chronologically sorted list of events.      */
/*                                                 */
/***************************************************/

public class Timeline {

    private PriorityQueue<Event> _timeline = new PriorityQueue<Event>();

    /* No need for a constructor. The default implicit constructor
     * should be just fine. */
    
    /* Wrapper to add events to the timeline */
    public void addEvent(Event evt) {
	_timeline.add(evt);
    }

    /* Wrapper to retrieve and remove the oldest event in the
     * timeline */
    public Event popEvent() {
	return _timeline.poll();
    }
    
}

/* END -- Q1BSR1QgUmVuYXRvIE1hbmN1c28= */
