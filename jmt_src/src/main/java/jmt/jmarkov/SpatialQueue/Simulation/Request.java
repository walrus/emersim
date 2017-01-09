package jmt.jmarkov.SpatialQueue.Simulation;

import com.teamdev.jxmaps.DirectionsResult;

import static jmt.jmarkov.SpatialQueue.Simulation.Request.RequestState.*;

/**
 * Requests are sent by Senders to Receivers. They fulfil the same function as Jobs in JMCH.
 */
public class Request implements Comparable<Request> {
    private Client client;

    private int requestId;

    private DirectionsResult directionsResult;

    private double creationTime;

    private double startServiceTime;

    private double finishServiceTime;

    private double nextEventTime;

    private double responseTime;

    private RequestState currentState;

    enum RequestState {IN_QUEUE, BEING_SERVED, FINISHED}

    private int priority;

    public Request(int requestId, double time, Client client, int priority) {
        this.requestId = requestId;
        this.creationTime = time;
        this.currentState = IN_QUEUE;
        this.client = client;
        this.priority = priority;
    }

    @Override
    public int compareTo(Request other) {
        if (other == null) {
            return 0;
        }

        int priorityDiff = this.getPriority() - other.getPriority();

        if (priorityDiff > 0) {
            return 1;
        } else if (priorityDiff < 0) {
            return -1;
        } else {
            double diff = this.getResponseTime() - other.getResponseTime();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void serve(double currentTime, double finishTime) {
        this.currentState = BEING_SERVED;
        this.startServiceTime = currentTime;
        this.finishServiceTime = finishTime;
        this.setNextEventTime(finishTime);
    }

    public void setFinishServiceTime(double time) {
        this.finishServiceTime = time;
    }

    public void finishServing(double time) {
        this.currentState = FINISHED;
        this.finishServiceTime = time;
    }

    public Client getClient() {
        return this.client;
    }

    public double getNextEventTime() {
        return this.nextEventTime;
    }

    public void setNextEventTime(double time) {
        this.nextEventTime = time;
    }

    public void setResponseTime(double time) {
        this.responseTime = time;
    }

    public double getResponseTime() {
        return this.responseTime;
    }

    public RequestState getCurrentState() {
        return this.currentState;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public DirectionsResult getDirectionsResult() {
        return directionsResult;
    }

    public void setDirectionsResult(DirectionsResult directionsResult) {
        this.directionsResult = directionsResult;
    }

    public int getPriority() {return this.priority;}
}
