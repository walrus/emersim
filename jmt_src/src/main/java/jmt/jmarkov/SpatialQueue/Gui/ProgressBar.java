package jmt.jmarkov.SpatialQueue.Gui;

public class ProgressBar implements Runnable {

    private double jobLength = 0;
    private double timeMultiplier;

    public ProgressBar(double timeMultiplier){
        this.timeMultiplier = timeMultiplier;
    }

    public synchronized void setJobLength(double jobLength) {
        this.jobLength = jobLength;
        this.notify();
    }

    @Override
    public synchronized void run() {
        int percentageStep = 1;
        while (true) {
            int progressPercentage = 0;
            while (progressPercentage < 100 && jobLength > 0) {
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
