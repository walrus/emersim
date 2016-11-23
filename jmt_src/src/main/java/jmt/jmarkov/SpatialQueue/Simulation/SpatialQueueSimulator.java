/**
 * Based on the standard Jmarkov SpatialQueueSimulator
 */

package jmt.jmarkov.SpatialQueue.Simulation;


import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.SpatialQueue.ClientRegion;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Gui.StatsUtils;
import jmt.jmarkov.SpatialQueue.Location;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.util.Date;
import java.util.Random;

public class SpatialQueueSimulator implements Runnable {

    // Receiver is the server that deals with requests.
    // All logic related to dealing with requests is delegated to it
    private Receiver receiver;

    private ClientRegion[] regions;

    // Generates Requests & runs in a separate thread
    private RequestGenerator generator;

    //current simulation time
    private double currentTime;// in milliseconds

    // Number of arrivals per second
    private float lambda;

    //if lambda is zero this value is set to true
    //(if lambda set to zero new requests will not be created
    private boolean lambdaZero = false;

    //Max inter arrival time of requests
    private float maxInterval;

    //it saves the data if the simulator is paused or not
    private boolean paused = false;

    //multiplier is used for multiplier for timer.(comes from simulation time slide bar)
    private double timeMultiplier = 1.0;

    private boolean running = false;

    private boolean started = false;

    private int currentRequestID;

    private int maxRequests;

    private QueueDrawer queueDrawer;

    private MapConfig mapConfig;

    private boolean returnJourney;

    public SpatialQueueSimulator(double timeMultiplier,
                                 QueueDrawer queueDrawer,
                                 Receiver receiver,
                                 MapConfig mapConfig,
                                 int maxRequests,
                                 boolean returnJourney) {
        super();

        currentTime = 0;
        setTimeMultiplier(timeMultiplier);
        this.receiver = receiver;
        this.regions = mapConfig.getClientRegions();
        this.currentRequestID = 0;
        this.maxRequests = maxRequests;
        this.queueDrawer = queueDrawer;
        this.mapConfig = mapConfig;
        this.returnJourney = returnJourney;
        //TODO: make lambda and maxInterval changeable from front end
        this.lambda = 5;
        this.maxInterval = 3;

        //Create a new request generator
        this.generator = new RequestGenerator(this);
    }

    protected Sender generateNewSenderWithinArea(ClientRegion clientRegion) {
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

        // Start a new thread and run the generator from it
        Thread generatorThread = new Thread(this.generator);
        generatorThread.start();

        // While not paused, process requests or wait for another one to be added
        while (!paused && moreRequests()) {
            if (this.receiver.getQueue().size() > 0) {
                // Serve the next request and grab a link to the request being served
                Request currentRequest = this.receiver.serveRequest(currentTimeMultiplied);
                //notify visualisation with which job is being served
                queueDrawer.servingJob(currentRequest.getRequestId());
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

                StatsUtils.setSI(receiver.getAverageServiceTime());
                //Having waited till the request has been served, deal with it
                currentTime = currentRequest.getNextEventTime();
                this.receiver.stopServing(currentTime);
                // update queue visualisation
                queueDrawer.exitQueue();

            } else {
                // No requests in queue, so just loop till another is added
            }
        }
        running = false;
        System.out.println("Stopping, total requests served: " + this.receiver.getNumberOfRequestsServed());

    }
    // Return true iff receiver has served fewer then maxRequests requests or if maxRequests == 0
    protected synchronized boolean moreRequests() {
        return ((this.receiver.getNumberOfRequestsServed() < this.maxRequests) || maxRequests == 0);
    }

    protected synchronized int getNextRequestID() {
        int r = this.currentRequestID;
        this.currentRequestID ++;
        return r;
    }

    public synchronized Request createRequest() {
        //Current implementation: create a new sender then generate a request from them
        //Future implementation could take existing sender (generate before running sim)
        int randomInt = new Random().nextInt(this.regions.length);

        Sender sender = this.generateNewSenderWithinArea(this.regions[randomInt]);

        Request r = sender.makeRequest(getNextRequestID(), this.currentTime);
        return r;
    }

    public synchronized void enqueueRequest(Request newRequest) {
        if (newRequest != null){
            this.receiver.handleRequest(newRequest, returnJourney);
        }
    }

    public synchronized Request dequeueRequest() {
        return this.receiver.getNextRequest();
    }

    public Request peekRequest() {
        return this.receiver.getQueue().peek();
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

    public float getLambda() { return this.lambda;}

    public float getMaxInterval() { return this.maxInterval;}

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

    public ClientRegion[] getRegions() {
        return regions;
    }

    public QueueDrawer getQueueDrawer() {
        return queueDrawer;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }
}
