package jmt.jmarkov.SpatialQueue.Gui;

public class ProgressBar implements Runnable {

    private double jobLength = 0;
    private double timeMultiplier;

    public ProgressBar(double timeMultiplier){
        this.timeMultiplier = timeMultiplier;
    }

    public void setJobLength(double jobLength) {
        this.jobLength = jobLength;
    }

    @Override
    public synchronized void run() {
        int percentageStep = 5;
        while (true) {
            int progressPercentage = 0;
            while (progressPercentage < 100 && jobLength > 0) {
                double increaseInterval = (jobLength / percentageStep) / timeMultiplier;
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
