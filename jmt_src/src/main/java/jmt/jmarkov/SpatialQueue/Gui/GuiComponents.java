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

    //To change service time change this variable
    private int S_I =  70;

    private JPanel parametersP;
    private JButton start;
    private JButton pause;
    private JButton stop;
    private JButton client;
    private JButton receiver;
    private JLabel mediaJobsL;
    private JLabel utilizationL;
    private JSlider lambdaS;
    private boolean paused = false;
    static int lambdaMultiplierChange = 0; //for the lambda slide bar
    private int LAMBDA_I = 50;
    static double sMultiplier = 1; //service time slide bar multiplier
    static double lambdaMultiplier = 1; //lambda slide bar multiplier
    static SpatialQueueSimulator sim;
    static DrawNormal dCst;
    static MM1Logic ql;
    static QueueDrawer queueDrawer;
    static StatiDrawer statiDrawer;
    private MapConfig mapView;
    private JobsDrawer jobsDrawer;
    private JSlider accelerationS;
    private LogFile logFile;
    static JLabel thrL;
    static JLabel responseL;
    static TANotifier outputTA;
    private JFrame mf;


    public GuiComponents(JFrame mf) {
        init();
        showQueue(1);
        this.mf = mf;
    }

    //Initialise objects
    private void init() {
        sim = null;
        paused = false;
        lambdaS = new JSlider();
        ql = new MM1Logic(0.0, 0.0);
        queueDrawer = new QueueDrawer(ql);
        statiDrawer = new StatiDrawer(ql);
        parametersP = new JPanel();
        mediaJobsL = new JLabel();
        utilizationL = new JLabel();
        start = new JButton("Start");
        start.setEnabled(false);
        pause = new JButton("Pause");
        stop = new JButton("Stop");
        client = new JButton("Add Client");
        client.setEnabled(false);
        receiver = new JButton("Add Receiver");
        dCst = new DrawNormal();
        thrL = new JLabel();
        responseL = new JLabel();
        jobsDrawer = new JobsDrawer();
        outputTA = new TANotifier();
    }

    //Create queueDrawer for queue visualisation
    protected void generateQueueDrawer(JPanel interfacePanel) {
        queueDrawer.setPreferredSize(new Dimension(300, 150));
        interfacePanel.add(queueDrawer);
    }

    //create map panel for gui
    protected void generateMapPanel(JPanel interfacePanel) {
        MapViewOptions mapOptions = new MapViewOptions();
        mapOptions.importPlaces();
        mapView = new MapConfig(mapOptions, this);
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

    public void finishClientCreation() {
        client.setEnabled(true);
        start.setEnabled(true);
    }

    // create an add client button
    private JButton clientButton() {

//        client.setMaximumSize(new Dimension(100,40));
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.setButtonState(MapConfig.BUTTON_STATE.ADD_CLIENT);
                // Disable add client button to ensure a new region is created in full
                client.setEnabled(false);
                // Disable start button to prevent starting with incomplete clients
                start.setEnabled(false);
            }
        });
        return client;
    }

    // create an add receiver button
    private JButton receiverButton() {

//        receiver.setMaximumSize(new Dimension(100,40));
        receiver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.setButtonState(MapConfig.BUTTON_STATE.ADD_RECEIVER);
                // Disable add receiver button (restricts to only using 1 receiver)
                receiver.setEnabled(false);
                client.setEnabled(true);
            }
        });

        return receiver;
    }

    public void stopProcessing() {
        sim.stop();
        while (sim.isRunning()) {
            //waiting to stop
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        outputTA.reset();
        logFile.reset();
        queueDrawer.reset();
        statiDrawer.reset();
        jobsDrawer.reset();

        updateFields(utilizationL, mediaJobsL, sim);
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
        start.setEnabled(false);



////        logFile.setLogging(false);


        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                client.setEnabled(false);
                SimulationSizeDialog jobsDialog = new SimulationSizeDialog(mf);
                jobsDialog.pack();
                jobsDialog.setLocationRelativeTo(mf);
                jobsDialog.setVisible(true);

                queueDrawer.setMediaJobs(Q - U);
                Notifier[] tan = new Notifier[5];
                logFile = new LogFile();
                tan[0] = outputTA;
                tan[1] = queueDrawer;
                tan[2] = statiDrawer;
                tan[3] = jobsDrawer;
                tan[4] = logFile;

                sim = new SpatialQueueSimulator(accelerationS.getValue(), tan, new Receiver(mapView.getReceiverLocation()), mapView);

                sim.start();
                start.setEnabled(false);
                stop.setEnabled(true);
                pause.setEnabled(true);
                setLogAnalyticalResults();
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

        accelerationP.setBorder(addTitle("Simulation Options", dCst.getSmallGUIFont()));
        JLabel accelerationL = new JLabel("Time x0.0");
        accelerationL.setFont(dCst.getNormalGUIFont());
        accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
        accelerationP.add(accelerationL);

        accelerationS = makeSlider();

        accelerationP.add(accelerationS);
        accelerationS.setValue(50);
        final JLabel finalAccelerationL = accelerationL;
        makeSpeedSliderFunctional(accelerationS, finalAccelerationL);
        accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));
    }

    // functionality for the speed slider
    private void makeSpeedSliderFunctional(final JSlider accelerationS, final JLabel finalAccelerationL) {
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

    // create a service time slider
    protected void setupServiceTime() {
        sMultiplier = 0.02;
        ql.setS(S_I * sMultiplier);
    }

    //create a lambda slider
    protected void createLambdaSlider(GridBagConstraints c) {
        final boolean[] lambdaSChange = {true};
        JPanel lambdaPanel = new JPanel();
        setupServiceTime();

        lambdaPanel.setLayout(new GridLayout(2, 1));
        c.weightx = 0.5;

        parametersP.add(lambdaPanel, c);

        c.gridx = 1;
        c.weightx = 0;
        parametersP.add(getSplitter(10, 1), c);
        c.weightx = 0.5;

        final JLabel lambdaL = new JLabel();
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
        StatsUtils.setLambdaSlider(lambdaS, lambdaL);

        lambdaS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                StatsUtils.lambdaSStateChanged(utilizationL, mediaJobsL, sim, lambdaS, lambdaL);

                if (lambdaSChange[0]) {
                    StatsUtils.setLambdaMultiplier(lambdaS, lambdaL);
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
                lambdaSChange[0] = false;
            }

            public void mouseReleased(MouseEvent e) {
                StatsUtils.setLambdaMultiplier(lambdaS, lambdaL);
                lambdaSChange[0] = true;
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
        JPanel resultsP = new JPanel();
        resultsP.setLayout(new GridLayout(2, 2));
        resultsP.setBorder(addTitle("Simulation Results", dCst.getSmallGUIFont()));
        c.gridx = 0;
        c.gridy = 1;
        simulationP.add(resultsP, c);
        StatsUtils.generateSimulationStats(resultsP, mediaJobsL, utilizationL);
    }

    //setup queue visualisation and pointer
    protected void showQueue(int cpuNumber) {

        ql = new MM1Logic(lambdaMultiplier * lambdaS.getValue(), S_I * sMultiplier);

        lambdaS.setValue(LAMBDA_I);
        statiDrawer.updateLogic(ql);
        queueDrawer.updateLogic(ql);
        queueDrawer.setMaxJobs(0);
        statiDrawer.setMaxJobs(0);
        queueDrawer.setCpuNumber(1);
        StatsUtils.updateFields(utilizationL, mediaJobsL, sim);
    }
}
