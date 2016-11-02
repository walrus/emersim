package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.gui.common.CommonConstants;
import jmt.jmarkov.Graphics.*;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.Arrivals;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.Queues.JobQueue;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.Queues.Processor;
import jmt.jmarkov.Simulator;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.utils.Formatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

/* Dialog to contain Spatial Queue Window. */

public class SpatialQueueFrame extends JFrame implements ActionListener, PropertyChangeListener {


	private MapConfig mapView;


	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG = false;

	private boolean nonErgodic;//if the utilization is less than 1
	private double U; // Utilization [%]
	private double Q; // Average customer in station
	private double sMultiplier = 1; //service time slide bar multiplier
	private double lambdaMultiplier = 1; //lambda slide bar multiplier
	private int lambdaMultiplierChange = 0; //for the lambda slide bar
	private int sMultiplierChange = 1; //for the service slide bar

	private int buffer; //number of place for the waiting queue
	private int cpuNum; //number of server in the system
	private boolean paused = false; //if the system is paused

	private Dimension initSize = new Dimension(CommonConstants.MAX_GUI_WIDTH_JMCH, CommonConstants.MAX_GUI_HEIGHT_JMCH);

	private JPanel sPanel;
	private JPanel lambdaPanel;
	private JSlider sS;
	private JSlider lambdaS;
	private JSlider buffS;

	private QueueDrawer queueDrawer;
	private StatiDrawer statiDrawer;
	private JobsDrawer jobsDrawer;
	private JTabbedPane outputTabP;
	private JScrollPane txtScroll;
	private TANotifier outputTA;
	private LogFile logFile;
	private Notifier[] tan = new Notifier[5];

	private JPanel buttonsP;

	private JPanel resultsP;
	public JFrame mf;
	private JPanel outputP;
	private JPanel parametersP;
	private JPanel simulationP;

	private JPanel buffPanel;
	private JPanel accelerationP;
	private JPanel jobsP;
	private JSlider accelerationS;

	// Label & Label strings
	private JLabel sL;

	private JLabel lambdaL;

	private JLabel mediaJobsL;

	private JLabel utilizationL;

	private JLabel buffL;

	private JLabel thrL;

	private JLabel responseL;

	private String sStrS = "Avg. Service Time S = ";

	private String sStrE = " s";

	private String lambdaStrS = "Avg. Arrival Rate (lambda) = ";

	private String lambdaStrE = " cust./s";

	private String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";

	private String nStrE = " cust.";

	private String uStrS = "Avg. Utilization (Sum of All Servers) U = ";

	private String uStrE = "";

	private String bufStrS = "Max Station Capacity k = ";

	private String bufStrE = " cust.";

	private String thrStrS = "Avg. Throughput X =";

	private String thrStrE = " cust./s";

	private String respStrS = "Avg. Response Time R = ";

	private String respStrE = " s";

	// Settings
	private Color emptyC = Color.WHITE;

	private Color probC = Color.GREEN;

	private Color queueC = Color.BLUE;

	private Color animC = Color.RED;
	private boolean gradientF = false;
	private DrawConstrains dCst = new DrawNormal();
	private int BUFF_I = 15;

	private int LAMBDA_I = 50;

	private int S_I = 95;

	// menu
	private JMenuBar menuB;

	// help
	private JMenu helpMenu;

	// queue
	private JMenu queueMenu;
	private Action selectQueueRB;
	private JRadioButtonMenuItem gradientItem;
	// spatial queue
	private JMenu spatialMenu;
	// settings
	private JMenu settingsMenu;
	// colors
	private JMenu colorsMenu;

	// size
	private JMenu sizeMenu;
	JobQueue jq;

	Arrivals arrival;
	Processor[] processors;

	private jmt.jmarkov.SpatialQueue.Simulator sim = null;
	private boolean lambdaSChange = true;
	private boolean sSChange = true;

	/** Creates the dialog. */

	public SpatialQueueFrame() {
		this.init();
	}

	public void init(){
		setTitle("Create a new Spatial Queue");
		lambdaS = new JSlider();
		simulationP = new JPanel();
		parametersP = new JPanel();
		lambdaPanel = new JPanel();
		lambdaL = new JLabel();
		lambdaS = new JSlider();
		buffS = new JSlider();
		sPanel = new JPanel();
		sS = new JSlider();
		resultsP = new JPanel();
		mediaJobsL = new JLabel();
		utilizationL = new JLabel();
		mediaJobsL = new JLabel();
		thrL = new JLabel();
		responseL = new JLabel();

		// simulation output panels
		outputP = new JPanel();
		outputTabP = new JTabbedPane();
		txtScroll = new JScrollPane();
		outputTA = new TANotifier();
		logFile = new LogFile();
		// logD = new LogDrawer();
		statiDrawer = new StatiDrawer(ql);
		queueDrawer = new QueueDrawer(ql);
		jobsDrawer = new JobsDrawer();

		buffPanel = new JPanel();
		accelerationP = new JPanel();
		jobsP = new JPanel();
		accelerationS = new JSlider();


		paused = false;
		Dimension d = new Dimension(1000,800);
		setPreferredSize(d);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		generateSideButtons(buttonPanel);
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));
		generateMapPanel(interfacePanel);
		generateQueueDrawer(interfacePanel);
		interfacePanel.add(Box.createVerticalGlue());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

//		JPanel simulationP = new JPanel();
		simulationP.setLayout(new GridBagLayout());
//		JPanel parametersP = new JPanel();
		this.getContentPane().add(simulationP, BorderLayout.SOUTH);


		resultsP.setLayout(new GridLayout(2, 2));
		resultsP.setBorder(addTitle("Simulation Results", dCst.getSmallGUIFont()));
		c.gridx = 0;
		c.gridy = 1;
		simulationP.add(resultsP, c);
		generateSimulationStats(resultsP);

		parametersP.setLayout(new GridBagLayout());
		parametersP.setBorder(addTitle("Simulation Parameters", dCst.getSmallGUIFont()));
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		simulationP.add(parametersP, c);

//		JPanel resultsP = new JPanel();




		// lambda
//		JPanel lambdaPanel = new JPanel();
		lambdaPanel.setLayout(new GridLayout(2, 1));
		c.weightx = 0.5;

		parametersP.add(lambdaPanel, c);

		c.gridx = 1;
		c.weightx = 0;
		parametersP.add(getSplitter(10, 1), c);
		c.weightx = 0.5;

//		JLabel lambdaL = new JLabel();
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
		setLambdaSlider();
		lambdaS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				lambdaSStateChanged(evt);
				if (lambdaSChange) {
					setLambdaMultiplier();
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
				setLambdaMultiplier();
				lambdaSChange = true;
			}

		});
		lambdaS.repaint();

		// S slider
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

		setSSlider();
		sS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				sSStateChanged(evt);
				if (sSChange) {
					setSMultiplier();
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
				setSMultiplier();
				sSChange = true;
			}

		});

		// queueBuffer slider
		buffPanel.setLayout(new GridLayout(2, 1));
		c.gridx = 4;
		buffPanel.setVisible(false);
		parametersP.add(buffPanel, c);
		buffL = new JLabel();
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
				buffSStateChanged(evt);
			}
		});




		add(buttonPanel, BorderLayout.LINE_START);
		add(interfacePanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void generateSimulationStats(JPanel resultsP) {
		// media

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

	private void generateQueueDrawer(JPanel interfacePanel) {
		MM1Logic ql = new MM1Logic(0.0, 0.0);
		QueueDrawer queueDrawer = new QueueDrawer(ql);
		queueDrawer.setPreferredSize(new Dimension(300, 150));
		interfacePanel.add(queueDrawer);
	}

	private void generateMapPanel(JPanel interfacePanel) {
		MapViewOptions mapOptions = new MapViewOptions();
		mapOptions.importPlaces();
		mapView = new MapConfig(mapOptions);
		mapView.setPreferredSize(new Dimension(300, 375));
		interfacePanel.add(mapView);
	}

	private void generateSideButtons(JPanel panel) {
		JButton receiver;
		JButton client;
		final JButton start = new JButton("Start");
		final JButton pause = new JButton("Pause");
		final JButton stop = new JButton("Stop");

		receiver = new JButton("Add Receiver");
		receiver.setPreferredSize(new Dimension(100,40));
		receiver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapView.toggleMarkerPlacement();
			}
		});

		client = new JButton("Add Client");
		client.setPreferredSize(new Dimension(100,40));
		client.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapView.toggleAreaPlacement();
			}
		});

		pause.setPreferredSize(new Dimension(100,40));
		pause.setEnabled(false);
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (paused) {
					paused = false;
//					sim.pause();
				} else {
					paused = true;
				}
				start.setEnabled(true);
				pause.setEnabled(false);
			}
		});

		start.setPreferredSize(new Dimension(100,40));
		start.setEnabled(true);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				start.setEnabled(false);
				stop.setEnabled(true);
				pause.setEnabled(true);
			}
		});

		stop.setPreferredSize(new Dimension(100,40));
		stop.setEnabled(false);
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(true);
				stop.setEnabled(false);
				pause.setEnabled(false);
			}
		});

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

	private void addSpeedSlider(JPanel accelerationP) {

		dCst = new DrawNormal();

		accelerationP.setBorder(addTitle("Simulation Options", dCst.getSmallGUIFont()));
		JLabel accelerationL = new JLabel("Time x0.0");
		accelerationL.setFont(dCst.getNormalGUIFont());
		accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
		accelerationP.add(accelerationL);

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

		accelerationP.add(accelerationS);
		accelerationS.setValue(50);
		final JLabel finalAccelerationL = accelerationL;
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
		accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));
	}


	private void addJobsPanel(JPanel jobsP) {
		JobsDrawer jobsDrawer = new JobsDrawer();
		jobsP.add(jobsDrawer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}

	public static void main(String[] args) {
		new SpatialQueueFrame();
	}

	private TitledBorder addTitle(String title, Font f) {
		return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
	}

	protected JPanel getSplitter(int widht, int height) {
		JPanel splitPane = new JPanel();
		Dimension dim = new Dimension(widht, height);
		splitPane.setEnabled(false);
		splitPane.setPreferredSize(dim);
		splitPane.setMaximumSize(dim);
		splitPane.setMinimumSize(dim);
		return splitPane;
	}

	public void setLambdaSlider() {
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

	public void setLambdaMultiplier() {
		while (true) {
			if (lambdaS.getValue() > lambdaS.getMaximum() * 0.95) {
				if (lambdaMultiplierChange <= 4) {
					if (lambdaMultiplierChange % 2 == 0) {
						lambdaMultiplier *= 2;
						setLambdaSlider();
						lambdaS.setValue((lambdaS.getValue() + 1) / 2);
					} else {
						lambdaMultiplier *= 5;
						setLambdaSlider();
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
						setLambdaSlider();
						lambdaS.setValue(lambdaS.getValue() * 2);
					} else {
						lambdaMultiplier /= 5;
						setLambdaSlider();
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

	public void setSMultiplier() {
		while (true) {
			if (sS.getValue() > sS.getMaximum() * 0.95) {
				if (sMultiplierChange <= 4) {
					if (sMultiplierChange % 2 == 0) {
						sMultiplier *= 2;
						setSSlider();
						sS.setValue((sS.getValue() + 1) / 2);
					} else {
						sMultiplier *= 5;
						setSSlider();
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
						setSSlider();
						sS.setValue(sS.getValue() * 2);
					} else {
						sMultiplier /= 5;
						setSSlider();
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

	MM1Logic ql = new MM1Logic(0.0, 0.0);


	public void setSSlider() {
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
	protected void lambdaSStateChanged(ChangeEvent evt) {
		if (lambdaS.getValue() == 0) {
			lambdaMultiplier = 0.01;
			lambdaMultiplierChange = 0;
			lambdaS.setValue(1);
		}
		ql.setLambda(lambdaMultiplier * lambdaS.getValue());
		lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
		setSSlider();
		updateFields();
	}

	protected void sSStateChanged(ChangeEvent evt) {
		setSSlider();
		updateFields();
	}

	private void updateFields() {
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
		statiDrawer.repaint();

		if (sim == null || !sim.isStarted()) {
			setLogAnalyticalResults();
		} else {
			outputTA.setAnalyticalResult();
		}
	}
	protected void buffSStateChanged(ChangeEvent evt) {
		buffer = buffS.getValue() - cpuNum;
		if (buffer < 1) {
			buffS.setValue(1);
			buffer = 1;
		}
		ql.setMaxStates(buffer);
		queueDrawer.setMaxJobs(buffer + 1);
		statiDrawer.setMaxJobs(buffer + cpuNum);
		buffL.setText(bufStrS + buffS.getValue() + bufStrE);
		updateFields();
	}
	private void setLogAnalyticalResults() {
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

}
