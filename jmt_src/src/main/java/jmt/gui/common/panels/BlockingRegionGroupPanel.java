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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;

import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.definitions.BlockingRegionDefinition;
import jmt.gui.common.definitions.ClassDefinition;

import jmt.gui.common.editors.GrayCellRenderer;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;
import jmt.gui.jsimgraph.DialogFactory;

public class BlockingRegionGroupPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	protected ImagedComboBoxCellEditorFactory groupEditor;
	protected BlockingGroupTable groupTable;
	protected BlockingRegionDefinition brd;
	protected ClassDefinition cd;
	protected Object key;
	protected JPanel groupPanel;
	protected JCheckBox gEnable;
	protected JPanel configHPSPanel;
	protected ButtonGroup configHPSGroup;
	protected JRadioButton HPS_LCFS;
	protected JRadioButton HPS_FCFS;
	private DialogFactory dialogFactory;
	private boolean[] classAssignTogroup;

	protected AbstractAction editMemberClasses = new AbstractAction("Edit") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Edits Member Classes");
		}

		public void actionPerformed(ActionEvent e) {
			int index = groupTable.getSelectedRow();
			dialogFactory = new DialogFactory(getFrame(BlockingRegionGroupPanel.this.getParent()));
			if (index >= 0 && index < groupTable.getRowCount()) {
				dialogFactory.getDialog(new GroupEditor(cd, brd, key, index), "Editing " + brd.getRegionGroupName(key, index) + " Member Classes...");
			}
		}

	};

	protected AbstractAction deleteGroup = new AbstractAction("") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Delete Group");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
		}

		public void actionPerformed(ActionEvent e) {
			int index = groupTable.getSelectedRow();
			if (index >= 0 && index < groupTable.getRowCount()) {
				deleteGroup(index);
			}
		}

	};

	public BlockingRegionGroupPanel(ClassDefinition cd, BlockingRegionDefinition brd, Object key, JCheckBox gEnable) {
		this.brd = brd;
		this.cd = cd;
		this.key = key;
		this.gEnable = gEnable;

		groupEditor = new ImagedComboBoxCellEditorFactory();
		setData(cd,brd, key);
		initComponents();
		addDataManagers(); 
	}

	public void setData(ClassDefinition cd, BlockingRegionDefinition brd, Object key) {
		this.brd = brd;
		this.cd = cd;
		this.key = key;
		groupEditor.setData(cd);
	}

	private void initComponents() {
		setLayout(new BorderLayout(5, 5));

		groupPanel = new JPanel(new BorderLayout());
		groupTable = new BlockingGroupTable();
		groupPanel.add(new WarningScrollTable(groupTable, WARNING_CLASS));
		add(groupPanel, BorderLayout.CENTER);

		classAssignTogroup = new boolean[cd.getClassKeys().size()];
		for (int i = 0; i < cd.getClassKeys().size(); i++) {
			classAssignTogroup[i] = false;
			for (int j = 0; j < brd.getRegionGroupList(key).size(); j++) {
				if (brd.getRegionGroupStrategy(key, j).contains(REGION_GROUP_STRATEGY_HPS)) {
					continue;
				}
				Object classKey = cd.getClassKeys().get(i);
				if (brd.getRegionGroupClassList(key, j).contains(classKey)) {
					classAssignTogroup[i] = true;
					break;
				}
			}
		}

		HPS_LCFS = new JRadioButton(REGION_GROUP_STRATEGY_HPS_LCFS);
		HPS_FCFS = new JRadioButton(REGION_GROUP_STRATEGY_HPS_FCFS);
		configHPSGroup = new ButtonGroup();
		configHPSGroup.add(HPS_LCFS);
		configHPSGroup.add(HPS_FCFS);
		configHPSPanel = new JPanel(new GridLayout(2, 1));
		configHPSPanel.setPreferredSize(new Dimension(100, 250));
		configHPSPanel.setBorder(new TitledBorder(new EtchedBorder(), "HPS Strategy"));
		configHPSPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		configHPSPanel.add(HPS_LCFS);
		configHPSPanel.add(HPS_FCFS);
		add(configHPSPanel, BorderLayout.EAST);
		getHPSStrategy();
	}

	private void addDataManagers() {
		ChangeListener buttonListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (HPS_LCFS.isSelected()) {
					updateHPSStrategy(REGION_GROUP_STRATEGY_HPS_LCFS);
				} else {
					updateHPSStrategy(REGION_GROUP_STRATEGY_HPS_FCFS);
				}
				TableCellEditor editor = groupTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
				groupTable.repaint();
			}
		};

		HPS_LCFS.addChangeListener(buttonListener);
		HPS_FCFS.addChangeListener(buttonListener);
	}

	private void updateHPSStrategy(String name) {
		for (int i = 0; i < brd.getRegionGroupList(key).size(); i++) {
			if (brd.getRegionGroupStrategy(key, i).contains(REGION_GROUP_STRATEGY_HPS)) {
				brd.setRegionGroupStrategy(key, i, name);
			}
		}
	}

	private void getHPSStrategy() {
		String HPSStrategy = "";
		for (int i = 0; i < brd.getRegionGroupList(key).size(); i++) {
			if (brd.getRegionGroupStrategy(key, i).contains(REGION_GROUP_STRATEGY_HPS)) {
				HPSStrategy = brd.getRegionGroupStrategy(key, i);
				break;
			}
		}
		if (HPSStrategy.equals(REGION_GROUP_STRATEGY_HPS_LCFS)) {
			configHPSGroup.setSelected(HPS_LCFS.getModel(), true);
		} else if (HPSStrategy.equals(REGION_GROUP_STRATEGY_HPS_FCFS)) {
			configHPSGroup.setSelected(HPS_FCFS.getModel(), true);
		} else {
			configHPSGroup.setSelected(HPS_LCFS.getModel(), true);
		}
	}

	@Override
	public String getName() {
		return "Group Specific";
	}

	@Override
	public void lostFocus() {
		TableCellEditor editor = groupTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	@Override
	public void gotFocus() {
	}

	protected void deleteGroup(int index) {
		brd.deleteRegionGroup(key, index);
		Refresh();
		if (brd.getRegionGroupList(key).size() == 0) {
			gEnable.doClick();
		}
	}

	public void Refresh() {
		groupTable.tableChanged(new TableModelEvent(groupTable.getModel()));
	}

	protected Frame getFrame(Container parent) {
		while (!(parent instanceof Frame)) {
			parent = parent.getParent();
		}
		return (Frame) parent;
	}

	protected class BlockingGroupTable extends JTable {

		private static final long serialVersionUID = 1L;

		protected String[][] strategies = new String[][]{
			new String[]{ REGION_GROUP_STRATEGY_FCFS, REGION_GROUP_STRATEGY_HPS_LCFS },
			new String[]{ REGION_GROUP_STRATEGY_FCFS, REGION_GROUP_STRATEGY_HPS_FCFS } };
		protected TableCellRenderer gray = new GrayCellRenderer();
		int[] columnSizes = new int[] { 150, 50, 50, 50, 20 };

		protected JButton editButton = new JButton() {
			private static final long serialVersionUID = 1L;

			{
				setText("Edit");
			}
		};

		protected JButton deleteButton = new JButton() {
			private static final long serialVersionUID = 1L;

			{
				setAction(deleteGroup);
				setFocusable(false);
			}
		};

		public BlockingGroupTable() {
			super();
			setModel(new BlockingGroupTableModel());
			sizeColumns();
			setRowHeight(ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				return gray;
			} else if (column == 2) {
				return ComboBoxCellEditor.getRendererInstance();
			} else if (column == 3) {
				return new ButtonCellEditor(editButton); 
			} else if (column == 4) {
				return new ButtonCellEditor(deleteButton);
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 2) {
				if (HPS_LCFS.isSelected()) {
					return ComboBoxCellEditor.getEditorInstance(strategies[0]);
				} else {
					return ComboBoxCellEditor.getEditorInstance(strategies[1]);
				}
			} else if (column == 3) {
				return new ButtonCellEditor(new JButton(editMemberClasses));
			} else if (column == 4) {
				return new ButtonCellEditor(new JButton(deleteGroup));
			} else {
				return super.getCellEditor(row, column);
			}
		}

		private void sizeColumns() {
			for (int i = 0; i < columnSizes.length && i < getColumnCount(); i++) {
				this.getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
			}
		}

		protected void sizeColumnsAndRows() {
			for (int i = 0; i < columnSizes.length && i < getColumnCount(); i++) {
				this.getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
				if (i == columnSizes.length - 1) {
					this.getColumnModel().getColumn(i).setMaxWidth(columnSizes[i]);
					this.setRowHeight(columnSizes[i]);
				}
			}
		}

	}

	protected class BlockingGroupTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private Class<?>[] columnClasses = new Class[] { String.class, String.class, String.class, Object.class, Object.class };
		private String[] columnNames = new String[] { "Group", "Capacity", "Strategy", "Member Classes", "" };

		public int getRowCount() {
			return brd.getRegionGroupList(key).size();
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
			return (columnIndex == 0 || columnIndex == 1 || columnIndex == 2|| columnIndex == 3 || columnIndex == 4);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case (0):
				return brd.getRegionGroupName(key, rowIndex);
			case (1):
				return brd.getRegionGroupCapacity(key, rowIndex);
			case (2):
				return brd.getRegionGroupStrategy(key, rowIndex);
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String svalue = null;
			Integer ivalue = null;
			switch (columnIndex) {
			case (0):
				svalue = (String) aValue;
				if (!svalue.equals("")) {
					brd.setRegionGroupName(key, rowIndex, svalue);
				}
				break;
			case (1):
				try {
					ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() >= 0) {
						brd.setRegionGroupCapacity(key, rowIndex, ivalue);
					}
				} catch (NumberFormatException e) {
					// Do nothing
				}
				break;
			case (2):
				svalue = (String) aValue;
				if (svalue.equals(REGION_GROUP_STRATEGY_FCFS) && brd.getRegionGroupStrategy(key, rowIndex).contains(REGION_GROUP_STRATEGY_HPS)) {
					brd.deleteAllClassesFromRegionGroup(key, rowIndex);
				}
				if (svalue.contains(REGION_GROUP_STRATEGY_HPS) && brd.getRegionGroupStrategy(key, rowIndex).equals(REGION_GROUP_STRATEGY_FCFS)) {
					for (int i = 0; i < cd.getClassKeys().size(); i++) {
						if (brd.getRegionGroupClassList(key, rowIndex).contains(cd.getClassKeys().get(i))) {
							classAssignTogroup[i] = false;
						}
					}
					brd.deleteAllClassesFromRegionGroup(key, rowIndex);
				}
				brd.setRegionGroupStrategy(key, rowIndex, svalue);
				break;
			}
			repaint();
		}

	}

	protected class GroupEditor extends WizardPanel {

		private static final long serialVersionUID = 1L;

		protected BlockingRegionDefinition brd;
		protected ClassDefinition cd;
		protected Object key;
		protected int groupIndex;
		protected ImagedComboBoxCellEditorFactory classEditor;

		protected JPanel dialogPanel;
		protected GroupEditTable table;

		public GroupEditor(ClassDefinition cd, BlockingRegionDefinition brd, Object key, int groupIndex) {
			this.brd = brd;
			this.cd = cd;
			this.key = key;
			this.groupIndex = groupIndex;		
			classEditor = new ImagedComboBoxCellEditorFactory(cd);

			initComponents();
			setData(cd, brd, key, groupIndex);
		}

		public void setData(ClassDefinition cd, BlockingRegionDefinition brd, Object key, int groupIndex) {
			this.brd = brd;
			this.cd = cd;
			this.key = key;
			this.groupIndex = groupIndex;
			classEditor.setData(cd);
		}

		protected void initComponents() {
			setLayout(new BorderLayout(10, 10));
			dialogPanel = new JPanel(new SpringLayout());
			dialogPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Select Member Classes"));
			dialogPanel.setLayout(new BorderLayout(1, 1));
			table = new GroupEditTable();
			dialogPanel.add(new WarningScrollTable(table, WARNING_CLASS));
			add(dialogPanel, BorderLayout.CENTER);
		}

		@Override
		public String getName() {
			return "Blocking Region Group Panel";
		}

		protected class GroupEditTable extends JTable {

			private static final long serialVersionUID = 1L;

			protected TableCellRenderer gray = new BooleanRenderer();
			int[] columnSizes = new int[] { 320, 50 };

			public GroupEditTable() {
				super();
				setModel(new GroupEditTableModel());
				sizeColumns();
				setRowHeight(ROW_HEIGHT);
				getTableHeader().setReorderingAllowed(false);
			}

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column == 0) {
					return classEditor.getRenderer();
				} else if (column == 1) {
					return gray;
				} else {
					return super.getCellRenderer(row, column);
				}
			}

			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				return super.getCellEditor(row, column);
			}

			private void sizeColumns() {
				for (int i = 0; i < columnSizes.length && i < getColumnCount(); i++) {
					this.getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
				}
			}

		}

		protected class GroupEditTableModel extends AbstractTableModel {

			private static final long serialVersionUID = 1L;

			private Class<?>[] columnClasses = new Class[] { String.class,  Boolean.class };
			private String[] columnNames = new String[] { "Class", "Select", };

			public int getRowCount() {
				return cd.getClassKeys().size();
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
				return  columnIndex == 1 && (brd.getRegionGroupStrategy(key, groupIndex).contains(REGION_GROUP_STRATEGY_HPS)
						|| !classAssignTogroup[rowIndex]
						|| brd.getRegionGroupClassList(key, groupIndex).contains(cd.getClassKeys().get(rowIndex)));
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				Object classKey = cd.getClassKeys().get(rowIndex);
				switch (columnIndex) {
				case (0):
					return classKey;
				case (1):
					return brd.getRegionGroupClassList(key, groupIndex).contains(classKey);
				}
				return null;
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				Object classKey = cd.getClassKeys().get(rowIndex);
				switch (columnIndex) {
				case (1):
					if (((Boolean) aValue).booleanValue()) {
						if (!brd.getRegionGroupStrategy(key, groupIndex).contains(REGION_GROUP_STRATEGY_HPS)) {
							classAssignTogroup[rowIndex] = true;
						}
						brd.addClassIntoRegionGroup(key, groupIndex, classKey);
					} else {
						if (!brd.getRegionGroupStrategy(key, groupIndex).contains(REGION_GROUP_STRATEGY_HPS)) {
							classAssignTogroup[rowIndex] = false;
						}
						brd.deleteClassFromRegionGroup(key, groupIndex, classKey);
					}
					break;
				}
				repaint();
			}
		}

		protected class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {

			private static final long serialVersionUID = 1L;

			private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

			public BooleanRenderer() {
				super();
				setHorizontalAlignment(JLabel.CENTER);
				setBorderPainted(true);
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(table.getBackground());
				}
				if (brd.getRegionGroupStrategy(key, groupIndex).contains(REGION_GROUP_STRATEGY_HPS)
						|| !classAssignTogroup[row]
						|| brd.getRegionGroupClassList(key, groupIndex).contains(cd.getClassKeys().get(row))) {
					setBackground(Color.white);
				} else {
					setBackground(Color.LIGHT_GRAY);
				}
				setSelected(value != null && ((Boolean) value).booleanValue());
				if (hasFocus) {
					setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				} else {
					setBorder(noFocusBorder);
				}
				return this;
			}

		}
	}	

}
