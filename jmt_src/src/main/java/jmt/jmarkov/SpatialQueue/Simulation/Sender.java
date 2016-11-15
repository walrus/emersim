package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Location;

/**
 * Senders send requests to Receivers.
 */
public class Sender {

    private Location location;

    //How many requests the Sender should send
    //For now, hard coded to 1
    private int requestsToSend;
    private int requestsSent;

    public Sender(Location location) {
        this.location = location;
        this.requestsToSend = 1;
        this.requestsSent = 0;
    }

    //Return the Sender's location
    public Location getLocation() {
        return location;
    }

    //Send a request to the Receiver
    //Called by the simulator, which provides jobid and time
    public Request makeRequest(int jobid, double time) {
        if (requestsSent < requestsToSend) {
            requestsSent++;
            return new Request(jobid, time, this);
        } else {
            return null;
        }
    }
}
