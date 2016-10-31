package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.MapViewOptions;
import jmt.jmarkov.Graphics.JobsDrawer;
import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Queues.MM1Logic;
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

public class SpatialQueueFrame extends JFrame implements ActionListener, PropertyChangeListener {

	private Simulator sim = null;
	private MapConfig mapView;
	private boolean paused;

	/** Creates the dialog. */

	public SpatialQueueFrame() {
		this.init();
	}

	public void init(){
		setTitle("Create a new Spatial Queue");

		paused = false;
		Dimension d = new Dimension(800,600);
		setPreferredSize(d);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		generateSideButtons(buttonPanel);
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));
		generateMapPanel(interfacePanel);
		generateQueueDrawer(interfacePanel);
		interfacePanel.add(Box.createVerticalGlue());

		add(buttonPanel, BorderLayout.LINE_START);
		add(interfacePanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void generateQueueDrawer(JPanel interfacePanel) {
		MM1Logic ql = new MM1Logic(0.0, 0.0);
		QueueDrawer queueDrawer = new QueueDrawer(ql);
		queueDrawer.setPreferredSize(new Dimension(300, 150));
		interfacePanel.add(queueDrawer);
	}

	private void generateMapPanel(JPanel interfacePanel) {
		MapViewOptions mapOptions = new MapViewOptions();
		mapOptions.importPlaces();
		mapView = new MapConfig(mapOptions);
		mapView.setPreferredSize(new Dimension(300, 375));
		interfacePanel.add(mapView);
	}

	private void generateSideButtons(JPanel panel) {
		JButton receiver;
		JButton client;
		final JButton start = new JButton("Start");
		final JButton pause = new JButton("Pause");
		final JButton stop = new JButton("Stop");

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
				mapView.toggleAreaPlacement();
			}
		});

		pause.setPreferredSize(new Dimension(100,40));
		pause.setEnabled(false);
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (paused) {
					paused = false;
//					sim.pause();
				} else {
					paused = true;
				}
			}
		});

		start.setPreferredSize(new Dimension(100,40));
		start.setEnabled(true);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				start.setEnabled(false);
				stop.setEnabled(true);
				pause.setEnabled(true);
			}
		});

		stop.setPreferredSize(new Dimension(100,40));
		stop.setEnabled(false);
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(true);
				stop.setEnabled(false);
				pause.setEnabled(false);
			}
		});

		panel.add(receiver);
		panel.add(client);
		panel.add(start);
		panel.add(pause);
		panel.add(stop);
		panel.setBorder(new EmptyBorder(50, 0, 0, 0));

		//scroll bar
		addSpeedSlider(panel);

		//number of arrivals
		addJobsPanel(panel);
	}

	private void addSpeedSlider(JPanel accelerationP) {

		DrawConstrains dCst = new DrawNormal();

		accelerationP.setBorder(addTitle("Simulation Options", dCst.getSmallGUIFont()));
		JLabel accelerationL = new JLabel("Time x0.0");
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
	}


	private void addJobsPanel(JPanel jobsP) {
		JobsDrawer jobsDrawer = new JobsDrawer();
		jobsP.add(jobsDrawer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}

	public static void main(String[] args) {
		new SpatialQueueFrame();
	}

	private TitledBorder addTitle(String title, Font f) {
		return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
	}
}
