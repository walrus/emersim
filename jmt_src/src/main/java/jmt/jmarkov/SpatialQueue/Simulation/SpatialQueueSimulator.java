/**
 * Based on the standard Jmarkov SpatialQueueSimulator
 */

package jmt.jmarkov.SpatialQueue.Simulation;


import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.SpatialQueue.*;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Date;

public class SpatialQueueSimulator implements Runnable {

    // Receiver is the server that deals with requests.
    // All logic related to dealing with requests is delegated to it
    private Receiver receiver;

    private ClientRegion[] regions;

    private Notifier[] notifier;

    //current simulation time
    private double currentTime;// in milliseconds

    //if lambda is zero this value is set to true
    //(if lambda set to zero new requests will not be created
    private boolean lambdaZero = false;

    //it saves the data if the simulator is paused or not
    private boolean paused = false;

    //multiplier is used for multiplier for timer.(comes from simulation time slide bar)
    private double timeMultiplier = 1.0;

    private boolean running = false;

    private boolean started = false;

    private int currentRequestID;

    public SpatialQueueSimulator(double timeMultiplier,
                                 Notifier[] notifier,
                                 Receiver receiver,
                                 MapConfig mapConfig) {
        super();

        currentTime = 0;
        setTimeMultiplier(timeMultiplier);
        this.notifier = notifier;
        this.receiver = receiver;
        this.regions = mapConfig.getClientRegions();
        this.currentRequestID = 0;
    }

    private Sender generateNewSenderWithinArea(ClientRegion clientRegion) {
        Location senderLocation = clientRegion.generatePoint();
        return new Sender(senderLocation);    }

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

        //this is the first request which is created
        //if request queue is not empty this means run is called from the paused situation
        if (this.receiver.getQueue().isEmpty()) {
            this.receiver.handleRequest(this.createRequest());
        }

        //if there is still at least one request waiting for a response it is running recursive(?)
        //if paused the running will stop.
        while (this.receiver.getQueue().size() > 0 && !paused) {
            //this is calculating how long system will sleep
            currentTimeMultiplied += (peekRequest().getNextEventTime() - currentTime) / timeMultiplier;
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

            Request request = dequeueRequest();
            currentTime = request.getNextEventTime();
        }

        running = false;

        while(true) {
            enqueueRequest(this.createRequest());
            currentTimeMultiplied += generateNextTime(10, 3);
        }

    }

    private int getNextRequestID() {
        int r = this.currentRequestID;
        this.currentRequestID ++;
        return r;
    }

    public Request createRequest() {
        //Current implementation: create a new sender then generate a request from them
        Sender sender = this.generateNewSenderWithinArea(this.regions[0]);
        return new Request(getNextRequestID(), this.currentTime, sender);
    }

    public void enqueueRequest(Request newRequest) {
        this.receiver.handleRequest(newRequest);
    }

    public Request dequeueRequest() {
        return this.receiver.getNextRequest();
    }

    public Request peekRequest() {
        return this.receiver.getQueue().element();
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

    public double generateNextTime(float rateParameter, float random_max) {
        return -Math.log(1.0 - Math.random()/(random_max + 1)/rateParameter);
    }
}
