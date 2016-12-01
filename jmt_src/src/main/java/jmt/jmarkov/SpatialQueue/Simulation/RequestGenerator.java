package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Gui.Statistics;

public class RequestGenerator implements Runnable {

    private SpatialQueueSimulator sim;
    private double lambda;
    private Statistics stats;

    RequestGenerator(SpatialQueueSimulator sim, double lambda) {
        this.sim = sim;
        // set lambda to be #(arrivals per millisecond)
        this.lambda = lambda / 1000;
        stats = new Statistics();
        stats.setLambda(this.lambda);
        stats.setSI(sim.getAverageServiceTime());
    }

    public void run() {
        while (this.sim.isRunning() && this.sim.moreRequests()) {
            stats.setSI(sim.getAverageServiceTime());
            stats.setLambda(lambda * 1000);
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

    public Statistics getStats() {
        return stats;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
}



