package jmt.gui.jsimgraph.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.Defaults;
import jmt.gui.common.panels.CustomizableDialogFactory;
import jmt.gui.common.xml.TemplateFileOperation;
import jmt.gui.jsimgraph.controller.Mediator;
import jmt.gui.jsimgraph.template.ITemplate;
import jmt.gui.jsimgraph.template.TemplateConstants;
import jmt.gui.jsimgraph.template.TemplateData;
import org.apache.commons.io.FileUtils;

/**
 * @author S Jiang
 * 
 */
public class TemplatePanel extends WizardPanel implements TemplateConstants {

	private static final long serialVersionUID = 1L;

	private Mediator mediator;

	// used to show the template adding panel
	private CustomizableDialogFactory customizableDialogFactory;

	// local templates
	private TemplateData[] templates;

	private TemplateTable templateTable;

	private JScrollPane tempPane;

	// refers to the template panel itself
	// parent of the template adding panel
	private TemplatePanel parent;

	// template
	private ITemplate myTemp;
        private JButton update;

	public TemplatePanel(Mediator mediator) {
		this.mediator = mediator;
		customizableDialogFactory = new CustomizableDialogFactory(
				mediator.getMainWindow());
		parent = this;

		initComponents();

	}
        
        public void updateTemplates() {
                templates = TemplateFileOperation.readTemplates(TEMPLATE_FOLDER);
                templateTable.invalidate();
                templateTable.repaint();
        }

	public void initComponents() {
		templates = TemplateFileOperation.readTemplates(TEMPLATE_FOLDER);
		templateTable = new TemplateTable();

		this.setLayout(new BorderLayout());

		JButton add = new JButton("Add/See all");
		JButton remove = new JButton("Remove");
		JButton refresh = new JButton("Refresh");

		update = new JButton("Update");
                update.setEnabled(false);

		JButton create = new JButton("Instantiate");
		JButton close = new JButton("Close");

		add.setPreferredSize(new Dimension(160, 25));
		remove.setPreferredSize(new Dimension(160, 25));
		refresh.setPreferredSize(new Dimension(140, 25));

		update.setPreferredSize(new Dimension(160, 25));

		create.setPreferredSize(new Dimension(120, 25));
		close.setPreferredSize(new Dimension(120, 25));

		// adding a new template from website
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				if (ConnectionCheck.netCheck(CONN_TEST_ADDRESSES)) {
					customizableDialogFactory.getDialog(1000, 600,
							new TemplateAddingPanel(parent), "Add/See Available Templates");
//				} else {
//					JOptionPane.showMessageDialog(parent,
//							"Please check your internet connection");
//				}
			}
		});

		// remove a existing template
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = templateTable.getSelectedRow();
				if (selectedRow != -1 && templateTable.getRowCount() != 0) {
                                        templates[selectedRow].getFile().delete();
					// refresh the existing template list
					refresh();
				}

			}
		});

		// refresh the existing template list manually
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh();
			}
		});

		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<TemplateData> updateList = new ArrayList<TemplateData>();
                                for (int i: templateTable.getSelectedRows()) {
                                        TemplateData template = templates[i];
                                        updateList.add(template);
                                }
                                if (updateList.size() > 0) {
                                        customizableDialogFactory.getDialog(
						520,
						200,
						new UpdatePanel(TemplatePanel.this, updateList),
						"Updating...");
					refresh();
                                }
			}
		});

		// invoke the selected template
		create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
//				new Template(mediator).showDialog(mediator.getMainWindow());
				openTemplate();

			}

		});
		
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.getWindowAncestor(parent).setVisible(false);
			}

		});

		templateTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openTemplate();
				}
			}
		});
                
                ListSelectionModel selectionModel = templateTable.getSelectionModel();
                selectionModel.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                                if (templateTable.getSelectedRowCount() > 0) {
                                        update.setEnabled(true);
                                } else {
                                        update.setEnabled(false);
                                }
                        }

                });

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(add);
		buttonPanel.add(remove);
//		buttonPanel.add(refresh);
		buttonPanel.add(update);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(create);
		bottomPanel.add(close);

		this.add(buttonPanel, BorderLayout.NORTH);
		tempPane = new JScrollPane();
		tempPane.getViewport().add(templateTable);
		this.add(tempPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

	}

	// refresh the template list
	public void refresh() {
		templates = TemplateFileOperation.readTemplates(TEMPLATE_FOLDER);
		// repaint the list
		tempPane.getViewport().revalidate();
		tempPane.getViewport().repaint();

	}

	public void openTemplate() {
		if (Defaults.getAsBoolean("showTemplateDialog")) {
			JOptionPane
					.showMessageDialog(
							parent,
							"<html>Each template has some default values for the parameters."
									+ "<br>\nOnce instantiated you may change them according to your needs");

			Defaults.set("showTemplateDialog", "false");
                                        Defaults.save();
		}

		// if (open == 0) {
		int selectedRow = templateTable.getSelectedRow();

		if (selectedRow != -1 && templateTable.getRowCount() != 0) {
			// close the template panel dialog
			SwingUtilities.getWindowAncestor(parent).setVisible(false);
			try {
                                File template = templates[selectedRow].getFile();
                                File copy = File.createTempFile(template.getName(), "");
                                FileUtils.copyFile(templates[selectedRow].getFile(), copy);
                                JarFile templateJarFile = new JarFile(copy);

				// get the path of template
				Manifest m = templateJarFile.getManifest();
				String mainClass = m.getMainAttributes().getValue("Main-Class")
						.toString();

				URL url = new URL("file:"
						+ copy.getAbsolutePath());

				URLClassLoader myLoader = new URLClassLoader(new URL[] { url },
						Thread.currentThread().getContextClassLoader());
				Class<?> myClass = myLoader.loadClass(mainClass);

				// instantiate the template
				myTemp = (ITemplate) myClass
						.getConstructor(mediator.getClass()).newInstance(
								mediator);
				// shows the input panel of the template
				myTemp.showDialog(mediator.getMainWindow());

				templateJarFile.close();
				myLoader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		// }

	}

	public TemplateData[] getTemplates() {
		return this.templates;
	}

	// table shows the local template list
	protected class TemplateTable extends JTable {
		private static final long serialVersionUID = 1L;

		public TemplateTable() {
			super();
			setModel(new TemplateTableModel());
			sizeColumns();
			setRowHeight(CELL_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(
						((TemplateTableModel) getModel()).columnSizes[i]);
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String tip = null;
			Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);

			try {
				tip = templates[rowIndex].getToolTip();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			return tip;
		}

	}

	protected class TemplateTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "Template", "Version", "Description",
				"Last Downloaded" };

		public int[] columnSizes = new int[] { 140, 80, 320, 160 };

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return templates.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {

				return templates[rowIndex].getName();

			} else if (columnIndex == 1) {

				return templates[rowIndex].getVersion();

			} else if (columnIndex == 2) {

				return templates[rowIndex].getShortDescription();

			} else if (columnIndex == 3) {

				Path temPath = Paths.get(templates[rowIndex].getFile()
						.getAbsolutePath());

				try {
					BasicFileAttributes view = Files.getFileAttributeView(
							temPath, BasicFileAttributeView.class)
							.readAttributes();

					FileTime creationTime = view.creationTime();
					DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
					String cTime = df.format(creationTime.toMillis());
					return cTime;
				} catch (IOException e) {
					e.printStackTrace();
					return "N/A";
				}
			} else {
				return null;
			}

		}

	}

	@Override
	public String getName() {
		return "Template";
	}

}
