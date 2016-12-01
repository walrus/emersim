package jmt.jmarkov.SpatialQueue.Simulation;

import jmt.jmarkov.SpatialQueue.Gui.Statistics;

public class RequestGenerator implements Runnable {

    private SpatialQueueSimulator sim;
    private double lambda;
    private Statistics stats;

    RequestGenerator(SpatialQueueSimulator sim, double lambda) {
        this.sim = sim;
        // set lambda to be #(arrivals per millisecond)
        this.lambda = lambda;
        stats = new Statistics();
        stats.setLambda(lambda);
        stats.setSI(sim.getAverageServiceTime());
    }

    public void run() {
        while (this.sim.isRunning() && this.sim.moreRequests()) {
            stats.setSI(sim.getAverageServiceTime());
            stats.setLambda(lambda);
            Request newRequest = this.sim.createRequest();
            this.sim.enqueueRequest(newRequest);
            this.sim.getQueueDrawer().enterQueue();

            double timeToWait = getNextArrivalTime(lambda) / sim.getTimeMultiplier();
            try {
                Thread.sleep((long) timeToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double getNextArrivalTime(double lambda){
        return (Math.log(1.0-Math.random())/-(lambda*1000));
    }

    public Statistics getStats() {
        return stats;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
}



