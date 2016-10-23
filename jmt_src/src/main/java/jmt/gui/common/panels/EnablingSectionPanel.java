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
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.exact.table.ExactCellRenderer;
import jmt.gui.exact.table.ExactTable;
import jmt.gui.exact.table.ExactTableModel;

/**
 * <p>Title: Enabling Section Panel</p>
 * <p>Description: This panel is used to parametrise the enabling section.</p>
 *
 * @author Lulai Zhu
 * Date: 24-06-2016
 * Time: 16.00.00
 */
public class EnablingSectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private static final String DESCRIPTION =
			"Each row of the upper table represents the enabling condition for a class, i.e. "
			+ "the number of jobs required in each input place for enabling the transition. "
			+ "Each row of the lower table represents the inhibiting condition for a class, "
			+ "i.e. the number of jobs required in an input place for inhibiting the transition "
			+ "(set 0 for infinity).";
	private static final String NO_NONZERO_ROWS_NOTICE =
			"Error: The enabling table contains no non-zero rows. The transition will not be "
			+ "enabled for any classes.";
	private static final String NO_ROWS_WITH_ALL_VALUES_VALID_NOTICE =
			"Error: The enabling table contains no rows with all the values less than those "
			+ "corresponding in the inhibiting table. The transition will not be enabled for "
			+ "any classes.";
	private static final String ZERO_ROWS_NOTICE =
			"Warning: The enabling table contains zero rows. The transition will not be enabled "
			+ "for classes represented by these rows.";
	private static final String ROWS_WITH_VALUES_INVALID_NOTICE =
			"Warning: The enabling table contains rows with values no less than those corresponding "
			+ "in the inhibiting table. The transition will not be enabled for classes represented "
			+ "by these rows.";

	private boolean isInitComplete;

	protected StationDefinition stationData;
	protected ClassDefinition classData;
	protected Object stationKey;
	protected Vector<Object> classes;
	protected Vector<Object> stationsIn;

	private ConditionTable enablingTable;
	private ConditionTable inhibitingTable;
	private JTextArea descriptionText;
	private JTextArea noticeText;

	public EnablingSectionPanel(StationDefinition sd, ClassDefinition cd, Object sk) {
		isInitComplete = false;
		setData(sd, cd, sk);
		initComponents();
		updateDescription();
		updateNotice();
		isInitComplete = true;
	}

	public void setData(StationDefinition sd, ClassDefinition cd, Object sk) {
		stationData = sd;
		classData = cd;
		stationKey = sk;
		classes = classData.getClassKeys();
		stationsIn = stationData.getBackwardConnections(stationKey);
		if (isInitComplete) {
			enablingTable.tableChanged(new TableModelEvent(enablingTable.getModel(), TableModelEvent.HEADER_ROW));
			inhibitingTable.tableChanged(new TableModelEvent(inhibitingTable.getModel(), TableModelEvent.HEADER_ROW));
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

		JPanel tablePanel = new JPanel(new GridLayout(2, 1, 3, 3));

		enablingTable = new ConditionTable(true);
		JScrollPane enablingTablePanel = new JScrollPane();
		enablingTablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Enabling Conditions"));
		enablingTablePanel.setViewportView(enablingTable);
		enablingTablePanel.setMinimumSize(new Dimension(300, 120));

		inhibitingTable = new ConditionTable(false);
		JScrollPane inhibitingTablePanel = new JScrollPane();
		inhibitingTablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Inhibiting Conditions"));
		inhibitingTablePanel.setViewportView(inhibitingTable);
		inhibitingTablePanel.setMinimumSize(new Dimension(300, 120));

		tablePanel.add(enablingTablePanel);
		tablePanel.add(inhibitingTablePanel);

		WarningScrollTable warningTablePanel = new WarningScrollTable(tablePanel, WARNING_CLASS_INCOMING_ROUTING);
		warningTablePanel.addCheckVector(classes);
		warningTablePanel.addCheckVector(stationsIn);

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
		descriptionPanel.setMinimumSize(new Dimension(200, 120));

		noticeText = new JTextArea("");
		noticeText.setOpaque(false);
		noticeText.setEditable(false);
		noticeText.setLineWrap(true);
		noticeText.setWrapStyleWord(true);
		JScrollPane noticePanel = new JScrollPane(noticeText);
		noticePanel.setBorder(new TitledBorder(new EtchedBorder(), "Notice"));
		noticePanel.setMinimumSize(new Dimension(200, 120));

		informationPanel.setLeftComponent(descriptionPanel);
		informationPanel.setRightComponent(noticePanel);

		mainPanel.setLeftComponent(warningTablePanel);
		mainPanel.setRightComponent(informationPanel);

		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Enabling Section";
	}

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		TableCellEditor editor = null;
		editor = enablingTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
		editor = inhibitingTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
	}

	private void updateDescription() {
		if (stationsIn.isEmpty() || classes.isEmpty()) {
			descriptionText.setText("");
			return;
		}
		descriptionText.setText(DESCRIPTION);
	}

	private void updateNotice() {
		if (stationsIn.isEmpty() || classes.isEmpty()) {
			noticeText.setText("");
			noticeText.setForeground(Color.BLACK);
			return;
		}

		boolean hasNonzeroRows = false;
		boolean hasZeroRows = false;
		boolean hasRowsWithValuesInvalid = false;
		boolean hasRowsWithAllValuesValid = false;
		for (Object classKey : classes) {
			boolean isZeroRow = true;
			boolean isRowWithAllValuesValid = true;
			for (Object stationInKey : stationsIn) {
				int enablingValue = stationData.getEnablingCondition(stationKey, classKey, stationInKey).intValue();
				int inhibitingValue = stationData.getInhibitingCondition(stationKey, classKey, stationInKey).intValue();
				if (enablingValue > 0) {
					isZeroRow = false;
					hasNonzeroRows = true;
				}
				if (enablingValue > 0 && inhibitingValue > 0 && enablingValue >= inhibitingValue) {
					isRowWithAllValuesValid = false;
					hasRowsWithValuesInvalid = true;
				}
			}
			if (isZeroRow) {
				hasZeroRows = true;
			}
			if (isRowWithAllValuesValid) {
				hasRowsWithAllValuesValid = true;
			}
		}

		if (!hasNonzeroRows) {
			noticeText.setText(NO_NONZERO_ROWS_NOTICE);
			noticeText.setForeground(Color.RED);
		} else if (!hasRowsWithAllValuesValid) {
			noticeText.setText(NO_ROWS_WITH_ALL_VALUES_VALID_NOTICE);
			noticeText.setForeground(Color.RED);
		} else if (hasZeroRows) {
			noticeText.setText(ZERO_ROWS_NOTICE);
			noticeText.setForeground(Color.BLUE);
		} else if (hasRowsWithValuesInvalid) {
			noticeText.setText(ROWS_WITH_VALUES_INVALID_NOTICE);
			noticeText.setForeground(Color.BLUE);
		} else {
			noticeText.setText("");
			noticeText.setForeground(Color.BLACK);
		}
	}

	private class ConditionTable extends ExactTable {

		private static final long serialVersionUID = 1L;

		private boolean isEnablingTable;
		private InfiniteExactCellRenderer inhibitingCellRenderer;

		public ConditionTable(boolean isEnabling) {
			super(new ConditionTableModel(isEnabling));
			isEnablingTable = isEnabling;
			inhibitingCellRenderer = isEnabling ? null : new InfiniteExactCellRenderer();
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setRowHeight(ROW_HEIGHT);
			setRowSelectionAllowed(false);
			setColumnSelectionAllowed(false);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (!isEnablingTable) {
				return inhibitingCellRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}

	}

	private class ConditionTableModel extends ExactTableModel {

		private static final long serialVersionUID = 1L;

		private boolean isEnablingTableModel;

		public ConditionTableModel(boolean isEnabling) {
			rowHeaderPrototype = "Class10000";
			prototype = "Station10000";
			isEnablingTableModel = isEnabling;
		}

		@Override
		public int getRowCount() {
			return classes.size();
		}

		@Override
		public int getColumnCount() {
			return stationsIn.size();
		}

		@Override
		protected String getRowName(int rowIndex) {
			return classData.getClassName(classes.get(rowIndex));
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex >= stationsIn.size()) {
				return "";
			}
			return stationData.getStationName(stationsIn.get(columnIndex));
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == -1) {
				return String.class;
			} else {
				return String.class;
			}
		}

		@Override
		public Object getPrototype(int i) {
			if (i == -1) {
				return rowHeaderPrototype;
			} else {
				return prototype;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			if (isEnablingTableModel) {
				return stationData.getEnablingCondition(stationKey, classes.get(rowIndex), stationsIn.get(columnIndex));
			} else {
				return stationData.getInhibitingCondition(stationKey, classes.get(rowIndex), stationsIn.get(columnIndex));
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			try {
				Integer ivalue = Integer.valueOf((String) value);
				if (isEnablingTableModel) {
					stationData.setEnablingCondition(stationKey, classes.get(rowIndex), stationsIn.get(columnIndex), ivalue);
				} else {
					stationData.setInhibitingCondition(stationKey, classes.get(rowIndex), stationsIn.get(columnIndex), ivalue);
				}
				updateNotice();
			} catch (NumberFormatException e) {
				// Aborts modification if String is invalid
			}
		}

		@Override
		public void clear(int row, int col) {
			if (isEnablingTableModel) {
				stationData.setEnablingCondition(stationKey, classes.get(row), stationsIn.get(col), Integer.valueOf(0));
			} else {
				stationData.setInhibitingCondition(stationKey, classes.get(row), stationsIn.get(col), Integer.valueOf(0));
			}
			updateNotice();
		}

	}

	private class InfiniteExactCellRenderer extends ExactCellRenderer {

		private static final long serialVersionUID = 1L;

		private JLabel label;

		public InfiniteExactCellRenderer() {
			label = new JLabel("\u221e");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (((Integer) value).intValue() == 0) {
				if (hasFocus) {
					label.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				} else {
					label.setBorder(noFocusBorder);
				}
				return label;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}

}
