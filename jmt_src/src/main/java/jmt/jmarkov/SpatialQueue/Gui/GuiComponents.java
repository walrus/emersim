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
import jmt.jmarkov.SpatialQueue.Utils.SavedSimulation;
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

/**
 * Created by joshuazeltser on 02/11/2016.
 */
public class GuiComponents{

    //Gui buttons
    private JButton start;
    private JButton pause;
    private JButton stop;
    private JButton client;
    private JButton server;

    private Action Save;
    private Action SaveAs;

    static SpatialQueueSimulator sim;
    static DrawConstrains dCst;
    static QueueDrawer queueDrawer;

    private MapConfig mapConfig;
    private JSlider accelerationS;

    private SpatialQueueFrame mf;
    private JMenu settingsMenu;
    private JMenu colorsMenu;
    private JRadioButtonMenuItem gradientItem;
    private Color emptyC = Color.WHITE;
    private Color queueC = Color.BLUE;
    private Color animC = Color.RED;
    private JMenu sizeMenu;

    private boolean gradientF = false;
    private boolean paused = false;
    private boolean returnJourney;
    private boolean stopped;
    static boolean simSizeSet;

    private String simServer;
    private String simClient;

    private static JProgressBar progressBar;

    private JCheckBoxMenuItem on;
    private JCheckBoxMenuItem off;
    private JCheckBoxMenuItem drive;
    private JCheckBoxMenuItem walk;
    private JCheckBoxMenuItem cycle;
    private JCheckBoxMenuItem publicTransport;
    private JCheckBoxMenuItem fly;

    private Statistics stats;

    public GuiComponents(SpatialQueueFrame mf) {
        init();
        stats.showQueue();
        this.mf = mf;
    }

    //Initialise objects
    private void init() {
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

    // after one client has been created allow for more
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

    // stop threads that are running and set the buttons accordingly
    public void stopProcessing() {
        //stop simulator
        sim.stop();

        start.setEnabled(true);
        stop.setEnabled(false);
        pause.setEnabled(false);

        //wait for threads to finish
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

        // display summary stats page
        if (!stopped) {
            new SummaryPage(sim);
            stopped = true;
        }
    }

    // create a stop button
    private void stopButton() {
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopProcessing();
            }
        });

    }

    // create a start button
    private void startButton() {
        start.setEnabled(false);
        final GuiComponents gui = this;
        simSizeSet = false;
        stopped = false;

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                client.setEnabled(false);

                //display dialog to set the arrivals to unlimited or a fixed number
                SimulationSizeDialog jobsDialog = new SimulationSizeDialog(mf);
                jobsDialog.pack();
                jobsDialog.setLocationRelativeTo(mf);
                jobsDialog.setVisible(true);

                // if mode set create a new simulator and run it
                if (simSizeSet) {
                    queueDrawer.setMediaJobs(stats.Q - stats.U);

                    // Get one server TODO: support for multiple servers
                    Server server = mapConfig.getServers().get(0);

                    sim = new SpatialQueueSimulator(gui, accelerationS.getValue(), server, jobsDialog.getTypedValue());

                    sim.start();
                    start.setEnabled(false);
                    stop.setEnabled(true);
                    pause.setEnabled(true);
                }
            }
        });
    }

    // create a pause button
    private void pauseButton() {
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

    // create slider labels that update as the slider moves
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
        return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f,
                new java.awt.Color(0, 0, 0));
    }

    // add progress bar to show progress of a client job being executed
    protected void addProgressBar(JPanel simulationP, GridBagConstraints c) {
        progressBar = new JProgressBar();
        progressBar.setVisible(true);
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

    // set the value that corresponds to the amount of progress a job has done
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


    // creates a menu bar at top of frame
    public void createMenuBar(JMenuBar menuBar) {
        menuBar.add(fileMenu());
        menuBar.add(simulationSettingsMenu());
        menuBar.add(settingsMenu());
        menuBar.add(helpMenu());
    }

    // creates a file menu
    private JMenu fileMenu() {

        JMenu fileMenu = new JMenu("File");

        //creates a new option in the file menu
        Action newMenu = new AbstractAction("New") {
            //when new is clicked dialog asks if you want to save your work, if not it refreshes
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
                    //refresh the simulator
                    mf.dispose();
                    mf = new SpatialQueueFrame();
                }
            }
        };
        // creates an open simulation option in the file menu
        Action Open = new AbstractAction("Open...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] clientServer = SavedSimulation.fromFile();
                mapConfig.loadClients(clientServer[0]);
                mapConfig.loadServers(clientServer[1]);
                start.setEnabled(true);
                client.setEnabled(true);
            }
        };
        // creates a save simulation option in the file menu
        Save = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SavedSimulation.toExistingFile(mf.getTitle(), mapConfig.saveClients(), mapConfig.saveServers());
            }
        };
        // creates a save as simulation option in the file menu
        SaveAs = new AbstractAction("Save As...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = SavedSimulation.toNewFile(mapConfig.saveClients(), mapConfig.saveServers());
                if (!fileName.isEmpty()){
                    mf.setTitle("Spatial Queue Simulator - " + fileName);
                }
            }
        };

        fileMenu.add(newMenu);
        fileMenu.add(Open);
        fileMenu.add(openRecentSubMenu());
        fileMenu.addSeparator();
        fileMenu.add(Save);
        fileMenu.add(SaveAs);
        fileMenu.addSeparator();
        Save.setEnabled(false);
        SaveAs.setEnabled(false);

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

    // creates a menu with options to change various parameters affecting the simulation
    private JMenu simulationSettingsMenu() {

        JMenu simSettings = new JMenu("Simulation");

        // option to create a custom simulation
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

    // setting to choose whether to include a return journey in the simulation
    private JMenu setReturnJourney() {
        JMenu setReturnJourney = new JMenu("Return Journey");
        on = new JCheckBoxMenuItem("On");
        on.setSelected(true);
        off = new JCheckBoxMenuItem("Off");

        // if on has been clicked off is deselected
        on.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                on.setSelected(true);
                off.setSelected(false);
                returnJourney = true;

            }
        });
        // if off has been clicked on is deselected
        off.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                off.setSelected(true);
                on.setSelected(false);
                returnJourney = false;
            }
        });
        setReturnJourney.add(on);
        setReturnJourney.add(off);
        return setReturnJourney;
    }

    // select your mode of transport menu
    private JMenu setTravelModeSubMenu() {
        JMenu travelMode = new JMenu("Travel Mode");

        drive = new JCheckBoxMenuItem("Drive");
        drive.setSelected(true);
        walk = new JCheckBoxMenuItem("Walk");
        cycle = new JCheckBoxMenuItem("Cycle");
        publicTransport = new JCheckBoxMenuItem("Public Transport");
        fly = new JCheckBoxMenuItem("As-crow-flies");

        // if drive selected deselect other options
        drive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapConfig.setTravelMethod(MapConfig.TRAVEL_METHOD.DRIVING);
                drive.setSelected(true);
                walk.setSelected(false);
                cycle.setSelected(false);
                publicTransport.setSelected(false);
                fly.setSelected(false);
            }
        });
        // if walk selected deselect other options
        walk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapConfig.setTravelMethod(MapConfig.TRAVEL_METHOD.WALKING);
                walk.setSelected(true);
                drive.setSelected(false);
                cycle.setSelected(false);
                publicTransport.setSelected(false);
                fly.setSelected(false);
            }
        });
        // if cycle selected deselect other options
        cycle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapConfig.setTravelMethod(MapConfig.TRAVEL_METHOD.BICYCLING);
                cycle.setSelected(true);
                walk.setSelected(false);
                drive.setSelected(false);
                publicTransport.setSelected(false);
                fly.setSelected(false);
            }
        });
        // if transport selected deselect other options
        publicTransport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapConfig.setTravelMethod(MapConfig.TRAVEL_METHOD.PUBLIC_TRANSPORT);
                publicTransport.setSelected(true);
                walk.setSelected(false);
                cycle.setSelected(false);
                drive.setSelected(false);
                fly.setSelected(false);
            }
        });
        // if fly selected deselect other options
        fly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //display dialog to choose speed to fly at
                final JFrame speedFrame = new JFrame("Speed Settings");
                speedFrame.setLayout(new GridLayout(2,0));
                final JTextField speedValue = new JTextField();
                speedValue.setText("Type your speed (m/s)");
                JButton speedSet = new JButton("Save");
                speedSet.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int speed = Integer.parseInt(speedValue.getText());
                        speedFrame.dispose();
                    }
                });

                speedFrame.add(speedValue);
                speedFrame.add(speedSet);

                speedFrame.pack();
                speedFrame.setLocationRelativeTo(null);
                speedFrame.setVisible(true);

                mapConfig.setTravelMethod(MapConfig.TRAVEL_METHOD.AS_CROW_FLIES);
                fly.setSelected(true);
                walk.setSelected(false);
                cycle.setSelected(false);
                publicTransport.setSelected(false);
                drive.setSelected(false);
            }
        });


        travelMode.add(drive);
        travelMode.add(walk);
        travelMode.add(cycle);
        travelMode.add(publicTransport);
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

    // visualisation setting menu
    private JMenu settingsMenu() {

        // settings
        settingsMenu = new JMenu("Settings");
        colorsMenu = new JMenu("Colors");

        //option to change queue colour
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
        // option to change queue visualisation colour
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

        gradientItem = new JRadioButtonMenuItem("Use gradient", false);
        gradientItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gradientF = gradientItem.isSelected();
                changeColors();
            }
        });
        colorsMenu.add(gradientItem);
        settingsMenu.add(colorsMenu);

        sizeMenu = new JMenu("Icon size");

        // option to change size of visualisation
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

    // function to change size of queue drawer
    protected void changeSize() {
        queueDrawer.changeDrawSettings(dCst);
        queueDrawer.repaint();
        // logD.changeDrawSettings(dCst);
        mf.validate();

    }
    //function to change colour of queue drawer
    protected void changeColors() {
        queueDrawer.setColors(emptyC, queueC, animC, gradientF);
        queueDrawer.repaint();

    }

    // change the text on the client button
    public void setSimClient(String simClient) {
        client.setText("Add " + simClient);
    }

    // change the text on the server button
    public void setSimServer(String simServer) {
        server.setText("Add " + simServer);
    }

    // get the current name of the client
    public String getSimClient() {
        return simClient;
    }

    //get the current name of the server
    public String getSimServer() {
        return simServer;
    }

    // get the current job name
    public void setJobParam(String job) {
        queueDrawer.setJobName(job);
    }

    // get the stats corresponding to this simulation
    public Statistics getStats() {
        return stats;
    }

    // get the map corresponding to this simulation
    public MapConfig getMapConfig() {
        return mapConfig;
    }

    // is a return journey included
    public boolean isReturnJourney() {
        return returnJourney;
    }
}
