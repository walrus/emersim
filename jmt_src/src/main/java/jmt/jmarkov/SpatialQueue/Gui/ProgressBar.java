package jmt.jmarkov.SpatialQueue.Gui;

public class ProgressBar implements Runnable {

    private double jobLength = 0;
    private double timeMultiplier;
    private int progressPercentage;
    //True if resuming from a non zero progress percentage
    private boolean hasBeenResumed;
    public ProgressBar(double timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
        this.hasBeenResumed = false;
    }

    public synchronized void setJobLength(double jobLength) {
        this.jobLength = jobLength;
        this.notify();
    }

    @Override
    public synchronized void run() {
        int percentageStep = 1;
        // Store the progress percentage
        int tempProgress = this.progressPercentage;

        while (true) {
            this.progressPercentage = 0;

            if (this.hasBeenResumed) {
                this.progressPercentage = tempProgress;
                this.hasBeenResumed = false;
            }
            while (progressPercentage < 100 && jobLength > 0) {
                // Amount of time to sleep until next increase
                double increaseInterval = (jobLength / (100 / percentageStep)) / timeMultiplier;
                try {
                    Thread.sleep((long) increaseInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressPercentage += percentageStep;
                GuiComponents.setProgressBarValue(progressPercentage);
            }
            GuiComponents.setProgressBarValue(0);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTimeMultiplier(double timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }
}
