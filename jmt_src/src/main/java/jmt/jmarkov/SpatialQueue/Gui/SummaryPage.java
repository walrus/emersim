package jmt.jmarkov.SpatialQueue.Gui;

import jmt.jmarkov.SpatialQueue.Simulation.ClientRegion;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static jmt.jmarkov.SpatialQueue.Gui.GuiComponents.dCst;

/**
 * Created by joshuazeltser on 30/11/2016.
 */
public class SummaryPage extends JFrame {

    private SpatialQueueSimulator sim;
    private GridBagConstraints c ;

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
        statistics.setLayout(new GridLayout(3,1));
        for(ClientRegion cr : sim.getRegions()) {
            count++;
            JPanel resultsP = new JPanel();
            resultsP.setLayout(new GridLayout(2, 2));
            resultsP.setBorder(addTitle("Client Region " + count, dCst.getSmallGUIFont()));
            c.gridx = 0;
            c.gridy = 1;

            cr.getGenerator().getStats().generateSimulationStats(resultsP);
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
