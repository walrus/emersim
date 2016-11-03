package jmt.jmarkov.SpatialQueue;

import jmt.jmarkov.SpatialQueue.Location;

/**
 * Senders send requests to Receivers.
 */
public class Sender {

    private Location location;
    //How many requests the Sender should send
    private int requestsToSend;

    public Sender(Location location) {
        this.location = location;
        //this.requestsToSend = requestsToSend;
    }

    //Return the Sender's location
    public Location getLocation() {
        //TODO: implement
        return location;
    }

    //Send a variable number of requests to the Receiver
    public void sendRequests() {
        //TODO: implement
    }

    //Send a request to the Receiver
    public void makeRequest() {
        //TODO: implement
    }
}
