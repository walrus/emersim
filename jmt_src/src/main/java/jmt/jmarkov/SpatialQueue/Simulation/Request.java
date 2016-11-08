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

    private double nextEventTime; // Needed?

    private RequestState currentState;

    enum RequestState {IN_QUEUE, BEING_SERVED, FINISHED}

    public Request(int requestId, double time, Sender sender){
        this.requestId = requestId;
        this.creationTime = time;
        this.currentState = IN_QUEUE;
        this.sender = sender;
    }

    public void serve(double time){
        this.currentState = BEING_SERVED;
        this.startServiceTime = time;
    }

    public void finishServing(double time){
        this.currentState = FINISHED;
        this.finishServiceTime = time;
    }

    public Sender getSender() {
        return sender;
    }
}
