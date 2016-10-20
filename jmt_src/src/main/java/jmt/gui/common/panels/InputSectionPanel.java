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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 30-giu-2005
 * Time: 9.33.59
 * Modified by Bertoli Marco 7-oct-2005
 *                           9-jan-2006  --> ComboBoxCellEditor
 */
public class InputSectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	/**
	 * Used to define queue policies
	 */
	protected static final Object[] serverQueuePolicies = { QUEUE_STRATEGY_FCFS, QUEUE_STRATEGY_LCFS, QUEUE_STRATEGY_RAND, QUEUE_STRATEGY_SJF, QUEUE_STRATEGY_LJF };
	protected static final Object[] otherQueuePolicies = { QUEUE_STRATEGY_FCFS, QUEUE_STRATEGY_LCFS, QUEUE_STRATEGY_RAND };
	/**
	 * Used to define drop rules
	 */
	protected static final Object[] dropRules = { FINITE_DROP, FINITE_BLOCK, FINITE_WAITING };
	/**
	 * Used to define station queue policies
	 */
	protected static final Object[] serverStationQueuePolicies = { QUEUE_STRATEGY_STATION_PS, QUEUE_STRATEGY_STATION_QUEUE, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY }; // QUEUE_STRATEGY_STATION_SRPT };
	protected static final Object[] otherStationQueuePolicies = { QUEUE_STRATEGY_STATION_QUEUE, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY };

	private ButtonGroup queueLengthGroup;
	private JRadioButton infiniteQueueSelector;
	private JRadioButton finiteQueueSelector;
	private JSpinner queueLengthSpinner;
	private QueueTable queueTable;
	/** Used to display classes with icon */
	protected ImagedComboBoxCellEditorFactory classEditor;

	protected StationDefinition data;
	protected ClassDefinition classData;
	protected Object stationKey;

	protected JComboBox queuePolicyCombo;

	public InputSectionPanel(StationDefinition sd, ClassDefinition cd, Object stationKey) {
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		setData(sd, cd, stationKey);
		initComponents();
		addDataManagers();
		getQueueLength();
	}

	private void initComponents() {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout(5, 5));
		infiniteQueueSelector = new JRadioButton("Infinite");
		finiteQueueSelector = new JRadioButton("Finite");
		queueLengthGroup = new ButtonGroup();
		queueLengthGroup.add(infiniteQueueSelector);
		queueLengthGroup.add(finiteQueueSelector);
		queueLengthSpinner = new JSpinner();
		queueLengthSpinner.setValue(new Integer(1));
		queueLengthSpinner.setPreferredSize(DIM_BUTTON_XS);
		queueTable = new QueueTable();

		//queue details panel
		JPanel queuePolicyPanel = new JPanel(new BorderLayout());
		queuePolicyPanel.setBorder(new TitledBorder(new EtchedBorder(), "Queue Policy"));
		queuePolicyPanel.add(new WarningScrollTable(queueTable, WARNING_CLASS), BorderLayout.CENTER);
		JPanel queueLengthPanel = new JPanel(new GridLayout(3, 1, 3, 3));
		queueLengthPanel.setBorder(new TitledBorder(new EtchedBorder(), "Capacity"));

		// Queue strategy selector
		JPanel queueStrategy = new JPanel(new BorderLayout());
		queueStrategy.add(new JLabel("Station queue policy: "), BorderLayout.WEST);
		queuePolicyCombo = new JComboBox();
		queueStrategy.add(queuePolicyCombo, BorderLayout.CENTER);
		queuePolicyPanel.add(queueStrategy, BorderLayout.NORTH);
		queueStrategy.setBorder(BorderFactory.createEmptyBorder(2, 5, 10, 5));

		queueLengthPanel.add(infiniteQueueSelector);
		queueLengthPanel.add(finiteQueueSelector);
		JPanel spinnerPanel = new JPanel();
		JLabel label = new JLabel("<html>Max no. customers <br>(queue+service)</html>");
		label.setToolTipText("The maximum number of customers allowed in the station.");
		spinnerPanel.add(label);
		spinnerPanel.add(queueLengthSpinner);
		queueLengthPanel.add(spinnerPanel);

		this.add(queueLengthPanel, BorderLayout.WEST);
		this.add(queuePolicyPanel, BorderLayout.CENTER);
	}

	public void setData(StationDefinition sd, ClassDefinition cd, Object stationKey) {
		data = sd;
		classData = cd;
		this.stationKey = stationKey;
		classEditor.setData(cd);
		if (queueLengthGroup != null) {
			getQueueLength();
		}
	}

	private void addDataManagers() {
		queueLengthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (queueLengthSpinner.isEnabled()) {
					Integer queueLength = (Integer) queueLengthSpinner.getValue();
					if (queueLength.intValue() < 1) {
						queueLength = new Integer(1);
						queueLengthSpinner.setValue(queueLength);
					}
					data.setStationQueueCapacity(queueLength, stationKey);
					queueTable.repaint();
				}
			}
		});

		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setQueueLength();
			}
		};

		infiniteQueueSelector.addActionListener(buttonListener);
		finiteQueueSelector.addActionListener(buttonListener);

		queuePolicyCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String queuePolicy = (String) queuePolicyCombo.getSelectedItem();
				data.setStationQueueStrategy(stationKey, queuePolicy);
				if (queuePolicy.equals(QUEUE_STRATEGY_STATION_PS) || queuePolicy.equals(QUEUE_STRATEGY_STATION_PS)) {
					Vector<Object> classKeys = classData.getClassKeys();
					for (Object classKey : classKeys) {
						data.setQueueStrategy(stationKey, classKey, QUEUE_STRATEGY_FCFS);
					}
				}
				TableCellEditor editor = queueTable.getCellEditor();
				if (editor != null) {
					editor.cancelCellEditing();
				}
				queueTable.repaint();
			}
		});
	}

	private void getQueueLength() {
		Integer queueLength = data.getStationQueueCapacity(stationKey);
		if (queueLength.intValue() < 0) {
			queueLengthGroup.setSelected(infiniteQueueSelector.getModel(), true);
			queueLengthSpinner.setEnabled(false);
		} else {
			queueLengthGroup.setSelected(finiteQueueSelector.getModel(), true);
			queueLengthSpinner.setEnabled(true);
			queueLengthSpinner.setValue(queueLength);
		}
	}

	private void setQueueLength() {
		if (infiniteQueueSelector.isSelected()) {
			queueLengthSpinner.setEnabled(false);
			data.setStationQueueCapacity(new Integer(-1), stationKey);
			TableCellEditor editor = queueTable.getCellEditor();
			if (editor != null) {
				editor.cancelCellEditing();
			}
			queueTable.repaint();
		} else {
			queueLengthSpinner.setEnabled(true);
			data.setStationQueueCapacity((Integer) queueLengthSpinner.getValue(), stationKey);
			queueTable.repaint();
		}
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Queue Section";
	}

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
		classEditor.clearCache();
		if (data.getStationType(stationKey).equals(STATION_TYPE_SERVER)) {
			queuePolicyCombo.setModel(new DefaultComboBoxModel(serverStationQueuePolicies));
		} else {
			queuePolicyCombo.setModel(new DefaultComboBoxModel(otherStationQueuePolicies));
		}
		queuePolicyCombo.setSelectedItem(data.getStationQueueStrategy(stationKey));
	}

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		// Aborts editing of table
		TableCellEditor editor = queueTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	protected class QueueTable extends JTable {

		private static final long serialVersionUID = 1L;

		private DisabledColumnRenderer queuePolicyRenderer = new DisabledColumnRenderer("");
		private DisabledColumnRenderer dropRuleRenderer = new DisabledColumnRenderer(INFINITE_CAPACITY);

		public QueueTable() {
			super();
			setModel(new QueueTableModel());
			sizeColumns();
			setRowHeight(ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(((QueueTableModel) getModel()).columnSizes[i]);
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				if (data.getStationType(stationKey).equals(STATION_TYPE_SERVER)) {
					return ComboBoxCellEditor.getEditorInstance(serverQueuePolicies);
				} else {
					return ComboBoxCellEditor.getEditorInstance(otherQueuePolicies);
				}
			} else if (column == 2) {
				return ComboBoxCellEditor.getEditorInstance(dropRules);
			} else {
				return super.getCellEditor(row, column);
			}
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 0) {
				return classEditor.getRenderer();
			} else if (column == 1) {
				return queuePolicyRenderer;
			} else if (column == 2) {
				return dropRuleRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}
	}

	protected class QueueTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[] { "Class", "Queue Policy", "Drop Rule" };
		private Class<?>[] columnClasses = new Class[] { String.class, String.class, String.class };
		public int[] columnSizes = new int[] { 100, 100, 100 };

		public int getRowCount() {
			return classData.getClassKeys().size();
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
			return (columnIndex == 1 && !data.getStationQueueStrategy(stationKey).equals(QUEUE_STRATEGY_STATION_PS)
					&& !data.getStationQueueStrategy(stationKey).equals(QUEUE_STRATEGY_STATION_SRPT))
					|| (columnIndex == 2 && data.getStationQueueCapacity(stationKey).intValue() >= 0);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object classKey = indexToKey(rowIndex);
			if (columnIndex == 0) {
				return classKey;
			} else if (columnIndex == 1) {
				return data.getQueueStrategy(stationKey, classKey);
			} else {
				return data.getDropRule(stationKey, classKey);
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Object classKey = indexToKey(rowIndex);
			if (columnIndex == 1) {
				data.setQueueStrategy(stationKey, classKey, (String) aValue);
			} else if (columnIndex == 2) {
				data.setDropRule(stationKey, classKey, (String) aValue);
			}
		}

		private Object indexToKey(int index) {
			return classData.getClassKeys().get(index);
		}

	}

	/**
	 * <p><b>Name:</b> DisabledColumnRenderer</p> 
	 * <p><b>Description: </b> A special renderer that will show disabled text when
	 * queue capacity is infinite, otherwise will show a combobox. 
	 * 
	 * </p>
	 * <p><b>Date:</b> 21/ott/06
	 * <b>Time:</b> 15:59:56</p>
	 * @author Bertoli Marco
	 * @version 1.0
	 */
	private class DisabledColumnRenderer extends ComboBoxCellEditor {

		private static final long serialVersionUID = 1L;
		private JLabel label;

		public DisabledColumnRenderer(String text) {
			label = new JLabel(text);
			label.setEnabled(false);
		}

		/* (non-Javadoc)
		 * @see jmt.gui.common.editors.ComboBoxCellEditor#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (column == 1) {
				if (data.getStationQueueStrategy(stationKey).equals(QUEUE_STRATEGY_STATION_PS)) {
					if (isSelected) {
						label.setBackground(table.getSelectionBackground());
					} else {
						label.setBackground(table.getBackground());
					}
					label.setText(QUEUE_STRATEGY_STATION_PS);
					return label;
				} else if (data.getStationQueueStrategy(stationKey).equals(QUEUE_STRATEGY_STATION_SRPT)) {
					if (isSelected) {
						label.setBackground(table.getSelectionBackground());
					} else {
						label.setBackground(table.getBackground());
					}
					label.setText(QUEUE_STRATEGY_STATION_SRPT);
					return label;
				}
			} else if (column == 2) {
				if (data.getStationQueueCapacity(stationKey).intValue() < 0) {
					if (isSelected) {
						label.setBackground(table.getSelectionBackground());
					} else {
						label.setBackground(table.getBackground());
					}
					return label;
				}
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}

}
