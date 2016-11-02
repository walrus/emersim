package jmt.jmarkov.SpatialQueue;

/**
 * Receivers handle requests from Senders
 */
public class Receiver {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public Receiver(Location location) {
        this.location = location;
    }

    //Given a (newly arrived) Request, add it to the queue.
    //This should be overridden to implement different behaviours
    public void handleRequest(Request request) {
        //TODO: implement
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

    //Given the request, respond to it.
    public void serveRequest(Request request) {
        //TODO: implement
    }

    //Find the next request in the queue. This should
    //be overridden to implement different behaviours
    public Request getNextRequest() {
        //TODO: implement
        return null;
    }
}
