import java.text.DecimalFormat;
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

    public static void main(String args[]) {
        double lambdaA = Double.parseDouble(args[0]);
        double lambdaB = Double.parseDouble(args[1]);
        double time = Double.parseDouble(args[2]);

        Timeline tl = new Timeline();
        Event eventAPrev = new Event("A", 0.0);
        Event eventBPrev = new Event("B", 0.0);
        Event eventANext;
        Event eventBNext;
        Boolean stopGeneratingA = false;
        Boolean stopGeneratingB = false;

        tl.addToTimeline(eventAPrev);
        tl.addToTimeline(eventBPrev);

        while (true) {
            if (!stopGeneratingA) {
                eventANext = new Event("A", eventAPrev.getTimestamp() + Exp.getExp(lambdaA));

                if (eventANext.getTimestamp() > time)
                    stopGeneratingA = true;
                else {
                    tl.addToTimeline(eventANext);
                    eventAPrev = eventANext;
                    eventANext = null;
                }
            }

            if (!stopGeneratingB) {
                eventBNext = new Event("B", eventBPrev.getTimestamp() + Exp.getExp(lambdaB));

                if (eventBNext.getTimestamp() > time)
                    stopGeneratingB = true;
                else {
                    tl.addToTimeline(eventBNext);
                    eventBPrev = eventBNext;
                    eventBNext = null;
                }
            }

            if (stopGeneratingA && stopGeneratingB) break;
        }

        DecimalFormat fmt = new DecimalFormat("#.000");

        for (int i = 0; i < tl.getSize(); i++) {
            Event e = tl.popNext();
            String type = e.getType();
            double timestamp = e.getTimestamp();

            System.out.println(type + Integer.toString(e.getEventID()) + ": " +
                               fmt.format(timestamp));
        }
    }
}
