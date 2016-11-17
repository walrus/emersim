package jmt.jmarkov.SpatialQueue.Simulation;

import java.util.Random;

/**
 * Created by risingpo on 16/11/2016.
 */
public class RequestGenerator implements Runnable {

    private boolean running = false;
    private boolean started = false;

    private int maxRequests;
    private SpatialQueueSimulator sim;

    public RequestGenerator(SpatialQueueSimulator sim, int maxRequests) {
        this.sim = sim;
        this.maxRequests = maxRequests;
    }

    public void run() {
        running = true;
        started = true;

        int count = 0;
        while (running == true & count < this.maxRequests) {
            Request newRequest = createRequestWithPoissonDistribution();
            this.sim.enqueueRequest(newRequest);
            this.sim.getQueueDrawer().enterQueue();
            count++;
        }
        running = false;
        //System.out.println("Stopping, total requests served: " + this.getReceiver().getNumberOfRequestsServed());
    }

    public Request createRequestWithPoissonDistribution() {

        int randomInt = new Random().nextInt(this.sim.getRegions().length);
        Sender sender = this.sim.generateNewSenderWithinArea(this.sim.getRegions()[randomInt]);
        Request r = sender.makeRequest(this.sim.getNextRequestID(), this.generateNextTime(10, 3));

        return r;
    }

    public double generateNextTime(float rateParameter, float random_max) {
        return -Math.log(1.0 - Math.random() / (random_max + 1) / rateParameter);
    }

}
