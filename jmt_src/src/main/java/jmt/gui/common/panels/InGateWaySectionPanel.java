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
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.editors.ForkEditor;
import jmt.gui.common.editors.ImagedComboBoxCellEditorFactory;
import jmt.gui.common.forkStrategies.ForkStrategy;

/**
 * <p>Title: ForkSection Panel</p>
 * <p>Description: This panel is used to parametrize fork special behaviour</p>
 *
 * @author Bertoli Marco
 *         Date: 15-mar-2006
 *         Time: 14.25.36
 *         
 *  Modified by Ashanka (Dec 09)
 *  Desc: Minor Cosmetic changes in the label. Changed some Label regarding the Definitions for better understanding of Users.
 */
public class InGateWaySectionPanel extends WizardPanel implements CommonConstants {

	private static final long serialVersionUID = 1L;
	protected StationDefinition sd;
	protected ClassDefinition cd;
	protected Object stationKey;
	protected JSplitPane forkStrategiesPane;
	private JTextArea descrTextPane = new JTextArea("");
	private JScrollPane descrPane = new JScrollPane();

	private ForkingSelectionTable forkingStrategies;
	private ForkEditor forkingProbEditor;

	protected ImagedComboBoxCellEditorFactory classEditor;

	protected JSpinner numForkSpinner;

	private JRootPane gPaneAdvanced;
	private JRootPane gPaneStandard;
	private final JCheckBox useSim;

	public InGateWaySectionPanel(StationDefinition sd, ClassDefinition cd, Object stationKey) {
		classEditor = new ImagedComboBoxCellEditorFactory(cd);
		setDataInternal(sd, cd, stationKey);
		forkingStrategies = new ForkingSelectionTable();
		forkingProbEditor = new ForkEditor(sd, cd, stationKey, null);
		useSim = new JCheckBox();
		updateSpinner();
		initComponents();
	}

	private void setDataInternal(StationDefinition sd, ClassDefinition cd, Object stationKey) {
		this.cd = cd;
		this.sd = sd;
		this.stationKey = stationKey;
		classEditor.setData(cd);
	}

	public void setData(StationDefinition sd, ClassDefinition cd, Object stationKey) {
		setDataInternal(sd, cd, stationKey);
		useSim.setSelected(!sd.getIsSimplifiedFork(stationKey));
		boolean isSim = !useSim.isSelected();
		numForkSpinner.setEnabled(isSim);
		gPaneAdvanced.getGlassPane().setVisible(isSim);
		gPaneStandard.getGlassPane().setVisible(!isSim);
		updateSpinner();
	}

	protected void initComponents() {
		this.setLayout(new BorderLayout());

		gPaneAdvanced = new JRootPane();
		gPaneStandard = new JRootPane();
		forkStrategiesPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		forkStrategiesPane.setDividerSize(4);
		forkStrategiesPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		WarningScrollTable jsp = new WarningScrollTable(forkingStrategies, WARNING_CLASS);
		leftPane.setResizeWeight(.65);

		jsp.setBorder(new TitledBorder(new EtchedBorder(), "Advanced forking strategies"));
		forkStrategiesPane.setResizeWeight(.75); // Gives more space to left component
		jsp.setMinimumSize(new Dimension(180, 100));

		descrTextPane = new JTextArea("");
		descrTextPane.setOpaque(false);
		descrTextPane.setEditable(false);
		descrTextPane.setLineWrap(true);
		descrTextPane.setWrapStyleWord(true);
		descrPane.setBorder(new TitledBorder(new EtchedBorder(), "Description"));
		descrPane.setViewportView(descrTextPane);

		leftPane.setLeftComponent(jsp);
		leftPane.setRightComponent(descrPane);

		forkStrategiesPane.setLeftComponent(leftPane);
		forkingProbEditor.setMinimumSize(new Dimension(160, 100));
		forkStrategiesPane.setRightComponent(forkingProbEditor);
		//enableComponents(forkStrategiesPane, !sd.getIsSimplifiedFork(stationKey));

		gPaneAdvanced.getContentPane().add(forkStrategiesPane);
		gPaneAdvanced.setGlassPane(new GlassPane());
		gPaneAdvanced.getGlassPane().setVisible(sd.getIsSimplifiedFork(stationKey));

		//layout of fragNum panel
		JPanel fragnum = new JPanel();
		fragnum.setLayout(new BorderLayout());
		fragnum.setBorder(new TitledBorder(new EtchedBorder(), "Standard fork strategy"));
		useSim.setText("Enable the advanced forking strategies.");
		//fragnum.add(useSim, BorderLayout.CENTER);
		JLabel text = new JLabel("Number of tasks to be generated on each output link for each input job (customer) to the Fork:");
		text.setLabelFor(numForkSpinner);
		JPanel forkDegreePane = new JPanel();
		forkDegreePane.add(text);
		forkDegreePane.add(numForkSpinner);
		fragnum.add(forkDegreePane, BorderLayout.NORTH);

		gPaneStandard.getContentPane().add(fragnum);
		gPaneStandard.setGlassPane(new GlassPane());
		gPaneStandard.getGlassPane().setVisible(!sd.getIsSimplifiedFork(stationKey));

		sd.getIsSimplifiedFork(stationKey);
		useSim.setSelected(!sd.getIsSimplifiedFork(stationKey));
		numForkSpinner.setEnabled(sd.getIsSimplifiedFork(stationKey));

		useSim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean isSim = !useSim.isSelected();
				sd.setIsSimplifiedFork(stationKey, isSim);
				numForkSpinner.setEnabled(isSim);
				gPaneAdvanced.getGlassPane().setVisible(isSim);
				gPaneStandard.getGlassPane().setVisible(!isSim);
			}
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(gPaneStandard, BorderLayout.NORTH);
		mainPanel.add(gPaneAdvanced, BorderLayout.CENTER);
		add(useSim, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * called by the Wizard before when switching to another panel
	 */
	@Override
	public void lostFocus() {
		// Aborts editing of table
		TableCellEditor editor = forkingStrategies.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
		if (forkingProbEditor != null) {
			forkingProbEditor.stopEditing();
		}
	}

	/**
	 * called by the Wizard when the panel becomes active
	 */
	@Override
	public void gotFocus() {
		classEditor.clearCache();
		if (forkingStrategies != null && forkingStrategies.getRowCount() > 0) {
			if (forkingStrategies.getSelectedRow() == -1) {
				forkingStrategies.setRowSelectionInterval(0, 0);
			}
			forkingProbEditor.setData(sd, cd, stationKey, cd.getClassKeys().get(forkingStrategies.getSelectedRow()));
			descrTextPane.setText(((ForkStrategy)sd.getForkingStrategy(stationKey, cd.getClassKeys().get(forkingStrategies.getSelectedRow()))).getDescription());
		} else {
			forkingProbEditor.setData(sd, cd,  stationKey, 0);
			descrTextPane.setText("");
		}
	}

	@Override
	public String getName() {
		return "Fork Strategies";
	}

	protected class ForkingSelectionTable extends JTable {

		private static final long serialVersionUID = 1L;

		public ForkingSelectionTable() {
			super();
			setModel(new ForkingSelectionTableModel());
			sizeColumns();
			setRowHeight(CommonConstants.ROW_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				ForkStrategy[] forkingStrategies = null;
				Vector<Object> sources = sd.getStationKeysSource();
				if (sources.contains(stationKey)) {
					forkingStrategies = ForkStrategy.findAllForSource();
				} else {
					forkingStrategies = ForkStrategy.findAll();
				}
				return ComboBoxCellEditor.getEditorInstance(forkingStrategies);
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
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		private void sizeColumns() {
			int[] columnSizes = ((ForkingSelectionTableModel) getModel()).columnSizes;
			for (int i = 0; i < columnSizes.length; i++) {
				getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);
			int row = getSelectedRow();
			if (row != -1) {
				if (forkingProbEditor != null) {
					forkingProbEditor.stopEditing();
					forkingProbEditor.setData(sd, cd,  stationKey, cd.getClassKeys().get(row));
					descrTextPane.setText(((ForkStrategy) sd.getForkingStrategy(stationKey, cd.getClassKeys().get(row))).getDescription());
					InGateWaySectionPanel.this.doLayout();
					InGateWaySectionPanel.this.repaint();
				}
			}
		}

	}

	protected class ForkingSelectionTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[] { "Class", "Fork Strategy" };
		public int[] columnSizes = new int[] { 60, 100 };
		private Class<?>[] columnClasses = new Class[] { String.class, Object.class };

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
			return columnIndex == 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object key = indexToKey(rowIndex);
			if (columnIndex == 0) {
				return key;
			} else if (columnIndex == 1) {
				return sd.getForkingStrategy(stationKey, key);
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				Object classKey = indexToKey(rowIndex);
				if (!value.equals(sd.getForkingStrategy(stationKey, classKey))) {
					sd.setForkingStrategy(stationKey, classKey, ((ForkStrategy) value).clone());
				}			
				forkingProbEditor.setData(sd, cd, stationKey, classKey);
				descrTextPane.setText(((ForkStrategy) sd.getForkingStrategy(stationKey, classKey)).getDescription());
				//doLayout();
				//repaint();
				InGateWaySectionPanel.this.doLayout();
				InGateWaySectionPanel.this.repaint();
			}
		}

		private Object indexToKey(int index) {
			return cd.getClassKeys().get(index);
		}

	}

	private void updateSpinner() {
		if (numForkSpinner == null) {
			numForkSpinner = new JSpinner();
			numForkSpinner.setPreferredSize(DIM_BUTTON_XS);
			numForkSpinner.setToolTipText("Number of tasks created for each input job on each output link");
			numForkSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (numForkSpinner.getValue() instanceof Integer) {
						Integer serverNum = (Integer) numForkSpinner.getValue();
						if (serverNum.intValue() < 1) {
							serverNum = new Integer(1);
							numForkSpinner.setValue(serverNum);
						}
						sd.setStationNumberOfServers(serverNum, stationKey);
					}
				}
			});
		}
		numForkSpinner.setValue(sd.getStationNumberOfServers(stationKey));
		numForkSpinner.invalidate();
		numForkSpinner.repaint();
	}

	class GlassPane extends JComponent {

		private static final long serialVersionUID = 1L;

		public GlassPane()
		{
			setOpaque(false);
			setVisible(false);
			Color base = UIManager.getColor("inactiveCaptionBorder");
			base = (base == null) ? Color.LIGHT_GRAY : base;
			Color background = new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
			setBackground(background);

			// Disable Mouse events for the panel
			addMouseListener(new MouseAdapter() {});
			addMouseMotionListener(new MouseMotionAdapter() {});
		}

		/**
		 * The component is transparent but we want to paint the background
		 * to give it the disabled look.
		 */
		@Override
		protected void paintComponent(Graphics g)
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, getSize().width, getSize().height);
		}

	}

}
