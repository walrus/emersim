package jmt.jmarkov.SpatialQueue.Gui;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.jmarkov.Graphics.*;
import jmt.jmarkov.Graphics.constants.DrawBig;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Graphics.constants.DrawSmall;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Simulation.Server;
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



    private JPanel parametersP;
    private JButton start;
    private JButton pause;
    private JButton stop;
    private JButton client;

    private JButton server;
    static JLabel mediaJobsL;
    static JLabel utilizationL;

    private JSlider lambdaS;
    private boolean paused = false;
    static int lambdaMultiplierChange = 0; //for the lambda slide bar
    static int LAMBDA_I = 50;
    static double sMultiplier = 1; //service time slide bar multiplier
    static double lambdaMultiplier = 1; //lambda slide bar multiplier
    static SpatialQueueSimulator sim;
    static DrawConstrains dCst;
    static MM1Logic ql;
    static QueueDrawer queueDrawer;
    private MapConfig mapView;
    private JSlider accelerationS;
    static JLabel thrL;
    static JLabel responseL;
//    static TANotifier outputTA;
    private SpatialQueueFrame mf;
    private JMenu settingsMenu;
    private JMenu colorsMenu;
    private JRadioButtonMenuItem gradientItem;
    private Color emptyC = Color.WHITE;
    private Color queueC = Color.BLUE;
    private Color animC = Color.RED;
    private JMenu sizeMenu;
    private boolean gradientF = false;
    private int numberClients;
    private boolean returnJourney;


    public GuiComponents(SpatialQueueFrame mf) {
        init();
        StatsUtils.showQueue(lambdaS, utilizationL, mediaJobsL);
        this.mf = mf;
    }

    //Initialise objects
    private void init() {
        sim = null;
        paused = false;
        lambdaS = new JSlider();
        ql = new MM1Logic(0.0, 0.0);
        queueDrawer = new QueueDrawer(ql, true);
        parametersP = new JPanel();
        mediaJobsL = new JLabel();
        utilizationL = new JLabel();
        start = new JButton("Start");
        start.setEnabled(false);
        pause = new JButton("Pause");
        stop = new JButton("Stop");
        client = new JButton("Add Client");
        client.setEnabled(false);
        server = new JButton("Add Server");
        dCst = new DrawNormal();
        thrL = new JLabel();
        responseL = new JLabel();
//        outputTA = new TANotifier();
        returnJourney = false;
        S_I = 70;
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
        serverButton();
        clientButton();
        pauseButton();
        startButton();
        stopButton();

        panel.add(server);
        panel.add(client);
        panel.add(start);
        panel.add(pause);
        panel.add(stop);
        panel.setBorder(new EmptyBorder(50, 0, 0, 0));

        //scroll bar
        addSpeedSlider(panel);
    }

    public void finishClientCreation() {
        client.setEnabled(true);
        start.setEnabled(true);
    }

    // create an add client button
    private JButton clientButton() {

        numberClients = 0;
//        client.setMaximumSize(new Dimension(100,40));
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.setButtonState(MapConfig.BUTTON_STATE.ADD_CLIENT);
                if (numberClients == 0) {
                    Object[] options = {"Include Return Journey",
                            "Don't Include Return Journey"};
                    int choice = JOptionPane.showOptionDialog(mf,
                            "Would you like to include a return journey in your simulation?",
                            "Return Journey",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            2,
                            null,
                            options,
                            options[1]);
                    if (choice == JOptionPane.YES_OPTION) {
                        returnJourney = true;
                    } else if (choice == JOptionPane.NO_OPTION) {
                        returnJourney = false;
                    }
                }

                // Disable add client button to ensure a new region is created in full
                client.setEnabled(false);
                // Disable start button to prevent starting with incomplete clients
                start.setEnabled(false);
                numberClients++;
            }
        });
        return client;
    }

    // create an add server button
    private JButton serverButton() {

//        server.setMaximumSize(new Dimension(100,40));
        server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapView.setButtonState(MapConfig.BUTTON_STATE.ADD_RECEIVER);
                // Disable add server button (restricts to only using 1 server)
                server.setEnabled(false);
                client.setEnabled(true);
            }
        });

        return server;
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
//        outputTA.reset();
        queueDrawer.reset();
        numberClients = 0;
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

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                client.setEnabled(false);
                SimulationSizeDialog jobsDialog = new SimulationSizeDialog(mf);
                jobsDialog.pack();
                jobsDialog.setLocationRelativeTo(mf);
                jobsDialog.setVisible(true);

                queueDrawer.setMediaJobs(Q - U);

                Server client = new Server(mapView.getReceiverLocation());


                sim = new SpatialQueueSimulator(accelerationS.getValue(),
                                                queueDrawer,
                                                client,
                                                mapView,
                                                jobsDialog.getTypedValue(),
                                                returnJourney);

                sim.start();
                start.setEnabled(false);
                stop.setEnabled(true);
                pause.setEnabled(true);
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
        StatsUtils.updateFields(utilizationL, mediaJobsL, sim);
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



    // creates a menu bar
    public void createMenuBar(JMenuBar menuBar) {
        menuBar.add(fileMenu());
        menuBar.add(settingsMenu());
        menuBar.add(helpMenu());
    }

    // creates a file menu
    private JMenu fileMenu() {

        JMenu fileMenu  = new JMenu("File");



        Action Open = new AbstractAction("Open...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action Save = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action SaveAs = new AbstractAction("Save As...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        // generates all of the menu buttons
        Action New = new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Custom button text
                Object[] options = {"Save",
                        "Don't Save",
                        "Cancel"};
                int choice = JOptionPane.showOptionDialog(mf,
                        "Would you like to save your work?",
                        "Create New Simulation",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        2,
                        null,
                        options,
                        options[2]);
                if (choice == JOptionPane.YES_OPTION) {
                    //Save the simulation
                } else if (choice == JOptionPane.NO_OPTION) {
                    mf.dispose();
                    mf = new SpatialQueueFrame();
                }

            }
        };

        Action Compare = new AbstractAction("Compare Simulations...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };


        fileMenu.add(New);
        fileMenu.add(Open);
        fileMenu.add(openRecentSubMenu());
        fileMenu.addSeparator();
        fileMenu.add(Save);
        fileMenu.add(SaveAs);
        fileMenu.addSeparator();
        fileMenu.add(Compare);

        return fileMenu;
    }

    // creates an open recent SUB menu. CURRENTLY JUST A TEMPLATE
    private JMenu openRecentSubMenu() {

        JMenu openRecentMenu = new JMenu("Open Recent");

        // generates all of the sub menu buttons
        Action NullSimulation1 = new AbstractAction("Null Simulation 1") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action NullSimulation2 = new AbstractAction("Null Simulation 2") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action NullSimulation3 = new AbstractAction("Null Simulation 3") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };


        openRecentMenu.add(NullSimulation1);
        openRecentMenu.add(NullSimulation2);
        openRecentMenu.add(NullSimulation3);

        return openRecentMenu;
    }

    // creates a help menu
    private JMenu helpMenu() {

        JMenu helpMenu = new JMenu("Help");

        // generates all of the menu buttons
        Action help = new AbstractAction("Spatial Queue Help") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action about = new AbstractAction("About the Spatial Queue") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };


        helpMenu.add(help);
        helpMenu.addSeparator();
        helpMenu.add(about);

        return helpMenu;
    }

    private JMenu settingsMenu() {

        // settings
        settingsMenu = new JMenu("Settings");
        colorsMenu = new JMenu("Colors");

        Action queueFCAction = new AbstractAction("Queue...") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
                // action code goes here
                Color tmpC;
                tmpC = JColorChooser.showDialog(null, "Queue color", queueC);
                if (tmpC != null) {
                    queueC = tmpC;
                    changeColors();
                }
            }

        };
        colorsMenu.add(queueFCAction);
        colorsMenu.addSeparator();
        Action statusCAction = new AbstractAction("Empty state...") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
                // action code goes here
                Color tmpC;
                tmpC = JColorChooser.showDialog(null, "Empty state color", emptyC);
                if (tmpC != null) {
                    emptyC = tmpC;
                    changeColors();
                }
            }

        };
        colorsMenu.add(statusCAction);

        colorsMenu.addSeparator();

        // gradientItem = new JRadioButtonMenuItem("usa gradiente", false);
        gradientItem = new JRadioButtonMenuItem("Use gradient", false);
        gradientItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gradientF = gradientItem.isSelected();
                changeColors();
            }
        });
        colorsMenu.add(gradientItem);
        settingsMenu.add(colorsMenu);

        // sizeMenu = new JMenu("Dimensioni");
        sizeMenu = new JMenu("Icon size");

        // Action drawSmallAction = new AbstractAction("Piccole") {
        Action drawSmallAction = new AbstractAction("Small") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
                // action code goes here
                dCst = new DrawSmall();
                changeSize();
            }

        };
        sizeMenu.add(drawSmallAction);

        // Action drawNormalAction = new AbstractAction("Normali") {
        Action drawNormalAction = new AbstractAction("Normal") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
                // action code goes here
                dCst = new DrawNormal();
                changeSize();
            }

        };
        sizeMenu.add(drawNormalAction);
        // Action drawBigAction = new AbstractAction("Grandi") {
        Action drawBigAction = new AbstractAction("Large") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
                // action code goes here
                dCst = new DrawBig();
                changeSize();
            }

        };
        sizeMenu.add(drawBigAction);
        settingsMenu.add(sizeMenu);
        return settingsMenu;
    }

    protected void changeSize() {
        queueDrawer.changeDrawSettings(dCst);
        queueDrawer.repaint();
        // logD.changeDrawSettings(dCst);
        mf.validate();

    }

    protected void changeColors() {
        queueDrawer.setColors(emptyC, queueC, animC, gradientF);
        queueDrawer.repaint();

    }
}
