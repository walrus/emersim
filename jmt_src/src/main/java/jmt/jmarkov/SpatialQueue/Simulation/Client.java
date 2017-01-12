package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Senders send requests to Receivers.
 */
public class Client {

    private final ClientRegion clientRegion;
    private Location location;
    private int priorityLevels;
    private GuiComponents.QUEUE_MODE queueMode;

    //How many requests the Client should send
    //For now, hard coded to 1
    private int requestsToSend;
    private int requestsSent;

    public Client(ClientRegion clientRegion, Location location, int priorityLevels, GuiComponents.QUEUE_MODE queueMode) {
        this.clientRegion = clientRegion;
        this.location = location;
        this.requestsToSend = 1;
        this.requestsSent = 0;
        this.priorityLevels = priorityLevels;
        this.queueMode = queueMode;
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

            int priority = 0;

            if (priorityLevels > 0) {
                priority = ThreadLocalRandom.current().nextInt(1, priorityLevels + 1);
            }

            return new Request(jobid, time, this, priority, queueMode);
        } else {
            return null;
        }
    }

    ClientRegion getClientRegion() {
        return clientRegion;
    }
}
