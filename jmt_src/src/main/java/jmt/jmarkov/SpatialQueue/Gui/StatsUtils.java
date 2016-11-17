package jmt.jmarkov.SpatialQueue.Gui;

import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;
import jmt.jmarkov.utils.Formatter;
import javax.swing.*;
import java.awt.*;
import java.util.Dictionary;
import static jmt.jmarkov.SpatialQueue.Gui.GuiComponents.*;

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class StatsUtils {

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
    private static String lambdaStrS = "Avg. Arrival Rate (lambda) = ";
    private static String lambdaStrE = " cust./s";
    private static boolean nonErgodic = false;//if the utilization is less than 1


    protected static void setLogAnalyticalResults() {
        try {
            if (ql.getMaxStates() == 0) {
                outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(),
                        ql.responseTime(), ql.getLambda(), ql.getS(), 0);
            } else {
                outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(), ql.responseTime(),
                        ql.getLambda(), ql.getS(), ql.getStatusProbability(ql.getMaxStates() + ql.getNumberServer()));
            }
        } catch (NonErgodicException e) {
            outputTA.setAnalyticalResult();
        }
    }


    protected static void updateFields(JLabel utilizationL, JLabel mediaJobsL, SpatialQueueSimulator sim) {
        try {
            Q = ql.mediaJobs();
            U = ql.utilization();
            utilizationL.setForeground(Color.BLACK);
            utilizationL.setText(uStrS + Formatter.formatNumber(U, 2) + uStrE);
            mediaJobsL.setText(nStrS + Formatter.formatNumber(Q, 2) + nStrE);

            thrL.setText(thrStrS + Formatter.formatNumber(ql.throughput(), 2) + thrStrE);
            responseL.setText(respStrS + Formatter.formatNumber(ql.responseTime(), 2) + respStrE);
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

        if (sim == null || !sim.isStarted()) {
            setLogAnalyticalResults();
        } else {
            outputTA.setAnalyticalResult();
        }
    }

    protected static void lambdaSStateChanged(JLabel utilizationL, JLabel mediaJobsL, SpatialQueueSimulator sim,
                                              JSlider lambdaS, JLabel lambdaL) {
        if (lambdaS.getValue() == 0) {
            lambdaMultiplier = 0.01;
            lambdaMultiplierChange = 0;
            lambdaS.setValue(1);
        }
        ql.setLambda(lambdaMultiplier * lambdaS.getValue());
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
        updateFields(utilizationL, mediaJobsL,sim);
    }

    protected static void setLambdaMultiplier(JSlider lambdaS, JLabel lambdaL) {
        while (true) {
            if (lambdaS.getValue() > lambdaS.getMaximum() * 0.95) {
                if (lambdaMultiplierChange <= 4) {
                    if (lambdaMultiplierChange % 2 == 0) {
                        lambdaMultiplier *= 2;
                        setLambdaSlider(lambdaS, lambdaL);
                        lambdaS.setValue((lambdaS.getValue() + 1) / 2);
                    } else {
                        lambdaMultiplier *= 5;
                        setLambdaSlider(lambdaS, lambdaL);
                        lambdaS.setValue((lambdaS.getValue() + 1) / 5);
                    }
                    lambdaMultiplierChange++;
                    //System.out.println("LambdaMultiplier:" + lambdaMultiplier);
                } else {
                    break;
                }
            } else if (lambdaS.getValue() < lambdaS.getMaximum() * 0.05) {
                if (lambdaMultiplierChange > 0) {
                    if (lambdaMultiplierChange % 2 == 1) {
                        lambdaMultiplier /= 2;
                        setLambdaSlider(lambdaS, lambdaL);
                        lambdaS.setValue(lambdaS.getValue() * 2);
                    } else {
                        lambdaMultiplier /= 5;
                        setLambdaSlider(lambdaS, lambdaL);
                        lambdaS.setValue(lambdaS.getValue() * 5);
                    }
                    lambdaMultiplierChange--;
                    //System.out.println("LambdaMultiplier:" + lambdaMultiplier);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public static void setLambdaSlider(JSlider lambdaS, JLabel lambdaL) {
        Dictionary<Integer, JLabel> ld = lambdaS.getLabelTable();

        for (int i = lambdaS.getMinimum(); i <= lambdaS.getMaximum(); i += lambdaS.getMajorTickSpacing()) {
            ld.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * lambdaMultiplier, 2)));
        }


        lambdaS.setLabelTable(ld);
        ql.setLambda(lambdaMultiplier * lambdaS.getValue());
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);

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
}