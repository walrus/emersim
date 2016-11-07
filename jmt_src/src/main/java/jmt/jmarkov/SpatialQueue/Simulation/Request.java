package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Job;
import jmt.jmarkov.SpatialQueue.Simulation.Sender;

/**
 * Requests are sent by Senders to Receivers
 */
public class Request extends Job{
    private Sender sender;

    public Request(int jobId, double time, Sender sender){
        super(jobId, time);
        this.sender = sender;
    }

    public Sender getSender() {
        return sender;
    }
}
