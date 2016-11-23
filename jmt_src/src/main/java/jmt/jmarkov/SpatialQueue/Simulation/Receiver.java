package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Queues.JobQueue;
import jmt.jmarkov.Queues.QueueLogic;
import jmt.jmarkov.SpatialQueue.Location;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Receivers handle requests from Senders
 */
public class Receiver {

    private Location location;

    private PriorityQueue<Request> requestQueue;

    private LinkedList<Request> servedRequests;

    private JobQueue q;

    private QueueLogic ql;

    private double averageServiceTime;

    // True iff a request is currently being served
    private boolean serving;

    // The request currently being served
    private Request currentRequest;

    public Receiver(Location location) {
        this.location = location;
        this.serving = false;
        this.currentRequest = null;
        this.requestQueue = new PriorityQueue<>();
        this.servedRequests = new LinkedList<>();
        this.averageServiceTime = 0;
    }

    public Location getLocation() {
        return location;
    }

    // Get the first request from the queue and serve it.
    // Performs a similar function to process in Processor from JMCH
    public Request serveRequest(double currentTime) {
        try {
            if (!this.isServing()) {
                if (!requestQueue.isEmpty()) {
                    Request request = getNextRequest();
                    request.serve(currentTime, currentTime + request.getResponseTime());
                    System.out.println("Serving: " + request.getRequestId() + ", distance: " + request.getResponseTime());
                    setServing(true);
                    this.currentRequest = request;
                    return request;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopServing(double currentTime) {
        this.setServing(false);
        this.currentRequest.finishServing(currentTime);
        this.servedRequests.add(this.currentRequest);
        this.currentRequest = null;
        this.updateAverageServiceTime(currentTime);
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
    public void handleRequest(Request request, boolean returnJourney) {
        calculateResponseTime(request, returnJourney);
        this.requestQueue.offer(request);
    }

    // Given a Request object, calculate the response time in seconds and store it in the request
    public void calculateResponseTime(Request request, boolean returnJourney) {
        Location senderLocation = request.getSender().getLocation();
        Location receiverLocation = this.getLocation();

        double xDistance = senderLocation.getX() - receiverLocation.getX();
        double yDistance = senderLocation.getY() - receiverLocation.getY();

        //Straight line distance in degrees
        double time = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

        if (returnJourney) {
            request.setResponseTime(time * 2);
        } else {
            request.setResponseTime(time);
        }
    }

    private void updateAverageServiceTime(double currentTime) {
        if (this.servedRequests.isEmpty()) {
            this.averageServiceTime = 0;
        } else {
            this.averageServiceTime = currentTime / this.servedRequests.size();
        }
    }

    public Request getNextRequest() {
        return this.requestQueue.poll();
    }

    public double getAverageServiceTime() {
        return this.averageServiceTime;
    }

    public PriorityQueue<Request> getQueue() {
        return this.requestQueue;
    }

    public int getNumberOfRequestsServed() {
        return this.servedRequests.size();
    }
}
