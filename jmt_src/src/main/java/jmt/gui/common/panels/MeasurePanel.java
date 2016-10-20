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

package jmt.gui.common.panels;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.data.LabelValue;
import jmt.framework.data.MacroReplacer;
import jmt.framework.gui.layouts.SpringUtilities;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;
import jmt.gui.exact.table.BooleanCellRenderer;
import jmt.gui.exact.table.DisabledCellRenderer;
import jmt.gui.exact.table.ExactCellEditor;

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 26-lug-2005
 * Time: 16.08.15
 * Modified by Bertoli Marco 29/09/2005, 7-oct-2005
 *                           9-jan-2006  --> ComboBoxCellEditor
 *                           
 * Modified by Ashanka (May 2010):
 * Description: Resized some column's width and edited the column headings.
 * 
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for the capturing the 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class. 
 * 
 */
public class MeasurePanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private static final LabelValue[] DELIMITERS = {new LabelValue(";"), new LabelValue(","), new LabelValue("Tab", "\t"), new LabelValue("Space"," ")};
	private static final LabelValue[] DECIMAL = {new LabelValue("."), new LabelValue(",")};

	//Interfaces for model data exchange
	protected ClassDefinition classData;

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		// Aborts editing of table
		TableCellEditor editor = measureTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	protected StationDefinition stationData;
	protected SimulationDefinition simData;

	protected WarningScrollTable warningPanel;

	//label containing description of this panel's purpose
	private JLabel descrLabel = new JLabel(MEASURES_DESCRIPTION);
	private JLabel logPath;

	//table containing measure data
	protected MeasureTable measureTable;

	//types of measures selectable
	protected static final String[] measureTypes = new String[] {
			"---Select an index---",
			SimulationDefinition.MEASURE_DR,
			SimulationDefinition.MEASURE_FCR_TOTAL_WEIGHT,
			SimulationDefinition.MEASURE_FCR_MEMORY_OCCUPATION,
			SimulationDefinition.MEASURE_FJ_CN,
			SimulationDefinition.MEASURE_FJ_RESPONSE_TIME,
			SimulationDefinition.MEASURE_QL,
			SimulationDefinition.MEASURE_QT,
			SimulationDefinition.MEASURE_RD,
			SimulationDefinition.MEASURE_RP,
			SimulationDefinition.MEASURE_R_PER_SINK,
			SimulationDefinition.MEASURE_S_DR,
			SimulationDefinition.MEASURE_S_CN,
			SimulationDefinition.MEASURE_S_SP,
			SimulationDefinition.MEASURE_S_RP,
			SimulationDefinition.MEASURE_S_X,
			SimulationDefinition.MEASURE_X,
			SimulationDefinition.MEASURE_X_PER_SINK,
			SimulationDefinition.MEASURE_U	
	};

	// Measure selection ComboBox
	protected JComboBox measureSelection = new JComboBox(measureTypes);

	/** Editors and renderers for table */
	protected ImagedComboBoxCellEditorFactory stationsCombos;
	/** Editors and renderers for table */
	protected ImagedComboBoxCellEditorFactory classesCombos;

	//deletes a measure from list
	protected AbstractAction deleteMeasure = new AbstractAction("") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Deletes this measure");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
		}

		public void actionPerformed(ActionEvent e) {
			int index = measureTable.getSelectedRow();
			if (index >= 0 && index < measureTable.getRowCount()) {
				deleteMeasure(index);
			}
		}

	};

	//addition of a class one by one
	protected AbstractAction addMeasure = new AbstractAction("Add selected index") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, ActionEvent.ALT_MASK));
			putValue(Action.SHORT_DESCRIPTION, "Adds a new measure with selected performance index");
		}

		public void actionPerformed(ActionEvent e) {
			addMeasure();
		}

	};

	public MeasurePanel(ClassDefinition classes, StationDefinition stations, SimulationDefinition simParams) {
		stationsCombos = new ImagedComboBoxCellEditorFactory(stations);
		classesCombos = new ImagedComboBoxCellEditorFactory(classes);
		classesCombos.setAllowsNull(true);
		setData(classes, stations, simParams);
		initComponents();
	}

	private void initComponents() {
		this.setBorder(new EmptyBorder(20, 20, 20, 20));
		this.setLayout(new BorderLayout(5, 5));

		JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
		rightPanel.add(measureSelection, BorderLayout.CENTER);
		rightPanel.add(new JLabel(" "), BorderLayout.NORTH);
		measureSelection.addActionListener(addMeasure);
		//ARIF: show all elements in ComboBox
		measureSelection.setMaximumRowCount(measureTypes.length);
		Object popup = measureSelection.getUI().getAccessibleChild(measureSelection, 0);  
		if (popup instanceof ComboPopup) {  
			JList jlist = ((ComboPopup)popup).getList();  
			jlist.setVisibleRowCount(measureTypes.length);
		}
		// GC: Ensures that keyboard arrows can be used to select metric
		measureSelection.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

		measureTable = new MeasureTable();

		JPanel headPanel = new JPanel(new BorderLayout(5, 5));
		headPanel.add(descrLabel, BorderLayout.CENTER);
		headPanel.add(rightPanel, BorderLayout.EAST);
		this.add(headPanel, BorderLayout.NORTH);
		warningPanel = new WarningScrollTable(measureTable, WARNING_CLASS_STATION);
		warningPanel.addCheckVector(classData.getClassKeys());
		warningPanel.addCheckVector(stationData.getStationRegionKeysNoSourceSink());
		this.add(warningPanel, BorderLayout.CENTER);

		// Log definition panel
		JPanel logPanel = new JPanel(new BorderLayout());
		this.add(logPanel, BorderLayout.SOUTH);
		logPanel.add(new JLabel(MEASURE_LOG_DESCRIPTION), BorderLayout.NORTH);
		JPanel logSettings = new JPanel(new SpringLayout());
		JLabel label = new JLabel("Delimiter:");
		final JComboBox delimiters = new JComboBox(DELIMITERS);
		delimiters.setSelectedItem(LabelValue.getElement(DELIMITERS, stationData.getLoggingGlbParameter("delim")));
		delimiters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LabelValue element = (LabelValue) delimiters.getSelectedItem();
				if (!element.getValue().equals(stationData.getLoggingGlbParameter("decimalSeparator"))) {
					stationData.setLoggingGlbParameter("delim", element.getValue());
				} else {
					JOptionPane.showMessageDialog(MeasurePanel.this,
							"Delimiter and Decimal separator cannot be the same. Please make sure that they are different.",
							"Error", JOptionPane.ERROR_MESSAGE);
					delimiters.setSelectedItem(LabelValue.getElement(DELIMITERS, stationData.getLoggingGlbParameter("delim")));
				}
			}
		});
		label.setLabelFor(delimiters);
		logSettings.add(label);
		logSettings.add(delimiters);
		label = new JLabel("Decimal separator:");
		final JComboBox decimals = new JComboBox(DECIMAL);
		decimals.setSelectedItem(LabelValue.getElement(DECIMAL, stationData.getLoggingGlbParameter("decimalSeparator")));
		label.setLabelFor(decimals);
		decimals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LabelValue element = (LabelValue) decimals.getSelectedItem();
				if (!element.getValue().equals(stationData.getLoggingGlbParameter("delim"))) {
					stationData.setLoggingGlbParameter("decimalSeparator", element.getValue());
				} else {
					JOptionPane.showMessageDialog(MeasurePanel.this,
							"Delimiter and Decimal separator cannot be the same. Please make sure that they are different.",
							"Error", JOptionPane.ERROR_MESSAGE);
					decimals.setSelectedItem(LabelValue.getElement(DECIMAL, stationData.getLoggingGlbParameter("decimalSeparator")));
				}
			}
		});
		logSettings.add(label);
		logSettings.add(decimals);
		SpringUtilities.makeCompactGrid(logSettings, 2, 2, 0, 0, 5, 2);
		logPanel.add(logSettings, BorderLayout.EAST);

		final String filePath = new File(MacroReplacer.replace(stationData.getLoggingGlbParameter("path"))).getAbsolutePath();
		JPanel logPathPanel = new JPanel(new FlowLayout(10));
		logPanel.add(logPathPanel, BorderLayout.WEST);
		logPath = new JLabel("CSV files path: " +  filePath);
		logPathPanel.add(logPath);
		final Button filepathButton = new Button("Browse");
		logPathPanel.add(filepathButton);
		filepathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Choose Save Path for all CSV log files...");
				fc.setCurrentDirectory(new File(filePath));
				int ret = fc.showSaveDialog(MeasurePanel.this);

				if (ret == JFileChooser.APPROVE_OPTION) {
					File directory = fc.getSelectedFile();
					stationData.setLoggingGlbParameter("path", directory.getAbsolutePath());
					logPath.setText("CSV files path: " +  directory.getAbsolutePath());
				}
			}
		});
	}

	/**Updates data contained in this panel's components*/
	public void setData(ClassDefinition classes, StationDefinition stations, SimulationDefinition simParams) {
		classData = classes;
		stationData = stations;
		simData = simParams;
		stationsCombos.setData(stations);
		classesCombos.setData(classes);
		refreshComponents();
	}

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
		stationsCombos.clearCache();
		classesCombos.clearCache();
		refreshComponents();
	}

	@Override
	public void repaint() {
		refreshComponents();
		super.repaint();
	}

	private void refreshComponents() {
		if (measureTable != null) {
			measureTable.tableChanged(new TableModelEvent(measureTable.getModel()));
		}
		if (warningPanel != null) {
			warningPanel.clearCheckVectors();
			warningPanel.addCheckVector(classData.getClassKeys());
			warningPanel.addCheckVector(stationData.getStationRegionKeysNoSourceSink());
		}
	}

	private void addMeasure() {
		if (measureSelection.getSelectedIndex() <= 0) {
			return;
		}			
		if (stationData.getStationRegionKeysNoSourceSink().size() == 0 || classData.getClassKeys().size() == 0) {
			measureSelection.setSelectedIndex(0);
			return;
		}
		simData.addMeasure((String) measureSelection.getSelectedItem(), null, null);
		measureTable.tableChanged(new TableModelEvent(measureTable.getModel()));
		measureSelection.setSelectedIndex(0);
	}

	private void deleteMeasure(int index) {
		simData.removeMeasure(simData.getMeasureKeys().get(index));
		measureTable.tableChanged(new TableModelEvent(measureTable.getModel()));
	}

	@Override
	public String getName() {
		return "Performance Indices";
	}

	protected class MeasureTable extends JTable {

		private static final long serialVersionUID = 1L;

		private JButton deleteButton = new JButton(deleteMeasure);

		public MeasureTable() {
			setModel(new MeasureTableModel());
			setRowHeight(ROW_HEIGHT);
			sizeColumns();
			setDefaultEditor(Double.class, new ExactCellEditor());
			setDefaultRenderer(Object.class, new DisabledCellRenderer());
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			int[] columnWidths = ((MeasureTableModel) getModel()).columnWidths;
			for (int i = 0; i < columnWidths.length; i++) {
				int prefWidth = columnWidths[i];
				if (i == columnWidths.length - 1) {
					getColumnModel().getColumn(i).setMaxWidth(getRowHeight());
				} else {
					getColumnModel().getColumn(i).setPreferredWidth(prefWidth);
				}
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 6) {
				return new ButtonCellEditor(deleteButton);
			} else if (column == 2 && simData.isSinkMeasure(simData.getMeasureKeys().get(row))) {
				return stationsCombos.getEditor(stationData.getStationKeysSink());
			} else if (column == 2 && simData.getMeasureType(simData.getMeasureKeys().get(row)).equals(SimulationDefinition.MEASURE_X)) {
				Vector<Object> l1 = stationData.getStationRegionKeysNoSourceSink();
				Vector<Object> l2 = stationData.getStationKeysSource();
				l1.addAll(l2);
				//ARIF: remove generic elements if exists
				l1.removeElement(STATION_TYPE_CLASSSWITCH);
				l1.removeElement(STATION_TYPE_FORK);
				l1.removeElement(STATION_TYPE_SCALER);
				return stationsCombos.getEditor(l1);
			} else if (column == 2 && simData.isFCRMeasure(simData.getMeasureKeys().get(row))) {
				Vector<Object> l1 = stationData.getFCRegionKeys();
				return stationsCombos.getEditor(l1);
			} else if (column == 2 && simData.isFJMeasure(simData.getMeasureKeys().get(row))) {
				Vector<Object> l1 = stationData.getFJKeys();
				return stationsCombos.getEditorFJ(l1);
			} else if (column == 2 && simData.getMeasureType(simData.getMeasureKeys().get(row)).equals(SimulationDefinition.MEASURE_U)) {
				Vector<Object> l1 = stationData.getStationRegionKeysNoSourceSink();
				Vector<Object> l2 = stationData.getStationKeysPlace();
				Vector<Object> l3 = stationData.getStationKeysTransition();
				l1.removeAll(l2);
				l1.removeAll(l3);
				return stationsCombos.getEditor(l1);
			} else if (column == 2) {
				return stationsCombos.getEditor(stationData.getStationRegionKeysNoSourceSink());
			} else if (column == 1 && simData.isFCRMeasure(simData.getMeasureKeys().get(row))) {
				return classesCombos.getEditor(new ArrayList<Object>());
			} else if (column == 1) {
				return classesCombos.getEditor(classData.getClassKeys());
			} else {
				return super.getCellEditor(row, column);
			}
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 6) {
				return new ButtonCellEditor(deleteButton);
			} else if (column == 2 && !simData.isGlobalMeasure(simData.getMeasureKeys().get(row))) {
				if (simData.isFJMeasure(simData.getMeasureKeys().get(row))) {
					return stationsCombos.getRendererFJ();
				} else {
					return stationsCombos.getRenderer();
				}
			} else if (column == 1) {
				return classesCombos.getRenderer();
			} else if (column == 3) {
				return new BooleanCellRenderer();
			} else {
				return super.getCellRenderer(row, column);
			}
		}

	}

	protected class MeasureTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[] { "Performance Index", "Class", "Station/Region", "Stat.Res.", "Conf.Int.",
				"Max Rel.Err.", "" };
		private Class<?>[] columnClasses = new Class[] { String.class, String.class, String.class, Boolean.class, Double.class, Double.class, Object.class };
		public int[] columnWidths = new int[] { 120, 80, 120, 30, 60, 60, 20 };

		public int getRowCount() {
			return simData.getMeasureKeys().size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// Avoid editing of Measure type
			if (columnIndex == 0) {
				return false;
			}
			// Avoid set reference station for global measures
			if (columnIndex == 2 && simData.isGlobalMeasure(simData.getMeasureKeys().get(rowIndex))) {
				return false;
			}
			return true;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object key = simData.getMeasureKeys().get(rowIndex);
			switch (columnIndex) {
			case 0: 
				return simData.getMeasureType(key);
			case 1: 
				return simData.getMeasureClass(key);
			case 2: 
				return simData.getMeasureStation(key);
			case 3:
				return simData.getMeasureLog(key);
			case 4: 
				return simData.getMeasureAlpha(key);
			case 5: 
				return simData.getMeasurePrecision(key);
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Object key = simData.getMeasureKeys().get(rowIndex);
			switch (columnIndex) {
			case 0: 
				simData.setMeasureType((String) aValue, key);
				break;
			case 1: 
				simData.setMeasureClass(aValue, key);
				break;
			case 2: 
				simData.setMeasureStation(aValue, key);
				break;
			case 3: 
				simData.setMeasureLog((Boolean)aValue, key);
				break;
			case 4: {
				try {
					String doubleVal = (String) aValue;
					simData.setMeasureAlpha(Double.valueOf(doubleVal), key);
					break;
				} catch (NumberFormatException e) {
				}
			}
			case 5: {
				try {
					String doubleVal = (String) aValue;
					simData.setMeasurePrecision(Double.valueOf(doubleVal), key);
					break;
				} catch (NumberFormatException e) {
				}
			}
			}
		}

	}
	
}
