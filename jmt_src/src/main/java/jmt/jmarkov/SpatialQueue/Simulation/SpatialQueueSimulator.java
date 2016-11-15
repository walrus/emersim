/**
 * Based on the standard Jmarkov SpatialQueueSimulator
 */

package jmt.jmarkov.SpatialQueue.Simulation;


import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.SpatialQueue.*;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Date;
import java.util.Random;

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

    private int maxRequests;

    public SpatialQueueSimulator(double timeMultiplier,
                                 Notifier[] notifier,
                                 Receiver receiver,
                                 MapConfig mapConfig,
                                 int maxRequests) {
        super();

        currentTime = 0;
        setTimeMultiplier(timeMultiplier);
        this.notifier = notifier;
        this.receiver = receiver;
        this.regions = mapConfig.getClientRegions();
        this.currentRequestID = 0;
        this.maxRequests = maxRequests;
    }

    private Sender generateNewSenderWithinArea(ClientRegion clientRegion) {
        Location senderLocation = clientRegion.generatePoint();
        return new Sender(senderLocation);    }

    public void run() {
        running = true;
        started = true;
        // this is the simulation time till run command is called (?)
        double currentTimeMultiplied;
        //when calling run getting the current real time (?)
        long realTimeStart;
        //this is the time after return the thread.sleep (?)
        long realTimeCurrent;
        currentTimeMultiplied = 0;
        realTimeStart = new Date().getTime();

        // TODO: use actual request generation
        for (int i = 0; i < 10; i++) {
            this.enqueueRequest(this.createRequest());
        }
        // While not paused, process requests or wait for another one to be added
        while (!paused && moreRequests()) {
            if (this.receiver.getQueue().size() > 0) {
                // Serve the next request and grab a link to the request being served
                Request currentRequest = this.receiver.serveRequest(currentTimeMultiplied);
                currentTimeMultiplied += (currentRequest.getNextEventTime() - currentTime) / timeMultiplier;
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
                currentTime = currentRequest.getNextEventTime();
                this.receiver.stopServing(currentTime);
            } else {
                // No requests in queue, so just loop till another is added
                System.out.println("Total requests served: " + this.receiver.getNumberOfRequestsServed());
            }
        }
        running = false;
        System.out.println("Stopping, total requests served: " + this.receiver.getNumberOfRequestsServed());
    }
    // Return true iff receiver has served fewer then maxRequests requests or if maxRequests == 0
    private boolean moreRequests() {
        return (this.receiver.getNumberOfRequestsServed() < this.maxRequests || maxRequests == 0);
    }

    private synchronized int getNextRequestID() {
        int r = this.currentRequestID;
        this.currentRequestID ++;
        return r;
    }

    public Request createRequest() {
        //Current implementation: create a new sender then generate a request from them
        //Future implementation could take existing sender (generate before running sim)
        int randomInt = new Random().nextInt(this.regions.length);

        Sender sender = this.generateNewSenderWithinArea(this.regions[randomInt]);

        //Sender sender = this.generateNewSenderWithinArea(this.regions[0]);

        Request r = sender.makeRequest(getNextRequestID(), this.currentTime);
        return r;
    }

    public void enqueueRequest(Request newRequest) {
        if (newRequest != null){
            this.receiver.handleRequest(newRequest);
        }
    }

    public Request dequeueRequest() {
        return this.receiver.getNextRequest();
    }

    public Request peekRequest() {
        return this.receiver.getQueue().getFirst();
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
