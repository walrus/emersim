package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Random;

/**
 * Created by risingpo on 16/11/2016.
 */
public class RequestRunnable extends SpatialQueueSimulator implements Runnable {

    private boolean running = false;
    private boolean started = false;

    public RequestRunnable(double timeMultiplier, QueueDrawer queueDrawer, Receiver receiver, MapConfig mapConfig, int maxRequests) {
        super(timeMultiplier, queueDrawer, receiver, mapConfig, maxRequests);
    }

    @Override
    public void run() {
        running = true;
        started = true;
        
        int count = 0;
        while (running == true & count < this.getMaxRequestNumber()) {
            Request newRequest = createRequestWithPoissonDistribution();
            enqueueRequest(newRequest);
            this.getQueueDrawer().enterQueue();
            count++;
        }
        running = false;
        System.out.println("Stopping, total requests served: " + this.getReceiver().getNumberOfRequestsServed());
    }

    public Request createRequestWithPoissonDistribution() {

        int randomInt = new Random().nextInt(this.getRegions().length);
        Sender sender = this.generateNewSenderWithinArea(this.getRegions()[randomInt]);
        Request r = sender.makeRequest(getNextRequestID(), this.generateNextTime(10, 3));

        return r;
    }

    public double generateNextTime(float rateParameter, float random_max) {
        return -Math.log(1.0 - Math.random() / (random_max + 1) / rateParameter);
    }

}
