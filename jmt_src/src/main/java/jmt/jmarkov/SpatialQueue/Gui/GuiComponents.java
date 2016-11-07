package jmt.jmarkov.SpatialQueue.Gui;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.jmarkov.Graphics.*;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Simulation.Receiver;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;
import jmt.jmarkov.utils.Formatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Dictionary;

import static jmt.jmarkov.SpatialQueue.Gui.StatsUtils.*;

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class GuiComponents {

    private JPanel buffPanel;
    private JPanel sPanel;
    private JPanel lambdaPanel;
    private JPanel parametersP;
    private JPanel resultsP;

    private JButton start;
    private JButton pause;
    private JButton stop;
    private JButton client;
    private JButton receiver;

    private JLabel buffL;
    private JLabel sL;
    private JLabel lambdaL;
    private JLabel mediaJobsL;
    private JLabel utilizationL;

    private JSlider buffS;
    private JSlider lambdaS;
    protected static JSlider sS;

    private int BUFF_I = 15;

    private int sMultiplierChange = 1; //for the service slide bar
    private int S_I = 95;
    private int lambdaMultiplierChange = 0; //for the lambda slide bar
    private int LAMBDA_I = 50;

    private String bufStrS = "Max Station Capacity k = ";
    private String bufStrE = " cust.";

    private double sMultiplier = 1; //service time slide bar multiplier
    private double lambdaMultiplier = 1; //lambda slide bar multiplier

    private boolean sSChange = true;
    private boolean paused;
    private boolean lambdaSChange = true;

    private DrawNormal dCst;
    private SpatialQueueSimulator sim;
    private MM1Logic ql;
    private QueueDrawer queueDrawer;
    private StatiDrawer statiDrawer;
    private MapConfig mapView;
    private JobsDrawer jobsDrawer;
    private JSlider accelerationS;
    private LogFile logFile;

    public GuiComponents() {
        init();
    }

    //Initialise objects
    private void init() {
        paused = false;
        buffL = new JLabel();
        buffPanel = new JPanel();
        buffS = new JSlider();
        sPanel = new JPanel();
        lambdaS = new JSlider();
        sS = new JSlider();
        ql = new MM1Logic(0.0, 0.0);
        queueDrawer = new QueueDrawer(ql);
        statiDrawer = new StatiDrawer(ql);
        lambdaPanel = new JPanel();
        lambdaL = new JLabel();
        parametersP = new JPanel();
        resultsP = new JPanel();
        mediaJobsL = new JLabel();
        utilizationL = new JLabel();
        start = new JButton("Start");
        pause = new JButton("Pause");
        stop = new JButton("Stop");
    }

    //Create queueDrawer for queue visualisation
    protected  void generateQueueDrawer(JPanel interfacePanel) {
        QueueDrawer queueDrawer = new QueueDrawer(ql);
        queueDrawer.setPreferredSize(new Dimension(300, 150));
        interfacePanel.add(queueDrawer);
    }

    //create map panel for gui
    protected void generateMapPanel(JPanel interfacePanel) {
        MapViewOptions mapOptions = new MapViewOptions();
        mapOptions.importPlaces();
        mapView = new MapConfig(mapOptions);
        mapView.setPreferredSize(new Dimension(300, 375));
        interfacePanel.add(mapView);
    }

    // create side panel for functionality buttons
    protected void generateSideButtons(JPanel panel) {
        receiverButton();
        clientButton();
        pauseButton();
        startButton();
        stopButton();

        panel.add(receiver);
        panel.add(client);
        panel.add(start);
        panel.add(pause);
        panel.add(stop);
        panel.setBorder(new EmptyBorder(50, 0, 0, 0));

        //scroll bar
        addSpeedSlider(panel);

        //number of arrivals
        addJobsPanel(panel);
    }

    // create an add client button
    private JButton clientButton() {
        client = new JButton("Add Client");
//        client.setMaximumSize(new Dimension(100,40));
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.toggleAreaPlacement();
            }
        });
        return client;
    }

    // create an add receiver button
    private JButton receiverButton() {
        receiver = new JButton("Add Receiver");
//        receiver.setMaximumSize(new Dimension(100,40));
        receiver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.toggleMarkerPlacement();
            }
        });
        return receiver;
    }

    public void stopProcessing() {
        sim.stop();
        while (sim.isRunning()) {
            //waiting to stop
            try {Thread.sleep(100);}
            catch (InterruptedException e) {}
        }
        try {Thread.sleep(100);}
        catch (InterruptedException e) {}
        outputTA.reset();
        logFile.reset();
        queueDrawer.reset();
        statiDrawer.reset();
        jobsDrawer.reset();

        updateFields(ql, utilizationL, mediaJobsL, sim, queueDrawer, statiDrawer);
    }

    // create a stop button
    private void stopButton() {
//        stop.setMaximumSize(new Dimension(100,40));
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start.setEnabled(true);
                stop.setEnabled(false);
                pause.setEnabled(false);
                stopProcessing();
            }
        });
    }
    // create a start button
    private void startButton() {
//        start.setMaximumSize(new Dimension(300,200));
        start.setEnabled(true);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
//                JobQueue jq = new JobQueue();
                queueDrawer.setMediaJobs(Q - U);
//                queueDrawer.setTotalJobs(jobsDialog.getValidatedValue());
//                jobsDrawer.setTotalJobs(jobsDialog.getValidatedValue());
                queueDrawer.setTotalJobs(100);
                jobsDrawer.setTotalJobs(100);
                Notifier[] tan = new Notifier[5];
                logFile = new LogFile();
                tan[0] = new TANotifier();
                tan[1] = queueDrawer;
                tan[2] = statiDrawer;
                tan[3] = jobsDrawer;
                tan[4] = logFile;

                sim = new SpatialQueueSimulator(accelerationS.getValue(), tan, new Receiver(mapView.getReceiverLocation()), mapView);

                sim.start();
                start.setEnabled(false);
                stop.setEnabled(true);
                pause.setEnabled(true);
                setLogAnalyticalResults(ql, (TANotifier) tan[0]);
            }
        });
    }

    // create a pause button
    private void pauseButton() {
//        pause.setMaximumSize(new Dimension(100,40));
        pause.setEnabled(false);
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (paused) {
                    paused = false;
					sim.pause();
                } else {
                    paused = true;
                }
                start.setEnabled(true);
                pause.setEnabled(false);
            }
        });
    }

    //create a slider to control simulation speed
    protected void addSpeedSlider(JPanel accelerationP) {

        dCst = new DrawNormal();

        accelerationP.setBorder(addTitle("Simulation Options", dCst.getSmallGUIFont()));
        JLabel accelerationL = new JLabel("Time x0.0");
        accelerationL.setFont(dCst.getNormalGUIFont());
        accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
        accelerationP.add(accelerationL);

        accelerationS = makeSlider();

        accelerationP.add(accelerationS);
        accelerationS.setValue(50);
        final JLabel finalAccelerationL = accelerationL;
        makeSliderFunctional(accelerationS, finalAccelerationL);
        accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));
    }

    // functionality for the speed slider
    private void makeSliderFunctional(final JSlider accelerationS, final JLabel finalAccelerationL) {
        accelerationS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                int value = accelerationS.getValue();
                if (sim != null) {
                    sim.setTimeMultiplier(value);
                    finalAccelerationL.setText("Time x" + Formatter.formatNumber(sim.getTimeMultiplier(), 2));
                } else {
                    finalAccelerationL.setText("Time x" + Formatter.formatNumber(value, 2));
                }
            }

        });
    }

    // slider visuals
    private JSlider makeSlider() {
        final JSlider accelerationS = new JSlider();
        accelerationS.setValue(50);
        accelerationS.setMaximum(100);
        accelerationS.setMinimum(1);
        accelerationS.setMajorTickSpacing(50);
        accelerationS.setMinorTickSpacing(1);
        accelerationS.setSnapToTicks(true);
        accelerationS.setPaintTicks(true);
        accelerationS.setPaintLabels(true);
        Dictionary<Integer, JLabel> ad = accelerationS.getLabelTable();
        ad.keys();
        ad.put(new Integer(1), new JLabel("real time"));
        ad.put(new Integer(51), new JLabel("faster"));
        ad.put(new Integer(100), new JLabel("fastest"));
        accelerationS.setLabelTable(ad);
        return accelerationS;
    }

    // add title to panels
    protected TitledBorder addTitle(String title, Font f) {
        return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
    }

    private void addJobsPanel(JPanel jobsP) {
        jobsDrawer = new JobsDrawer();
        jobsP.add(jobsDrawer);
    }

    // split a pane in half to view to objects side by side
    protected JPanel getSplitter(int width, int height) {
        JPanel splitPane = new JPanel();
        Dimension dim = new Dimension(width, height);
        splitPane.setEnabled(false);
        splitPane.setPreferredSize(dim);
        splitPane.setMaximumSize(dim);
        splitPane.setMinimumSize(dim);
        return splitPane;
    }

    // create a queue buffer slider
    protected void createQueueBufferSlider(GridBagConstraints c) {

        buffPanel.setLayout(new GridLayout(2, 1));
        c.gridx = 4;
        buffPanel.setVisible(false);
        parametersP.add(buffPanel, c);
        buffL.setAlignmentX(SwingConstants.CENTER);
        buffL.setFont(dCst.getNormalGUIFont());
        buffPanel.add(buffL);
        buffS.setValue(BUFF_I);
        buffS.setMaximum(31);
        buffS.setMinimum(1);
        buffS.setMajorTickSpacing(5);
        buffS.setMinorTickSpacing(1);
        buffS.setPaintLabels(true);
        buffPanel.add(buffS);
        buffL.setText(bufStrS + buffS.getValue() + bufStrE);
        buffS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                StatsUtils.buffSStateChanged(buffS, utilizationL, mediaJobsL, ql, queueDrawer, statiDrawer, buffL, sim);
            }
        });
    }

    // create a service time slider
    protected void createSSlider(GridBagConstraints c) {
        sPanel.setLayout(new GridLayout(2, 1));
        c.gridx = 2;
        parametersP.add(sPanel, c);

        c.gridx = 3;
        c.weightx = 0;
        parametersP.add(getSplitter(10, 1), c);
        c.weightx = 0.5;

        sL = new JLabel();
        sL.setAlignmentX(SwingConstants.CENTER);
        sPanel.add(sL);
        sS.setMaximum(100);
        sS.setMinimum(0);
        sS.setMajorTickSpacing(25);
        sS.setMinorTickSpacing(1);
        sS.setPaintLabels(true);
        sL.setFont(dCst.getNormalGUIFont());

        sPanel.add(sS);

        sMultiplier = 0.02;
        sMultiplierChange = 1;
        sS.setValue(S_I);

        StatsUtils.setSSlider(sS, sMultiplier, sL, ql);
        sS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                StatsUtils.sSStateChanged(ql, utilizationL, mediaJobsL, sim, queueDrawer, statiDrawer, sS, sMultiplier,
                        sL);
                if (sSChange) {
                    StatsUtils.setSMultiplier(sS, sMultiplier, sL, ql, sMultiplierChange);
                }
            }
        });
        sS.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                sSChange = false;
            }

            public void mouseReleased(MouseEvent e) {
                StatsUtils.setSMultiplier(sS, sMultiplier, sL, ql, sMultiplierChange);
                sSChange = true;
            }

        });
    }

    //create a lambda slider
    protected void createLambdaSlider(GridBagConstraints c) {
        lambdaPanel.setLayout(new GridLayout(2, 1));
        c.weightx = 0.5;

        parametersP.add(lambdaPanel, c);

        c.gridx = 1;
        c.weightx = 0;
        parametersP.add(getSplitter(10, 1), c);
        c.weightx = 0.5;

        lambdaL.setAlignmentX(SwingConstants.CENTER);
        lambdaPanel.add(lambdaL);
        lambdaMultiplier = 0.01;
        lambdaMultiplierChange = 0;


        lambdaS.setMaximum(100);
        lambdaS.setMinimum(0);
        lambdaS.setMajorTickSpacing(25);
        lambdaS.setMinorTickSpacing(1);
        lambdaS.setPaintLabels(true);
        lambdaS.setSnapToTicks(true);
        lambdaPanel.add(lambdaS);
        lambdaL.setFont(dCst.getNormalGUIFont());
        lambdaS.setValue(LAMBDA_I);
        StatsUtils.setLambdaSlider(lambdaS, lambdaMultiplier, ql, lambdaL);
        lambdaS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                StatsUtils.lambdaSStateChanged(ql, utilizationL, mediaJobsL,sim, queueDrawer, statiDrawer, lambdaS,
                        lambdaMultiplier, lambdaMultiplierChange, lambdaL, sS, sMultiplier, sL);
                if (lambdaSChange) {
                    StatsUtils.setLambdaMultiplier(lambdaS, lambdaMultiplierChange, lambdaMultiplier, ql, lambdaL);
                }

            }
        });
        lambdaS.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                lambdaSChange = false;
            }

            public void mouseReleased(MouseEvent e) {
                StatsUtils.setLambdaMultiplier(lambdaS, lambdaMultiplierChange, lambdaMultiplier, ql, lambdaL);
                lambdaSChange = true;
            }

        });
        lambdaS.repaint();
    }

    // create the panel that contains the parameter sliders
    protected void createSimulationParametersPanel(GridBagConstraints c, JPanel simulationP) {
        parametersP.setLayout(new GridBagLayout());
        parametersP.setBorder(addTitle("Simulation Parameters", dCst.getSmallGUIFont()));
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        simulationP.add(parametersP, c);
    }

    // create the panel that contains the simulation stats
    protected void createSimulationResultsPanel(GridBagConstraints c, JPanel simulationP) {
        resultsP.setLayout(new GridLayout(2, 2));
        resultsP.setBorder(addTitle("Simulation Results", dCst.getSmallGUIFont()));
        c.gridx = 0;
        c.gridy = 1;
        simulationP.add(resultsP, c);
        StatsUtils.generateSimulationStats(resultsP, mediaJobsL, utilizationL, dCst);
    }
}
