package jmt.jmarkov.SpatialQueue.gui;

import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.Graphics.StatiDrawer;
import jmt.jmarkov.Graphics.TANotifier;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;
import jmt.jmarkov.utils.Formatter;

import javax.swing.*;
import java.awt.*;
import java.util.Dictionary;

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class StatsUtils {

    static double U; // Utilization [%]
    static double Q; // Average customer in station

    private static JLabel responseL;
    private static JLabel thrL;

    private static String bufStrS = "Max Station Capacity k = ";
    private static String bufStrE = " cust.";
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
    private static String sStrS = "Avg. Service Time S = ";
    private static String sStrE = " s";

    static TANotifier outputTA = new TANotifier();
    private static boolean nonErgodic;//if the utilization is less than 1

    private static int buffer; //number of place for the waiting queue
    private static int cpuNum; //number of server in the system


    protected static void setLogAnalyticalResults(MM1Logic ql, TANotifier outputTA) {
        try {
            if (ql.getMaxStates() == 0) {
                outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(), ql.responseTime(), ql.getLambda(), ql.getS(), 0);
            } else {
                outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(), ql.responseTime(), ql.getLambda(), ql.getS(), ql
                        .getStatusProbability(ql.getMaxStates() + ql.getNumberServer()));
            }
        } catch (NonErgodicException e) {
            outputTA.setAnalyticalResult();
        }
    }

    protected static void buffSStateChanged(JSlider buffS, JLabel utilizationL, JLabel mediaJobsL,
                                            MM1Logic ql, QueueDrawer queueDrawer,
                                            StatiDrawer statiDrawer, JLabel buffL, SpatialQueueSimulator sim) {

        buffer = buffS.getValue() - cpuNum;
        if (buffer < 1) {
            buffS.setValue(1);
            buffer = 1;
        }
        ql.setMaxStates(buffer);
        queueDrawer.setMaxJobs(buffer + 1);
        statiDrawer.setMaxJobs(buffer + cpuNum);
        buffL.setText(bufStrS + buffS.getValue() + bufStrE);
        updateFields(ql, utilizationL, mediaJobsL,sim, queueDrawer, statiDrawer);
    }

    protected static void updateFields(MM1Logic ql, JLabel utilizationL, JLabel mediaJobsL,
                                       SpatialQueueSimulator sim, QueueDrawer queueDrawer, StatiDrawer statiDrawer  ) {
        try {
            Q = ql.mediaJobs();
            U = ql.utilization();
            utilizationL.setForeground(Color.BLACK);
            utilizationL.setText(uStrS + Formatter.formatNumber(U, 2) + uStrE);
            mediaJobsL.setText(nStrS + Formatter.formatNumber(Q, 2) + nStrE);

            thrL = new JLabel();
            thrL.setText(thrStrS + Formatter.formatNumber(ql.throughput(), 2) + thrStrE);
            responseL = new JLabel();
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
        statiDrawer.repaint();
        outputTA = new TANotifier();
        if (sim == null || !sim.isStarted()) {
            setLogAnalyticalResults(ql, outputTA);
        } else {
            outputTA.setAnalyticalResult();
        }
    }

    protected static void sSStateChanged(MM1Logic ql, JLabel utilizationL, JLabel mediaJobsL, SpatialQueueSimulator sim,
                                         QueueDrawer queueDrawer, StatiDrawer statiDrawer, JSlider sS,
                                         double sMultiplier, JLabel sL) {
        setSSlider(sS, sMultiplier, sL, ql);
        updateFields(ql, utilizationL, mediaJobsL,sim, queueDrawer, statiDrawer);
    }

    protected static void lambdaSStateChanged(MM1Logic ql, JLabel utilizationL, JLabel mediaJobsL, SpatialQueueSimulator sim,
                                              QueueDrawer queueDrawer, StatiDrawer statiDrawer, JSlider lambdaS,
                                              double lambdaMultiplier, int lambdaMultiplierChange, JLabel lambdaL,
                                              JSlider sS, double sMultiplier, JLabel sL) {
        if (lambdaS.getValue() == 0) {
            lambdaMultiplier = 0.01;
            lambdaMultiplierChange = 0;
            lambdaS.setValue(1);
        }
        ql.setLambda(lambdaMultiplier * lambdaS.getValue());
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
        setSSlider(sS, sMultiplier, sL, ql);
        updateFields(ql, utilizationL, mediaJobsL,sim, queueDrawer, statiDrawer);
    }

    protected static void setSSlider(JSlider sS, double sMultiplier, JLabel sL, MM1Logic ql) {
        //sMultiplier = ql.getMaxErgodicS();
        Dictionary<Integer, JLabel> d = sS.getLabelTable();
        //for (int i = 0; i < 6; i++) {
        //	d.put(new Integer(i * 25), new JLabel("" + Formatter.formatNumber(i * sMultiplier ), 2));
        //}
        for (int i = sS.getMinimum(); i <= sS.getMaximum(); i += sS.getMajorTickSpacing()) {
            d.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * sMultiplier, 2)));
        }
        sS.setLabelTable(d);
        sL.setText(sStrS + Formatter.formatNumber(sS.getValue() * sMultiplier, 2) + sStrE);
        sS.repaint();
        ql.setS(sS.getValue() * sMultiplier);
    }

    protected static void setSMultiplier(JSlider sS, double sMultiplier, JLabel sL, MM1Logic ql, int sMultiplierChange) {
        while (true) {
            if (sS.getValue() > sS.getMaximum() * 0.95) {
                if (sMultiplierChange <= 4) {
                    if (sMultiplierChange % 2 == 0) {
                        sMultiplier *= 2;
                        setSSlider(sS, sMultiplier, sL, ql);
                        sS.setValue((sS.getValue() + 1) / 2);
                    } else {
                        sMultiplier *= 5;
                        setSSlider(sS, sMultiplier, sL, ql);
                        sS.setValue((sS.getValue() + 1) / 5);
                    }
                    sMultiplierChange++;
                    //System.out.println("SMultiplier:" + sMultiplier);
                } else {
                    break;
                }
            } else if (sS.getValue() < sS.getMaximum() * 0.05) {
                if (sMultiplierChange > 0) {
                    if (sMultiplierChange % 2 == 1) {
                        sMultiplier /= 2;
                        setSSlider(sS, sMultiplier, sL, ql);
                        sS.setValue(sS.getValue() * 2);
                    } else {
                        sMultiplier /= 5;
                        setSSlider(sS, sMultiplier, sL, ql);
                        sS.setValue(sS.getValue() * 5);
                    }
                    sMultiplierChange--;
                    //System.out.println("SMultiplier:" + sMultiplier);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }
    protected static void setLambdaMultiplier(JSlider lambdaS, int lambdaMultiplierChange, double lambdaMultiplier,
                                              MM1Logic ql, JLabel lambdaL) {
        while (true) {
            if (lambdaS.getValue() > lambdaS.getMaximum() * 0.95) {
                if (lambdaMultiplierChange <= 4) {
                    if (lambdaMultiplierChange % 2 == 0) {
                        lambdaMultiplier *= 2;
                        setLambdaSlider(lambdaS, lambdaMultiplier, ql, lambdaL);
                        lambdaS.setValue((lambdaS.getValue() + 1) / 2);
                    } else {
                        lambdaMultiplier *= 5;
                        setLambdaSlider(lambdaS, lambdaMultiplier, ql, lambdaL);
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
                        setLambdaSlider(lambdaS, lambdaMultiplier, ql, lambdaL);
                        lambdaS.setValue(lambdaS.getValue() * 2);
                    } else {
                        lambdaMultiplier /= 5;
                        setLambdaSlider(lambdaS, lambdaMultiplier, ql, lambdaL);
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

    public static void setLambdaSlider(JSlider lambdaS, double lambdaMultiplier, MM1Logic ql, JLabel lambdaL) {
        Dictionary<Integer, JLabel> ld = lambdaS.getLabelTable();

        for (int i = lambdaS.getMinimum(); i <= lambdaS.getMaximum(); i += lambdaS.getMajorTickSpacing()) {
            ld.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * lambdaMultiplier, 2)));
        }

        //for (int i = 0; i <= 4; i++) {
        //	ld.put(new Integer(i * 25), new JLabel("" + Formatter.formatNumber(i * 0.25, 2)));
        //}
        lambdaS.setLabelTable(ld);
        ql.setLambda(lambdaMultiplier * lambdaS.getValue());
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
    }

    protected static void generateSimulationStats(JPanel resultsP, JLabel mediaJobsL, JLabel utilizationL,
                                                  DrawConstrains dCst) {
        // media
        mediaJobsL.setText(nStrS + "0" + nStrE);
        mediaJobsL.setFont(dCst.getNormalGUIFont());
        resultsP.add(mediaJobsL);

        // utilization

        utilizationL.setText(uStrS + "0" + uStrE);
        utilizationL.setFont(dCst.getNormalGUIFont());
        resultsP.add(utilizationL);

        // throughput
        thrL = new JLabel();
        thrL.setText(thrStrS + "0" + thrStrE);
        thrL.setFont(dCst.getNormalGUIFont());
        resultsP.add(thrL);

        // response time
        responseL = new JLabel();
        responseL.setText(respStrS + "0" + respStrE);
        responseL.setFont(dCst.getNormalGUIFont());
        resultsP.add(responseL);
    }
}
