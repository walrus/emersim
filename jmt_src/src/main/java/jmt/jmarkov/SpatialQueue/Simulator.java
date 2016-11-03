/**
 * Based on the standard Jmarkov Simulator
 */

package jmt.jmarkov.SpatialQueue;


import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.Job;
import jmt.jmarkov.Queues.Arrivals;
import jmt.jmarkov.Queues.Processor;
import jmt.jmarkov.SpatialQueue.Location;
import jmt.jmarkov.SpatialQueue.Sender;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Simulator implements Runnable {

    //list which contains jobs waiting in the simulator.
    private LinkedList<Job> jobList;

    private Location[] area;

    private Notifier[] notifier;

    //current simulation time
    private double currentTime;// in milliseconds

    //if lambda is zero this value is set to true
    //(if lambda set to zero new jobs will not be created
    private boolean lambdaZero = false;

    //Arrival class
    private Arrivals arrival;

    //array of processor
    private Processor[] processors;

    //it saves the data if the simulator is paused or not
    private boolean paused = false;

    //multiplier is used for multiplier for timer.(comes from simulation time slide bar)
    private double timeMultiplier = 1.0;

    private boolean running = false;

    private boolean started = false;

    public Simulator(Arrivals arrival, Processor[] processors, double timeMultiplier, Notifier[] notifier, Location[] area) {
        super();

        jobList = new LinkedList<Job>();
        this.arrival = arrival;
        currentTime = 0;
        this.processors = processors;
        setTimeMultiplier(timeMultiplier);
        this.notifier = notifier;
        this.area = area;
    }

    public static boolean isLocationWithinArea(Location location, Location[] polygon) {
        double minX = polygon[0].getX();
        double maxX = polygon[0].getX();
        double minY = polygon[0].getY();
        double maxY = polygon[0].getY();

        for (int i = 1; i < polygon.length; i++) {
            Location l = polygon[i];
            minX = Math.min(minX, l.getX());
            maxX = Math.max(maxX, l.getX());
            minY = Math.min(minY, l.getY());
            maxY = Math.max(maxY, l.getY());
        }

        if (location.getX() < minX || location.getX() > maxX
                || location.getY() < minY || location.getY() > maxY) {
            return false;
        }

        boolean inside = false;

        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((polygon[i].getY() > location.getY()) != (polygon[j].getY() > location.getY()) &&
                    location.getX() < (polygon[j].getX() - polygon[i].getX())
                            * (location.getY() - polygon[i].getY())
                            / (polygon[j].getY() - polygon[i].getY()) + polygon[i].getX()) {
                inside = !inside;
            }
        }
        return inside;
    }


    public Sender generateNewSenderWithinArea(Location location, Location[] area) {
        if (isLocationWithinArea(location, area)) {
            Sender sender = new Sender();
            //setLocation maybe?
        }
    }
    public void run() {
        running = true;
        started = true;
        // this is the simulation time till run command is called
        double currentTimeMultiplied;
        //when calling run getting the current real time
        long realTimeStart;
        //this is the time after return the thread.sleep
        long realTimeCurrent;
        currentTimeMultiplied = 0;
        realTimeStart = new Date().getTime();

        //this is the first job which is created
        //if job list is not empty this means run is called from the paused situation
        if (jobList.isEmpty()) {
            arrival.createJob(currentTime); // this is the first job which is created after pressed start
            //it is called in order calculate first arrival time to the system
        }

        //if there is still some job is waiting for processing it is running recursive
        //if paused the running will stop.
        while (jobList.size() > 0 && !paused) {
            //this is calculating how long system will sleep
            currentTimeMultiplied += (peekJob().getNextEventTime() - currentTime) / timeMultiplier;
            //this is calculating how long system will sleep
            realTimeCurrent = new Date().getTime() - realTimeStart;

            //this is for calculating if the system will pause or not
            if ((long) currentTimeMultiplied > realTimeCurrent) {
                try {
                    Thread.sleep((long) currentTimeMultiplied - realTimeCurrent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                realTimeCurrent = new Date().getTime() - realTimeStart;
            }

            Job job = dequeueJob();
            currentTime = job.getNextEventTime();
            switch (job.getCurrentStateType()) {
                case Job.CURRENT_STATE_CREATED:
                    newJobArrival(job);
                    break;
                case Job.CURRENT_STATE_ANIMATION:
                    if ((long) currentTimeMultiplied + 300 > realTimeCurrent) {
                        animate(job);
                    }
                    //No animation
                    break;
                case Job.CURRENT_STATE_IN_CPU:
                    exitProcessor(job);
                    break;
            }
        }
        running = false;
    }

    private void newJobArrival(Job job) {
        //the job whose arrival time has calculated entering the queue
        arrival.addQ(job, currentTime);
        //the new job is creating for calculating arrival time
        arrival.createJob(currentTime);
        // if (numberOfTotalProcessor > numberOfWorkingProcessor) {
        startProcess();// if there is an empty processor start it
        //for the red circle animation
        createAnimation(job);
    }

    private void createAnimation(Job job) {
        Job cloneJob;
        double nextEventTime;
        if (!jobList.isEmpty()) {
            nextEventTime = peekJob().getNextEventTime();
        } else {
            nextEventTime = currentTime + 100;
        }
        cloneJob = (Job) job.clone();
        cloneJob.setAnimationTime(currentTime + (nextEventTime - currentTime) * 1 / 4);
        this.enqueueJob(cloneJob);
        cloneJob = (Job) job.clone();
        cloneJob.setAnimationTime(currentTime + (nextEventTime - currentTime) * 2 / 4);
        this.enqueueJob(cloneJob);
        cloneJob = (Job) job.clone();
        cloneJob.setAnimationTime(currentTime + (nextEventTime - currentTime) * 3 / 4);
        this.enqueueJob(cloneJob);
    }

    private void animate(Job job) {
        arrival.animate(job, currentTime);
        for (Processor processor : processors) {
            processor.animate(currentTime);
        }
    }

    private void startProcess() {// if there is an empty processor start it
        int i;
        //assign randomly to free one
        int numFreeProcessor = 0;
        for (i = 0; i < processors.length; i++) {
            if (!processors[i].isProcessing()) {
                numFreeProcessor++;
            }
        }
        if (numFreeProcessor != 0) {
            int assigned = new Random().nextInt(numFreeProcessor);
            for (i = 0; i < processors.length; i++) {
                if (!processors[i].isProcessing()) {
                    if (assigned == 0) {
                        processors[i].process(currentTime);
                        break;
                    }
                    assigned--;
                }
            }
        }

    }

    private void exitProcessor(Job job) {
        job.getProcessor().endProcessing(currentTime);// this processor is stopped
        job.setStateExitSystem();// set the state of the job as finished
        for (Notifier element : notifier) {
            element.exitSystem(job.getJobId(), job.getProcessorId(), job.getEnteringQueueTime(), job.getEnteringCpuTime(), job.getSystemExitTime());
        }
        startProcess();
        createAnimation(job);
    }

    public void enqueueJob(Job newJob) {// priority queue wrt their next job
        int i;
        for (i = 0; i < jobList.size(); i++) {
            if (jobList.get(i).getNextEventTime() > newJob.getNextEventTime()) {
                break;
            }
        }
        jobList.add(i, newJob);
    }

    public Job dequeueJob() {
        return jobList.removeFirst();
    }

    public Job peekJob() {
        return jobList.element();
    }

    public boolean isLambdaZero() {
        return lambdaZero;
    }

    public void setLambdaZero(boolean lambdaZero) {
        //This doesn't seem to actually set lambda to zero?
        this.lambdaZero = lambdaZero;
    }

    public void pause() {
        if (paused) {
            paused = false;
            start();
        } else {
            paused = true;
        }

    }

    public void start() {
        Thread simt = new Thread(this);
        simt.setDaemon(true);
        simt.start();
    }

    public void stop() {
        this.paused = true;
        this.started = false;
    }

    public double getTimeMultiplier() {
        return this.timeMultiplier;
    }

    public void setTimeMultiplier(double timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isStarted() {
        return this.started;
    }
}
