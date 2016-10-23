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

import javax.swing.ButtonGroup;
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
 * <p>Title: Store Section Panel</p>
 * <p>Description: This panel is used to parametrise the store section.</p>
 *
 * @author Lulai Zhu
 * Date: 24-06-2016
 * Time: 16.00.00
 */
public class StoreSectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private static final Object[] queuePolicies = { QUEUE_STRATEGY_FCFS, QUEUE_STRATEGY_LCFS, QUEUE_STRATEGY_RAND };
	private static final Object[] dropRules = { FINITE_DROP }; // FINITE_BLOCK, FINITE_WAITING };

	private boolean isInitComplete;

	private StationDefinition stationData;
	private ClassDefinition classData;
	private Object stationKey;

	private ImagedComboBoxCellEditorFactory classEditor;
	private ButtonGroup capacityButtonGroup;
	private JRadioButton infiniteButton;
	private JRadioButton finiteButton;
	private JSpinner capacitySpinner;
	private StoreStrategyTable strategyTable;

	public StoreSectionPanel(StationDefinition sd, ClassDefinition cd, Object sk) {
		isInitComplete = false;
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		setData(sd, cd, sk);
		initComponents();
		addDataManagers();
		updateCapacity();
		isInitComplete = true;
	}

	public void setData(StationDefinition sd, ClassDefinition cd, Object sk) {
		stationData = sd;
		classData = cd;
		stationKey = sk;
		classEditor.setData(cd);
		if (isInitComplete) {
			updateCapacity();
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout(5, 5));
		setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel capacityPanel = new JPanel(new GridLayout(3, 1, 3, 3));
		capacityPanel.setBorder(new TitledBorder(new EtchedBorder(), "Capacity"));

		capacityButtonGroup = new ButtonGroup();
		infiniteButton = new JRadioButton("Infinite");
		finiteButton = new JRadioButton("Finite");
		capacityButtonGroup.add(infiniteButton);
		capacityButtonGroup.add(finiteButton);

		capacitySpinner = new JSpinner();
		capacitySpinner.setPreferredSize(DIM_BUTTON_XS);
		JPanel capacitySpinnerPanel = new JPanel();
		JLabel capacitySpinnerLabel = new JLabel("<html>Max no. customers</html>");
		capacitySpinnerLabel.setToolTipText("The maximum number of customers allowed in the station.");
		capacitySpinnerPanel.add(capacitySpinnerLabel);
		capacitySpinnerPanel.add(capacitySpinner);

		capacityPanel.add(infiniteButton);
		capacityPanel.add(finiteButton);
		capacityPanel.add(capacitySpinnerPanel);

		strategyTable = new StoreStrategyTable();
		JPanel strategyPanel = new JPanel(new BorderLayout());
		strategyPanel.setBorder(new TitledBorder(new EtchedBorder(), "Store Strategies"));
		strategyPanel.add(new WarningScrollTable(strategyTable, WARNING_CLASS));

		add(capacityPanel, BorderLayout.WEST);
		add(strategyPanel, BorderLayout.CENTER);
	}

	private void addDataManagers() {
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object value = capacitySpinner.getValue();
				if (infiniteButton.isSelected()) {
					if (value instanceof Integer) {
						capacitySpinner.setValue(Float.POSITIVE_INFINITY);
						capacitySpinner.setEnabled(false);
						TableCellEditor editor = strategyTable.getCellEditor();
						if (editor != null) {
							editor.cancelCellEditing();
						}
					}
				} else {
					if (value instanceof Float) {
						capacitySpinner.setValue(Integer.valueOf(1));
						capacitySpinner.setEnabled(true);
					}
				}
				strategyTable.repaint();
			}
		};
		infiniteButton.addActionListener(buttonListener);
		finiteButton.addActionListener(buttonListener);

		capacitySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Object value = capacitySpinner.getValue();
				if (value instanceof Integer) {
					if (((Integer) value).intValue() < 1) {
						value = Integer.valueOf(1);
						capacitySpinner.setValue(value);
						return;
					}
					stationData.setStationQueueCapacity((Integer) value, stationKey);
				} else {
					stationData.setStationQueueCapacity(Integer.valueOf(-1), stationKey);
				}
			}
		});
	}

	private void updateCapacity() {
		Integer capacity = stationData.getStationQueueCapacity(stationKey);
		if (capacity.intValue() < 0) {
			capacityButtonGroup.setSelected(infiniteButton.getModel(), true);
			capacitySpinner.setValue(Float.POSITIVE_INFINITY);
			capacitySpinner.setEnabled(false);
		} else {
			capacityButtonGroup.setSelected(finiteButton.getModel(), true);
			capacitySpinner.setValue(capacity);
			capacitySpinner.setEnabled(true);
		}
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Store Section";
	}

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
		classEditor.clearCache();
	}

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		TableCellEditor editor = strategyTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	protected class StoreStrategyTable extends JTable {

		private static final long serialVersionUID = 1L;

		private DisabledComboBoxCellRenderer dropRuleRenderer;

		public StoreStrategyTable() {
			dropRuleRenderer = new DisabledComboBoxCellRenderer(INFINITE_CAPACITY);
			setModel(new StoreStrategyTableModel());
			setRowHeight(ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
			sizeColumns();
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(((StoreStrategyTableModel) getModel()).getColumnSize(i));
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				return ComboBoxCellEditor.getEditorInstance(queuePolicies);
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
				return ComboBoxCellEditor.getRendererInstance();
			} else if (column == 2) {
				return dropRuleRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}
	}

	private class StoreStrategyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[] { "Class", "Queue Policy", "Drop Rule" };
		private Class<?>[] columnClasses = new Class[] { String.class, String.class, String.class };
		private int[] columnSizes = new int[] { 100, 100, 100 };

		@Override
		public int getRowCount() {
			return classData.getClassKeys().size();
		}

		@Override
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

		public int getColumnSize(int columnIndex) {
			return columnSizes[columnIndex];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			} else if (columnIndex == 2 && stationData.getStationQueueCapacity(stationKey).intValue() < 0) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object classKey = classData.getClassKeys().get(rowIndex);
			if (columnIndex == 0) {
				return classKey;
			} else if (columnIndex == 1) {
				return stationData.getQueueStrategy(stationKey, classKey);
			} else if (columnIndex == 2) {
				return stationData.getDropRule(stationKey, classKey);
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Object classKey = classData.getClassKeys().get(rowIndex);
			if (columnIndex == 1) {
				stationData.setQueueStrategy(stationKey, classKey, (String) aValue);
			} else if (columnIndex == 2) {
				stationData.setDropRule(stationKey, classKey, (String) aValue);
			}
		}

	}

	private class DisabledComboBoxCellRenderer extends ComboBoxCellEditor {

		private static final long serialVersionUID = 1L;

		private JLabel label;

		public DisabledComboBoxCellRenderer(String text) {
			label = new JLabel(text);
			label.setEnabled(false);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (!table.isCellEditable(row, column)) {
				return label;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}

}
