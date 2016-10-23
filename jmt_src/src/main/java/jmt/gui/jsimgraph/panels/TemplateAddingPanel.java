package jmt.gui.jsimgraph.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.CopyOption;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.xml.sax.SAXException;

import jmt.common.MD5Checksum;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.xml.TemplateFileOperation;
import jmt.gui.jsimgraph.template.AddingPanelData;
import jmt.gui.jsimgraph.template.TemplateConstants;
import jmt.gui.jsimgraph.template.TemplateData;
import jmt.gui.common.RootNode;
import jmt.gui.common.TreeTableNode;

public class TemplateAddingPanel extends WizardPanel implements
		TemplateConstants {

	private static final long serialVersionUID = 1L;
        
        private static final String CONNECTION_ERROR = "Connection error";
        
        private static final String NO_DESC_PAGE = "NoDescriptionPage.html";
        private static final String ERROR_PAGE = "ErrorPage.html";
        private static final String BLANK_PAGE = "BlankPage.html";
        private static final String MAIN_PAGE = "MainPage.html";

	// parent panel
	private TemplatePanel templatePanel;

	private AddingPanelData data;

	// current status displayed in status bar (bottom of the window)
	private JLabel status;

	// list shows all stored sites
	private JComboBox<String> siteList;

	// table shows all available templates
//	private AvailableTable availableTable = null;
	private AvailableTreeTable availableTreeTable = null;

	private JButton siteUpdate;
	private JButton siteAdd;
	private JButton tempDownload;
//	private JButton tempHelp;
	private JButton close;
        private String indexFile;
	// private JButton tempDone;

	// refer to the template adding panel itself
	// parent of the adding site dialog
	private JPanel parent;

	// stores the template list
	private JScrollPane tablePane;
	// stores the description page panel
	private JScrollPane desPane;

	// displays description pages
	private JEditorPane pagePane;

	// stores all templates those are downloaded already
//	private List<String> downloadedList;

	// existing templates
	private File[] templates;
        
        private boolean index_init_error = false;

	public TemplateAddingPanel(TemplatePanel templatePanel) {
		data = new AddingPanelData();
		this.templatePanel = templatePanel;
//		availableTable = new AvailableTable();
		parent = this;

		initComponents();
	}

	public void initComponents() {
		// check the essential files
		// Without these files, it's unable to show the dialog
		// So, it's not put into SwingWorker
                
                pagePane = new JEditorPane();
		pagePane.setEditable(false);
                
		try {
                        TemplateFileOperation.parseSiteXML(data);
                        File index = File.createTempFile("F_NAME_INDEX", "");
                        indexFile = index.getAbsolutePath();
			TemplateFileOperation.download(DEFAULT_SITE + "/" + F_NAME_INDEX , index.getAbsolutePath(),
						CONN_TIMEOUT, READ_TIMEOUT);
			TemplateFileOperation.parseIndexXML(indexFile, data);
                        pagePane.setPage(TemplateAddingPanel.class.getResource(MAIN_PAGE));

		} catch (Exception e) {
                        try {
                                index_init_error = true;
                                pagePane.setPage(TemplateAddingPanel.class.getResource(ERROR_PAGE));
                        } catch (IOException ex) {
                                Logger.getLogger(TemplateAddingPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
		}

		availableTreeTable = new AvailableTreeTable();
                availableTreeTable.expandRow(0);

		templates = TemplateFileOperation.findTemplateFiles(TEMPLATE_FOLDER);

//		downloadedList = intersect();

		this.setLayout(new BorderLayout());

		// currentPage = DEFAULT_HOMEPAGE;

		status = new JLabel("");

		JLabel siteTag = new JLabel("Site:");

		siteUpdate = new JButton("Update");
		siteAdd = new JButton("Add Site");

		siteList = new JComboBox<String>(new SiteComboBoxModel());
		siteList.setEditable(true);
		siteList.setPreferredSize(new Dimension(580, 24));

		// siteText.setText(siteURL);

		siteTag.setLabelFor(siteList);

		siteUpdate.setPreferredSize(new Dimension(70, 24));
		siteAdd.setPreferredSize(new Dimension(100, 24));

		JPanel upperPane = new JPanel();
		upperPane.add(siteTag);
		upperPane.add(siteList);
		upperPane.add(siteUpdate);
		upperPane.add(siteAdd);

		// display the current site
		int index = data.getSiteURLs()
				.indexOf(data.getSiteURL());

		if (index != -1 && !index_init_error) {
			siteList.getEditor().setItem(data.getSiteInfos().get(index));
		} else if (!index_init_error) {
			siteList.getEditor().setItem(data.getSiteInfo());
		} else {
                        siteList.getEditor().setItem(DEFAULT_SITE);
                }

//		JPanel centerPane = new JPanel();
//		centerPane.add(availableTable);

		tempDownload = new JButton("Download");
//		tempHelp = new JButton("Help");
		close = new JButton("Close");
		// tempDone = new JButton("Done");

		tempDownload.setPreferredSize(new Dimension(100, 24));
//		tempHelp.setPreferredSize(new Dimension(100, 24));
		close.setPreferredSize(new Dimension(100, 24));
		// tempDone.setPreferredSize(new Dimension(100, 24));

		tempDownload.setBackground(Color.RED);
                tempDownload.setEnabled(false);

		// update the index.xml when user inputing a new site
		// two situations
		// 1. the site is stored in the site list
		// 2. the site is not in the site list, it's typed by user in the bar
		siteUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doUpdate();
			}
		});
                
                siteList.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
                        public void keyPressed(KeyEvent evt) {
                                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                                        doUpdate();
                                }
                        }
                });

		// add a new site to the site list
		siteAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				status.setText("creating a new site...");
				status.repaint();

				AddingSitePanel newSite = new AddingSitePanel();

				String siteName = "";
				String siteAddress = "";

				int result = JOptionPane.showConfirmDialog(parent, newSite,
						newSite.getPanelName(), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);

				if (result == JOptionPane.OK_OPTION) {

					siteName = newSite.getName();
					siteAddress = newSite.getAddress();

					if (!"http://".equals(siteAddress)) {
						String siteIndexUrl = siteAddress + "/" + F_NAME_INDEX;
						// verify the site
						new Checker(siteIndexUrl, status, siteName, siteAddress)
								.execute();
					} else {
						JOptionPane.showMessageDialog(parent,
								"Error: please check the URL");
						status.setText("");
						status.repaint();
					}

				} else {
					status.setText("");
					status.repaint();
				}

			}

		});

		// download the selected template
		tempDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int rowIndex = availableTreeTable.getSelectedRow();
				if (rowIndex != -1) {

					TreePath path = availableTreeTable.getPathForRow(rowIndex);
					TreeTableNode node = (TreeTableNode) path
							.getLastPathComponent();
					if ("template".equals(node.getType())) {

						String tempURL = ((TemplateData) node.getData()[0])
								.getUpdateAddress();
						String tempFileName = ((TemplateData) node.getData()[0])
								.getFileName();

						boolean flag = false;
						for (File template : templates) {
							if (template.getName().equals(tempFileName)) {
								flag = true;
							}
						}

						int overwrite = 0;

						if (flag) {
							overwrite = JOptionPane.showConfirmDialog(parent,
									"Replace the existing file?", "Replace",
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.PLAIN_MESSAGE);
						}

						if (overwrite == 0) {
							new Downloader(tempURL, TEMPLATE_FOLDER
									+ tempFileName, status, D_TYPE_TEMPLATE)
									.execute();
						}

					}
				}

			}
		});

//		tempHelp.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				try {
//					// if (!currentPage.equals(DEFAULT_HOMEPAGE)) {
//					new PageLoader(DEFAULT_HOMEPAGE).execute();
//					// }
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.getWindowAncestor(parent).setVisible(false);
			}
		});

		JPanel bottomPane = new JPanel();

		JPanel buttons = new JPanel();
//		buttons.add(tempHelp);
		buttons.add(tempDownload);
		buttons.add(close);
		buttons.setLayout(new FlowLayout(FlowLayout.TRAILING));

		bottomPane.setLayout(new GridLayout(1, 2));

		bottomPane.add(status);
		bottomPane.add(buttons);

		this.add(upperPane, BorderLayout.NORTH);
		tablePane = new JScrollPane();
		// tablePane.getViewport().add(availableTable);
		tablePane.getViewport().add(availableTreeTable);

		// load the home page of JMT template
		try {
			// make the appearing time long;(?)
			// new PageLoader(DEFAULT_HOMEPAGE).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		desPane = new JScrollPane(pagePane);
		JPanel cenPane = new JPanel();

		cenPane.setLayout(new GridLayout(2, 1));
		cenPane.add(tablePane);
		cenPane.add(desPane);

		// if user select a template, the description page of that template will
		// be displayed
		availableTreeTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				int rowIndex = availableTreeTable.getSelectedRow();
				if (rowIndex != -1) {

					TreePath path = availableTreeTable.getPathForRow(rowIndex);
					TreeTableNode node = (TreeTableNode) path
							.getLastPathComponent();
					if ("template".equals(node.getType())) {

						String DescriptionURL = ((TemplateData) node.getData()[0])
								.getDescriptionAddress();

						if (DescriptionURL != null) {
							new PageLoader(DescriptionURL).execute();
						} else {

							new PageLoader(TemplateAddingPanel.class.getResource(NO_DESC_PAGE).toString())
									.execute();
						}

					}
				}
				
			}
		});

		this.add(cenPane, BorderLayout.CENTER);
		this.add(bottomPane, BorderLayout.SOUTH);

	}

	@Override
	public String getName() {
		return "Add templates";
	}
        
        private void doUpdate() {
                String indexURL = null;
                // prepare the index URL
                if (siteList.getSelectedIndex() != -1) {
                        // in the list
                        indexURL = data.getSiteURLs().get(siteList.getSelectedIndex())
                                        + "/" + F_NAME_INDEX;
                } else {
                        // not in the list
//					indexURL = siteList.getEditor().getItem() + "/"
//							+ F_NAME_INDEX;

                        String raw = (String)siteList.getEditor().getItem();
                        String find = "http.*";
                        Pattern pattern = Pattern.compile(find);
                        Matcher matcher = pattern.matcher(raw);
                        if (matcher.find()) {
                                indexURL = matcher.group()  + "/"
                                                + F_NAME_INDEX;
                        }

                }
                File index;
                try {
                        index = File.createTempFile("index", ".xml");
                        // download the index.xml
                        indexFile = index.getAbsolutePath();
                        new Downloader(indexURL, index.getAbsolutePath(), status, D_TYPE_INDEX)
                                .execute();
                } catch (IOException ex) {
                        Logger.getLogger(TemplateAddingPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

	public class AvailableTreeTable extends JXTreeTable {

		private static final long serialVersionUID = 1L;
		// TODO:move it to model class
		public int[] columnSizes = new int[] { 240, 100, 60, 100, 400, 200 };

		public AvailableTreeTable() {
			super();
			setTreeTableModel(new AvailableTreeTableModel(data.getRoot()));
			sizeColumns();
			setRowHeight(TemplateConstants.CELL_HEIGHT);
                        this.addTreeSelectionListener(new TreeSelectionListener() {

                                @Override
                                public void valueChanged(TreeSelectionEvent e) {
                                        TreePath path = e.getPath();
					TreeTableNode node = (TreeTableNode) path
							.getLastPathComponent();
					if ("template".equals(node.getType())) {
                                                tempDownload.setEnabled(true);
                                        } else {
                                                tempDownload.setEnabled(false);
                                        }
                                }
                                
                        });
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(columnSizes[i]);
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String tip = null;
			Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);
			if (rowIndex != -1) {
				TreePath path = availableTreeTable.getPathForRow(rowIndex);
				TreeTableNode node = (TreeTableNode) path
						.getLastPathComponent();
				if ("template".equals(node.getType())) {

					tip = ((TemplateData) node.getData()[0])
							.getToolTip();
				}
			}

			return tip;
		}

	}

	public class AvailableTreeTableModel extends AbstractTreeTableModel {

		private String[] columnNames = new String[] { "Name", "Author",
				"Version", "Upload Date", "Description", "Downloaded" };

		public int[] columnSizes = new int[] { 140, 100, 60, 100, 500, 200 };

		AvailableTreeTableModel(Object root) {
			super(root);
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnNames.length;
		}

		@Override
		public String getColumnName(int colIndex) {
			return columnNames[colIndex];
		}
                
                public void resetRoot(Object root) {
                        this.root = root;
                        modelSupport.fireNewRoot();
                }

		@Override
		public Object getValueAt(Object parent, int index) {
			if (!(parent instanceof RootNode)) {
				if (("template".equals(((TreeTableNode) parent).getType()))) {
					switch (index) {
					case 0:
						return ((TemplateData) ((TreeTableNode) parent)
								.getData()[0]).getName();
					case 1:
						return ((TemplateData) ((TreeTableNode) parent)
								.getData()[0]).getAuthor();
					case 2:
						return ((TemplateData) ((TreeTableNode) parent)
								.getData()[0]).getVersion();
					case 3:
						return ((TemplateData) ((TreeTableNode) parent)
								.getData()[0]).getDate();
					case 4:
						return ((TemplateData) ((TreeTableNode) parent)
								.getData()[0]).getShortDescription();
					case 5:
//						if (downloadedList
//								.contains(((TemplateData) ((TreeTableNode) parent)
//										.getData()[0]).getMd5())) {
							Path temPath = Paths
									.get(TemplateConstants.TEMPLATE_FOLDER
											+ ((TemplateData) ((TreeTableNode) parent)
													.getData()[0])
													.getFileName());
							
//							System.out.println(((TemplateData) ((TreeTableNode) parent).getData()[0]).getAuthor());
//							System.out.println(temPath);
							
							try {
								
								BasicFileAttributes view = Files
										.getFileAttributeView(temPath,
												BasicFileAttributeView.class)
										.readAttributes();

								FileTime creationTime = view.creationTime();
								DateFormat df = new SimpleDateFormat(
										"HH:mm:ss dd/MM/yyyy");
								String cTime = df.format(creationTime
										.toMillis());
								return cTime;
							} catch (Exception e) {
//								e.printStackTrace();
								return "--";
							}
//						} else {
//							return "--";
//						}
					default:
						return null;
					}

				} else {
					if (index < 1)
						return ((TreeTableNode) parent).getData()[index];
					else
						return null;
				}
			}
			return null;

		}

		@Override
		public Object getChild(Object parent, int index) {
			// TODO Auto-generated method stub
			return ((TreeTableNode) parent).getChildAt(index);
		}

		@Override
		public int getChildCount(Object parent) {
			// TODO Auto-generated method stub
			return ((TreeTableNode) parent).getChildCount();
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			// TODO Auto-generated method stub
			return ((TreeTableNode) parent).getIndex((TreeTableNode) child);
		}

	}

	// table of all available templates
//	public class AvailableTable extends JTable {
//
//		private static final long serialVersionUID = 1L;
//
//		public AvailableTable() {
//			super();
//			setModel(new AvailableTableModel());
//			sizeColumns();
//			setRowHeight(TemplateConstants.CELL_HEIGHT);
//		}
//
//		private void sizeColumns() {
//			for (int i = 0; i < getColumnCount(); i++) {
//				getColumnModel().getColumn(i).setPreferredWidth(
//						((AvailableTableModel) getModel()).columnSizes[i]);
//			}
//		}
//
//		@Override
//		public String getToolTipText(MouseEvent e) {
//			String tip = null;
//			Point p = e.getPoint();
//			int rowIndex = rowAtPoint(p);
//
//			try {
//				tip = data.getTemplateDatas().get(rowIndex)
//						.getShortDescription();
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//
//			return tip;
//		}
//
//	}
//
//	// Model of the above table
//	public class AvailableTableModel extends AbstractTableModel {
//
//		private static final long serialVersionUID = 1L;
//
//		private String[] columnNames = new String[] { "Name", "Author",
//				"Version", "Upload Date", "Description", "Downloaded" };
//
//		public int[] columnSizes = new int[] { 140, 100, 60, 100, 500, 200 };
//
//		@Override
//		public int getColumnCount() {
//			return columnNames.length;
//		}
//
//		@Override
//		public int getRowCount() {
//			return data.getTemplateDatas().size();
//		}
//
//		@Override
//		public String getColumnName(int colIndex) {
//			return columnNames[colIndex];
//		}
//
//		@Override
//		public Object getValueAt(int rowIndex, int colIndex) {
//
//			switch (colIndex) {
//			case 0:
//				return data.getTemplateDatas().get(rowIndex).getName();
//			case 1:
//				return data.getTemplateDatas().get(rowIndex).getAuthor();
//			case 2:
//				return data.getTemplateDatas().get(rowIndex).getVersion();
//			case 3:
//				return data.getTemplateDatas().get(rowIndex).getDate();
//			case 4:
//				return data.getTemplateDatas().get(rowIndex)
//						.getShortDescription();
//			case 5:
//				if (downloadedList.contains(data.getTemplateDatas()
//						.get(rowIndex).getMd5())) {
//					System.out.println(downloadedList);
//					Path temPath = Paths.get(TemplateConstants.TEMPLATE_FOLDER
//							+ data.getTemplateDatas().get(rowIndex)
//									.getFileName());
//
//					try {
//						BasicFileAttributes view = Files.getFileAttributeView(
//								temPath, BasicFileAttributeView.class)
//								.readAttributes();
//
//						FileTime creationTime = view.creationTime();
//						DateFormat df = new SimpleDateFormat(
//								"HH:mm:ss dd/MM/yyyy");
//						String cTime = df.format(creationTime.toMillis());
//						return "Yes: " + cTime;
//					} catch (IOException e) {
//						e.printStackTrace();
//						return "N/A";
//					}
//				} else {
//					return "--";
//				}
//			default:
//				return null;
//			}
//
//		}
//
//	}

	// site list combobox model
	public class SiteComboBoxModel extends AbstractListModel<String> implements
			ComboBoxModel<String> {

		private static final long serialVersionUID = 1L;

		Object selection = null;

		@Override
		public String getElementAt(int index) {
				return data.getSiteInfos().get(index);
		}

		@Override
		public int getSize() {
			return data.getSiteInfos().size();
		}

		@Override
		public Object getSelectedItem() {
			return selection;
		}

		@Override
		public void setSelectedItem(Object selection) {
			this.selection = selection;
		}

	}

	// verify the new web site
	public class Checker extends SwingWorker<Boolean, String> {

		String siteIndexURL;
		JLabel status;
		String siteName;
		String siteAddress;

		public Checker(String siteIndexURL, JLabel status, String siteName,
				String siteAddress) {
			this.siteIndexURL = siteIndexURL;
			this.status = status;
			this.siteName = siteName;
			this.siteAddress = siteAddress;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			publish("validating the new site(time out:" + READ_TIMEOUT / 1000
					+ "s)");
			try {
				URL url = new URL(siteIndexURL);
				// if the InputStream can be opened, then it's verified
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(CONN_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				// in = new BufferedInputStream(conn.getInputStream());
				// in.close();
				conn.connect();

				// write the site into sites.xml and re-parse it
				TemplateFileOperation.writeSiteXML(siteName, siteAddress);
				TemplateFileOperation.parseSiteXML(data);

				publish("has been added to the site list successfully");
				Thread.sleep(1000);
				publish("");
				return true;
			} catch (MalformedURLException e) {
				// if the URL format is invalid, shows the err msg
				publish("the URL is invalid");
				JOptionPane.showMessageDialog(parent, "Invalid URL");
				Thread.sleep(1000);
				publish("");
			} catch (IOException e) {
				// if it's unable to open the input stream, shows the err msg
				publish(CONNECTION_ERROR);
				JOptionPane.showMessageDialog(parent, CONNECTION_ERROR);
				Thread.sleep(1000);
				publish("");

			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			} finally {

			}
			return false;
		}

		@Override
		protected void process(final List<String> chunks) {
			for (final String string : chunks) {
				status.setText(string);
				status.repaint();
				if (string.equals("validating the new site(time out:"
						+ READ_TIMEOUT / 1000 + "s)")) {
					// when validating the site, disable all the buttons and
					// change the cursor
					disableAll();
				} else if (string
						.equals("has been added to the site list successfully")) {
					// refresh the site bar
					siteList.setModel(new SiteComboBoxModel());
				} else if (string.equals("")) {
					// after verification, enable all buttons
					enableAll();
				}
			}
		}

	}

	// download index.xml or templates
	public class Downloader extends SwingWorker<File, String> {

		private String type;

		private String myURL;
		private String fileName;

		private JLabel status;

		public Downloader(String myURL, String fileName, JLabel status,
				String type) {
			this.type = type;

			this.myURL = myURL;
			this.fileName = fileName;
			this.status = status;

		}

		@Override
		protected File doInBackground() throws Exception {
			File temp = null;
			// if downloading index.xml
			if (type.equals(D_TYPE_INDEX)) {
				try {
					// approximately 60s (?)
					publish("Connecting to the site (time out:" + READ_TIMEOUT
							/ 1000 + "s)");
					temp = TemplateFileOperation.download(myURL, fileName,
							CONN_TIMEOUT, READ_TIMEOUT);
					publish("index.xml is downloaded successfully");
					// is it necessary to sleep for 100ms for showing the msg
					// (?)
					data.clearSitesData();
					Thread.sleep(100);
					publish("Updating index");
					Thread.sleep(100);
					// ???????????????????????????????????
					// I want to tell user that the index is up to date,
					// but logically it's not garuanteed within 100ms
					// I tried to put it into process function, but it doesn't
					// work
					// any suggestions(?)
					publish("index is up to date");
					Thread.sleep(1000);
					publish("");
				} catch (IOException e) {
					publish(CONNECTION_ERROR);
					JOptionPane.showMessageDialog(parent,
							CONNECTION_ERROR);
					publish("");
				}
			} else if (type.equals(D_TYPE_TEMPLATE)) {
				// similar to the 'index', except for the msg
				try {
					publish("Connecting to the site (time out:" + READ_TIMEOUT
							/ 1000 + "s)");
					temp = TemplateFileOperation.download(myURL, fileName,
							CONN_TIMEOUT, READ_TIMEOUT);
//					downloadedList = intersect();
					templates = TemplateFileOperation.findTemplateFiles(TemplateConstants.TEMPLATE_FOLDER);
//					Thread.sleep(100);
					publish("template is downloaded successfully");
					Thread.sleep(100);
					publish("Updating template list");
					Thread.sleep(100);
					publish("template list is up to date");
					Thread.sleep(100);
					publish("");
				} catch (IOException e) {
					publish(CONNECTION_ERROR);
					JOptionPane.showMessageDialog(parent,
							CONNECTION_ERROR);
					publish("");
				}
			}

			return temp;
		}

		@Override
		protected void process(final List<String> chunks) {
			for (final String string : chunks) {
				// repaint the status first
				status.setText(string);
				status.repaint();
				if (string.equals("Connecting to the site (time out:"
						+ READ_TIMEOUT / 1000 + "s)")) {
					disableAll();

				} else if (string.equals("Updating index")) {
					// after downloading the index.xml successfully, it's parsed
					try {
                                                data = new AddingPanelData();
						TemplateFileOperation.parseIndexXML(indexFile, data);
						TemplateFileOperation.parseSiteXML(data);
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// refresh the table
                                        ((AvailableTreeTableModel) availableTreeTable.getTreeTableModel()).resetRoot(data.getRoot());
                                        availableTreeTable.expandPath(availableTreeTable.getPathForRow(0));
//                                        availableTreeTable.updateUI();
					siteList.setModel(new SiteComboBoxModel());
					siteList.getEditor().setItem(data.getSiteInfo());
                                        try {
                                                pagePane.setPage(TemplateAddingPanel.class.getResource(BLANK_PAGE));
                                        } catch (IOException ex) {
                                                Logger.getLogger(TemplateAddingPanel.class.getName()).log(Level.SEVERE, null, ex);
                                        }

				} else if (string.equals("template is downloaded successfully")) {
//					System.out.println(downloadedList.get(0));
					// availableTable.revalidate();
//					tablePane.getViewport().revalidate();
//					tablePane.getViewport().repaint();
//					System.out.println(downloadedList.get(0));
					
//					availableTreeTable = new AvailableTreeTable();
					availableTreeTable.revalidate();
					availableTreeTable.repaint();
				} else if (string.equals("Updating template list")) {
					// refresh the template panel (local templates)
					templatePanel.refresh();
				} else if (string
						.equals(CONNECTION_ERROR)) {

					enableAll();

				} else if (string.equals("")) {
					enableAll();

				}
			}
		}

	}

	// loading the HTML page
	public class PageLoader extends SwingWorker<Boolean, String> {

		String URLString;

		public PageLoader(String URLString) {
			this.URLString = URLString;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			try {
				// pagePane.setPage(new URL(TemplateConstants.BLANK_PAGE));

				// SwingUtilities.invokeAndWait(new Runnable() {
				//
				// @Override
				// public void run() {
				//
				// try {

				// pagePane.setPage(new URL(TemplateConstants.BLANK_PAGE));

				// status.setText("Loading the description page...");
				// status.repaint();
				//
				// } catch (MalformedURLException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
				//
				// });

				publish("Loading the description page...");

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void process(final List<String> chunks) {
			for (final String string : chunks) {
				// try{
				//
				// }catch(Exception e) {
				// e.printStackTrace();
				// }

				// status.setText(string);
				// status.repaint();

				if (string.equals("Loading the description page...")) {
					try {

						// status.setText("Loading the description page...");
						// status.repaint();

						// JEditorPane is unable to throw a FileNotFound
						// exception once it's initialized
						// So check it's reachable first
						// if (ConnectionCheck
						// .netCheck(new String[] { URLString })) {
						                                          try {
                                                        pagePane.setPage(new URL(URLString));
                                                } catch (Exception exception) {
                                                        pagePane.setPage(TemplateAddingPanel.class.getResource(NO_DESC_PAGE));
                                                }
						// repaint the page
						desPane.getViewport().revalidate();
						desPane.getViewport().repaint();
						// } else {
						// pagePane.setPage(TemplateConstants.NO_DESC_PAGE);
						// }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void disableAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		siteAdd.setEnabled(false);
		siteUpdate.setEnabled(false);
		tempDownload.setEnabled(false);
		close.setEnabled(false);
//		tempHelp.setEnabled(false);
	}

	public void enableAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		siteAdd.setEnabled(true);
		siteUpdate.setEnabled(true);
		tempDownload.setEnabled(true);
		close.setEnabled(true);
//		tempHelp.setEnabled(true);
	}

	// find all templates which are already downloaded via MD5 checksum
//	public List<String> intersect() {
//		List<String> intersect = new ArrayList<String>(data.getMD5s());
//		File[] files = TemplateFileOperation.findTemplateFiles(TEMPLATE_FOLDER);
////		System.out.println("files: "+files.length);
//		
//		try {
////			 System.out.println(MD5Checksum.getMD5List(files).get(0));
////			 System.out.println("MD5s: " + data.getMD5s());
////			 System.out.println(MD5Checksum.getMD5List(files));
//			 intersect.retainAll(MD5Checksum.getMD5List(files));
////			 System.out.println(intersect.size());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return intersect;
//	}

}
