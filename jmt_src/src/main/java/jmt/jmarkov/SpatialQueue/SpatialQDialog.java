
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

package jmt.jmarkov.SpatialQueue;
		import com.teamdev.jxmaps.MapViewOptions;
		import jmt.jmarkov.Graphics.constants.DrawConstrains;
		import jmt.jmarkov.Graphics.constants.DrawNormal;
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

/* Dialog to contain Spatial Queue Window. */
public class SpatialQDialog extends JDialog implements ActionListener, PropertyChangeListener {

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

		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		Dimension d = new Dimension(800,600);
		window.setPreferredSize(d);

		//Side buttons
		sideButtons(window);

		// acceleration scroll bar
		speedSlider(window);

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
		final MapConfig mapView = new MapConfig(mapOptions);
		JDialog mapDialog = new JDialog();
//		map.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		map.add(mapView, BorderLayout.CENTER);
		map.setSize(230, 250);
//		map.setLocationRelativeTo(null);
		map.setVisible(true);
//		map.add(mapDialog);
//		window.add(map);
	}

	private void sideButtons(JFrame window) {
		JPanel buttons = new JPanel(new GridLayout(0, 1));
		JButton r = new JButton("Add Receiver");
		r.setPreferredSize(new Dimension(100,40));
		JButton c = new JButton("Add Client");
		c.setPreferredSize(new Dimension(100,40));
		JButton start = new JButton("Start");
		start.setPreferredSize(new Dimension(100,40));
		JButton pause = new JButton("Pause");
		pause.setPreferredSize(new Dimension(100,40));
		JButton stop = new JButton("Stop");
		stop.setPreferredSize(new Dimension(100,40));
		buttons.add(r);
		buttons.add(c);
		buttons.add(start);
		buttons.add(pause);
		buttons.add(stop);

		buttons.setBorder(new EmptyBorder(50, 0, 0, 0));


		JPanel left = new JPanel(new BorderLayout());
		left.add(buttons, BorderLayout.NORTH);
		window.add(left, BorderLayout.WEST);
	}

	private void speedSlider(JFrame window) {
		JPanel accelerationP = new JPanel(new BorderLayout());
		DrawConstrains dCst = new DrawNormal();

		accelerationP.setBorder(addTitle("Simulation time", dCst.getSmallGUIFont()));
		JLabel accelerationL = new JLabel();
		accelerationL = new JLabel("Time x0.0");
		accelerationL.setFont(dCst.getNormalGUIFont());
		accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
		accelerationP.add(accelerationL);

		JSlider accelerationS = new JSlider();
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
		accelerationS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {

			}

		});
		accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));

//		accelerationP.setSize(50,50);
		window.add(accelerationP, BorderLayout.PAGE_END);
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


	/** Creates the dialog. */
//	public SpatialQDialog(Frame aFrame) {
//		super(aFrame, true);
//		setTitle("Create a new Spatial Queue");
//
//		MapViewOptions mapOptions = new MapViewOptions();
//		mapOptions.importPlaces();
//		final MapConfig mapView = new MapConfig(mapOptions);
//		JDialog mapDialog = new JDialog();
//		mapDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		mapDialog.add(mapView, BorderLayout.CENTER);
//		mapDialog.setSize(700, 500);
//		mapDialog.setLocationRelativeTo(null);
//		mapDialog.setVisible(true);
//
//		//Handle window closing correctly.
//		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//	}
}
