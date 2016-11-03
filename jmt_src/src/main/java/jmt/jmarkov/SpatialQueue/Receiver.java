package jmt.jmarkov.SpatialQueue;

import java.util.LinkedList;
/**
 * Receivers handle requests from Senders
 */
public class Receiver {

    private Location location;

    private LinkedList<Request> requestQueue = new LinkedList<>();

    public Receiver(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    //Given a (newly arrived) Request, add it to the queue.
    //This should be overridden to implement different behaviours
    //TODO: actual implementation rather than default from JMCH
    public void handleRequest(Request request) {
        int i;
        for (i = 0; i < this.requestQueue.size(); i++) {
            if (this.requestQueue.get(i).getNextEventTime() > request.getNextEventTime()) {
                break;
            }
        }
        this.requestQueue.add(i, request);
    }

    // Given a Request object, calculate the response time in seconds
    public double calculateResponseTime(Request request) {
        Location senderLocation = request.getSender().getLocation();
        Location receiverLocation = this.getLocation();

        double xDistance = senderLocation.getX() - receiverLocation.getX();
        double yDistance = senderLocation.getY() - receiverLocation.getY();

        //Straight line distance in degrees
        return Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
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
