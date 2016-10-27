package jmt.jmarkov.SpatialQueue;

/**
 * Receivers handle requests from Senders
 */
public class Receiver {

    //Given a (newly arrived) Request, add it to the queue.
    //This should be overridden to implement different behaviours
    public void handleRequest(Request request) {
        //TODO: implement
    }

    // Given a Request object, calculate the response time
    public double calculateResponseTime(Request request) {
        //TODO: implement
        return 1.0;
    }

    //Given the a request, respond to it.
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
