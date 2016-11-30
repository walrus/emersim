package jmt.jmarkov.SpatialQueue.Gui;

import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;
import jmt.jmarkov.utils.Formatter;
import javax.swing.*;
import java.awt.*;


import static jmt.jmarkov.SpatialQueue.Gui.GuiComponents.*;

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class Statistics {

    //To change service time change this variable
    double S_I;
    double U; // Utilization [%]
    double Q; // Average customer in station
    private String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";
    private String nStrE = " cust.";
    private String uStrS = "Avg. Utilization (Sum of All Servers) U = ";
    private String uStrE = "";
    private String thrStrS = "Avg. Throughput X =";
    private String thrStrE = " cust./s";
    private String respStrS = "Avg. Response Time R = ";
    private String respStrE = " s";
    private boolean nonErgodic = false;//if the utilization is less than 1
    private MM1Logic ql;
    private QueueDrawer queueDrawer;
    private DrawNormal dCst;
    private JLabel mediaJobsL;
    private JLabel utilizationL;

    public Statistics() {
        ql = new MM1Logic(0,0);
        queueDrawer = new QueueDrawer(ql, true);
        init();
    }

    public void init() {
        mediaJobsL = new JLabel();
        utilizationL = new JLabel();
    }




    protected void updateFields(SpatialQueueSimulator sim) {
        try {
            Q = ql.mediaJobs();
            U = ql.utilization();
            utilizationL.setForeground(Color.BLACK);
            utilizationL.setText(uStrS + Formatter.formatNumber(U, 3) + uStrE);
            mediaJobsL.setText(nStrS + Formatter.formatNumber(Q, 3) + nStrE);

            thrL.setText(thrStrS + Formatter.formatNumber(ql.throughput(), 3) + thrStrE);
            responseL.setText(respStrS + Formatter.formatNumber(ql.responseTime(), 3) + respStrE);
            nonErgodic = false;

            if (sim != null && ql.getLambda() > 0) {
                sim.setLambdaZero(false);
            }
        } catch (NonErgodicException e) {
            Q = 0.0;
            U = 0.0;
            mediaJobsL.setText(nStrS + "Saturation");

            utilizationL.setForeground(Color.RED);
            utilizationL.setText(uStrS + "Saturation");


//            thrL.setText(thrStrS + "Saturation");
            responseL.setText(respStrS + "Saturation");
            nonErgodic = true;
        }
        queueDrawer.setMediaJobs(Q - U);
//        statiDrawer.repaint();

    }




    protected  void generateSimulationStats(JPanel resultsP) {
        // media
        dCst = new DrawNormal();
        mediaJobsL.setText(nStrS + "0" + nStrE);
        mediaJobsL.setFont(dCst.getNormalGUIFont());
        resultsP.add(mediaJobsL);

        // utilization
        utilizationL.setText(uStrS + "0" + uStrE);
        utilizationL.setFont(dCst.getNormalGUIFont());
        resultsP.add(utilizationL);

        // throughput
        thrL.setText(thrStrS + "0" + thrStrE);
        thrL.setFont(dCst.getNormalGUIFont());
        resultsP.add(thrL);

        // response time
        responseL.setText(respStrS + "0" + respStrE);
        responseL.setFont(dCst.getNormalGUIFont());
        resultsP.add(responseL);
    }

    //setup queue visualisation and pointer
    protected  void showQueue() {

        ql = new MM1Logic(0, 0);

//        statiDrawer.updateLogic(ql);
        queueDrawer.updateLogic(ql);
        queueDrawer.setMaxJobs(0);
//        statiDrawer.setMaxJobs(0);
        queueDrawer.setCpuNumber(1);
        updateFields(sim);
    }

    public  void setSI(double sI) {
        S_I = sI;
        System.out.println("SERVICE TIME: " +S_I);
        ql.setS(sI/1000);
        updateFields(sim);

    }

    public  void setLambda(double lambda) {
        ql.setLambda(lambda);
        updateFields(sim);
    }

    public QueueDrawer getQueueDrawer() {
        return queueDrawer;
    }

    public void setQueueDrawer(QueueDrawer queueDrawer) {
        this.queueDrawer = queueDrawer;
    }



}