package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Job;
import jmt.jmarkov.Queues.Processor;
import jmt.jmarkov.SpatialQueue.Simulation.Sender;

import static jmt.jmarkov.SpatialQueue.Simulation.Request.RequestState.BEING_SERVED;
import static jmt.jmarkov.SpatialQueue.Simulation.Request.RequestState.FINISHED;
import static jmt.jmarkov.SpatialQueue.Simulation.Request.RequestState.IN_QUEUE;

/**
 * Requests are sent by Senders to Receivers. They fulfil the same function as Jobs in JMCH.
 */
public class Request{
    private Sender sender;

    private int requestId;

    private double creationTime;

    private double startServiceTime;

    private double finishServiceTime;

    private double nextEventTime;

    private double responseTime;

    private RequestState currentState;

    enum RequestState {IN_QUEUE, BEING_SERVED, FINISHED}

    public Request(int requestId, double time, Sender sender){
        this.requestId = requestId;
        this.creationTime = time;
        this.currentState = IN_QUEUE;
        this.sender = sender;
    }

    public void serve(double currentTime, double finishTime){
        this.currentState = BEING_SERVED;
        this.startServiceTime = currentTime;
        this.finishServiceTime = finishTime;
        this.setNextEventTime(finishTime);
    }

    public void setFinishServiceTime(double time){
        this.finishServiceTime = time;
    }

    public void finishServing(double time){
        this.currentState = FINISHED;
        this.finishServiceTime = time;
    }

    public Sender getSender() {
        return this.sender;
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
}
