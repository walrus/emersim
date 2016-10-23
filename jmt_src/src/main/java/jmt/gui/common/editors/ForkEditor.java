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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.forkStrategies.MultiBranchClassSwitchFork;
import jmt.gui.common.forkStrategies.ClassSwitchFork;
import jmt.gui.common.forkStrategies.CombFork;
import jmt.gui.common.forkStrategies.ForkStrategy;
import jmt.gui.common.forkStrategies.OutPath;
import jmt.gui.common.forkStrategies.ProbabilitiesFork;
import jmt.gui.common.panels.WarningScrollTable;
import jmt.gui.exact.table.DisabledCellRenderer;
import jmt.gui.exact.table.ExactCellEditor;

public class ForkEditor extends JSplitPane implements CommonConstants {

	private static final long serialVersionUID = 1L;
	private Map<Object, OutPath> outPaths;
	private Map<Object, Double> combProbs;
	private Map<Object, Object> out;
	private ArrayList<Object> keys;
	private StationDefinition stations;
	private ClassDefinition classes;
	private Object stationKey, classKey;

	private WarningScrollTable rtPane;
	private WarningScrollTable otPane;
	private WarningScrollTable cbPane;
	private WarningScrollTable csPane;
	private WarningScrollTable onPane;
	private WarningScrollTable cnPane;
	private OutComboBox jl;
	private JTextArea descrTextPane = new JTextArea("");
	private ForkingTable forkingTable = new ForkingTable();
	private OutPathTable outTable = new OutPathTable();
	private ForkingSwitchTable switchTable = new ForkingSwitchTable();
	private OutNumTable numTable;
	private CombTable combTable = new CombTable();
	private ClassNumTable classNumTable;
	private JTextArea noOptLabel = new JTextArea("No options available for this fork strategy");
	private JScrollPane noOptLabelPanel = new JScrollPane(noOptLabel);
	private JSplitPane jsp = new JSplitPane();
	private JSplitPane jsp2 = new JSplitPane();
	private JPanel buttons;

	private Object item;

	public ForkEditor(StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		super();
		super.setOrientation(JSplitPane.VERTICAL_SPLIT);
		super.setDividerSize(3);
		super.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setResizeWeight(.5);
		classNumTable = new ClassNumTable(cs);
		numTable = new OutNumTable(cs);
		initComponents();
		setData(sd, cs, stationKey, classKey);
	}

	private void initComponents() {
		rtPane = new WarningScrollTable(forkingTable, WARNING_OUTGOING_ROUTING);
		otPane = new WarningScrollTable(outTable, "Select a branch");
		cbPane = new WarningScrollTable(combTable, WARNING_OUTGOING_ROUTING);
		csPane = new WarningScrollTable(switchTable, WARNING_OUTGOING_ROUTING);
		onPane = new WarningScrollTable(numTable, "Select a branch");
		cnPane = new WarningScrollTable(classNumTable, WARNING_OUTGOING_ROUTING);

		switchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (switchTable.getSelectedRow() > -1) {
					out = outPaths.get(indexToKey(switchTable.getSelectedRow())).getOutParameters();
					numTable.updateUI();
					onPane.updateUI();
				}
			}

			private Object indexToKey(int index) {
				if (stationKey == null) {
					return null;
				}
				return stations.getForwardConnections(stationKey).get(index);
			}
		});

		noOptLabelPanel.setBorder(new TitledBorder(new EtchedBorder(), "Forking Options"));
		noOptLabel.setOpaque(false);
		noOptLabel.setEditable(false);
		noOptLabel.setLineWrap(true);
		noOptLabel.setWrapStyleWord(true); 
		rtPane.setBorder(new TitledBorder(new EtchedBorder(), "Branch probability"));
		descrTextPane.setOpaque(false);
		descrTextPane.setEditable(false);
		descrTextPane.setLineWrap(true);
		descrTextPane.setWrapStyleWord(true);
		cbPane.setBorder(new TitledBorder(new EtchedBorder(), "Forking Options"));
		csPane.setBorder(new TitledBorder(new EtchedBorder(), "Branch"));
		onPane.setBorder(new TitledBorder(new EtchedBorder(), "Number of tasks per class"));
		cnPane.setBorder(new TitledBorder(new EtchedBorder(), "Number of tasks per class"));
		jl = new OutComboBox();
		JLabel label = new JLabel("Select the Branch:");
		label.setLabelFor(jl);
		JButton add = new JButton("Add");
		JButton delete = new JButton("Delete");

		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (out != null && keys != null) {
					out.put(null, null);
					keys.add(null);
					outTable.updateUI();
				}
			}
		});

		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (outTable.getSelectedRow() != -1) {
					if (outTable.getRowCount() != 1) {
						out.remove(keys.get(outTable.getSelectedRow()));
						keys.remove(outTable.getSelectedRow());
						outTable.updateUI();
					}
				}
			}
		});

		buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));
		buttons.add(add);
		buttons.add(delete);
		jsp.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jsp2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jsp.setDividerSize(3);
		jsp2.setDividerSize(3);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
		jsp2.setBorder(new TitledBorder(new EtchedBorder(), "Distribution of the number of tasks"));
		jsp2.setResizeWeight(1);
		jsp2.setRightComponent(buttons);
		jsp2.setLeftComponent(otPane);

		JPanel branchOp = new JPanel();
		branchOp.add(label);
		branchOp.add(jl);

		jsp.setLeftComponent(branchOp);
		jsp.setRightComponent(jsp2);
	}

	public void setData(StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		ForkStrategy fs = (ForkStrategy) sd.getForkingStrategy(stationKey, classKey);
		numTable.clearSelection();
		outTable.clearSelection();
		jsp2.getRightComponent().setVisible(false);
		forkingTable.clearSelection();
		combTable.clearSelection();
		switchTable.clearSelection();
		jl.setSelectedIndex(-1);
		if (fs == null) {
			emptyPane();
		} else {
			descrTextPane.setText(fs.getDescription());
			if (fs instanceof ProbabilitiesFork) {
				createDetails(fs, sd, stationKey, classKey);
			} else if (fs instanceof CombFork) {
				createCombDetails(fs, sd, stationKey, classKey);
			} else if (fs instanceof ClassSwitchFork) {
				createClassSwitchDetails(fs, sd, cs, stationKey, classKey);
			} else if (fs instanceof MultiBranchClassSwitchFork) {
				createMultiBranchClassSwitchDetails(fs, sd, cs, stationKey, classKey);
			} else {
				emptyPane();
			}
		}
		doLayout();
	}

	private void emptyPane() {
		setLeftComponent(noOptLabelPanel);
		setRightComponent(new JPanel());
		out = null;
	}

	private void createDetails(ForkStrategy fs, StationDefinition sd, Object stationKey, Object classKey) {
		outPaths = (Map<Object, OutPath>) fs.getOutDetails();
		stations = sd;
		out = null;
		combProbs = null;
		keys = null;
		this.stationKey = stationKey;
		this.classKey = classKey;
		if (outPaths.isEmpty()) {
			setupForking();
		}
		if (stations.getForwardConnections(stationKey).size() == 1) {
			jl.setSelectedIndex(0);
		}
		if (stations.getForwardConnections(stationKey).isEmpty()) {
			setLeftComponent(rtPane);
			setRightComponent(new JPanel());
		} else {
			setLeftComponent(rtPane);
			setRightComponent(jsp);
		}
	}

	private void createCombDetails(ForkStrategy fs, StationDefinition sd, Object stationKey, Object classKey) {
		combProbs = (Map<Object, Double>) fs.getOutDetails();
		stations = sd;
		this.stationKey = stationKey;
		this.classKey = classKey;
		out = null;
		keys = null;
		if (combProbs.isEmpty()) {
			setupCombForking();
		}
		setLeftComponent(cbPane);
		setRightComponent(new JPanel());
	}

	private void createClassSwitchDetails(ForkStrategy fs, StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		outPaths = (Map<Object, OutPath>) fs.getOutDetails();
		stations = sd;
		this.stationKey = stationKey;
		this.classKey = classKey;
		classes = cs;
		out = null;
		combProbs = null;
		keys = null;
		if (outPaths.isEmpty()) {
			setupClassSwitchForking();
		}
		if (outPaths.size() > 0) {
			out = outPaths.values().iterator().next().getOutParameters();
		}
		setLeftComponent(cnPane);
		setRightComponent(new JPanel());
	}

	private void createMultiBranchClassSwitchDetails(ForkStrategy fs, StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		outPaths = (Map<Object, OutPath>) fs.getOutDetails();
		stations = sd;
		this.stationKey = stationKey;
		this.classKey = classKey;
		classes = cs;
		out = null;
		combProbs = null;
		keys = null;
		if (outPaths.isEmpty()) {
			setupMultiBranchClassSwitchForking();
		}
		if (stations.getForwardConnections(stationKey).isEmpty()) {
			setLeftComponent(csPane);
			setRightComponent(new JPanel());
		} else {
			setLeftComponent(csPane);
			setRightComponent(onPane);
		}
	}

	/**
	 * sets up all of the entries in routing table from output connections for
	 * specified station
	 */
	protected void setupForking() {
		if (stationKey == null || classKey == null || stations == null) {
			return;
		}
		// fetching output-connected stations list
		Vector<Object> output = stations.getForwardConnections(stationKey);
		// saving all entries of routing strategy in a temporary data structure
		HashMap<Object, OutPath> temp2 = new HashMap<Object, OutPath>(outPaths);
		outPaths.clear();
		for (int i = 0; i < output.size(); i++) {
			// add old entries to map only if they are still in the current
			// connection set
			Object currentKey = output.get(i);
			if (temp2.containsKey(currentKey)) {
				outPaths.put(currentKey, temp2.get(currentKey));
			} else {
				// if connection set contains new entries, set them to (1, 1.0)
				// by default
				OutPath tempPath = new OutPath();
				tempPath.putEntry(1, 1.0);
				tempPath.setOutName(stations.getStationName(currentKey));
				tempPath.setProb(1.0);
				outPaths.put(currentKey, tempPath);
			}
			if (stations.getForwardConnections(stationKey).size() == 1) {
				jl.setSelectedIndex(0);
			}
		}
	}

	protected void setupCombForking() {
		if (stationKey == null || classKey == null || stations == null || combProbs == null) {
			return;
		}
		Vector<Object> output = stations.getForwardConnections(stationKey);
		HashMap<Object, Double> temp = new HashMap<Object, Double>(combProbs);
		combProbs.clear();
		for (int i = 0; i < output.size(); i++) {
			// add old entries to map only if they are still in the current
			// connection set
			String key = Integer.toString(i+1);
			if (temp.containsKey(key)) {
				combProbs.put(key, temp.get(key));
			} else {
				// if connection set contains new entries, set them to 0 by
				// default
				combProbs.put(key, new Double(0.0));
			}
		}
	}

	protected void setupClassSwitchForking() {
		if (stationKey == null || classKey == null || stations == null || outPaths == null) {
			return;
		}
		// fetching output-connected stations list
		Vector<Object> output = stations.getForwardConnections(stationKey);
		// saving all entries of routing strategy in a temporary data structure
		HashMap<Object, OutPath> temp2 = new HashMap<Object, OutPath>(outPaths);
		outPaths.clear();
		for (int i = 0; i < output.size(); i++) {
			// add old entries to map only if they are still in the current
			// connection set
			Object currentKey = output.get(i);

			if (temp2.containsKey(currentKey)) {
				outPaths.put(currentKey, temp2.get(currentKey));
			} else {
				OutPath tempPath = new OutPath();
				tempPath.setOutName(stations.getStationName(currentKey));
				tempPath.setProb(1.0);

				for (Object o: classes.getClassKeys()) {
					if (o == this.classKey) {
						tempPath.putEntry(o, 1);
					} else {
						tempPath.putEntry(o, 0);
					}
				}
				outPaths.put(currentKey, tempPath);
			}
		}
	}

	protected void setupMultiBranchClassSwitchForking() {
		if (stationKey == null || classKey == null || stations == null) {
			return;
		}
		// fetching output-connected stations list
		Vector<Object> output = stations.getForwardConnections(stationKey);
		// saving all entries of routing strategy in a temporary data structure
		HashMap<Object, OutPath> temp2 = new HashMap<Object, OutPath>(outPaths);
		outPaths.clear();
		for (int i = 0; i < output.size(); i++) {
			// add old entries to map only if they are still in the current
			// connection set
			Object currentKey = output.get(i);

			if (temp2.containsKey(currentKey)) {
				outPaths.put(currentKey, temp2.get(currentKey));
			} else {
				OutPath tempPath = new OutPath();
				tempPath.setOutName(stations.getStationName(currentKey));
				tempPath.setProb(1.0);

				for (Object o: classes.getClassKeys()) {
					if (o == this.classKey) {
						tempPath.putEntry(o, 1);
					} else {
						tempPath.putEntry(o, 0);
					}
				}
				outPaths.put(currentKey, tempPath);
			}
		}
	}

	public void stopEditing() {
		if (forkingTable.getCellEditor() != null) {
			forkingTable.getCellEditor().stopCellEditing();
		}
		if (outTable.getCellEditor() != null) {
			outTable.getCellEditor().stopCellEditing();
		}
	}

	protected class ForkingTable extends JTable {

		private static final long serialVersionUID = 1L;

		public ForkingTable() {
			super();
			setModel(new ForkingTableModel());
			setDefaultEditor(Object.class, new ExactCellEditor());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((ForkingTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class ForkingTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[] { "Destination", "Probability" };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };
		public int[] columnSizes = new int[] { 80, 60 };

		public int getRowCount() {
			if (stationKey != null) {
				return stations.getForwardConnections(stationKey).size();
			} else {
				return 0;
			}
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
			return columnIndex == 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (outPaths == null) {
				return null;
			}
			if (columnIndex == 0) {
				return stations.getStationName(indexToKey(rowIndex));
			} else if (columnIndex == 1) {
				return outPaths.get(indexToKey(rowIndex)).getProb();
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				try {
					Double value = Double.valueOf((String) aValue);
					if (value.doubleValue() >= 0 && value.doubleValue() <= 1.0) {
						outPaths.get(indexToKey(rowIndex)).setOutName(stations.getStationName(indexToKey(rowIndex)));
						outPaths.get(indexToKey(rowIndex)).setProb(value.doubleValue());
					}
				} catch (NumberFormatException e) {
				}
			}
		}

		// retrieves station search key from index in table
		private Object indexToKey(int index) {
			if (stationKey == null) {
				return null;
			}
			return stations.getForwardConnections(stationKey).get(index);
		}

	}

	protected class OutPathTable extends JTable {

		private static final long serialVersionUID = 1L;

		public OutPathTable() {
			super();
			setModel(new OutPathTableModel());
			setDefaultEditor(Object.class, new ExactCellEditor());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((OutPathTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class OutPathTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[] { "Number of tasks", "Probability" };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };
		public int[] columnSizes = new int[] { 80, 60 };

		public int getRowCount() {
			if (out != null) {
				return keys.size();
			} else {
				return 0;
			}
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
			return true;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (out == null) {
				return null;
			}
			// outProbs = (Map<Object, Double>)
			// outPaths.get(stations.getStationByName((String)item)).getOutProbabilities();
			if (columnIndex == 0) {
				return indexToKey(rowIndex);
			} else if (columnIndex == 1) {
				return out.get(indexToKey(rowIndex));
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				Object oldValue = indexToKey(rowIndex);
				Double temp = ((Double) out.get(oldValue) == null) ? 0 : (Double) out.get(oldValue);
				try {
					Integer value = Integer.valueOf((String) aValue);
					if (value.intValue() >= 0 && !keys.contains(value)) {
						keys.set(keys.indexOf(oldValue), value);
						out.remove(oldValue);
						out.put(value, temp);
					}
				} catch (NumberFormatException e) {
				}
			}
			if (columnIndex == 1) {
				try {
					Double value = Double.valueOf((String) aValue);
					if (value.doubleValue() >= 0 && value.doubleValue() <= 1) {
						out.put(indexToKey(rowIndex), value);
					}
				} catch (NumberFormatException e) {
				}
			}
		}

		// retrieves station search key from index in table
		private Object indexToKey(int index) {
			if (stationKey == null) {
				return null;
			}
			return keys.get(index);
		}

	}

	protected class OutComboBox extends JComboBox implements ActionListener {

		private static final long serialVersionUID = 1L;

		public OutComboBox() {
			super();
			setModel(new OutComboModel());
			this.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == this && this.getSelectedIndex() >= 0) {
				out = outPaths.get(stations.getStationByName((String) item)).getOutParameters();
				keys = new ArrayList<Object>(out.keySet());
				jsp2.getRightComponent().setVisible(true);
				jsp2.setDividerLocation(-1);
				outTable.updateUI();
			}
		}

	}

	protected class OutComboModel extends AbstractListModel implements ComboBoxModel {

		@Override
		public Object getElementAt(int index) {
			// TODO Auto-generated method stub
			return (String) stations.getStationName(indexToKey(index));
		}

		@Override
		public int getSize() {
			if (stationKey != null) {
				return stations.getForwardConnections(stationKey).size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getSelectedItem() {
			// TODO Auto-generated method stub
			return item;
		}

		@Override
		public void setSelectedItem(Object i) {
			// TODO Auto-generated method stub
			item = i;
		}

		private Object indexToKey(int index) {
			if (stationKey == null) {
				return null;
			}
			return stations.getForwardConnections(stationKey).get(index);
		}

	}

	//for the combination fork
	protected class CombTable extends JTable {

		private static final long serialVersionUID = 1L;

		public CombTable() {
			super();
			setModel(new CombTableModel());
			setDefaultEditor(Object.class, new ExactCellEditor());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((CombTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class CombTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[] { "No. of branches", "Probability" };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };
		public int[] columnSizes = new int[] { 80, 60 };

		public int getRowCount() {
			if (stationKey != null) {
				return stations.getForwardConnections(stationKey).size();
			} else {
				return 0;
			}
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
			return columnIndex == 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (combProbs == null) {
				return null;
			}
			if (columnIndex == 0) {
				return indexToKey(rowIndex);
			} else if (columnIndex == 1) {
				return combProbs.get(indexToKey(rowIndex));

			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				try {
					Double value = Double.valueOf((String) aValue);
					if (value.doubleValue() >= 0 && value.doubleValue() <= 1) {
						combProbs.put(indexToKey(rowIndex), value);
					}
				} catch (NumberFormatException e) {
				}
			}
		}

		private Object indexToKey(int index) {
			return Integer.toString(index + 1);
		}

	}

	protected class ClassNumTable extends JTable {

		private static final long serialVersionUID = 1L;
		ClassDefinition cs;

		public ClassNumTable(ClassDefinition cs) {
			super();
			setModel(new ClassNumTableModel(cs));
			setDefaultEditor(Object.class, new ExactCellEditor());
			this.setDefaultRenderer(Object.class, new DisabledCellRenderer());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((ClassNumTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class ClassNumTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private ClassDefinition cs;
		private String[] columnNames = new String[] { "Class", "Number of tasks" };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };
		public int[] columnSizes = new int[] { 80, 60 };

		public ClassNumTableModel(ClassDefinition cs) {
			this.cs = cs;
		}

		public int getRowCount() {
			if (classes.getClassKeys() != null && out != null) {
				return classes.getClassKeys().size();
			} else {
				return 0;
			}
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
			int rowClassType = cs.getClassType(indexToKey(rowIndex));
			int classType = cs.getClassType(classKey);
			return (columnIndex == 1) && (rowClassType == classType);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (out == null) {
				return null;
			}
			if (columnIndex == 0) {
				return classes.getClassName(indexToKey(rowIndex));
			} else if (columnIndex == 1 && isCellEditable(rowIndex, columnIndex)) {
				return out.get(indexToKey(rowIndex));

			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				try {
					Integer value = Integer.valueOf((String) aValue);
					if (value >= 0) {
						for (Entry<Object, OutPath> m: outPaths.entrySet()) {
							m.getValue().getOutParameters().put(indexToKey(rowIndex), value);
						}
					}
				} catch (NumberFormatException e) {
				}
			}
		}

		private Object indexToKey(int index) {
			return classes.getClassKeys().get(index);
		}

	}

	protected class ForkingSwitchTable extends JTable {

		private static final long serialVersionUID = 1L;

		public ForkingSwitchTable() {
			super();
			setModel(new ForkingSwitchTableModel());
			setDefaultEditor(Object.class, new ExactCellEditor());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((ForkingSwitchTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class ForkingSwitchTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[] { "Destination" };
		private Class<?>[] columnClasses = new Class[] { String.class };
		public int[] columnSizes = new int[] { 80 };

		public int getRowCount() {
			if (stationKey != null) {
				return stations.getForwardConnections(stationKey).size();
			} else {
				return 0;
			}
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
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return stations.getStationName(indexToKey(rowIndex));
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			return;
		}

		// retrieves station search key from index in table
		private Object indexToKey(int index) {
			if (stationKey == null) {
				return null;
			}
			return stations.getForwardConnections(stationKey).get(index);
		}

	}

	protected class OutNumTable extends JTable {

		private static final long serialVersionUID = 1L;

		public OutNumTable(ClassDefinition cs) {
			super();
			setModel(new OutNumTableModel(cs));
			setDefaultEditor(Object.class, new ExactCellEditor());
			this.setDefaultRenderer(Object.class, new DisabledCellRenderer());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((OutNumTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class OutNumTableModel extends ClassNumTableModel {

		private static final long serialVersionUID = 1L;

		public OutNumTableModel(ClassDefinition cs) {
			super(cs);
		}

		@Override
		public int getRowCount() {
			if (out != null) {
				return out.keySet().size();
			} else {
				return 0;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				try {
					Integer value = Integer.valueOf((String) aValue);
					if (value >= 0) {
						out.put(super.indexToKey(rowIndex), value);
					}
				} catch (NumberFormatException e) {
				}
			}
		}

	}

}

