package jmt.jmarkov.SpatialQueue.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    private GuiComponents guiComponents;
	private JPanel simulationP;

	/** Creates the dialog. */
	public SpatialQueueFrame() {
		init();
	}

	public void init(){
		setTitle("Create a new Spatial Queue");

        initObjects();

        //set window size
		Dimension d = new Dimension(1000,800);
		setPreferredSize(d);

        //Create button panel for left side of window
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		guiComponents.generateSideButtons(buttonPanel);

        //Create panel for map and queue drawer
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));
		guiComponents.generateMapPanel(interfacePanel);
		guiComponents.generateQueueDrawer(interfacePanel);
		interfacePanel.add(Box.createVerticalGlue());
        add(buttonPanel, BorderLayout.LINE_START);
        add(interfacePanel, BorderLayout.CENTER);

        //Create grid to store simulation data in
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		simulationP.setLayout(new GridBagLayout());
		this.getContentPane().add(simulationP, BorderLayout.SOUTH);

        //create Simulation Results panel
        guiComponents.createSimulationResultsPanel(c, simulationP);

        //create Simulation parameters panel
        guiComponents.createSimulationParametersPanel(c,simulationP);

        // create lamda slider
        guiComponents.createLambdaSlider(c);


		// create S slider
        guiComponents.createSSlider(c);

		// create queueBuffer slider
        guiComponents.createQueueBufferSlider(c);

        // window settings
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

    private void initObjects() {
        guiComponents = new GuiComponents();
        simulationP = new JPanel();
    }

    @Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}

	public static void main(String[] args) {
		new SpatialQueueFrame();
	}
}
