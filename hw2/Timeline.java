package hw2;

import java.util.Comparator;
import java.util.PriorityQueue;

class Timeline {
    private PriorityQueue<Event> eventQueue =
        new PriorityQueue<>(Comparator.comparingDouble(Event::getTimestamp));

    void addToTimeline(Event evtToAdd) {
        eventQueue.add(evtToAdd);
    }

    Event popNext() {
        return eventQueue.poll();
    }

    public int getSize() {
        return eventQueue.size();
    }
}
