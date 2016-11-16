package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Random;

/**
 * Created by risingpo on 16/11/2016.
 */
public class RequestRunnable extends SpatialQueueSimulator implements Runnable {

    public RequestRunnable(double timeMultiplier, Notifier[] notifier, Receiver receiver, MapConfig mapConfig, int maxRequests) {
        super(timeMultiplier, notifier, receiver, mapConfig, maxRequests);
    }

    @Override
    public void run() {
        int count = 0;
        while (count < this.getMaxRequestNumber()) {
            enqueueRequest(createRequestWithPoissonDistribution());
            count++;
        }
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
