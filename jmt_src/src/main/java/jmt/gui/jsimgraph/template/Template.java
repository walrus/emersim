package jmt.gui.jsimgraph.template;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.CommonModel;
import jmt.gui.common.panels.CustomizableDialogFactory;
import jmt.gui.common.routingStrategies.ProbabilityRouting;
import jmt.gui.common.routingStrategies.RoutingStrategy;
import jmt.gui.jsimgraph.JGraphMod.CellComponent;
import jmt.gui.jsimgraph.JGraphMod.CellFactory;
import jmt.gui.jsimgraph.JGraphMod.JmtCell;
import jmt.gui.jsimgraph.controller.JmtClipboard;
import jmt.gui.jsimgraph.controller.Mediator;
import jmt.gui.jsimgraph.definitions.JMTPoint;
import jmt.gui.jsimgraph.mainGui.MainWindow;
import jmt.gui.jsimgraph.template.ITemplate;

public class Template extends WizardPanel implements ITemplate, CommonConstants{
	
	private static final long serialVersionUID = 1L;
	
	private Mediator mediator;
	
	JSpinner number;
	
	private JmtCell fork;
	private JmtCell join;
	private JmtCell server;
	
	private JPanel parent;
	
	public Template(Mediator m) {
		mediator = m;
		parent = this;
		this.initComponents();
	}
	
	public void initComponents() {
		
		//------------------build the GUI components of your template here------------------//
		
		initCells();
		JPanel picPane = new JPanel();
		picPane.setBorder(new TitledBorder(new EtchedBorder(), "Illustration"));
		try{
			BufferedImage img = ImageIO.read(new File("/home/js/JMT-Refactory/template-sample-code/pictures/TemplateIllustration.png"));
			Image resizedImage = 
				    img.getScaledInstance(300, 200, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(resizedImage);
			
			JLabel pLabel = new JLabel(icon);
			picPane.add(pLabel);
		}catch(Exception e) {
			
		}
		JLabel label = new JLabel("Number of service center: ");
		number = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		label.setLabelFor(number);
		
		JPanel structurePane = new JPanel();
		structurePane.setBorder(new TitledBorder(new EtchedBorder(), "Structure"));
		structurePane.add(label);
		structurePane.add(number);
		
		JButton editFork = new JButton("Edit Fork");
		JButton editServer = new JButton("Edit Service Center");
		JButton editJoin = new JButton("Edit Join");
		
		editFork.setPreferredSize(new Dimension(150,25));
		editServer.setPreferredSize(new Dimension(150,25));
		editJoin.setPreferredSize(new Dimension(150,25));
		
		editFork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mediator.startEditingAtAbstractCell(fork);
			}
		});
		
		editServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mediator.startEditingAtAbstractCell(server);
			}
		});
		
		editJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mediator.startEditingAtAbstractCell(join);
			}
		});
		
		JPanel parameterPane = new JPanel();
		parameterPane.setBorder(new TitledBorder(new EtchedBorder(), "Parameter"));
		parameterPane.add(editFork);
		parameterPane.add(editServer);
		parameterPane.add(editJoin);
		
		JPanel centerPane = new JPanel(new GridLayout(2,1));
		centerPane.add(structurePane);
		centerPane.add(parameterPane);
		
		JButton create = new JButton("Insert");
		
		create.setPreferredSize(new Dimension(100,25));
		
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createModel();
				SwingUtilities.getWindowAncestor(parent).setVisible(false);
			}
		});
		
		JPanel bottoms = new JPanel();
		bottoms.add(create);
		this.setLayout(new BorderLayout());
		this.add(picPane, BorderLayout.NORTH);
		this.add(centerPane, BorderLayout.CENTER);
		this.add(bottoms, BorderLayout.SOUTH);
		
		//------------------------------------END------------------------------------------//
	}
	
	public void initCells() {
		CellFactory cf = new CellFactory(mediator);
		fork = cf.createCell("ForkCell");
		server = cf.createCell("ServerCell");
		join = cf.createCell("JoinCell");
	}
	
	
	@Override
	public CommonModel createModel() {
		
		mediator.setSelectState();
		
		double YBound = mediator.getBound();
		
		int num = (Integer)number.getValue();
		
		
		//-----------------Adding cells to mediator----------------------//
		mediator.InsertCell(new JMTPoint(50, YBound + 50 * num, false), fork);
		mediator.InsertCell(new JMTPoint(300, YBound + 50 * num, false), join);
		mediator.InsertCell(new JMTPoint(150, YBound + 50, false), server);
		
		mediator.connect(fork, server);
		mediator.connect(server, join);
		
		JmtClipboard clipboard = new JmtClipboard(mediator);
		
		for (int i = 1; i < num; i++) {
			
			clipboard.copyCell(server);
			JmtCell sub = clipboard.pasteCell(new JMTPoint(150, YBound + 50 + 100 * i, false));
			
			
			
			mediator.connect(fork, sub);
			mediator.connect(sub, join);
			
		}
		
//		
//		CellFactory cf = new CellFactory(mediator);
//		JmtCell server1 = cf.createCell("ServerCell");
//		JmtCell server2 = cf.createCell("ServerCell");
//		JmtCell server3 = cf.createCell("ServerCell");
//		JmtCell server4 = cf.createCell("ServerCell");
//		
//		JmtCell server5 = cf.createCell("ServerCell");
//		
//		mediator.InsertCell(new JMTPoint(50, YBound + 500, false), server1);
//		mediator.InsertCell(new JMTPoint(200, YBound + 700, false), server2);
//		mediator.InsertCell(new JMTPoint(200, YBound + 500, false), server3);
//		mediator.InsertCell(new JMTPoint(200, YBound + 700, false), server4);
//		
//		mediator.InsertCell(new JMTPoint(200, YBound + 900, false), server5);
//		
//		mediator.connect(server1, server3);
////		mediator.connect(server1, server4);
//		
////		mediator.connect(server1, server5);
//		
////		mediator.connect(server2, server3);
////		mediator.connect(server2, server4);
//		
////		mediator.startEditingAtAbstractCell(server1);
//		Object key = ((CellComponent) server1.getUserObject()).getKey();
//		Object clkey = mediator.getModel().getClassKeys().get(0);
////		RoutingStrategy rs = ((RoutingStrategy) mediator.getModel().getRoutingStrategy(key, clkey)).clone();
////		Object key2 = ((CellComponent) server2.getUserObject()).getKey();
////		mediator.getModel().setRoutingStrategy(key2, clkey, rs);
//		
////		RoutingStrategy ros = new ProbabilityRouting();
//		Vector<Object> output = new Vector<Object>();
//		Object test = new Long(-1);
//		output.add(test);
////		ros.getValues().put(output.get(0), 1.0);
////		mediator.getModel().getBackwardConnections(key).get(0); 
//		//= ((CellComponent) server5.getUserObject()).getKey();
////		ros.getValues().put(output.get(1), 0.9);
//		
////		mediator.getModel().setRoutingStrategy(key, clkey, ros);
////		
//		mediator.deleteCells(new Object[] {server3});
////		
////		mediator.connect(server1, server5);
////		
////		ros.getValues().put(((CellComponent) server5.getUserObject()).getKey(), ros.getValues().remove(output.get(0)));
//		
//		mediator.getModel().setFakeForwordConnections(key, output);
//		System.out.println(key);
//		System.out.println(mediator.getModel().getFakeForwardConnections(key));
//		
//		
//		mediator.startEditingAtAbstractCell(server1);
//		
//		mediator.connect(server1, server2);
//		RoutingStrategy rs = ((RoutingStrategy) mediator.getModel().getRoutingStrategy(key, clkey));
//		Object key2 = ((CellComponent) server2.getUserObject()).getKey();
//		rs.getValues().put(key2, rs.getValues().remove(test));
////		mediator.getModel().setRoutingStrategy(key2, clkey, rs);
//		
		
		
		//--------------------------END---------------------------------//
	
		CommonModel cm = mediator.getModel();
		return cm;
	}
	
	@Override
	public void showDialog(MainWindow mainWindow) {
		CustomizableDialogFactory templateDialogFactory = new CustomizableDialogFactory(mainWindow);
		//change size/name of your template dialog here
		//width, height, ..., title
		templateDialogFactory.getDialog(500, 420, this, "Parallel Model Template");
	}
	
	//change the name of your template
	@Override
	public String getName() {
		return "Parallel Model Template";
	}
	
	public static void main(String[] args) {
		
	}
	
}
