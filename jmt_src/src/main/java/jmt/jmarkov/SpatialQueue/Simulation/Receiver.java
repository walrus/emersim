package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Queues.JobQueue;
import jmt.jmarkov.Queues.QueueLogic;
import jmt.jmarkov.SpatialQueue.Location;

import java.util.LinkedList;
/**
 * Receivers handle requests from Senders
 */
public class Receiver {

    private Location location;

    private LinkedList<Request> requestQueue;

    private JobQueue q;

    private QueueLogic ql;

    // True iff a request is currently being served
    private boolean serving;

    // The request currently being served
    private Request currentRequest;

    public Receiver(Location location) {
        this.location = location;
        this.serving = false;
        this.currentRequest = null;
        this.requestQueue = new LinkedList<>();
    }

    public Location getLocation() {
        return location;
    }

    // Get the first request from the queue and serve it.
    // Performs a similar function to process in Processor from JMCH
    public void serveRequest(double currentTime) {
        try {
            if (!this.isServing()) {
                if (!requestQueue.isEmpty()) {
                    Request request = getNextRequest();
                    request.serve(currentTime, currentTime + request.getResponseTime());
                    requestQueue.getFirst().setNextEventTime(currentTime + request.getResponseTime());
                    setServing(true);
                    currentRequest = request;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServing() {
        this.setServing(false);
    }

    private boolean isServing() {
        return this.serving;
    }

    private void setServing(boolean serving) {
        this.serving = serving;
    }

    // Given a (newly arrived) Request, add it to the queue.
    // This implementation adds requests in strict order of response time
    // Can be overridden to implement different behaviours
    public void handleRequest(Request request) {
        calculateResponseTime(request);
        int i;
        for (i = 0; i < this.requestQueue.size(); i++) {
            if (this.requestQueue.get(i).getResponseTime() < request.getResponseTime()) {
                break;
            }
        }
        this.requestQueue.add(i, request);
    }

    // Given a Request object, calculate the response time in seconds and store it in the request
    public void calculateResponseTime(Request request) {
        Location senderLocation = request.getSender().getLocation();
        Location receiverLocation = this.getLocation();

        double xDistance = senderLocation.getX() - receiverLocation.getX();
        double yDistance = senderLocation.getY() - receiverLocation.getY();

        //Straight line distance in degrees
        double time = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
        request.setResponseTime(time);
    }

    //Find the next request in the queue. This should
    //be overridden to implement different behaviours
    public Request getNextRequest() {
        //TODO: implement better algorithm that uses distances
        return this.requestQueue.removeFirst();
    }

    public LinkedList<Request> getQueue() {
        return this.requestQueue;
    }
}
