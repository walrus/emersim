package jmt.jmarkov.SpatialQueue;

import jmt.jmarkov.Job;

/**
 * Requests are sent by Senders to Receivers
 */
public class Request extends Job{
    private Sender sender;

    public Request(int jobId, double time){ super(jobId, time); }

    public Sender getSender() {
        return sender;
    }
}
