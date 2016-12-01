package jmt.jmarkov.SpatialQueue.Simulation;

import com.teamdev.jxmaps.DirectionsLeg;
import com.teamdev.jxmaps.DirectionsResult;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Receivers handle requests from Senders
 */
public class Server {

    private final MapConfig mapConfig;
    private Location location;

    private PriorityQueue<Request> requestQueue;

    private LinkedList<Request> servedRequests;

    private double averageServiceTime;

    // True iff a request is currently being served
    private boolean serving;

    // The request currently being served
    private Request currentRequest;

    public Server(MapConfig mapConfig, Location location) {
        this.mapConfig = mapConfig;
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
    Request serveRequest(double currentTime) {
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

    void stopServing(double currentTime) {
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
    void handleRequest(Request request, boolean returnJourney) {
        calculateResponseTime(request, returnJourney);
        this.requestQueue.offer(request);
    }

    // Given a Request object, calculate the response time in seconds and store it in the request
    void calculateResponseTime(Request request, boolean returnJourney) {
        Location senderLocation = request.getClient().getLocation();
        Location receiverLocation = this.getLocation();

        DirectionsResult directionsResult = mapConfig.handleDirectionCall(senderLocation.getX(), senderLocation.getY(), receiverLocation.getX(), receiverLocation.getY());
        DirectionsLeg[] legs = directionsResult.getRoutes()[0].getLegs();
        // Journey duration converted into milliseconds
        double time = legs[0].getDuration().getValue() * 1000;
        // Store directions for later
        request.setDirectionsResult(directionsResult);

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

    Request getNextRequest() {
        return this.requestQueue.poll();
    }

    double getAverageServiceTime() {
        return this.averageServiceTime;
    }

    public PriorityQueue<Request> getQueue() {
        return this.requestQueue;
    }

    int getNumberOfRequestsServed() {
        return this.servedRequests.size();
    }
}
