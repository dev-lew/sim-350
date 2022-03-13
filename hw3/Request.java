package hw3;

class Request {
    private double arrivalTime;
    private double startTime;
    private double finishTime;
    private int requestID;
    private static int numRequests = 0;
    private static double totalResponseTime = 0;
    private static double totalWaitingTime = 0;

    Request() {
        this.requestID = numRequests++;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public int getRequestID() {
        return requestID;
    }

    public static int getNumRequests() {
        return numRequests;
    }

    public static double getTotalResponseTime() {
        return totalResponseTime;
    }

    public static double getTotalWaitingTime() {
        return totalWaitingTime;
    }

    public static void incrementTotalResponseTime(double t) {
        totalResponseTime += t;
    }

    public static void incrementTotalWaitingTime(double t) {
        totalWaitingTime = t;
    }

    @Override
    public String toString() {
        return "Request [arrivalTime=" + arrivalTime + ", finishTime=" +
            finishTime + ", requestID=" + requestID + ", startTime=" + startTime + "]";
    }
}
