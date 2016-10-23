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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/* Dialog to contain Spatial Queue Window. */
public class SpatialQDialog extends JDialog implements ActionListener, PropertyChangeListener {

	/** Creates the dialog. */
	public SpatialQDialog(Frame aFrame) {
		super(aFrame, true);
		setTitle("Create a new Spatial Queue");

		MapViewOptions mapOptions = new MapViewOptions();
		mapOptions.importPlaces();
		final MapConfig mapView = new MapConfig(mapOptions);
		JDialog mapDialog = new JDialog();
		mapDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		mapDialog.add(mapView, BorderLayout.CENTER);
		mapDialog.setSize(700, 500);
		mapDialog.setLocationRelativeTo(null);
		mapDialog.setVisible(true);

		//Handle window closing correctly.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}
}
