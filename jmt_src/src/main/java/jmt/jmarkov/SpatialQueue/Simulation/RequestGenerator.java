package jmt.jmarkov.SpatialQueue.Simulation;

import java.util.Date;

/**
 * Created by risingpo on 16/11/2016.
 */
public class RequestGenerator implements Runnable {

    private SpatialQueueSimulator sim;
    private double currentTime;
    private float maxInterval;

    public RequestGenerator(SpatialQueueSimulator sim, float maxInterval) {
        this.sim = sim;
        this.maxInterval = maxInterval;
        this.currentTime = 0;
    }

    public void run() {
        double currentTimeMultiplied;
        //Time when run() was called
        long realTimeStart;
        //Time after sleeping the thread
        long realTimeCurrent;
        currentTimeMultiplied = 0;
        realTimeStart = new Date().getTime();
        double nextInterArrivalTime;

        while (this.sim.isRunning() && this.sim.moreRequests()) {
            Request newRequest = this.sim.createRequest();
            this.sim.enqueueRequest(newRequest);
            this.sim.getQueueDrawer().enterQueue();

            nextInterArrivalTime = generateNextTime(this.sim.getLambda(), this.maxInterval);

            currentTimeMultiplied += (nextInterArrivalTime) / this.sim.getTimeMultiplier();
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

            //Increment the current time by a random (poisson) amount
            currentTime += nextInterArrivalTime;
        }
    }


    public double generateNextTime(float rateParameter, float random_max) {
        return -Math.log(1.0 - Math.random() / (random_max + 1) / rateParameter);
    }

}



