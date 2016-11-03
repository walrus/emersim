package jmt.jmarkov.SpatialQueue.gui;


import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Graphics.constants.DrawSmall;
import org.fest.swing.util.Range;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Created by Dyl on 02/11/2016.
 */

public class StatisticalSummaryFrame extends JFrame implements ActionListener, PropertyChangeListener {

    private static String sStrS = "Avg. Service Time S = ";
    private static String sStrE = " s";
    private double AvgService;

    private static String tStrS = "Total Customers Arrived = ";
    private static String tStrE = "";
    private double TotalCustomers;

    private static String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";
    private static String nStrE = " cust.";
    private double AvgCust;

    private static String uStrS = "Avg. Utilization (Sum of All Servers) U = ";
    private static String uStrE = "";
    private double AvgUtilization;

    private static String thrStrS = "Avg. Throughput X = ";
    private static String thrStrE = " cust./s";
    private double AvgThroughput;

    private static String respStrS = "Avg. Response Time R = ";
    private static String respStrE = " s";
    private double AvgResponse;


    private DrawNormal dCst;
    private JPanel resultsP;
    private JPanel sideButtons;

    private JLabel serviceL = new JLabel();
    private JLabel customerL = new JLabel();
    private JLabel mediaJobsL = new JLabel();
    private JLabel utilizationL = new JLabel();
    private JLabel thrL = new JLabel();
    private JLabel responseL = new JLabel();

    private JButton save;
    private JButton load;
    private JButton compare;


    public StatisticalSummaryFrame(double TotalCustomers, double AvgService,
                                   double AvgCust, double AvgUtilization,
                                   double AvgThroughput, double AvgResponse) {
        this.TotalCustomers = TotalCustomers;
        this.AvgService = AvgService;
        this.AvgCust = AvgCust;
        this.AvgUtilization = AvgUtilization;
        this.AvgThroughput = AvgThroughput;
        this.AvgResponse = AvgResponse;
        dCst = new DrawNormal();
        init();
    }


    public void init(){
        setTitle("Simulation Summary");

        // window creation
        Dimension d = new Dimension(900,300);
        setPreferredSize(d);

        // creates statistics panel
        resultsP = new JPanel(new GridLayout(3,2));
        resultsP.setBorder(new TitledBorder("Simulation Statistics Summary"));
        generateStatisticsPanel(resultsP, TotalCustomers, AvgService,
                           AvgCust, AvgUtilization, AvgThroughput, AvgResponse);

        // creates button panel
        generateSideButtons(sideButtons);

        // window settings
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void generateSideButtons(JPanel sideButtons) {
        sideButtons = new JPanel(new GridLayout(3,1));

        save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        load = new JButton("Load");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        compare = new JButton("Compare");
        compare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        sideButtons.add(save);
        sideButtons.add(load);
        sideButtons.add(compare);

        add(sideButtons, BorderLayout.WEST);
    }


    private void generateStatisticsPanel(JPanel resultsP, double TotalCustomers,
                                    double AvgService, double AvgCust,
                                    double AvgUtilization, double AvgThroughput,
                                    double AvgResponse) {

        // customer total
        customerL.setText(tStrS + TotalCustomers + tStrE);
        customerL.setFont(dCst.getNormalGUIFont());
        resultsP.add(customerL);

        // service
        serviceL.setText(sStrS + AvgService + sStrE);
        serviceL.setFont(dCst.getNormalGUIFont());
        resultsP.add(serviceL);


        // media
        mediaJobsL.setText(nStrS + AvgCust + nStrE);
        mediaJobsL.setFont(dCst.getNormalGUIFont());
        resultsP.add(mediaJobsL);

        // utilization

        utilizationL.setText(uStrS + AvgUtilization + uStrE);
        utilizationL.setFont(dCst.getNormalGUIFont());
        resultsP.add(utilizationL);

        // throughput
        thrL.setText(thrStrS + AvgThroughput + thrStrE);
        thrL.setFont(dCst.getNormalGUIFont());
        resultsP.add(thrL);

        // response time
        responseL.setText(respStrS + AvgResponse + respStrE);
        responseL.setFont(dCst.getNormalGUIFont());
        resultsP.add(responseL);

        add(resultsP, BorderLayout.CENTER);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }


    public static void main(String[] args) {
        new StatisticalSummaryFrame(40, 1.3,0,1,2,3);
    }

}
