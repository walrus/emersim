package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Gui.Statistics;

public class RequestGenerator implements Runnable {

    private SpatialQueueSimulator sim;
    private double lambda;
    private double averageServiceTime;
    private Statistics stats;

    public RequestGenerator(SpatialQueueSimulator sim, double lambda) {
        this.sim = sim;
        // set lambda to be #(arrivals per millisecond)
        this.lambda = lambda / 1000;
        this.averageServiceTime = sim.getAverageServiceTime();
        stats = new Statistics();
        stats.setLambda(lambda);
    }

    public void run() {
        while (this.sim.isRunning() && this.sim.moreRequests()) {
            Request newRequest = this.sim.createRequest();
            this.sim.enqueueRequest(newRequest);
            this.sim.getQueueDrawer().enterQueue();

            double timeToWait = (1 / lambda) / sim.getTimeMultiplier();
            try {
                Thread.sleep((long) timeToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



