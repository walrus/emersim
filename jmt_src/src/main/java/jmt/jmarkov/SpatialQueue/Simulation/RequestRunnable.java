package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Date;
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

        double currentTimeMultiplied;

        long realTimeStart;

        long realTimeCurrent;

        currentTimeMultiplied = 0;

        realTimeStart = new Date().getTime();

        int count = 0;
        while (count < this.getMaxRequestNumber()) {
            Request newRequest = createRequestWithPoissonDistribution();
            enqueueRequest(newRequest);
            this.getQueueDrawer().enterQueue();
            count++;
        }

        while (moreRequests()) {
            if (this.getReceiver().getQueue().size() > 0) {
                // Serve the next request and grab a link to the request being served
                Request currentRequest = this.getReceiver().serveRequest(currentTimeMultiplied);
                currentTimeMultiplied += (currentRequest.getNextEventTime() - this.getCurrentTime()) / this.getTimeMultiplier();
                //this is calculating how long system will sleep
                realTimeCurrent = new Date().getTime() - realTimeStart;

                // If necessary, sleep
                if ((long) currentTimeMultiplied > realTimeCurrent) {
                    try {
                        Thread.sleep((long) currentTimeMultiplied - realTimeCurrent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    realTimeCurrent = new Date().getTime() - realTimeStart;
                }

                //Having waited till the request has been served, deal with it
                double currentTime = currentRequest.getNextEventTime();
                this.getReceiver().stopServing(currentTime);

                // update queue visualisation
                this.getQueueDrawer().exitQueue();

            } else {
                // No requests in queue, so just loop till another is added
                System.out.println("Total requests served: " + this.getReceiver().getNumberOfRequestsServed());
                break;
            }

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
