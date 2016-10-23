package jmt.gui.jsimgraph.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import jmt.common.ConnectionCheck;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.Defaults;
import jmt.gui.common.xml.TemplateFileOperation;
import jmt.gui.jsimgraph.controller.Mediator;
import jmt.gui.jsimgraph.template.TemplateConstants;
import static jmt.gui.jsimgraph.template.TemplateConstants.CONN_TIMEOUT;
import static jmt.gui.jsimgraph.template.TemplateConstants.DEFAULT_SITE;
import static jmt.gui.jsimgraph.template.TemplateConstants.F_NAME_INDEX;
import static jmt.gui.jsimgraph.template.TemplateConstants.READ_TIMEOUT;
import jmt.gui.jsimgraph.template.TemplateData;

public class AutoDownloadPanel extends WizardPanel {

	private static final long serialVersionUID = 1L;

	private String indexFile;

	private JProgressBar progress;

	private JButton download;
	private JButton cancel;
	private JButton retry;

	private JLabel errorLabel = new JLabel("Connection error");
	private JLabel progLabel = new JLabel("PROGRESS:");
	private JPanel bottomPane;

	private List<TemplateData> data;
	private DefaultPackageTable defaultPackageTable;

	private JPanel parent;

	private TemplatePanel panel;
	private boolean error;

	public AutoDownloadPanel() {
		parent = this;
		this.panel = null;
		initComponents();
	}

	public void initComponents() {

		downloadIndex();

		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setString("0%");
		download = new JButton("Download");
		cancel = new JButton("Cancel");
		retry = new JButton("Retry");

		progress.setPreferredSize(new Dimension(200, 25));
		download.setPreferredSize(new Dimension(110, 25));
		cancel.setPreferredSize(new Dimension(110, 25));
		retry.setPreferredSize(new Dimension(110, 25));

		download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File folder = new File(TemplateConstants.TEMPLATE_FOLDER);
				if (!folder.exists()) {
					folder.mkdir();
				}
				progress.setValue(0);
				progress.setString("0%");
				download.setEnabled(false);
				cancel.setEnabled(false);
				if (ConnectionCheck.netCheck(TemplateConstants.CONN_TEST_ADDRESSES)) {
					new Downloader().execute();
				} else {
					download.setEnabled(true);
					cancel.setEnabled(true);
					JOptionPane.showMessageDialog(parent, "Connection error");
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.getWindowAncestor(parent).setVisible(false);
			}
		});

		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				downloadIndex();
				for (TemplateData t : data)
					System.out.println(t.getName());
				((DefaultPackageTableModel) defaultPackageTable.getModel()).fireDataChanged();
				((DefaultPackageTableModel) defaultPackageTable.getModel()).setFlags(data.size());
				createBottom();
				AutoDownloadPanel.this.revalidate();
				AutoDownloadPanel.this.repaint();
			}
		});

		defaultPackageTable = new DefaultPackageTable();
		JScrollPane fileListPane = new JScrollPane();
		fileListPane.getViewport().add(defaultPackageTable);

		JLabel label = new JLabel("Select the templates that you want to download: ");
		label.setLabelFor(fileListPane);

		JLabel progLabel = new JLabel("PROGRESS:");
		progLabel.setLabelFor(progress);

		this.setLayout(new BorderLayout());

		JPanel upperPane = new JPanel();
		upperPane.setLayout(new FlowLayout(FlowLayout.LEADING));

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		createBottom();
		upperPane.add(label);
		this.add(upperPane, BorderLayout.NORTH);
		this.add(fileListPane, BorderLayout.CENTER);
	}

	private void downloadIndex() {
		try {
			File index = File.createTempFile(F_NAME_INDEX, "");
			index.deleteOnExit();
			indexFile = index.getAbsolutePath();
			TemplateFileOperation.download(DEFAULT_SITE + "/" + F_NAME_INDEX , indexFile,
					CONN_TIMEOUT, READ_TIMEOUT);
			data = TemplateFileOperation.getDefaultTemplatesFromIndex(indexFile);
			error = false;
		} catch (Exception e) {
			data = new ArrayList<>();
			error = true;
		}
	}

	private void createBottom() {
		if (bottomPane != null) {
			this.remove(bottomPane);
		}
		bottomPane = new JPanel();
		bottomPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		if (error) {
			bottomPane.setLayout(new BorderLayout());
			bottomPane.add(errorLabel, BorderLayout.WEST);
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));
			buttons.add(retry);
			buttons.add(cancel);
			bottomPane.add(buttons, BorderLayout.EAST);
		} else {
			bottomPane.setLayout(new FlowLayout(FlowLayout.LEADING));
			bottomPane.add(progLabel);
			bottomPane.add(progress);
			bottomPane.add(download);
			bottomPane.add(cancel);
		}
		this.add(bottomPane, BorderLayout.SOUTH);
	}

	public class DefaultPackageTable extends JTable {

		private static final long serialVersionUID = 1L;

		public DefaultPackageTable() {
			super();
			setModel(new DefaultPackageTableModel());
			sizeColumns();
			setRowHeight(TemplateConstants.CELL_HEIGHT);
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(((DefaultPackageTableModel) getModel()).columnSizes[i]);
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String tip = null;
			Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);

			try {
				tip = data.get(rowIndex).getToolTip();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			return tip;
		}

	}

	public class DefaultPackageTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		String[] columnNames = { "Template", "Download" };
		Class<?>[] columnClass = { String.class, Boolean.class };

		public int[] columnSizes = new int[] { 420,100 };

		boolean[] flags = new boolean[data.size()];

		public void setFlags(int size) {
			flags = new boolean[data.size()];
			Arrays.fill(flags, true);
		}

		public void fireDataChanged() {
			super.fireTableDataChanged();
		}

		public DefaultPackageTableModel() {
			Arrays.fill(flags, true);
		}

		@Override
		public Class<?> getColumnClass(int colIndex) {
			return (columnClass[colIndex]);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int colIndex) {
			return columnNames[colIndex];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return data.get(rowIndex).getName();
			} else {
				return flags[rowIndex];
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			flags[row] = (Boolean) value;
		}

	}

	public class Downloader extends SwingWorker<Boolean, Integer> {

		List<TemplateData> toDownload;

		public Downloader() {
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			toDownload = new ArrayList<>();
			for (int i = 0; i < data.size(); i++) {
				if ((Boolean) defaultPackageTable.getValueAt(i, 1)) {
					toDownload.add(data.get(i));
				}
			}
			for (int i = 0; i < toDownload.size(); i++) {
				String file = toDownload.get(i).getUpdateAddress();
				String fileName = toDownload.get(i).getFileName();
				try {
					if (fileName != null) {
						fileName = TemplateConstants.TEMPLATE_FOLDER
								+ fileName;
						TemplateFileOperation.download(file, fileName,
								TemplateConstants.CONN_TIMEOUT,
								TemplateConstants.READ_TIMEOUT);
					} else {
						throw new Exception();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(parent,
							"<html> Error: Unable to download <br>\n"
									+ file);
				}
				publish(i + 1);
			}
			Thread.sleep(300);
			publish(-1);
			return null;
		}

		protected void process(final List<Integer> chunks) {
			for (final Integer task : chunks) {
				if (task > -1) {
					int value = task * 100 / toDownload.size();
					progress.setValue(value);
					progress.setString(value + "%");
				}

				if (task == -1) {
					download.setEnabled(true);
					cancel.setEnabled(true);
					Defaults.set("showDefaultTemplates", "false");
					Defaults.save();
					SwingUtilities.getWindowAncestor(parent).setVisible(false);
					if (panel != null) {
						panel.updateTemplates();
					}
				}
			}
		}

	}

	@Override
	public String getName() {
		return "Downloading";
	}

}
