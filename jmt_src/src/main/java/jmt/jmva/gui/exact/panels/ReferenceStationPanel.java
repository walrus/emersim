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

package jmt.jmva.gui.exact.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.exact.ld.LDEditor;
import jmt.gui.exact.table.ComboBoxCell;
import jmt.gui.exact.table.ExactTable;
import jmt.gui.exact.table.ExactTableModel;
import jmt.jmva.analytical.ExactConstants;
import jmt.jmva.analytical.ExactModel;
import jmt.jmva.gui.exact.ExactWizard;

/**
 * @author Kourosh Sheykhvand
 * Date: 03-OCT-2013
 * panel: Reference Station
 * This panel has requested to be added in order to make the possibility for each user to choose a reference station for a class in closed model. when the
 * class is open the no "Reference station" is chosen. the data of the the stations are saved in the XML file. here the variable which shows the data
 * for the reference station is ReferenceStation has n rows which refers to the the all the classes. 
 * 
 */
public final class ReferenceStationPanel extends WizardPanel implements ForceUpdatablePanel, ExactConstants{

	private static final long serialVersionUID = 1L;
	private ExactWizard ew;
	private HoverHelp help;
	private static final String helpText = "<html>In this panel you can choose stations for each class.</html>";	
	private int classes;
	private String[] classNames;
	private String[] stationNames;
	private TableCellEditor stationEditor;
	private ComboBoxCell StationCombo;	
	private STTable stTable;
	//private double[][] ReferenceStation;
	private int[] ReferenceStation;
	private int[] classTypes;
	static Color[] colors = {Color.GRAY};
	private double[][] visits;

		
	public ReferenceStationPanel(ExactWizard ew) {		
		this.ew = ew;
		help = ew.getHelp();
		sync();
		initComponents();		
	}	
	public static void infoBox(String infoMessage, String location)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + location, JOptionPane.INFORMATION_MESSAGE);
    }
	public void sync() {		
		ExactModel data = ew.getData();
		synchronized (data) {	
			classes = data.getClasses();						
			classNames = ArrayUtils.copy(data.getClassNames());			 
			stationNames = ArrayUtils.copy(data.getStationNames());
			stationNames = ArrayUtils.resize(stationNames, stationNames.length+1, null);
			stationNames[stationNames.length-1] = "Arrival Process";
			visits = ArrayUtils.copy2(data.getVisits());
			// To set the Gray color for the Arrival Process (just for the UI)
			colors = ArrayUtils.resize(colors, stationNames.length);
			for (int i=0 ; i< stationNames.length; i++)
			{
				if (stationNames[i].equals("Arrival Process"))
				{
					colors[i] = Color.GRAY;
				}
				else
				{
					colors[i] = null;					
				}				
			}						
			ReferenceStation = ArrayUtils.copy(data.getReferenceStation());
			
			classTypes = ArrayUtils.copy(data.getClassTypes());
			
		}
		for (int i=0;i<classes;i++) //if the class is open there is no RefStation
		{
			if (classTypes[i] == 1) 
				ReferenceStation[i] = stationNames.length-1;
			else if (classTypes[i] == 0 && ReferenceStation[i] == stationNames.length-1)
				ReferenceStation[i] = 0;
		}
	}		
	/**
	 * Set up the panel contents and layout
	 */
	private void initComponents() {
		stTable = new STTable();
		JPanel totalBox = new JPanel(new BorderLayout(10, 10));
		JLabel descrLabel = new JLabel(jmt.jmva.analytical.ExactConstants.DESCRIPTION_ReferenceStation);
		JPanel descrBox = new JPanel(new BorderLayout());
		descrBox.setPreferredSize(new Dimension(200, 1000));
		descrBox.add(descrLabel, BorderLayout.NORTH);

		JScrollPane visitTablePane = new JScrollPane(stTable);
		visitTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		visitTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		totalBox.add(visitTablePane, BorderLayout.CENTER);
		totalBox.add(descrBox, BorderLayout.WEST);

		setLayout(new BorderLayout());
		add(totalBox, BorderLayout.CENTER);
		add(Box.createVerticalStrut(30), BorderLayout.NORTH);
		add(Box.createVerticalStrut(30), BorderLayout.SOUTH);
		add(Box.createHorizontalStrut(20), BorderLayout.EAST);
		add(Box.createHorizontalStrut(20), BorderLayout.WEST);	
	}
	@Override
	public String getName() {
		return "Reference Station";
	}
	private void commit() {

		stTable.stopEditing();

		ExactModel data = ew.getData();
		synchronized (data) {			
			data.setReferenceStation(ReferenceStation);
		}
	}
	@Override
	public void gotFocus() {				
		
		this.sync();			
		this.refreshComponents();		
		stTable.update();
		stTable.updateStructure();
		StationCombo = new ComboBoxCell(stationNames);			
		JComboBox stationBox = new JComboBox(stationNames);	
		stationBox.setEditable(false);
		ComboBoxRenderer renderer = new ComboBoxRenderer(stationBox);
		renderer.setColors(colors);
        renderer.setStrings(stationNames);
        stationBox.setRenderer(renderer);
        
		stationEditor = new DefaultCellEditor(stationBox);
										
	}	
	/*These two following classes added to change the color of the Combo box cell when the value of the combobox is Arrival Process */
	class ComboBoxRenderer extends JPanel implements ListCellRenderer
	{

	    private static final long serialVersionUID = -1L;
	    private Color[] colors;
	    private String[] strings;

	    JPanel textPanel;
	    JLabel text;

	    public ComboBoxRenderer(JComboBox combo) {

	        textPanel = new JPanel();
	        textPanel.add(this);
	        text = new JLabel();
	        text.setOpaque(true);
	        text.setFont(combo.getFont());
	        textPanel.add(text);
	    }

	    public void setColors(Color[] col)
	    {
	        colors = col;
	    }

	    public void setStrings(String[] str)
	    {
	        strings = str;
	    }

	    public Color[] getColors()
	    {
	        return colors;
	    }

	    public String[] getStrings()
	    {
	        return strings;
	    }

	    public Component getListCellRendererComponent(JList list, Object value,
	            int index, boolean isSelected, boolean cellHasFocus) {

	        if (isSelected)
	        {
	            setBackground(list.getSelectionBackground());
	        }
	        else
	        {
	            setBackground(Color.WHITE);
	        }

	        if (colors.length != strings.length)
	        {
	            System.out.println("colors.length does not equal strings.length"+ colors.length);
	            return this;
	        }
	        else if (colors == null)
	        {
	            System.out.println("use setColors first.");
	            return this;
	        }
	        else if (strings == null)
	        {
	            System.out.println("use setStrings first.");
	            return this;
	        }

	        text.setBackground(getBackground());

	        text.setText(value.toString());
	        if (index>-1) {
	            text.setForeground(colors[index]);
	        }
	        return text;
	    }
	}
	@Override
	public void lostFocus() {
		commit();
		}
	@Override
	public boolean canFinish() {
		return !stTable.isLDEditing();
	}
	@Override
	public boolean canGoBack() {
		return !stTable.isLDEditing();
	}
	@Override
	public boolean canGoForward() {
		return !stTable.isLDEditing();
	}
	@Override
	public void help() {
		JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
	}
	public void retrieveData() {
		this.sync();
		//refreshComponents();
	}
	public void commitData() {
		this.commit();
	}
	private void refreshComponents() {	
		if (stTable != null) {
			stTable.tableChanged(new TableModelEvent(stTable.getModel()));				
		}
		}
	protected class STTable extends ExactTable implements PropertyChangeListener, Runnable{
				
		private static final long serialVersionUID = 1L;
		private int row;
		private int column;
		private Object oldValue;
		private Object newValue;
		
		public STTable() {
			super(new STTableModel());			
			this.row = -1;
			this.column = -1;
			this.oldValue = null;
			this.newValue = null;
			this.addPropertyChangeListener( this );
			autoResizeMode = AUTO_RESIZE_SUBSEQUENT_COLUMNS;
                        setRowHeight(CommonConstants.ROW_HEIGHT);
                        setDisplaysScrollLabels(true);			
			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(true);
			setClipboardTransferEnabled(true);
			help.addHelp(this,
					"Click or drag to select cells; to edit data single-click and start typing. Right-click for a list of available operations");
			help.addHelp(moreColumnsLabel, "There are more classes: scroll right to see them");
			help.addHelp(moreRowsLabel, "There are more stations: scroll down to see them");
			help.addHelp(selectAllButton, "Click to select all cells");
			tableHeader.setToolTipText(null);
			help.addHelp(tableHeader, "Click, SHIFT-click or drag to select columns");
			rowHeader.setToolTipText(null);
			help.addHelp(rowHeader, "Click, SHIFT-click or drag to select rows");
						
		}

		/**
		 * @return true if the LDEditor window is currently open
		 */
		public boolean isLDEditing() {
			return (cellEditor instanceof LDEditor);
		}
		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) { //stations type, select the right editor
				return stationEditor;				
			} else {
				return super.getCellEditor(row, column);
			}
		}
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			//if this is type column, I must render it as a combo box instead of a jtextfield
			if (column == 1 && classTypes[row] == CLASS_CLOSED) {				
				return StationCombo;
			}			
			else {
				return new DefaultTableCellRenderer();
			}
		}	
		
		@Override
		public void propertyChange(PropertyChangeEvent e)
		{
			//  A cell has started/stopped editing

			if ("tableCellEditor".equals(e.getPropertyName()))
			{
				if (this.isEditing())
					processEditingStarted();
				else
					processEditingStopped();
			}
		}

		/*
		 *  Save information of the cell about to be edited
		 */
		private void processEditingStarted()
		{
			//  The invokeLater is necessary because the editing row and editing
			//  column of the table have not been set when the "tableCellEditor"
			//  PropertyChangeEvent is fired.
			//  This results in the "run" method being invoked

			SwingUtilities.invokeLater( this );
		}
		/*
		 *  See above.
		 */
		@Override
		public void run()
		{
			row = this.convertRowIndexToModel( this.getEditingRow() );
			column = this.convertColumnIndexToModel( this.getEditingColumn() );
			oldValue = this.getModel().getValueAt(row, column);
			newValue = null;
		}

		/*
		 *	Update the Cell history when necessary
		 */
		private void processEditingStopped()
		{
			newValue = this.getModel().getValueAt(row, column);
			
			
			if (! newValue.equals(oldValue))
			{
				//  Make a copy of the data in case another cell starts editing
				//  while processing this change
				for (int i=0; i< classes; i++)
					if (classTypes[i] == 0)
						setValueAt(newValue, i, 1);
				this.update();

			}
			
		}
		
		
	/**
	 * the model backing the service times table.
	 * Rows represent stations, columns classes.
	 */
	}
	private class STTableModel extends ExactTableModel {

		private static final long serialVersionUID = 1L;		
		public int getRowCount() {
			return classNames.length;
		}
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int index) {
			if (index == 0) {				
				return "Class";
			} else if (index == 1) {
				return "Station";
			} else {
				return null;
			}
		}
		@Override
		protected Object getRowName(int rowIndex) {
			return null;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {											
			for (int i=0 ; i< stationNames.length ; i++)
			{
				if (classTypes[rowIndex] == 0 && value.toString().equals("Arrival Process"))
				{									
					ReferenceStation[rowIndex] = 0;
					break;															
				}
				else if (value.toString().equals(stationNames[i]))
				{					
					if (visits[i][rowIndex] != 0)
					{
						ReferenceStation[rowIndex] = i;
						break;
					}
					else
					{
						infoBox("you can not choose a station which has Zero visits. please check the visit panel","Warning!");
						for (int j=0 ; j < stationNames.length-1; j++)					 	
							for (int k=0; k < classNames.length; k++)
								if (visits[j][k] != 0)
									ReferenceStation[rowIndex] = j;					 				
						break;					 	
					}
				}				
				else if (classTypes[rowIndex] == 1)					
					ReferenceStation[rowIndex] = stationNames.length-1;
				else 
					ReferenceStation[rowIndex] = 0;
			}
		}		
					
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return false;
			case 1:
				if (classTypes[rowIndex] == CLASS_CLOSED)
					return true;
				else
					return false;
			default:
				return true;
			}
		}
		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0://name				
						
				return classNames[rowIndex];
			case 1://type						
				return stationNames[(int) ReferenceStation[rowIndex]];
			default:
				return null;
		}
		}
	}

	}



