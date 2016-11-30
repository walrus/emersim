package jmt.jmarkov.SpatialQueue.Gui;

import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.Queues.QueueLogic;
import jmt.jmarkov.SpatialQueue.ClientRegion;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedList;

import static jmt.jmarkov.SpatialQueue.Gui.GuiComponents.dCst;

/**
 * Created by joshuazeltser on 30/11/2016.
 */
public class SummaryPage extends JFrame {

    private SpatialQueueSimulator sim;
    private GridBagConstraints c ;
    private String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";
    private String nStrE = " cust.";
    private String uStrS = "Avg. Utilization (Sum of All Servers) U = ";
    private String uStrE = "";
    private String thrStrS = "Avg. Throughput X =";
    private String thrStrE = " cust./s";
    private String respStrS = "Avg. Response Time R = ";
    private String respStrE = " s";

    public SummaryPage(SpatialQueueSimulator sim) {
        this.sim = sim;
        init();
    }

    private void init() {
        setTitle("Summary Statistics");

        c = new GridBagConstraints();

        //set window size
        Dimension d = new Dimension(900,400);
        setPreferredSize(d);

        int count = 0;

        JPanel statistics = new JPanel();
        statistics.setLayout(new GridLayout( sim.getRegions().length,1));

        double mediaJobs;
        double lambda;
        double utilisation;
        double responseTime;

        String statsString = "";

        for(ClientRegion cr : sim.getRegions()) {
            count++;
            JTextField resultsP = new JTextField();
            resultsP.setEditable(false);
            Font font = new Font("Courier", Font.BOLD,12);
            resultsP.setFont(font);
            resultsP.setLayout(new GridLayout(2, 2));
            resultsP.setBorder(addTitle("Client Region " + count, dCst.getSmallGUIFont()));
//            cr.getGenerator().getStats().getQueueLogic();


//            cr.getGenerator().getStats().generateSimulationStats(resultsP);
//            cr.getGenerator().getStats().updateFields(sim);
            try {
                mediaJobs = cr.getGenerator().getStats().getQueueLogic().mediaJobs();
                statsString += nStrS + mediaJobs + nStrE;
            } catch (NonErgodicException e) {
                statsString += nStrS + "Saturation" + nStrE;
            }

            statsString += "\n";

            lambda = cr.getGenerator().getStats().getQueueLogic().getLambda();
            statsString += thrStrS + lambda + thrStrE;

            statsString += "\n";

            try {
               utilisation = cr.getGenerator().getStats().getQueueLogic().utilization();
                statsString += uStrS + utilisation + uStrE;
            } catch(NonErgodicException e) {
                statsString += uStrS + "Saturation" + uStrE;
            }

            statsString += "\n";

            try {
                responseTime = cr.getGenerator().getStats().getQueueLogic().responseTime();
                statsString += respStrS + responseTime + respStrE;
            } catch (NonErgodicException e) {
                statsString += respStrS + "Saturation" + respStrE;
            }

            resultsP.setText(statsString);
            statistics.add(resultsP);

        }



        this.add(statistics);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // add title to panels
    protected TitledBorder addTitle(String title, Font f) {
        return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
    }
}
