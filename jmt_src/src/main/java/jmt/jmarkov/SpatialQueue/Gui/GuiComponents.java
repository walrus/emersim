package jmt.jmarkov.SpatialQueue.Gui;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.Graphics.constants.DrawBig;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Graphics.constants.DrawSmall;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Simulation.Server;
import jmt.jmarkov.SpatialQueue.Simulation.SpatialQueueSimulator;
import jmt.jmarkov.utils.Formatter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;

;

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class GuiComponents {

    private JButton start;
    private JButton pause;
    private JButton stop;
    private JButton client;

    private JButton server;


    private boolean paused = false;

    static double sMultiplier = 1; //service time slide bar multiplier
    static SpatialQueueSimulator sim;
    static DrawConstrains dCst;
    static QueueDrawer queueDrawer;
    private MapConfig mapConfig;
    private JSlider accelerationS;
    static JLabel thrL;
    static JLabel responseL;

    private SpatialQueueFrame mf;
    private JMenu settingsMenu;
    private JMenu colorsMenu;
    private JRadioButtonMenuItem gradientItem;
    private Color emptyC = Color.WHITE;
    private Color queueC = Color.BLUE;
    private Color animC = Color.RED;
    private JMenu sizeMenu;
    private boolean gradientF = false;

    private boolean returnJourney;

    private String simServer;

    private String simClient;
    private static JProgressBar progressBar;
    private JCheckBoxMenuItem on;
    private JCheckBoxMenuItem off;
    private JCheckBoxMenuItem drive;
    private JCheckBoxMenuItem walk;
    private JCheckBoxMenuItem cycle;
    private JCheckBoxMenuItem transport;
    private JCheckBoxMenuItem fly;

    private Statistics stats;


    public GuiComponents(SpatialQueueFrame mf) {
        init();
        stats.showQueue();
        this.mf = mf;
    }

    //Initialise objects
    private void init() {
//        simClient = "Client";
//        simServer = "Receiver";
        sim = null;
        paused = false;
        stats = new Statistics();
        queueDrawer = stats.getQueueDrawer();
        start = new JButton("Start");
        start.setEnabled(false);
        pause = new JButton("Pause");
        stop = new JButton("Stop");
        client = new JButton("Add Client");
        client.setEnabled(false);

        server = new JButton("Add Server");
        simServer = "Server";
        simClient = "Client";
        dCst = new DrawNormal();
        thrL = new JLabel();
        responseL = new JLabel();
//        outputTA = new TANotifier();
        returnJourney = false;
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
        mapConfig = new MapConfig(mapOptions, this);
        mapConfig.setPreferredSize(new Dimension(300, 375));
        interfacePanel.add(mapConfig);
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

//        client.setMaximumSize(new Dimension(100,40));
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapConfig.setButtonState(MapConfig.BUTTON_STATE.ADD_CLIENT);
                // Disable add client button to ensure a new region is created in full
                client.setEnabled(false);
                // Disable start button to prevent starting with incomplete clients
                start.setEnabled(false);
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
                mapConfig.setButtonState(MapConfig.BUTTON_STATE.ADD_RECEIVER);
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

        new SummaryPage(sim);
//        outputTA.reset();

//        queueDrawer.reset();
//        stats.updateFields(sim);
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

                queueDrawer.setMediaJobs(stats.Q - stats.U);

                // Get one server TODO: support for multiple servers
                Server server = mapConfig.getServers().get(0);

                sim = new SpatialQueueSimulator(accelerationS.getValue(),
                        stats,
                        server,
                        mapConfig,
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

    protected void addProgressBar(JPanel simulationP, GridBagConstraints c) {
        progressBar = new JProgressBar();
        progressBar.setVisible(true);

//        progressBar.setValue(25);
        int count = 0;

        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Executing...");
        progressBar.setBorder(border);

        progressBar.setLayout(new GridBagLayout());
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        simulationP.add(progressBar, c);

    }

    public static void setProgressBarValue(int percentage) {
        progressBar.setValue(percentage);
    }


    // create the panel that contains the simulation stats
    protected void createSimulationResultsPanel(GridBagConstraints c, JPanel simulationP) {
        JPanel resultsP = new JPanel();
        resultsP.setLayout(new GridLayout(2, 2));
        resultsP.setBorder(addTitle("Simulation Results", dCst.getSmallGUIFont()));
        c.gridx = 0;
        c.gridy = 1;
        simulationP.add(resultsP, c);
        stats.generateSimulationStats(resultsP);
    }


    // creates a menu bar
    public void createMenuBar(JMenuBar menuBar) {
        menuBar.add(fileMenu());
        menuBar.add(simulationSettingsMenu());
        menuBar.add(settingsMenu());
        menuBar.add(helpMenu());
    }

    // creates a file menu
    private JMenu fileMenu() {

        JMenu fileMenu = new JMenu("File");

        Action newMenu = new AbstractAction("New") {
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

        Action Compare = new AbstractAction("Compare Simulations...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };


        fileMenu.add(newMenu);
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

    private JMenu simulationSettingsMenu() {

        JMenu simSettings = new JMenu("Simulation");

        Action customSim = new AbstractAction("Custom Simulation") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CustomSimulationDialog(mf);

            }
        };


        simSettings.add(customSim);
        simSettings.add(setTravelModeSubMenu());
        simSettings.add(setReturnJourney());


        return simSettings;
    }

    private JMenu setReturnJourney() {
        JMenu setReturnJourney = new JMenu("Return Journey");
        on = new JCheckBoxMenuItem("On");
        on.setSelected(true);
        off = new JCheckBoxMenuItem("Off");


        on.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                off.setSelected(false);
                returnJourney = true;

            }
        });

        off.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                on.setSelected(false);
                returnJourney = false;
            }
        });
        setReturnJourney.add(on);
        setReturnJourney.add(off);
        return setReturnJourney;
    }

    private JMenu setTravelModeSubMenu() {
        JMenu travelMode = new JMenu("Travel Mode");


        drive = new JCheckBoxMenuItem("Drive");
        drive.setSelected(true);
        walk = new JCheckBoxMenuItem("Walk");
        cycle = new JCheckBoxMenuItem("Cycle");
        transport = new JCheckBoxMenuItem("Transport");
        fly = new JCheckBoxMenuItem("Fly");

        drive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                walk.setSelected(false);
                cycle.setSelected(false);
                transport.setSelected(false);
                fly.setSelected(false);
            }
        });

        walk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drive.setSelected(false);
                cycle.setSelected(false);
                transport.setSelected(false);
                fly.setSelected(false);
            }
        });

        cycle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                walk.setSelected(false);
                drive.setSelected(false);
                transport.setSelected(false);
                fly.setSelected(false);
            }
        });

        transport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                walk.setSelected(false);
                cycle.setSelected(false);
                drive.setSelected(false);
                fly.setSelected(false);
            }
        });

        fly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                walk.setSelected(false);
                cycle.setSelected(false);
                transport.setSelected(false);
                drive.setSelected(false);
            }
        });


        travelMode.add(drive);
        travelMode.add(walk);
        travelMode.add(cycle);
        travelMode.add(transport);
        travelMode.add(fly);

        return travelMode;
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

    public void setSimClient(String simClient) {

        client.setText("Add " + simClient);

    }

    public void setSimServer(String simServer) {
        server.setText("Add " + simServer);
    }

    public String getSimClient() {
        return simClient;
    }

    public String getSimServer() {
        return simServer;
    }

    public void setJobParam(String job) {
        queueDrawer.setJobName(job);
    }


}
