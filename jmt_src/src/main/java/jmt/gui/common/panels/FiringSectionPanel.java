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
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.exact.table.ExactTable;
import jmt.gui.exact.table.ExactTableModel;

/**
 * <p>Title: Firing Section Panel</p>
 * <p>Description: This panel is used to parametrise the firing section.</p>
 *
 * @author Lulai Zhu
 * Date: 24-06-2016
 * Time: 16.00.00
 */
public class FiringSectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private static final String DESCRIPTION =
			"Each row of the table represents the firing outcome for a class, i.e. the number "
			+ "of jobs released to each output station on firing the transition.";
	private static final String NO_NONZERO_ROWS_NOTICE =
			"Error: The firing table has no non-zero rows. When the transition fires, it will "
			+ "not release any jobs for any classes.";
	private static final String ZERO_ROWS_NOTICE =
			"Warning: The fring table has zero rows. When the transition fires, it will not "
			+ "release any jobs for classes represented by these rows.";

	private boolean isInitComplete;

	protected StationDefinition stationData;
	protected ClassDefinition classData;
	protected Object stationKey;
	protected Vector<Object> classes;
	protected Vector<Object> stationsOut;

	private OutcomeTable firingTable;
	private JTextArea descriptionText;
	private JTextArea noticeText;

	public FiringSectionPanel(StationDefinition sd, ClassDefinition cd, Object sk) {
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
		stationsOut = stationData.getForwardConnections(stationKey);
		if (isInitComplete) {
			firingTable.tableChanged(new TableModelEvent(firingTable.getModel(), TableModelEvent.HEADER_ROW));
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

		firingTable = new OutcomeTable();
		JPanel firingTablePanel = new JPanel(new BorderLayout());
		firingTablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Firing Outcomes"));
		firingTablePanel.add(new WarningScrollTable(firingTable, WARNING_CLASS_OUTGOING_ROUTING));
		firingTablePanel.setMinimumSize(new Dimension(300, 240));

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

		mainPanel.setLeftComponent(firingTablePanel);
		mainPanel.setRightComponent(informationPanel);

		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Firing Section";
	}

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		TableCellEditor editor = firingTable.getCellEditor();
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
		if (stationsOut.isEmpty() || classes.isEmpty()) {
			descriptionText.setText("");
			return;
		}
		descriptionText.setText(DESCRIPTION);
	}

	private void updateNotice() {
		if (stationsOut.isEmpty() || classes.isEmpty()) {
			noticeText.setText("");
			noticeText.setForeground(Color.BLACK);
			return;
		}

		boolean hasNonzeroRows = false;
		boolean hasZeroRows = false;
		for (Object classKey : classes) {
			boolean isZeroRow = true;
			for (Object stationOutKey : stationsOut) {
				int firingValue = stationData.getFiringOutcome(stationKey, classKey, stationOutKey).intValue();
				if (firingValue > 0) {
					isZeroRow = false;
					hasNonzeroRows = true;
				}
			}
			if (isZeroRow) {
				hasZeroRows = true;
			}
		}

		if (!hasNonzeroRows) {
			noticeText.setText(NO_NONZERO_ROWS_NOTICE);
			noticeText.setForeground(Color.RED);
		} else if (hasZeroRows) {
			noticeText.setText(ZERO_ROWS_NOTICE);
			noticeText.setForeground(Color.BLUE);
		} else {
			noticeText.setText("");
			noticeText.setForeground(Color.BLACK);
		}
	}

	private class OutcomeTable extends ExactTable {

		private static final long serialVersionUID = 1L;

		public OutcomeTable() {
			super(new OutcomeTableModel());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setRowHeight(ROW_HEIGHT);
			setRowSelectionAllowed(false);
			setColumnSelectionAllowed(false);
		}

	}

	private class OutcomeTableModel extends ExactTableModel {

		private static final long serialVersionUID = 1L;

		public OutcomeTableModel() {
			rowHeaderPrototype = "Class10000";
			prototype = "Station10000";
		}

		@Override
		public int getRowCount() {
			return classes.size();
		}

		@Override
		public int getColumnCount() {
			return stationsOut.size();
		}

		@Override
		protected String getRowName(int rowIndex) {
			return classData.getClassName(classes.get(rowIndex));
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex >= stationsOut.size()) {
				return "";
			}
			return stationData.getStationName(stationsOut.get(columnIndex));
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
			return stationData.getFiringOutcome(stationKey, classes.get(rowIndex), stationsOut.get(columnIndex));
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			try {
				Integer ivalue = Integer.valueOf((String) value);
				stationData.setFiringOutcome(stationKey, classes.get(rowIndex), stationsOut.get(columnIndex), ivalue);
				updateNotice();
			} catch (NumberFormatException e) {
				// Aborts modification if String is invalid
			}
		}

		@Override
		public void clear(int row, int col) {
			stationData.setFiringOutcome(stationKey, classes.get(row), stationsOut.get(col), Integer.valueOf(0));
			updateNotice();
		}

	}

}
