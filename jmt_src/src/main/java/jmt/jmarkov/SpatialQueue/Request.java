package jmt.jmarkov.SpatialQueue;

/**
 * Requests are sent by Senders to Receivers
 */
public class Request {
    private Sender sender;

    public Request(Sender sender) {
        this.sender = sender;
    }

    public Sender getSender() {
        return sender;
    }
}
