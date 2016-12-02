package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Utils.Location;

/**
 * Senders send requests to Receivers.
 */
public class Client {

    private final ClientRegion clientRegion;
    private Location location;

    //How many requests the Client should send
    //For now, hard coded to 1
    private int requestsToSend;
    private int requestsSent;

    public Client(ClientRegion clientRegion, Location location) {
        this.clientRegion = clientRegion;
        this.location = location;
        this.requestsToSend = 1;
        this.requestsSent = 0;
    }

    //Return the Client's location
    public Location getLocation() {
        return location;
    }

    //Send a request to the Server
    //Called by the simulator, which provides jobid and time
    Request makeRequest(int jobid, double time) {
        if (requestsSent < requestsToSend) {
            requestsSent++;
            return new Request(jobid, time, this);
        } else {
            return null;
        }
    }

    ClientRegion getClientRegion() {
        return clientRegion;
    }
}
