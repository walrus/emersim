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

import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.joinStrategies.GuardJoin;
import jmt.gui.common.joinStrategies.JoinStrategy;
import jmt.gui.common.joinStrategies.PartialJoin;
import jmt.gui.common.panels.WarningScrollTable;
import jmt.gui.exact.table.DisabledCellRenderer;
import jmt.gui.exact.table.ExactCellEditor;

public class JoinEditor extends JSplitPane implements CommonConstants {

	private static final long serialVersionUID = 1L;
	private int requiredNum;
	private Map<Object, Integer> firingMix;
	private StationDefinition stations;
	private Object stationKey, classKey;
	private ClassDefinition classes;

	private JPanel joinPane = new JPanel();
	private JSpinner jNumField = new JSpinner();
	private JTextArea descrTextPane = new JTextArea("");
	private JScrollPane descrPane = new JScrollPane();
	private JTextArea noOptLabel = new JTextArea("No options available for this join strategy");
	private JScrollPane noOptLabelPanel = new JScrollPane(noOptLabel);
	private MixTable mixTable;
	private WarningScrollTable mixPane;

	public JoinEditor(StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		super();
		super.setOrientation(JSplitPane.VERTICAL_SPLIT);
		super.setDividerSize(3);
		super.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setResizeWeight(.5);
		mixTable = new MixTable(cs);
		setData(sd, cs, stationKey, classKey);
		initComponents();
	}

	private void initComponents() {
		mixPane = new WarningScrollTable(mixTable, WARNING_CLASS);
		mixPane.setBorder(new TitledBorder(new EtchedBorder(), "Join Options"));
		noOptLabelPanel.setBorder(new TitledBorder(new EtchedBorder(), "Join Options"));
		noOptLabel.setOpaque(false);
		noOptLabel.setEditable(false);
		noOptLabel.setLineWrap(true);
		noOptLabel.setWrapStyleWord(true);
		descrTextPane.setOpaque(false);
		descrTextPane.setEditable(false);
		descrTextPane.setLineWrap(true);
		descrTextPane.setWrapStyleWord(true);
		descrPane.setBorder(new TitledBorder(new EtchedBorder(), "Description"));
		descrPane.setViewportView(descrTextPane);
		joinPane.setLayout(new FlowLayout());
		JLabel text = new JLabel("Number of Tasks Required:");
		jNumField.setPreferredSize(DIM_BUTTON_XS);
		joinPane.add(text);
		joinPane.add(jNumField);
		joinPane.setBorder(new TitledBorder(new EtchedBorder(), "Join Options"));
		setLeftComponent(descrPane);
	}

	public void setData(StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		JoinStrategy js = (JoinStrategy) sd.getJoinStrategy(stationKey, classKey);
		if (js == null) {
			emptyPane();
		} else {
			descrTextPane.setText(js.getDescription());
			if (js instanceof PartialJoin) {
				createDetails(js, sd, stationKey, classKey);
			} else if (js instanceof GuardJoin) {
				createFiringMixDetails(js, sd, cs, stationKey, classKey);
			} else {
				emptyPane();
			}
		}
		doLayout();
	}

	private void emptyPane() {
		firingMix = null;
		setRightComponent(noOptLabelPanel);
	}

	private void createDetails(JoinStrategy js, StationDefinition sd, Object stationKey, Object classKey) {
		requiredNum = js.getRequiredNum();
		stations = sd;
		this.stationKey = stationKey;
		this.classKey = classKey;
		firingMix = null;
		setRightComponent(joinPane);
		this.updateSpinner();
	}

	private void createFiringMixDetails(JoinStrategy js, StationDefinition sd, ClassDefinition cs, Object stationKey, Object classKey) {
		requiredNum = js.getRequiredNum();
		stations = sd;
		classes = cs;
		this.stationKey = stationKey;
		this.classKey = classKey;
		firingMix = ((GuardJoin) js).getGuard();
		if (firingMix.isEmpty()) {
			setupFiringMixJoin();
		}
		setRightComponent(mixPane);
		this.updateSpinner();
	}

	private void setupFiringMixJoin() {
		firingMix.clear();
		for (int i = 0; i < classes.getClassKeys().size(); i++) {
			Object currentKey = classes.getClassKeys().get(i);
			firingMix.put(currentKey, 0);
		}
	}

	private void updateSpinner() {
		jNumField.setValue(requiredNum);
		jNumField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Integer i = (Integer) jNumField.getValue();
				if (i.intValue() < 1) {
					i = new Integer(1);
					jNumField.setValue(i);
				}
				JoinStrategy js = (JoinStrategy)stations.getJoinStrategy(stationKey, classKey);
				js.setRequiredNum(i);
			}
		});
	}

	protected class MixTable extends JTable {

		private static final long serialVersionUID = 1L;
		ClassDefinition cs;

		public MixTable(ClassDefinition cs) {
			super();
			setModel(new MixTableModel(cs));
			setDefaultEditor(Object.class, new ExactCellEditor());
			this.setDefaultRenderer(Object.class, new DisabledCellRenderer());
			sizeColumns();
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(((MixTableModel) getModel()).columnSizes[i]);
			}
		}

	}

	protected class MixTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private ClassDefinition cs;
		private String[] columnNames = new String[] { "Class", "Tasks Required" };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };
		public int[] columnSizes = new int[] { 40, 100 };

		public MixTableModel(ClassDefinition cs) {
			this.cs = cs;
		}

		public int getRowCount() {
			if (classes.getClassKeys() != null) {
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
			if (firingMix == null) {
				return null;
			}
			if (columnIndex == 0) {
				return classes.getClassName(indexToKey(rowIndex));
			} else if (columnIndex == 1 && isCellEditable(rowIndex, columnIndex)) {
				return firingMix.get(indexToKey(rowIndex));
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
						firingMix.put(indexToKey(rowIndex), value);
					}
				} catch (NumberFormatException e) {

				}
			}
		}

		private Object indexToKey(int index) {
			return classes.getClassKeys().get(index);
		}

	}

}
