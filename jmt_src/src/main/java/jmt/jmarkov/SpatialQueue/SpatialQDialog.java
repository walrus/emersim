package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.jmarkov.Graphics.JobsDrawer;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Simulator;
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
public class SpatialQDialog extends JDialog implements ActionListener, PropertyChangeListener {


	private Simulator sim = null;
	private JobsDrawer jobsDrawer;
	private DrawConstrains dCst = new DrawNormal();
	private JPanel buttons;
	private JButton receiver;
	private JButton client;
	private JButton start;
	private JButton pause;
	private JButton stop;
	private JPanel leftPanel;
	private JFrame window;
	private MapConfig mapView;


	/** Creates the dialog. */
	public SpatialQDialog(Frame aFrame) {
		super(aFrame, true);
		this.init();
	}

	public SpatialQDialog() {
		this.init();
	}

	public void init(){
		setTitle("Create a new Spatial Queue");

		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		Dimension d = new Dimension(800,600);
		window.setPreferredSize(d);

		buttons = new JPanel(new GridLayout(0, 1));

		//Side buttons
		sideButtons(window);

		//maps
		maps(window);

		//Handle window closing correctly.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	private void maps(JFrame window) {
		JPanel map = new JPanel(new BorderLayout());
		window.add(map);
		MapViewOptions mapOptions = new MapViewOptions();
		mapOptions.importPlaces();
		mapView = new MapConfig(mapOptions);
		map.add(mapView, BorderLayout.CENTER);
		map.setSize(150, 150);
		map.setVisible(true);
	}

	private void sideButtons(JFrame window) {

		receiver = new JButton("Add Receiver");
		receiver.setPreferredSize(new Dimension(100,40));
		receiver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapView.toggleMarkerPlacement();
			}
		});

		client = new JButton("Add Client");
		client.setPreferredSize(new Dimension(100,40));
		client.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		start = new JButton("Start");
		start.setPreferredSize(new Dimension(100,40));
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {

			}
		});


		pause = new JButton("Pause");
		pause.setPreferredSize(new Dimension(100,40));
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		stop = new JButton("Stop");
		stop.setPreferredSize(new Dimension(100,40));
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});


		buttons.add(receiver);
		buttons.add(client);
		buttons.add(start);
		buttons.add(pause);
		buttons.add(stop);

		buttons.setBorder(new EmptyBorder(50, 0, 0, 0));

		//scroll bar
		buttons = speedSlider(buttons);

		//number of arrivals
		buttons = jobsPanel(buttons);

		leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(buttons, BorderLayout.NORTH);
		window.add(leftPanel, BorderLayout.WEST);
	}

	private JPanel speedSlider(JPanel accelerationP) {

		DrawConstrains dCst = new DrawNormal();

		accelerationP.setBorder(addTitle("Simulation Options", dCst.getSmallGUIFont()));
		JLabel accelerationL = new JLabel();
		accelerationL = new JLabel("Time x0.0");
		accelerationL.setFont(dCst.getNormalGUIFont());
		accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
		accelerationP.add(accelerationL);

		final JSlider accelerationS = new JSlider();
		accelerationS.setValue(50);
		accelerationS.setMaximum(100);
		accelerationS.setMinimum(1);
		accelerationS.setMajorTickSpacing(50);
		accelerationS.setMinorTickSpacing(1);
		accelerationS.setSnapToTicks(true);
		accelerationS.setPaintTicks(true);
		accelerationS.setPaintLabels(true);
		Dictionary<Integer, JLabel> ad = accelerationS.getLabelTable();
		ad.keys();
		ad.put(new Integer(1), new JLabel("real time"));
		ad.put(new Integer(51), new JLabel("faster"));
		ad.put(new Integer(100), new JLabel("fastest"));
		accelerationS.setLabelTable(ad);

		accelerationP.add(accelerationS);
		accelerationS.setValue(50);
		final JLabel finalAccelerationL = accelerationL;
		accelerationS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				int value = accelerationS.getValue();
				if (sim != null) {
					sim.setTimeMultiplier(value);
					finalAccelerationL.setText("Time x" + Formatter.formatNumber(sim.getTimeMultiplier(), 2));
				} else {
					finalAccelerationL.setText("Time x" + Formatter.formatNumber(value, 2));
				}
			}

		});
		accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));

		return accelerationP;
	}


	private JPanel jobsPanel(JPanel jobsP) {
		jobsDrawer = new JobsDrawer();
		jobsP.add(jobsDrawer);
		return jobsP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}

	public static void main(String[] args) {
		new SpatialQDialog();
	}

	private TitledBorder addTitle(String title, Font f) {
		return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
	}
}
