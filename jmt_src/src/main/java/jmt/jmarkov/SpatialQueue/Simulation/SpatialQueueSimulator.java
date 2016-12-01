/**
 * Based on the standard Jmarkov SpatialQueueSimulator
 */

package jmt.jmarkov.SpatialQueue.Simulation;


import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Gui.ProgressBar;
import jmt.jmarkov.SpatialQueue.Gui.Statistics;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class SpatialQueueSimulator implements Runnable {

    // Server is the server that deals with requests.
    // All logic related to dealing with requests is delegated to it
    private Server server;

    private LinkedList<ClientRegion> clientRegions;

    //current simulation time
    private double currentTime;// in milliseconds

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

    private Statistics stats;

    double systemLambda;

    private GuiComponents gui;

    // Store these with the simulator instead of locally in run()
    // to allow for pausing and resuming
    private double currentTimeMultiplied;
    private long realTimeStart;
    private long realTimeCurrent;

    private ProgressBar progressBar;

    public SpatialQueueSimulator(GuiComponents gui, double timeMultiplier, Server server, int maxRequests ) {

        super();
        this.gui = gui;
        currentTime = 0;
        setTimeMultiplier(timeMultiplier);
        this.server = server;
        this.mapConfig = gui.getMapConfig();
        this.clientRegions = mapConfig.getClientRegions();
        this.currentRequestID = 0;
        this.maxRequests = maxRequests;

        this.returnJourney = gui.isReturnJourney();
        // lambda is #(number of requests per second)

        this.maxInterval = 3;
        this.stats = gui.getStats();
        this.queueDrawer = stats.getQueueDrawer();

        double totalLambda = 0;
        //Create a new request generator for each client region
        for (ClientRegion cr : clientRegions) {
            RequestGenerator rg = new RequestGenerator(this, cr.getLambda());
            cr.setRequestGenerator(rg);
            totalLambda += cr.getLambda();
        }

        this.systemLambda = totalLambda / clientRegions.size();
        stats.setLambda(systemLambda);
    }

    protected Client generateNewSenderWithinArea(ClientRegion clientRegion) {
        Location senderLocation = clientRegion.generatePoint();
        return new Client(senderLocation);
    }

    public void run() {

        setUpSimulation();

        // While not paused, process requests or wait for another one to be added
        while (!paused && moreRequests()) {
            if (this.server.getQueue().size() > 0) {
                // Serve the next request and grab a link to the request being served
                Request currentRequest = this.server.serveRequest(currentTimeMultiplied);
                // notify visualisation with which job is being served
                queueDrawer.servingJob(currentRequest.getRequestId());
                mapConfig.displayRoute(currentRequest.getDirectionsResult());
                // notify progress bar and update the job time and time multiplier
                this.progressBar.setJobLength(currentRequest.getResponseTime());
                this.progressBar.setTimeMultiplier(timeMultiplier);
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

                stats.setSI(server.getAverageServiceTime());
                System.out.println("Service time " + stats.getQueueLogic().getS());

                //Having waited till the request has been served, deal with it
                currentTime = currentRequest.getNextEventTime();
                this.server.stopServing(currentTime);
                // update queue visualisation
                queueDrawer.exitQueue();

            } else {
                // No requests in queue, so just loop till another is added
            }
        }
        running = false;
        gui.stopProcessing();
        System.out.println("Stopping, total requests served: " + this.server.getNumberOfRequestsServed());

    }

    private void setUpSimulation() {
        this.running = true;
        this.started = true;
        this.currentTimeMultiplied = 0;
        // this is the simulation time till run command is called (?)
        this.realTimeStart = new Date().getTime();

        // For each client region, Start new thread and run the generator from it
        for (ClientRegion cr : this.clientRegions) {
            Thread generatorThread = new Thread(cr.getGenerator());
            generatorThread.start();
        }

        // Start progress bar thread
        this.progressBar = new ProgressBar(timeMultiplier);
        Thread progressBarThread = new Thread(progressBar);
        progressBarThread.start();

    }

    // Return true iff server has served fewer then maxRequests requests or if maxRequests == 0
    protected synchronized boolean moreRequests() {
        return ((this.server.getNumberOfRequestsServed() < this.maxRequests) || maxRequests == 0);
    }

    protected synchronized int getNextRequestID() {
        int r = this.currentRequestID;
        this.currentRequestID++;
        return r;
    }

    public synchronized Request createRequest() {
        //Current implementation: create a new client then generate a request from them
        //Future implementation could take existing client (generate before running sim)
        int randomInt = new Random().nextInt(this.clientRegions.size());

        Client client = this.generateNewSenderWithinArea(this.clientRegions.get(randomInt));

        Request r = client.makeRequest(getNextRequestID(), this.currentTime);
        return r;
    }

    public synchronized void enqueueRequest(Request newRequest) {
        if (newRequest != null) {
            this.server.handleRequest(newRequest, returnJourney);
        }
    }

    public synchronized Request dequeueRequest() {
        return this.server.getNextRequest();
    }

    public Request peekRequest() {
        return this.server.getQueue().peek();
    }

    public boolean isLambdaZero() {
        return lambdaZero;
    }

    public void setLambdaZero(boolean lambdaZero) {
        //This doesn't seem to actually set lambda to zero?
        this.lambdaZero = lambdaZero;
    }

    // Pause the simulation, preserving the current state
    // and allowing for resumption
    public void pause() {
        if (paused) {
            paused = false;
            //TODO: switch to resume() when ready
            start();
        } else {
            paused = true;
        }
    }

    // Resume the simulation from the paused state
    public void resume() {
        //TODO: implement
    }

    // Start the simulator for the first time:
    // Run in new thread and do simulator setup
    public void start() {
        Thread simt = new Thread(this);
        simt.setDaemon(true);
        simt.start();
    }

    // Stop the simulation and calculate the summary statistics. Cannot be resumed.
    public void stop() {
        this.paused = true;
        this.started = false;
    }

    public float getMaxInterval() {
        return this.maxInterval;
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

    public LinkedList<ClientRegion> getRegions() {
        return clientRegions;
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

    public double getAverageServiceTime() {
        return server.getAverageServiceTime();
    }
}
