package jmt.gui.jsimgraph.panels;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AddingSitePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField name;
	private JTextField address;
	
	public AddingSitePanel() {
		this.setLayout(new GridLayout(0,1));
		
		JLabel siteName = new JLabel("Site Name: ");
		JLabel siteAddress = new JLabel("Site Address: ");
		
		name = new JTextField();
		address = new JTextField("http://");
		
		siteName.setLabelFor(name);
		siteAddress.setLabelFor(address);
		
		this.add(siteName);
		this.add(name);
		this.add(siteAddress);
		this.add(address);
	}
	
	public String getName() {
		return name.getText();
	}
	
	public String getAddress() {
		return address.getText();
	}
	
	public String getPanelName() {
		return "New Site";
	}
}
