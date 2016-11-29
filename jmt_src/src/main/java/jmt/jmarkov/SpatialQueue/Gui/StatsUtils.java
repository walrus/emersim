package jmt.jmarkov.SpatialQueue.Gui;

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
public class StatsUtils {



    //To change service time change this variable
    static double S_I;

    static double U; // Utilization [%]
    static double Q; // Average customer in station
    private static String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";
    private static String nStrE = " cust.";
    private static String uStrS = "Avg. Utilization (Sum of All Servers) U = ";
    private static String uStrE = "";
    private static String thrStrS = "Avg. Throughput X =";
    private static String thrStrE = " cust./s";
    private static String respStrS = "Avg. Response Time R = ";
    private static String respStrE = " s";
    private static boolean nonErgodic = false;//if the utilization is less than 1




    protected static void updateFields(JLabel utilizationL, JLabel mediaJobsL, SpatialQueueSimulator sim) {
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

            thrL.setText(thrStrS + "Saturation");
            responseL.setText(respStrS + "Saturation");
            nonErgodic = true;
        }
        queueDrawer.setMediaJobs(Q - U);
//        statiDrawer.repaint();

    }




    protected static void generateSimulationStats(JPanel resultsP, JLabel mediaJobsL, JLabel utilizationL) {
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

    // create a service time slider
    protected static void setupServiceTime() {
        sMultiplier = 0.02;

        ql.setS(S_I * sMultiplier);
    }

    //setup queue visualisation and pointer
    protected static void showQueue(JSlider lambdaS, JLabel utilizationL, JLabel mediaJobsL) {

        ql = new MM1Logic(lambdaMultiplier * lambdaS.getValue(), ql.getS() * sMultiplier);

        lambdaS.setValue(LAMBDA_I);
//        statiDrawer.updateLogic(ql);
        queueDrawer.updateLogic(ql);
        queueDrawer.setMaxJobs(0);
//        statiDrawer.setMaxJobs(0);
        queueDrawer.setCpuNumber(1);
        updateFields(utilizationL, mediaJobsL, sim);
    }

    public static void setSI(double sI) {
        S_I = sI;
        System.out.println("SERVICE TIME: " +S_I);
        ql.setS(sI);
        updateFields(utilizationL, mediaJobsL, sim);

    }

    public static void setLambda(double lambda) {
        ql.setLambda(lambda);
        updateFields(utilizationL,mediaJobsL,sim);
    }




}