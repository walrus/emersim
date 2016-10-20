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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.distributions.Distribution;
import jmt.gui.common.editors.DistributionsEditor;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;
import jmt.gui.common.serviceStrategies.ZeroStrategy;
import jmt.gui.exact.table.ExactCellEditor;
import jmt.gui.exact.table.ExactCellRenderer;

/**
 * <p>Title: Timing Section Panel</p>
 * <p>Description: This panel is used to parametrise the timing section.</p>
 *
 * @author Lulai Zhu
 * Date: 24-06-2016
 * Time: 16.00.00
 */
public class TimingSectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private static final String INFINITE_DESCRIPTION =
			"With infinite-server semantics, there is no limit to the number of enabling sets "
			+ "being processed parallelly by the transition. Every enabling set is processed "
			+ "as soon as it forms in the input places.";
	private static final String FINITE_DESCRIPTION =
			"With finite-server semantics, the number of enabling sets being processed parallelly "
			+ "by the transition is limited by the specified number of servers. An enabling set "
			+ "is processed only when there are idle servers.";
	private static final String NOTICE = "";

	private static final String[] timingStrategies = { TIMING_STRATEGY_TIMED, TIMING_STRATEGY_IMMEDIATE };

	private boolean isInitComplete;

	private StationDefinition stationData;
	private ClassDefinition classData;
	private Object stationKey;
	private Vector<Object> classes;

	private ImagedComboBoxCellEditorFactory classEditor;
	private JCheckBox infiniteCheckBox;
	private JSpinner serverNumberSpinner;
	private TimingStrategyTable strategyTable;
	private JTextArea descriptionText;
	private JTextArea noticeText;

	private AbstractAction editDistribution = new AbstractAction("Edit") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Edits Firing Time Distribution");
		}

		public void actionPerformed(ActionEvent e) {
			Object classKey = classes.get(strategyTable.getSelectedRow());
			Object distrbution = stationData.getServiceTimeDistribution(stationKey, classKey);
			DistributionsEditor editor = DistributionsEditor.getInstance(TimingSectionPanel.this.getParent(), (Distribution) distrbution);
			editor.setTitle("Editing " + classData.getClassName(classKey) + " Firing Time Distribution...");
			editor.show();
			stationData.setServiceTimeDistribution(stationKey, classKey, editor.getResult());
			strategyTable.repaint();
		}

	};

	public TimingSectionPanel(StationDefinition sd, ClassDefinition cd, Object sk) {
		isInitComplete = false;
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		setData(sd, cd, sk);
		initComponents();
		addDataManagers();
		updateServerNumber();
		updateDescription();
		updateNotice();
		isInitComplete = true;
	}

	public void setData(StationDefinition sd, ClassDefinition cd, Object sk) {
		stationData = sd;
		classData = cd;
		stationKey = sk;
		classEditor.setData(cd);
		classes = classData.getClassKeys();
		if (isInitComplete) {
			updateServerNumber();
			updateDescription();
			updateNotice();
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPanel.setDividerSize(4);
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setResizeWeight(1.0);

		JPanel parameterPanel = new JPanel(new BorderLayout(3, 3));

		infiniteCheckBox = new JCheckBox("Infinite");
		serverNumberSpinner = new JSpinner();
		serverNumberSpinner.setPreferredSize(DIM_BUTTON_XS);
		JPanel serverNumberPanel = new JPanel();
		serverNumberPanel.setBorder(new TitledBorder(new EtchedBorder(), "Number of Servers"));
		serverNumberPanel.add(new JLabel("Number: "));
		serverNumberPanel.add(serverNumberSpinner);
		serverNumberPanel.add(infiniteCheckBox);
		serverNumberPanel.setMinimumSize(new Dimension(300, 40));

		strategyTable = new TimingStrategyTable();
		JPanel strategyPanel = new JPanel(new BorderLayout());
		strategyPanel.setBorder(new TitledBorder(new EtchedBorder(), "Timing Strategies"));
		strategyPanel.add(new WarningScrollTable(strategyTable, WARNING_CLASS));
		strategyPanel.setMinimumSize(new Dimension(300, 200));

		parameterPanel.add(serverNumberPanel, BorderLayout.NORTH);
		parameterPanel.add(strategyPanel, BorderLayout.CENTER);

		JSplitPane informationPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		informationPanel.setDividerSize(3);
		informationPanel.setResizeWeight(0.5);

		descriptionText = new JTextArea("");
		descriptionText.setOpaque(false);
		descriptionText.setEditable(false);
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		JScrollPane descriptionPanel = new JScrollPane(descriptionText);
		descriptionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Description"));
		descriptionPanel.setMinimumSize(new Dimension(200, 125));

		noticeText = new JTextArea("");
		noticeText.setOpaque(false);
		noticeText.setEditable(false);
		noticeText.setLineWrap(true);
		noticeText.setWrapStyleWord(true);
		JScrollPane noticePanel = new JScrollPane(noticeText);
		noticePanel.setBorder(new TitledBorder(new EtchedBorder(), "Notice"));
		noticePanel.setMinimumSize(new Dimension(200, 115));

		informationPanel.setLeftComponent(descriptionPanel);
		informationPanel.setRightComponent(noticePanel);

		mainPanel.setLeftComponent(parameterPanel);
		mainPanel.setRightComponent(informationPanel);

		add(mainPanel, BorderLayout.CENTER);
	}

	private void addDataManagers() {
		infiniteCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (infiniteCheckBox.isSelected()) {
					serverNumberSpinner.setValue(Float.POSITIVE_INFINITY);
					serverNumberSpinner.setEnabled(false);
				} else {
					serverNumberSpinner.setValue(Integer.valueOf(1));
					serverNumberSpinner.setEnabled(true);
				}
				updateDescription();
			}
		});

		serverNumberSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Object value = serverNumberSpinner.getValue();
				if (value instanceof Integer) {
					if (((Integer) value).intValue() < 1) {
						value = Integer.valueOf(1);
						serverNumberSpinner.setValue(value);
						return;
					}
					stationData.setStationNumberOfServers((Integer) value, stationKey);
				} else {
					stationData.setStationNumberOfServers(Integer.valueOf(-1), stationKey);
				}
			}
		});
	}

	private void updateServerNumber() {
		Integer serverNumber = stationData.getStationNumberOfServers(stationKey);
		if (serverNumber.intValue() < 0) {
			infiniteCheckBox.setSelected(true);
			serverNumberSpinner.setValue(Float.POSITIVE_INFINITY);
			serverNumberSpinner.setEnabled(false);
		} else {
			infiniteCheckBox.setSelected(false);
			serverNumberSpinner.setValue(serverNumber);
			serverNumberSpinner.setEnabled(true);
		}
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Timing Section";
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

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
		classEditor.clearCache();
	}

	private void updateDescription() {
		if (classes.isEmpty()) {
			descriptionText.setText("");
			return;
		}
		if (stationData.getStationNumberOfServers(stationKey) < 0) {
			descriptionText.setText(INFINITE_DESCRIPTION);
		} else {
			descriptionText.setText(FINITE_DESCRIPTION);
		}
	}

	private void updateNotice() {
		if (classes.isEmpty()) {
			noticeText.setText("");
			noticeText.setForeground(Color.BLACK);
			return;
		}
		noticeText.setText(NOTICE);
		noticeText.setForeground(Color.BLACK);
	}

	private class TimingStrategyTable extends JTable {

		private static final long serialVersionUID = 1L;

		private ButtonCellEditor distributionEditor;
		private DisabledButtonCellRenderer distributionRenderer;
		private ExactCellEditor priorityEditor;
		private ExactCellRenderer priorityRenderer;
		private ExactCellEditor weightEditor;
		private ExactCellRenderer weightRenderer;

		public TimingStrategyTable() {
			distributionEditor = new ButtonCellEditor(new JButton(editDistribution));
			distributionRenderer = new DisabledButtonCellRenderer(new JButton("Edit"));
			priorityEditor = new ExactCellEditor();
			priorityRenderer = new DisabledExactCellRenderer();
			weightEditor = new ExactCellEditor();
			weightRenderer = new DisabledExactCellRenderer();
			setModel(new TimingStrategyTableModel());
			setRowHeight(ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
			sizeColumns();
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(((TimingStrategyTableModel) getModel()).getColumnSize(i));
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				return ComboBoxCellEditor.getEditorInstance(timingStrategies);
			} else if (column == 3) {
				return distributionEditor;
			} else if (column == 4) {
				return priorityEditor;
			} else if (column == 5) {
				return weightEditor;
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
			} else if (column == 3) {
				return distributionRenderer;
			} else if (column == 4) {
				return priorityRenderer;
			} else if (column == 5) {
				return weightRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}

	}

	private class TimingStrategyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[] { "Class", "Strategy", "Firing Time Distribution", "", "Firing Priority", "Firing Weight"};
		private Class<?>[] columnClasses = new Class[] { String.class, String.class, String.class, Object.class, String.class, String.class };
		private int[] columnSizes = new int[] { 40, 50, 100, 10, 50, 50};

		@Override
		public int getRowCount() {
			return classes.size();
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
			} else if (columnIndex == 2) {
				return false;
			} else if (columnIndex == 3 && TIMING_STRATEGY_IMMEDIATE.equals((String) getValueAt(rowIndex, 1))) {
				return false;
			} else if ((columnIndex == 4 || columnIndex == 5) && TIMING_STRATEGY_TIMED.equals((String) getValueAt(rowIndex, 1))) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object classKey = classes.get(rowIndex);
			if (columnIndex == 0) {
				return classKey;
			} else if (columnIndex == 1) {
				if (stationData.getServiceTimeDistribution(stationKey, classKey) instanceof Distribution) {
					return TIMING_STRATEGY_TIMED;
				} else {
					return TIMING_STRATEGY_IMMEDIATE;
				}
			} else if (columnIndex == 2) {
				return stationData.getServiceTimeDistribution(stationKey, classKey);
			} else if (columnIndex == 4) {
				return stationData.getFiringPriority(stationKey, classKey);
			} else if (columnIndex == 5) {
				return stationData.getFiringWeight(stationKey, classKey);
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Object classKey = classes.get(rowIndex);
			if (columnIndex == 1) {
				if (TIMING_STRATEGY_TIMED.equals((String) aValue)) {
					if (!(stationData.getServiceTimeDistribution(stationKey, classKey) instanceof Distribution)) {
						stationData.setServiceTimeDistribution(stationKey, classKey, Defaults.getAsNewInstance("stationServiceStrategy"));
						stationData.setFiringPriority(stationKey, classKey, Integer.valueOf(-1));
						stationData.setFiringWeight(stationKey, classKey, Integer.valueOf(1));
					}
				} else {
					if (!(stationData.getServiceTimeDistribution(stationKey, classKey) instanceof ZeroStrategy)) {
						stationData.setFiringPriority(stationKey, classKey, Integer.valueOf(0));
						stationData.setFiringWeight(stationKey, classKey, Integer.valueOf(1));
						stationData.setServiceTimeDistribution(stationKey, classKey, new ZeroStrategy());
					}
				}
				repaint();
			} else if (columnIndex == 4) {
				try {
					Integer ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() < 0) {
						ivalue = Integer.valueOf(0);
					}
					stationData.setFiringPriority(stationKey, classKey, ivalue);
				} catch (NumberFormatException e) {
					// Aborts modification if String is invalid
				}
			} else if (columnIndex == 5) {
				try {
					Integer ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() < 1) {
						ivalue = Integer.valueOf(1);
					}
					stationData.setFiringWeight(stationKey, classKey, ivalue);
				} catch (NumberFormatException e) {
					// Aborts modification if String is invalid
				}
			}
		}

	}

	private class DisabledButtonCellRenderer extends ButtonCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton button;

		public DisabledButtonCellRenderer(JButton jbutt) {
			super(jbutt);
			button = jbutt;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (table.isCellEditable(row, column)) {
				button.setEnabled(true);
			} else {
				button.setEnabled(false);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}

	private class DisabledExactCellRenderer extends ExactCellRenderer {

		private static final long serialVersionUID = 1L;

		private JLabel label;

		public DisabledExactCellRenderer() {
			label = new JLabel("--");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
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
