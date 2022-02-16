package hw3;

import java.util.Comparator;
import java.util.PriorityQueue;

class Timeline {
    private PriorityQueue<Event> eventQueue =
        new PriorityQueue<>(Comparator.comparingDouble(Event::getTimestamp));

    void addToTimeline(Event e) {
        eventQueue.add(e);
    }

    Event popNext() {
        return eventQueue.poll();
    }

    public int getSize() {
        return eventQueue.size();
    }
}
