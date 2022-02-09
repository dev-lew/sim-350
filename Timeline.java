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
        Event eventA = new Event("A", 0.0);
        Event eventB = new Event("B", 0.0);
        double prevTimestampA = 0.0;
        double prevTimestampB = 0.0;
        Boolean stopGeneratingA = false;
        Boolean stopGeneratingB = false;

        tl.addToTimeline(eventA);
        tl.addToTimeline(eventB);

        while (true) {
            if (!stopGeneratingA) {
                eventA = new Event("A", prevTimestampA + Exp.getExp(lambdaA));

                if (eventA.getTimestamp() > time)
                    stopGeneratingA = true;
                else {
                    tl.addToTimeline(eventA);
                    prevTimestampA = eventA.getTimestamp();
                }
            }

            if (!stopGeneratingB) {
                eventB = new Event("B", prevTimestampB + Exp.getExp(lambdaB));

                if (eventB.getTimestamp() > time)
                    stopGeneratingB = true;
                else {
                    tl.addToTimeline(eventB);
                    prevTimestampB = eventB.getTimestamp();
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
