package jmt.jmarkov.Queues;

import jmt.jmarkov.Queues.Exceptions.InfiniteBufferException;
import jmt.jmarkov.Queues.Exceptions.NoJobsException;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;

/**
 *  Contatins the logic for a spatial queue
 *
 */
public class SpatialQLogic implements QueueLogic {


    @Override
    public double getStatusProbability(int status) throws NonErgodicException {
        return 0;
    }

    @Override
    public double getRunTime() {
        return 0;
    }

    @Override
    public double getArrivalTime() throws NoJobsException {
        return 0;
    }

    @Override
    public int getMaxStates() throws InfiniteBufferException {
        return 0;
    }

    @Override
    public double mediaJobs() throws NonErgodicException {
        return 0;
    }

    @Override
    public double utilization() throws NonErgodicException {
        return 0;
    }

    @Override
    public double throughput() throws NonErgodicException {
        return 0;
    }

    @Override
    public double responseTime() throws NonErgodicException {
        return 0;
    }

    @Override
    public int getNumberServer() {
        return 0;
    }
}
