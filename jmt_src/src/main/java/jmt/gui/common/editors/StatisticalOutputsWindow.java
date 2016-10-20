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

package jmt.gui.common.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jmt.engine.log.CSVLogger;
import jmt.engine.log.JSimLogger;
import jmt.engine.math.SampleStatistics;
import jmt.engine.math.SampleStatistics.DistributionType;
import jmt.engine.simEngine.EngineUtils;
import jmt.framework.gui.components.JMTFrame;
import jmt.framework.gui.graph.DistributionDensityGraph;
import jmt.framework.gui.layouts.SpringUtilities;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.definitions.MeasureDefinition;
import jmt.gui.common.definitions.StatisticalOutputsLoader;

/**
 * <p>
 * Title: StatisticalOutputsWindow Editor
 * </p>
 * <p>
 * Description: The dialog that displays statistical outputs along with the requested type of graph. It also
 * contains a Redraw button that can be used to vary the number of samples to be taken into consideration while
 * computing statistics, and changing the type of graph desired. Furthermore, there is an option to save the graph
 * for future use.
 * </p>
 * 
 * @author Aneish Goel: july-2013
 */
public class StatisticalOutputsWindow extends JMTFrame {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_INTERVALS = "10";

	private static final String[] GRAPH_TYPES = {"-- Select a type of graph --", "Histogram" , "Density Line", "Distribution"};

	private static final String FILTER_TYPE_SAMPLE = "SAMPLE";
	private static final String FILTER_TYPE_TIME = "TIME";

	/* Constants for type */
	public final static int TYPE_NONE = 0;
	public final static int TYPE_HISTOGRAM = 1;
	public final static int TYPE_DENSITY_LINE = 2;
	public final static int TYPE_DISTRIBUTION = 3;

	public static final String CSV_COLUMN_INDEX = "INTERVAL";
	public static final String CSV_COLUMN_START = "INTERVAL_START";
	public static final String CSV_COLUMN_END = "INTERVAL_END";
	public static final String CSV_COLUMN_VALUE = "VALUE";

	private JButton closeButton ;
	private JButton reDrawButton;
	private JButton saveButton;
	private JProgressBar progressBar ;
	private StatisticalOutputsLoader sop;
	public DistributionDensityGraph graph = null;
	private SampleStatistics statGraph;
	private JPanel graphHolder = new JPanel(new BorderLayout());
	private JLabel icon = new JLabel() ;
	private JPanel rightPanel;
	private JToolBar toolbar;
	public String windowName;
	private JTextField firstSample;
	private JTextField lastSample;
	private JTextField fieldMean;
	private JTextField fieldStanDev;
	private JTextField fieldCoefVar;
	private JTextField fieldVar;
	private JTextField fieldSkew;
	private JTextField fieldKur;
	private JTextField fieldMoment2;
	private JTextField fieldMoment1;
	private JTextField fieldMoment3;
	private JTextField fieldMoment4;
	private JTextField fieldMin;
	private JTextField fieldMax;
	private JTextField discardedSamples;
	private JTextField fieldMinTime;
	private JTextField fieldMaxTime;
	private JTextField minValue;
	private JTextField maxValue;
	private JTextField numberOfIntervals;
	private JLabel minRange;
	private JLabel maxRange;
	private JComboBox<String> graphType;
	private ButtonGroup filterTypeButtons;
	private DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
	private MeasureDefinition measureDefinition;
	private int measureIndex;
	private JSimLogger logger = JSimLogger.getLogger(StatisticalOutputsWindow.class);
	private String filterType = FILTER_TYPE_SAMPLE;
	private boolean inverseMeasure;

	public StatisticalOutputsWindow(MeasureDefinition measureDefinition, int measureIndex) {
		this.measureDefinition = measureDefinition;
		this.measureIndex = measureIndex;
		windowName = measureDefinition.getName(measureIndex);
		inverseMeasure = EngineUtils.isInverseMeasure(measureDefinition.getMeasureType(measureIndex));
		try {
			this.sop = new StatisticalOutputsLoader(measureDefinition, measureIndex);
			initGUI();
		} catch (FileNotFoundException ex) {
			logger.error("Verbose CSV file not found for measure.", ex);
		}
	}

	public void initGUI() {
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setTitle(windowName);
		this.centerWindow(CommonConstants.MAX_GUI_WIDTH_JSIM_STAT_OUTPUT, CommonConstants.MAX_GUI_HEIGHT_JSIM_STAT_OUTPUT);

		//makes LeftSidePanel
		JPanel pivotPanel = new JPanel() ;
		pivotPanel.setLayout(new BoxLayout(pivotPanel, BoxLayout.PAGE_AXIS));

		//makes BottomPanel
		JPanel graphOptionsPanel = new JPanel(new BorderLayout());

		graphOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Graph Drawing Options"));

		//adds ComboBox
		JPanel centrePanel = new JPanel(new BorderLayout());
		JLabel typeLabel = new JLabel("Select graph type");
		graphType = new JComboBox<String>(GRAPH_TYPES);
		typeLabel.setLabelFor(graphType);
		graphType.setSelectedIndex(0);
		graphType.setToolTipText("Choose graph type amongst Density Line, Histogram and Distribution.");
		graphType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGraph();
			}
		});
		centrePanel.add(typeLabel);
		centrePanel.add(graphType);
		graphOptionsPanel.add(centrePanel, BorderLayout.NORTH);

		//adds editable parameters panel
		JPanel parametersPanel = new JPanel(new SpringLayout());
		JLabel paralabel;

		//adds minValue
		paralabel = new JLabel("Min Value:");
		minValue = new JTextField();
		minValue.setEditable(true);
		minValue.setMaximumSize(new Dimension(minValue.getMaximumSize().width, minValue.getMinimumSize().height));
		paralabel.setLabelFor(minValue);
		minValue.setToolTipText("Select minimum Value to be considered in the graph. Press Enter to confirm.");
		minValue.setName("Min Value");

		parametersPanel.add(paralabel);
		parametersPanel.add(minValue);

		//adds maxValue
		paralabel = new JLabel("Max Value:");
		maxValue = new JTextField();
		maxValue.setEditable(true);
		maxValue.setMaximumSize(new Dimension(maxValue.getMaximumSize().width, maxValue.getMinimumSize().height));
		paralabel.setLabelFor(maxValue);
		maxValue.setToolTipText("Select maximum value to be considered in the graph. Press Enter to confirm.");
		maxValue.setName("Min Value");

		parametersPanel.add(paralabel);
		parametersPanel.add(maxValue);

		//adds numberOfIntervals
		paralabel = new JLabel("Number of Intervals:");
		numberOfIntervals = new JTextField();
		numberOfIntervals.setEditable(true);
		numberOfIntervals.setMaximumSize(new Dimension(numberOfIntervals.getMaximumSize().width, numberOfIntervals.getMinimumSize().height));
		paralabel.setLabelFor(numberOfIntervals);
		numberOfIntervals.setText(DEFAULT_INTERVALS);
		numberOfIntervals.setToolTipText("Select the number of intervals to divide the graph in between the minimum and maximum values. Press Enter to confirm.");
		numberOfIntervals.setName("Number of Intervals");

		parametersPanel.add(paralabel);
		parametersPanel.add(numberOfIntervals);

		SpringUtilities.makeCompactGrid(parametersPanel, 3, 2, //rows, cols
				2, 2, //initX, initY
				2, 2);//xPad, yPad
		graphOptionsPanel.add(parametersPanel, BorderLayout.CENTER);

		reDrawButton = new JButton("Redraw");
		reDrawButton.setEnabled(false);
		reDrawButton.setToolTipText("Redraws the graph with the new minimum, maximum values, number of intervals" +
				" and graphType. Also redraws on the basis of selected samples.");
		reDrawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent en) {
				refreshData();
			}
		});

		saveButton = new JButton("Save as CSV");
		saveButton.setEnabled(false);
		saveButton.setToolTipText("Save the data shown in the chart in a CSV file");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSaveChartWindow();
			}
		});

		closeButton = new JButton("EXIT");
		closeButton.setToolTipText("Closes this window.");
		closeButton.addActionListener(new ActionListener() {
			// Fires a window closing event
			@Override
			public void actionPerformed(ActionEvent e) {
				StatisticalOutputsWindow.this.dispatchEvent(new WindowEvent(StatisticalOutputsWindow.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(closeButton);
		buttonsPanel.add(reDrawButton);
		buttonsPanel.add(saveButton);
		graphOptionsPanel.add(buttonsPanel, BorderLayout.SOUTH);

		//makes ValuesPanel
		//Adds with all informations on this measure
		JLabel label;
		//creates MeasuresPanel to hold all Measure values (Mean, Variance, Standard Deviation, Coefficient of Variation,
		//Skewness, and Kurtosis

		JPanel measuresPanel= new JPanel(new SpringLayout());
		measuresPanel.setBorder(new TitledBorder(new EtchedBorder(), "Measures"));
		// MEAN
		label = new JLabel("Mean:");
		fieldMean = new JTextField();
		fieldMean.setEditable(false);
		fieldMean.setToolTipText("Displays mean of the data.");
		fieldMean.setMaximumSize(new Dimension(fieldMean.getMaximumSize().width, fieldMean.getMinimumSize().height));
		label.setLabelFor(fieldMean);
		measuresPanel.add(label);
		measuresPanel.add(fieldMean);

		// VARIANCE
		label = new JLabel("Variance:");
		fieldVar = new JTextField();
		fieldVar.setEditable(false);
		fieldVar.setToolTipText("Displays variance of the data.");
		fieldVar.setMaximumSize(new Dimension(fieldVar.getMaximumSize().width, fieldVar.getMinimumSize().height));
		label.setLabelFor(fieldVar);
		measuresPanel.add(label);
		measuresPanel.add(fieldVar);

		// STANDARD DEVIATION
		label = new JLabel("Standard Deviation:");
		fieldStanDev = new JTextField();
		fieldStanDev.setEditable(false);
		fieldStanDev.setToolTipText("Displays standard deviation of the data.");
		fieldStanDev.setMaximumSize(new Dimension(fieldStanDev.getMaximumSize().width, fieldStanDev.getMinimumSize().height));
		label.setLabelFor(fieldStanDev);
		measuresPanel.add(label);
		measuresPanel.add(fieldStanDev);

		// COEFFICIENT OF VARIATION
		label = new JLabel("Coefficient of Variation:");
		fieldCoefVar = new JTextField() ;
		fieldCoefVar.setEditable(false);
		fieldCoefVar.setToolTipText("Displays coefficient of variation of the data.");
		fieldCoefVar.setMaximumSize(new Dimension(fieldCoefVar.getMaximumSize().width, fieldCoefVar.getMinimumSize().height));
		label.setLabelFor(fieldCoefVar);
		measuresPanel.add(label);
		measuresPanel.add(fieldCoefVar);

		// SKEWNESS
		label = new JLabel("Skewness:");
		fieldSkew = new JTextField() ;
		fieldSkew.setEditable(false);
		fieldSkew.setToolTipText("Displays skewness of the data.");
		fieldSkew.setMaximumSize(new Dimension(fieldSkew.getMaximumSize().width, fieldSkew.getMinimumSize().height));
		label.setLabelFor(fieldSkew);
		measuresPanel.add(label);
		measuresPanel.add(fieldSkew);

		// KURTOSIS
		label = new JLabel("Kurtosis:");
		fieldKur = new JTextField() ;
		fieldKur.setEditable(false);
		fieldKur.setToolTipText("Displays kurtosis of the data.");
		fieldKur.setMaximumSize(new Dimension(fieldKur.getMaximumSize().width, fieldKur.getMinimumSize().height));
		label.setLabelFor(fieldKur);
		measuresPanel.add(label);
		measuresPanel.add(fieldKur);

		//Moments Panel : displays values of all Moments(Order 1,2,3,4)
		JPanel momentsPanel = new JPanel(new SpringLayout());
		momentsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Power Moments"));

		// FIRST MOMENT
		label = new JLabel("First Moment:");
		fieldMoment1 = new JTextField() ;
		fieldMoment1.setEditable(false);
		fieldMoment1.setToolTipText("Displays first moment(mean) of the data.");
		fieldMoment1.setMaximumSize(new Dimension(fieldMoment1.getMaximumSize().width, fieldMoment1.getMinimumSize().height));
		label.setLabelFor(fieldMoment2);
		momentsPanel.add(label);
		momentsPanel.add(fieldMoment1);

		// SECOND MOMENT
		label = new JLabel("Second Moment:");
		fieldMoment2 = new JTextField() ;
		fieldMoment2.setEditable(false);
		fieldMoment2.setToolTipText("Displays second moment of the data.");
		fieldMoment2.setMaximumSize(new Dimension(fieldMoment2.getMaximumSize().width, fieldMoment2.getMinimumSize().height));
		label.setLabelFor(fieldMoment2);
		momentsPanel.add(label);
		momentsPanel.add(fieldMoment2);

		// THIRD MOMENT
		label = new JLabel("Third Moment :");
		fieldMoment3 = new JTextField() ;
		fieldMoment3.setEditable(false);
		fieldMoment3.setToolTipText("Displays third moment of the data.");
		fieldMoment3.setMaximumSize(new Dimension(fieldMoment3.getMaximumSize().width, fieldMoment3.getMinimumSize().height));
		label.setLabelFor(fieldMoment3);
		momentsPanel.add(label);
		momentsPanel.add(fieldMoment3);

		// FOURTH MOMENT
		label = new JLabel("Fourth Moment:");
		fieldMoment4 = new JTextField() ;
		fieldMoment4.setEditable(false);
		fieldMoment4.setToolTipText("Displays fourth moment of the data.");
		fieldMoment4.setMaximumSize(new Dimension(fieldMoment4.getMaximumSize().width, fieldMoment4.getMinimumSize().height));
		label.setLabelFor(fieldMoment4);
		momentsPanel.add(label);
		momentsPanel.add(fieldMoment4);

		//BOUNDS PANEL : displays values of Bounds (min, max) of samples
		JPanel boundsPanel = new JPanel(new SpringLayout());
		boundsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Bounds"));

		//Displays Min Value amongst samples
		label = new JLabel("Min Value:");
		fieldMin = new JTextField() ;
		fieldMin.setEditable(false);
		fieldMin.setToolTipText("Displays minimun value amongst the samples.");
		fieldMin.setMaximumSize(new Dimension(fieldMin.getMaximumSize().width, fieldMin.getMinimumSize().height));
		label.setLabelFor(fieldMin);
		boundsPanel.add(label);
		boundsPanel.add(fieldMin);

		//Displays Max Value amongst samples
		label = new JLabel("Max Value:");
		fieldMax = new JTextField() ;
		fieldMax.setEditable(false);
		fieldMax.setToolTipText("Displays maximum value amongst the samples.");
		fieldMax.setMaximumSize(new Dimension(fieldMax.getMaximumSize().width, fieldMax.getMinimumSize().height));
		label.setLabelFor(fieldMax);
		boundsPanel.add(label);
		boundsPanel.add(fieldMax);

		//Displays Min Simulation time amongst samples
		label = new JLabel("Min Simulation Time:");
		fieldMinTime = new JTextField() ;
		fieldMinTime.setEditable(false);
		fieldMinTime.setToolTipText("Displays the simulation time of the first collected sample");
		fieldMinTime.setMaximumSize(new Dimension(fieldMinTime.getMaximumSize().width, fieldMinTime.getMinimumSize().height));
		label.setLabelFor(fieldMinTime);
		boundsPanel.add(label);
		boundsPanel.add(fieldMinTime);

		//Displays Max Simulation time amongst samples
		label = new JLabel("Max Simulation Time:");
		fieldMaxTime = new JTextField() ;
		fieldMaxTime.setEditable(false);
		fieldMaxTime.setToolTipText("Displays the simulation time of the last collected sample");
		fieldMaxTime.setMaximumSize(new Dimension(fieldMaxTime.getMaximumSize().width, fieldMaxTime.getMinimumSize().height));
		label.setLabelFor(fieldMaxTime);
		boundsPanel.add(label);
		boundsPanel.add(fieldMaxTime);

		label = new JLabel("No. of Discarded Samples:");
		discardedSamples = new JTextField() ;
		discardedSamples.setEditable(false);
		discardedSamples.setToolTipText("Displays the number of discarded samples(transients).");
		discardedSamples.setMaximumSize(new Dimension(discardedSamples.getMaximumSize().width, discardedSamples.getMinimumSize().height));
		label.setLabelFor(discardedSamples);
		discardedSamples.setText(String.valueOf(measureDefinition.getDiscardedSamples(measureIndex)));
		boundsPanel.add(label);
		boundsPanel.add(discardedSamples);

		boundsPanel.add(new JPanel());
		boundsPanel.add(new JPanel());

		SpringUtilities.makeCompactGrid(measuresPanel, 3, 4, //rows, cols
				2, 2, //initX, initY
				2, 2);//xPad, yPad
		//dataPanel.add(measuresPanel, BorderLayout.PAGE_START);

		SpringUtilities.makeCompactGrid(momentsPanel, 2, 4, //rows, cols
				2, 2, //initX, initY
				2, 2);//xPad, yPad
		//dataPanel.add(momentsPanel, BorderLayout.AFTER_LAST_LINE);

		SpringUtilities.makeCompactGrid(boundsPanel, 3, 4, //rows, cols
				2, 2, //initX, initY
				2, 2);//xPad, yPad
		//dataPanel.add(boundsPanel);

		//BOUNDS ON CONSIDERED SAMPLES - selects the range of samples chosen for computation
		JPanel sampleChooser = new JPanel(new SpringLayout());
		sampleChooser.setBorder(new TitledBorder(new EtchedBorder(), "Filter analyzed samples"));

		filterTypeButtons = new ButtonGroup();
		JRadioButton sampleRadio = new JRadioButton("Based on number of samples");
		sampleRadio.setActionCommand(FILTER_TYPE_SAMPLE);
		sampleChooser.add(sampleRadio);
		JRadioButton timeRadio = new JRadioButton("Based on simulation time");
		timeRadio.setActionCommand(FILTER_TYPE_TIME);
		sampleChooser.add(timeRadio);
		filterTypeButtons.add(sampleRadio);
		filterTypeButtons.add(timeRadio);
		filterTypeButtons.setSelected(sampleRadio.getModel(), true);
		sampleRadio.addChangeListener(new ChangeListener() {	
			@Override
			public void stateChanged(ChangeEvent e) {
				switchFilterType(filterTypeButtons.getSelection().getActionCommand());

			}
		});
		timeRadio.addChangeListener(new ChangeListener() {	
			@Override
			public void stateChanged(ChangeEvent e) {
				switchFilterType(filterTypeButtons.getSelection().getActionCommand());

			}
		});

		minRange = new JLabel("First sample:");
		firstSample = new JTextField();
		firstSample.setEditable(true);
		firstSample.setToolTipText("Displays the first sample considered in computation of statistics.");
		firstSample.setMaximumSize(new Dimension(firstSample.getMaximumSize().width, firstSample.getMinimumSize().height));
		firstSample.setName("First sample");
		minRange.setLabelFor(firstSample);

		sampleChooser.add(minRange);
		sampleChooser.add(firstSample);

		maxRange = new JLabel("Last sample:");
		lastSample = new JTextField();
		lastSample.setEditable(true);
		lastSample.setToolTipText("Displays the last sample considered in computation of statistics.");
		lastSample.setMaximumSize(new Dimension(lastSample.getMaximumSize().width, lastSample.getMinimumSize().height));
		lastSample.setName("Last sample");
		maxRange.setLabelFor(lastSample);

		sampleChooser.add(maxRange);
		sampleChooser.add(lastSample);

		SpringUtilities.makeCompactGrid(sampleChooser, 3, 2, //rows, cols
				2, 2, //initX, initY
				2, 2);//xPad, yPad

		pivotPanel.add(measuresPanel);
		pivotPanel.add(Box.createRigidArea(new Dimension(0,15)));
		pivotPanel.add(momentsPanel);
		pivotPanel.add(Box.createRigidArea(new Dimension(0,15)));
		pivotPanel.add(boundsPanel);
		pivotPanel.add(Box.createRigidArea(new Dimension(0,15)));
		pivotPanel.add(sampleChooser);
		pivotPanel.add(Box.createRigidArea(new Dimension(0,30)));
		pivotPanel.add(graphOptionsPanel);
		pivotPanel.add(Box.createRigidArea(new Dimension(0,30)));

		// Add warning that measure is inverse
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(pivotPanel, BorderLayout.CENTER);
		if (inverseMeasure) {
			JLabel warningLabel = new JLabel("<html>Please note that inter-departure times are displayed for Throughput, Drop rate and System power indices</html>");
			warningLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
			warningLabel.setIcon(JMTImageLoader.loadImage("lightbulb"));
			warningLabel.setIconTextGap(5);

			leftPanel.add(warningLabel, BorderLayout.NORTH);
		}

		// main RightPanel
		rightPanel = new JPanel(new BorderLayout()) ;
		//graph panel
		rightPanel.add(graphHolder, BorderLayout.CENTER);

		//Adds Toolbar with Loading/Successful Icon, and ProgressBar
		toolbar = new JToolBar();
		toolbar.setBorder(new TitledBorder(new EtchedBorder(), "Progress Status"));
		toolbar.setFloatable(false);
		toolbar.setRollover(true);

		//Adds a Progress Bar
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setToolTipText("Displays progress of graph loading.");
		progressBar.setForeground(Color.BLUE);
		setProgressBar(sop.getProgress());
		toolbar.add(progressBar);

		//Adds Loading/Loaded Icon
		toolbar.add(icon, BorderLayout.WEST);
		rightPanel.add(toolbar, BorderLayout.SOUTH);

		JSplitPane graphPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, rightPanel);
		graphPanel.setOneTouchExpandable(true);
		graphPanel.setDividerLocation(500);

		this.add(graphPanel, BorderLayout.CENTER);
		setFieldsEnabled(false);

		// Update progress
		setProgressBar(sop.getProgress());
		sop.setProgressTimeListener(new StatisticalOutputsLoader.ProgressTimeListener() {
			@Override
			public void timeChanged(double time) {
				setProgressBar(time);
			}
		});
	}

	private void setProgressBar(double percent) {
		int progress = (int) Math.round(percent * 100);
		progressBar.setValue(progress);
		if (progress < 100) {
			progressBar.setString("Loaded " + progress + "% of performance index samples...");
			icon.setIcon(JMTImageLoader.loadImage(jmt.gui.common.definitions.ResultsConstants.IN_PROGRESS_IMAGE));
			updateStatistics(false) ;
		} else {
			progressBar.setString("Load complete");
			icon.setIcon(JMTImageLoader.loadImage(jmt.gui.common.definitions.ResultsConstants.SUCCESS_IMAGE));
			updateStatistics(true) ;
			setFieldsEnabled(true);
			statGraph = sop.getStatistics();
			updateGraph();
		}
	}

	private void updateGraph() {
		//updates the Graph
		graphHolder.removeAll();
		int type = graphType.getSelectedIndex();
		if (type != TYPE_NONE && statGraph != null && statGraph.isDistributionAvailable()) {
			graph = new DistributionDensityGraph(type, statGraph);
			graph.setToolTipText("Displays desired graph. Right click to 'Save As' and double click to enlarge.");
			graph.setMinimumSize(new Dimension(200, 150));
			graph.setPreferredSize(new Dimension(300, 250));
			graphHolder.add(graph);
			graphHolder.validate();
			graphHolder.repaint();
			addGraphListener();
			saveButton.setEnabled(true);
		} else {
			graph = null;
			saveButton.setEnabled(false);
		}
		graphHolder.repaint();

		if (type != TYPE_NONE && !statGraph.isDistributionAvailable()) {
			// Force a refresh if a chart was selected but no distribution was set.
			refreshData();
		}
	}

	private void updateStatistics(boolean complete) {
		SampleStatistics stat = sop.getStatistics();
		if (stat == null) {
			// statistics are not ready yet.
			return;
		}
		fieldMean.setText(decimalFormat.format(stat.getMean()));
		fieldStanDev.setText(decimalFormat.format(stat.getStandardDeviation()));
		fieldMoment4.setText(decimalFormat.format(stat.getMoment4()));
		fieldMoment3.setText(decimalFormat.format(stat.getMoment3()));
		fieldMoment2.setText(decimalFormat.format(stat.getMoment2()));
		fieldMoment1.setText(decimalFormat.format(stat.getMean()));
		fieldSkew.setText(decimalFormat.format(stat.getSkew()));
		fieldCoefVar.setText(decimalFormat.format(stat.getCoefficienfOfVariation()));
		fieldVar.setText(decimalFormat.format(stat.getVariance()));
		fieldKur.setText(decimalFormat.format(stat.getKurtosis()));
		fieldMin.setText(decimalFormat.format(stat.getMin()));
		fieldMax.setText(decimalFormat.format(stat.getMax()));
		fieldMinTime.setText(decimalFormat.format(stat.getMinSimTime()));
		fieldMaxTime.setText(decimalFormat.format(stat.getMaxSimTime()));
		// Set meaningful initial values for option fields.
		if (complete) {
			if (isEmpty(minValue.getText())) {
				minValue.setText(decimalFormat.format(stat.getMin()));
			}
			if (isEmpty(maxValue.getText())) {
				maxValue.setText(decimalFormat.format(stat.getMax()));
			}
			if (isEmpty(firstSample.getText())) {
				if (filterType.equals(FILTER_TYPE_SAMPLE)) {
					firstSample.setText(String.valueOf(measureDefinition.getDiscardedSamples(measureIndex)+1));
				} else {
					firstSample.setText(String.valueOf(stat.getMinSimTime()));
				}
			}
			if (isEmpty(lastSample.getText())) {
				if (filterType.equals(FILTER_TYPE_SAMPLE)) {
					lastSample.setText(String.valueOf(sop.getTotalLines()));
				} else {
					lastSample.setText(String.valueOf(stat.getMaxSimTime()));
				}
			}
		}
	}

	/**
	 * Utility function to return if a string is empty
	 * @param str the string
	 * @return true if it is empty, false otherwise.
	 */
	private static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public void addGraphListener() {
		graph.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JFrame popupFrame = new JFrame();
					final DistributionDensityGraph popupGraph = new DistributionDensityGraph(graphType.getSelectedIndex(), statGraph);
					popupGraph.setToolTipText("Right click to 'Save As'.");
					popupFrame.getContentPane().add(popupGraph);
					popupFrame.setTitle(windowName);
					popupFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					int width = 640, height = 480;
					Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
					popupFrame.setBounds((scrDim.width - width) / 2, (scrDim.height - height) / 2, width, height);
					popupGraph.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							popupGraph.mouseClicked(e);

						}
					});
					popupFrame.setVisible(true);
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					graph.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							try{
								graph.mouseClicked(e);
							}catch(NullPointerException ek) {
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Refresh data reading parameters from UI form.
	 */
	private void refreshData() {
		try {
			double min = parseNumber(minValue);
			double max = parseNumber(maxValue);
			int start, end;
			double simStart, simEnd;
			if (filterType.equals(FILTER_TYPE_SAMPLE)) {
				simStart = Double.NaN;
				simEnd = Double.NaN;
				start = parseNumber(firstSample).intValue();
				if (start < 1) {
					start = 1;
				}
				end = parseNumber(lastSample).intValue();
				if (end > sop.getTotalLines()) {
					end = sop.getTotalLines();
				}
				firstSample.setText(String.valueOf(start));
				lastSample.setText(String.valueOf(end));
			} else {
				start = -1;
				end = -1;

				simStart = parseNumber(firstSample);
				if (simStart < 0.0) {
					simStart = 0.0;
				}
				simEnd = parseNumber(lastSample);
				if (simEnd > sop.getMaxSimulationTime()) {
					simEnd = sop.getMaxSimulationTime();
				}
				firstSample.setText(decimalFormat.format(simStart));
				lastSample.setText(decimalFormat.format(simEnd));
			}

			int intervals = parseNumber(numberOfIntervals).intValue();
			numberOfIntervals.setText(String.valueOf(intervals));

			sop.setStart(start);
			sop.setEnd(end);
			sop.setInitialSimTime(simStart);
			sop.setFinalSimTime(simEnd);
			sop.setDistribution(min, max, intervals);

			setFieldsEnabled(false);
			sop.reloadData();
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Enable/disable fields that should not be enabled while data is loaded.
	 * @param enabled true to enable, false to disable
	 */
	private void setFieldsEnabled(boolean enabled) {
		reDrawButton.setEnabled(enabled);
		graphType.setEnabled(enabled);
		minValue.setEnabled(enabled);
		maxValue.setEnabled(enabled);
		firstSample.setEnabled(enabled);
		lastSample.setEnabled(enabled);
		numberOfIntervals.setEnabled(enabled);
		Enumeration<AbstractButton> filterButtonsE = filterTypeButtons.getElements();
		while (filterButtonsE.hasMoreElements()) {
			filterButtonsE.nextElement().setEnabled(enabled);
		}		
	}

	private Double parseNumber(JTextField field) throws NumberFormatException {
		if (field.getText() == null || field.getText().length() == 0) {
			throw new NumberFormatException("No value selected for field '" + field.getName() + "'.");
		} else {
			try {
				return Double.valueOf(field.getText());
			} catch (Exception ex) {
				throw new NumberFormatException("Value '" + field.getText() + "' for field '" + field.getName() + "' is not a valid number.");
			}
		}
	}

	private void switchFilterType(String newFilterType) {
		// Ignore irrelevant changes.
		if (newFilterType.equals(filterType)) {
			return;
		}
		this.filterType = newFilterType;
		if (newFilterType.equals(FILTER_TYPE_SAMPLE)) {
			minRange.setText("First sample:");
			maxRange.setText("Last sample:");
			firstSample.setText(String.valueOf(measureDefinition.getDiscardedSamples(measureIndex)+1));
			lastSample.setText(String.valueOf(sop.getTotalLines()));
		} else {
			minRange.setText("Initial simulation time:");
			maxRange.setText("Final simulation time:");
			firstSample.setText(String.valueOf(0.0));
			lastSample.setText(String.valueOf(sop.getMaxSimulationTime()));
		}
	}

	/**
	 * Show a window to save chart data
	 */
	private void showSaveChartWindow() {
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		FileFilter csvFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "CSV file (*.csv)";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".csv");
			}
		};
		chooser.addChoosableFileFilter(csvFilter);
		chooser.setFileFilter(csvFilter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setCurrentDirectory(Defaults.getWorkingPath());
		chooser.setDialogTitle("Save graph data as CSV...");
		int returnCode = chooser.showSaveDialog(this);
		boolean save = false;
		File selectedFile = null;
		if (returnCode == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
			selectedFile = chooser.getSelectedFile();
			// Add extension if missing
			if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
				selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".csv");
			}

			// Check for overwrite.
			if (selectedFile.exists()) {
				int response = JOptionPane.showConfirmDialog(null, "Overwrite file '" + selectedFile.getName() + "'?", "Confirm overwrite", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				save = response == JOptionPane.YES_OPTION;
			} else {
				save = true;
			}
		}

		if (save) {
			DistributionType distrType;
			if (graphType.getSelectedIndex() == StatisticalOutputsWindow.TYPE_DISTRIBUTION) {
				distrType = DistributionType.CUMULATIVE;
			} else {
				distrType = DistributionType.FREQUENCY;
			}
			try {
				saveChartData(selectedFile, distrType);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Error while writing CSV file: " + ex.getMessage());
			}
		}
	}

	/**
	 * Save chart data in target file
	 * @param targetFile the target file for save operation
	 * @param type the distribution type to save
	 * @throws IOException when save operation failed
	 */
	private void saveChartData(File targetFile, DistributionType type) throws IOException {
		CSVLogger logger = new CSVLogger(targetFile, new String[] {CSV_COLUMN_INDEX, CSV_COLUMN_START, CSV_COLUMN_END, CSV_COLUMN_VALUE}, false, 
				Defaults.get("loggerDelimiter"), Defaults.get("loggerDecimalSeparator").charAt(0));
		try {
			HashMap<String, Number> value = new HashMap<>();
			Map<String,Number> defaults = Collections.emptyMap();
			double[] values = statGraph.getDistribution(type);
			double[] start = statGraph.getDistributionIntervalsStart();
			double[] end = statGraph.getDistriburionIntervalsEnd();

			for (int i=0; i<values.length; i++) {
				value.put(CSV_COLUMN_INDEX, i+1);
				value.put(CSV_COLUMN_START, start[i]);
				value.put(CSV_COLUMN_END, end[i]);
				value.put(CSV_COLUMN_VALUE, values[i]);
				logger.log(value, defaults);
			}
		} finally {
			logger.close();
		}
	}

}
