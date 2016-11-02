package jmt.jmarkov.SpatialQueue.gui;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.gui.common.CommonConstants;
import jmt.jmarkov.Graphics.*;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.Arrivals;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.Queues.Processor;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.utils.Formatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;

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

/* Dialog to contain Spatial Queue Window. */

public class SpatialQueueFrame extends JFrame implements ActionListener, PropertyChangeListener {

    private jmt.jmarkov.SpatialQueue.Simulator sim;
	private MapConfig mapView;
    private GuiComponents guiComponents;






	private Dimension initSize = new Dimension(CommonConstants.MAX_GUI_WIDTH_JMCH, CommonConstants.MAX_GUI_HEIGHT_JMCH);








	private JobsDrawer jobsDrawer;
	private JTabbedPane outputTabP;
	private JScrollPane txtScroll;

	private LogFile logFile;
	private Notifier[] tan = new Notifier[5];

	private JPanel buttonsP;
	private JPanel resultsP;
	public JFrame mf;
	private JPanel outputP;
	private JPanel parametersP;
	private JPanel simulationP;

	private JPanel accelerationP;
	private JPanel jobsP;
	private JSlider accelerationS;

	// Label & Label strings


	private JLabel mediaJobsL;
	private JLabel utilizationL;

	private JLabel thrL;







	// Settings
	private DrawConstrains dCst = new DrawNormal();




	Arrivals arrival;
	Processor[] processors;





	/** Creates the dialog. */

	public SpatialQueueFrame() {
		this.init();
	}

	public void init(){
		setTitle("Create a new Spatial Queue");

        initObjects();

		Dimension d = new Dimension(1000,800);
		setPreferredSize(d);
        
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		guiComponents.generateSideButtons(buttonPanel);
        
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));
		guiComponents.generateMapPanel(interfacePanel);
		guiComponents.generateQueueDrawer(interfacePanel);
		interfacePanel.add(Box.createVerticalGlue());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
        
		simulationP.setLayout(new GridBagLayout());
		this.getContentPane().add(simulationP, BorderLayout.SOUTH);


		resultsP.setLayout(new GridLayout(2, 2));
		resultsP.setBorder(guiComponents.addTitle("Simulation Results", dCst.getSmallGUIFont()));
		c.gridx = 0;
		c.gridy = 1;
		simulationP.add(resultsP, c);
		StatsUtils.generateSimulationStats(resultsP, mediaJobsL, utilizationL, dCst);

		parametersP.setLayout(new GridBagLayout());
		parametersP.setBorder(guiComponents.addTitle("Simulation Parameters", dCst.getSmallGUIFont()));
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		simulationP.add(parametersP, c);

//lambda
        guiComponents.createLambdaSlider(c, parametersP, utilizationL, mediaJobsL);


		// S slider
        guiComponents.createSSlider(c, parametersP, utilizationL, mediaJobsL);

		// queueBuffer slider
        guiComponents.createQueueBufferSlider(c, parametersP, utilizationL, mediaJobsL);

		add(buttonPanel, BorderLayout.LINE_START);
		add(interfacePanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}




    private void initObjects() {
        guiComponents = new GuiComponents(sim, mapView);
        simulationP = new JPanel();
        parametersP = new JPanel();
        resultsP = new JPanel();
        mediaJobsL = new JLabel();
        utilizationL = new JLabel();
        thrL = new JLabel();
        outputP = new JPanel();
        outputTabP = new JTabbedPane();
        txtScroll = new JScrollPane();
        logFile = new LogFile();
        jobsDrawer = new JobsDrawer();
        accelerationP = new JPanel();
        jobsP = new JPanel();
        accelerationS = new JSlider();
    }

    @Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}

	public static void main(String[] args) {
		new SpatialQueueFrame();
	}
}
