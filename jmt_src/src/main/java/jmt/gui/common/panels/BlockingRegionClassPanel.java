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

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.definitions.BlockingRegionDefinition;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.editors.GrayCellRenderer;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;

public class BlockingRegionClassPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	protected BlockingRegionDefinition brd;
	protected ClassDefinition cd;
	protected Object key;
	protected JPanel classPanel;
	protected ImagedComboBoxCellEditorFactory classEditor;
	public BlockingClassTable classTable;

	public BlockingRegionClassPanel(ClassDefinition cd, BlockingRegionDefinition brd, Object key) {
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		setData(cd, brd, key);
		initComponents();
	}

	public void setData(ClassDefinition cd, BlockingRegionDefinition brd, Object key) {
		this.brd = brd;
		this.cd = cd;
		this.key = key;
		classEditor.setData(cd);
	}

	private void initComponents() {
		setLayout(new BorderLayout(5, 5));
		classPanel = new JPanel(new BorderLayout());
		classTable = new BlockingClassTable();
		classPanel.add(new WarningScrollTable(classTable, WARNING_CLASS));
		add(classPanel, BorderLayout.CENTER);
	}

	@Override
	public String getName() {
		return "Blocking Region Class Panel";
	}

	@Override
	public void lostFocus() {
		TableCellEditor editor = classTable.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}

	@Override
	public void gotFocus() {
		classEditor.clearCache();
	}

	protected class BlockingClassTable extends JTable {

		private static final long serialVersionUID = 1L;

		protected String[] dropRules = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		protected TableCellRenderer gray = new GrayCellRenderer();
		int[] columnSizes = new int[] { 150, 60, 30, 50, 40, 40 };		

		public BlockingClassTable() {
			super();
			setModel(new BlockingClassTableModel());
			sizeColumns();
			setRowHeight(ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 0) {
				return classEditor.getRenderer();
			} else if (column == 1 || column == 4 || column == 5) {
				return gray;
			} else if (column == 3) {
				return ComboBoxCellEditor.getRendererInstance();
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 3) {
				return ComboBoxCellEditor.getEditorInstance(dropRules);
			} else {
				return super.getCellEditor(row, column);
			}
		}

		private void sizeColumns() {
			for (int i = 0; i < columnSizes.length && i < getColumnCount(); i++) {
				this.getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
			}
		}
	}

	protected class BlockingClassTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private Class<?>[] columnClasses = new Class[] { String.class, String.class, Boolean.class, String.class, String.class, String.class };
		private String[] columnNames = new String[] { "Class", "Capacity", "\u221e", "Drop", "Memory Size", "Weight" };

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
			// Disable the Weight setting
			return  //columnIndex == 5 ||
					columnIndex == 4 || columnIndex == 3 || columnIndex == 2
					|| (columnIndex == 1 && !((Boolean) getValueAt(rowIndex, columnIndex + 1)).booleanValue());
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object classKey = cd.getClassKeys().get(rowIndex);
			Integer ivalue = null;
			switch (columnIndex) {
			case (0):
				return classKey;
			case (1):
				ivalue = brd.getRegionClassCustomerConstraint(key, classKey);
				return (ivalue.intValue() > 0) ? ivalue : "\u221e";
			case (2):
				ivalue = brd.getRegionClassCustomerConstraint(key, classKey);
				return (ivalue.intValue() <= 0) ? Boolean.TRUE : Boolean.FALSE;
			case (3):
				return brd.getRegionClassDropRule(key, classKey).toString();
			case (4):
				return brd.getRegionClassSize(key, classKey);
			case (5):
				return brd.getRegionClassWeight(key, classKey);
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Object classKey = cd.getClassKeys().get(rowIndex);
			Integer ivalue = null;
			switch (columnIndex) {
			case (1):
				try {
					ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() > 0) {
						brd.setRegionClassCustomerConstraint(key, classKey, ivalue);
					}
				} catch (NumberFormatException e) {
					// Do nothing
				}
				break;
			case (2):
				if (((Boolean) aValue).booleanValue()) {
					brd.setRegionClassCustomerConstraint(key, classKey, Integer.valueOf(-1));
				} else {
					ivalue = Defaults.getAsInteger("blockingMaxJobsPerClass");
					if (ivalue.intValue() <= 0) {
						ivalue = Integer.valueOf(1);
					}
					brd.setRegionClassCustomerConstraint(key, classKey, ivalue);
				}
				break;
			case (3):
				brd.setRegionClassDropRule(key, classKey, Boolean.valueOf((String) aValue));
				break;
			case (4):
				try {
					ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() > 0) {
						brd.setRegionClassSize(key, classKey, ivalue);
					}
				} catch (NumberFormatException e) {
					// Do nothing
				}
				break;
			case (5):
				try {
					ivalue = Integer.valueOf((String) aValue);
					if (ivalue.intValue() > 0) {
						brd.setRegionClassWeight(key, classKey, ivalue);
					}
				} catch (NumberFormatException e) {
					// Do nothing
				}
				break;
			}
			repaint();
		}

	}

}
