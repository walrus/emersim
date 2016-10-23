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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmt.framework.gui.layouts.SpringUtilities;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.definitions.BlockingRegionDefinition;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;

/**
 * <p>Title: Blocking Region Parameter Panel</p>
 * <p>Description: This panel is used to parametrize a blocking region</p>
 *
 * @author Bertoli Marco
 *         Date: 30-mar-2006
 *         Time: 16.15.46
 */
public class BlockingRegionParameterPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;

	protected ClassDefinition cd;
	protected BlockingRegionDefinition brd;
	protected Object key;
	// Name of blocking region
	protected JTextField name;
	// Global number of jobs
	protected JSpinner number, memory, gNumber;
	protected JCheckBox inf, mInf, gEnable;
	// Panels for input data
	protected JPanel globalPanel;

	protected BlockingRegionClassPanel classTablePanel;
	protected BlockingRegionGroupPanel groupTablePanel;
	/** Used to display classes with icon */
	protected ImagedComboBoxCellEditorFactory classEditor;

	protected JTabbedPane mainPanel;
	protected WizardPanel current;

	// Index for temporary class name assignment
	protected int groupNameIndex;

	/**
	 * Builds a new Blocking Region Parameter Panel
	 * @param cd class definition data structure
	 * @param brd blocking region definition data structure
	 * @param key search's key for given blocking region
	 */
	public BlockingRegionParameterPanel(ClassDefinition cd, BlockingRegionDefinition brd, Object key) {
		this.cd = cd;
		this.brd = brd;
		this.key = key;
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		initComponents();
		setData(cd, brd, key);
		addActions();
	}

	/**
	 * Sets data for this panel
	 * @param cd class definition data structure
	 * @param brd blocking region definition data structure
	 * @param key search's key for given blocking region
	 */
	public void setData(ClassDefinition cd, BlockingRegionDefinition brd, Object key) {
		this.cd = cd;
		this.brd = brd;
		this.key = key;
		classEditor.setData(cd);

		mainPanel.removeAll();
		if (classTablePanel == null) {
			classTablePanel = new BlockingRegionClassPanel(cd, brd, key);
		} else {
			classTablePanel.setData(cd, brd, key);
		}
		if (groupTablePanel == null) {
			groupTablePanel = new BlockingRegionGroupPanel(cd, brd, key, gEnable);
		} else {
			groupTablePanel.setData(cd, brd, key);
		}
		mainPanel.add(classTablePanel, "Class Specific");
		mainPanel.add(groupTablePanel, "Group Specific");

		current = (WizardPanel) mainPanel.getSelectedComponent();
		updateData();
	}

	/**
	 * Initialize graphic components of this panel
	 */
	protected void initComponents() {
		setLayout(new BorderLayout(5, 5));

		// Panel with global region parameters
		globalPanel = new JPanel(new SpringLayout());
		globalPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), brd.getRegionName(key) + " Global Properties"));
		mainPanel = new JTabbedPane();
		mainPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), brd.getRegionName(key) + " Specific Properties"));

		// Enter region name
		JLabel label = new JLabel("Region Name: ");
		name = new JTextField();
		label.setLabelFor(name);
		globalPanel.add(label);
		globalPanel.add(name);
		// Enter global number of customers
		label = new JLabel("Region Capacity: ");
		JPanel tmpPanel = new JPanel(new BorderLayout(6, 6));
		JPanel tmp = new JPanel(new BorderLayout(6, 6));
		JPanel tmpG = new JPanel(new BorderLayout(6, 6));
		tmpG.setBorder(new EmptyBorder(0, 0, 0, 2));
		number = new JSpinner();
		memory = new JSpinner();
		gNumber = new JSpinner();
		inf = new JCheckBox("Infinite");
		mInf = new JCheckBox("Infinite");
		gEnable = new JCheckBox("Enable");
		// Disable grouping feature
		gEnable.setEnabled(false);
		JLabel mLabel = new JLabel("Region Memory Size: ");
		JLabel gLabel = new JLabel("Region Groups: ");
		tmpPanel.add(number, BorderLayout.CENTER);
		tmpPanel.add(inf, BorderLayout.EAST);
		tmp.add(memory, BorderLayout.CENTER);
		tmp.add(mInf, BorderLayout.EAST);
		tmpG.add(gNumber, BorderLayout.CENTER);
		tmpG.add(gEnable,BorderLayout.EAST);
		label.setLabelFor(tmpPanel);
		mLabel.setLabelFor(tmp);
		gLabel.setLabelFor(tmpG);
		globalPanel.add(label);
		globalPanel.add(tmpPanel);
		globalPanel.add(mLabel);
		globalPanel.add(tmp);
		globalPanel.add(gLabel);
		globalPanel.add(tmpG);

		SpringUtilities.makeCompactGrid(globalPanel, 4, 2, 20, 4, 20, 10);
		add(globalPanel, BorderLayout.NORTH);		
		add(mainPanel, BorderLayout.CENTER);
		groupNameIndex = brd.getRegionGroupList(key).size();
	}

	/**
	 * Adds action listeners to created components
	 */
	protected void addActions() {
		// Name change
		inputListener listener = new inputListener();
		name.addKeyListener(listener);
		name.addFocusListener(listener);

		// Global customer actions
		number.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object value = number.getValue();
				if (value instanceof Integer) {
					int num = ((Integer) value).intValue();
					if (num > 0) {
						brd.setRegionCustomerConstraint(key, (Integer) value);
					}
					number.setValue(brd.getRegionCustomerConstraint(key));
				}
			}
		});

		memory.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object value = memory.getValue();
				if (value instanceof Integer) {
					int num = ((Integer) value).intValue();
					if (num > 0) {
						brd.setRegionMemorySize(key, (Integer) value);
					}
					memory.setValue(brd.getRegionMemorySize(key));
				}
			}
		});

		gNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object value = gNumber.getValue();
				if (value instanceof Integer) {
					int num = ((Integer) value).intValue();
					if (num > 0) {
						setNumberOfGroups(num);
					}
					gNumber.setValue(brd.getRegionGroupList(key).size());
				}
			}
		});

		inf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (inf.isSelected()) {
					number.setValue(Float.POSITIVE_INFINITY);
					number.setEnabled(false);
					brd.setRegionCustomerConstraint(key, Integer.valueOf(-1));
				} else {
					Integer num = Defaults.getAsInteger("blockingMaxJobs");
					if (num.intValue() <= 0) {
						num = Integer.valueOf(1);
					}
					number.setValue(num);
					number.setEnabled(true);
					brd.setRegionCustomerConstraint(key, num);
				}
			}
		});

		mInf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mInf.isSelected()) {
					memory.setValue(Float.POSITIVE_INFINITY);
					memory.setEnabled(false);
					brd.setRegionMemorySize(key, Integer.valueOf(-1));
				} else {
					Integer memorySize = Defaults.getAsInteger("blockingMaxMemory");
					if (memorySize.intValue() <= 0) {
						memorySize = Integer.valueOf(1);
					}
					memory.setValue(memorySize);
					memory.setEnabled(true);
					brd.setRegionMemorySize(key, memorySize);
				}
			}
		});

		gEnable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (gEnable.isSelected()) {
					addGroup();
					gNumber.setModel(new SpinnerNumberModel());
					Integer numOfGroup = Integer.valueOf(1);
					gNumber.setValue(numOfGroup);
					gNumber.setEnabled(true);
					mainPanel.setEnabledAt(1, true);
				} else {
					deleteAllGroup();
					gNumber.setModel(new SpinnerListModel(new String[]{"--"}));
					((DefaultEditor) gNumber.getEditor()).getTextField().setHorizontalAlignment(JTextField.RIGHT);
					gNumber.setValue("--");
					gNumber.setEnabled(false);
					mainPanel.setSelectedIndex(0);
					mainPanel.setEnabledAt(1, false);
				}	
			}
		});

		mainPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// Loses focus on old panel
				if (current != null) {
					current.lostFocus();
				}
				// Gets focus on new panel
				if (mainPanel.getSelectedComponent() != null) {
					current = (WizardPanel) mainPanel.getSelectedComponent();
					current.gotFocus();
				}
			}
		});
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return "Blocking Region Parameter Panel";
	}

	/**
	 * This method will update data inside table and input components
	 */
	protected void updateData() {
		// Name of region and global number of customers
		name.setText(brd.getRegionName(key));
		Integer customer = brd.getRegionCustomerConstraint(key);
		if (customer.intValue() > 0) {
			number.setValue(customer);
			number.setEnabled(true);
			inf.setSelected(false);
		} else {
			number.setValue(Float.POSITIVE_INFINITY);
			number.setEnabled(false);
			inf.setSelected(true);
		}

		Integer MemorySize = brd.getRegionMemorySize(key);
		if (MemorySize.intValue() > 0) {
			memory.setValue(MemorySize);
			memory.setEnabled(true);
			mInf.setSelected(false);
		} else {
			memory.setValue(Float.POSITIVE_INFINITY);
			memory.setEnabled(false);
			mInf.setSelected(true);
		}

		Integer numOfGroup = brd.getRegionGroupList(key).size();
		if (numOfGroup.intValue() > 0) {
			gNumber.setModel(new SpinnerNumberModel());
			gNumber.setValue(numOfGroup);
			gNumber.setEnabled(true);
			gEnable.setSelected(true);
			mainPanel.setEnabledAt(1, true);
		} else {
			gNumber.setModel(new SpinnerListModel(new String[]{"--"}));
			((DefaultEditor) gNumber.getEditor()).getTextField().setHorizontalAlignment(JTextField.RIGHT);
			gNumber.setValue("--");
			gNumber.setEnabled(false);
			gEnable.setSelected(false);
			mainPanel.setEnabledAt(1, false);
		}
	}

	/**
	 * Sets if global panel is visible (default true)
	 * @param value true if panel is visible
	 */
	public void setGlobalVisible(boolean value) {
		globalPanel.setVisible(value);
	}

	/**
	 * Listener used to change region name (associated to name JTextFields).
	 * Parameters are set when JTextField loses focus or ENTER key is pressed.
	 */
	protected class inputListener implements KeyListener, FocusListener {

		/**
		 * Update station's name
		 */
		protected void updateValues() {
			brd.setRegionName(key, name.getText());
			name.setText(brd.getRegionName(key));
		}

		public void focusLost(FocusEvent e) {
			updateValues();
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				updateValues();
				e.consume();
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

	}

	/**
	 * Modifies global number of classes for this model all at once.
	 */
	protected void setNumberOfGroups(int newNumber) {
		// If new number is greater than a certain number, do nothing
		if (newNumber > MAX_NUMBER_OF_GROUPS) {
			setNumberOfGroups(MAX_NUMBER_OF_GROUPS);
			return;
		}

		// If new number is not valid, reset to 0
		if (newNumber < 0) {
			setNumberOfGroups(0);
			return;
		}

		int oldNumber = brd.getRegionGroupList(key).size();
		// If new number is greater than former one, just add
		if (newNumber > oldNumber) {
			for (int i = oldNumber; i < newNumber; i++) {
				addGroup();
			}
		} else if (newNumber < oldNumber) {
			// Otherwise, just delete
			for (int i = oldNumber - 1; i >= newNumber; i--) {
				deleteGroup(i);
			}
		}
		refreshComponents();
	}

	protected void addGroup() {
		groupNameIndex++;
		brd.addRegionGroup(key,Defaults.get("blockingGroupName") + groupNameIndex,
				Defaults.getAsInteger("blockingGroupCapacity").intValue(),
				Defaults.get("blockingGroupStratagy"));
		refreshComponents();
	}

	protected void deleteGroup(int index) {
		brd.deleteRegionGroup(key, index);
		refreshComponents();
	}

	protected void deleteAllGroup() {
		brd.deleteAllRegionGroups(key);
		groupNameIndex = 0;
		refreshComponents();
	}

	protected void refreshComponents() {
		groupTablePanel.Refresh();
	}

}
