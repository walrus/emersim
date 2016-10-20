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
package jmt.framework.gui.graph;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import jmt.engine.math.SampleStatistics;
import jmt.engine.math.SampleStatistics.DistributionType;
import jmt.gui.common.editors.StatisticalOutputsWindow;

import org.freehep.graphicsbase.util.export.ExportDialog;

// Hacked cut+paste from FastGraph class...very bad...

/**
 * @author Samarth Shukla, Aneish Goel
 *
 */
public class DistributionDensityGraph extends JPanel {
	private static final long serialVersionUID = 1L;

	private double[] values;

	private double maxx;
	private double minx;
	private double intwidth;
	private int type;

	private int x0;

	private int y0; 
	private double xstep;

	private double ystep; 
	private static final int MARGIN = 8;

	private static final Color COLOR_DRAW = Color.BLUE;
	private static final Color COLOR_AXIS = Color.BLACK;
	private static final Color COLOR_BG = Color.WHITE;

	protected PlotPopupMenu popup = new PlotPopupMenu();

	// Used to format numbers. Formatters are not thread safe, so they should never be static.
	private DecimalFormat decimalFormat2 = new DecimalFormat("#0.00");
	private DecimalFormat decimalFormat3 = new DecimalFormat("#0.0");
	private DecimalFormat decimalFormat4 = new DecimalFormat("#00 ");




	public DistributionDensityGraph(int type, SampleStatistics stat) {
		DistributionType distrType;
		if (type == StatisticalOutputsWindow.TYPE_DISTRIBUTION) {
			distrType = DistributionType.CUMULATIVE;
		} else {
			distrType = DistributionType.FREQUENCY;
		}
		values = stat.getDistribution(distrType);
		maxx = stat.getIntervalMax();
		minx = stat.getIntervalMin();
		intwidth = stat.getIntervalWidth();
		this.type = type;
	}

	public void paint(Graphics g) {
		super.paint(g);
		int height = this.getHeight();
		int width = this.getWidth();

		// Draw graph area
		g.setColor(COLOR_BG);
		g.fillRect(MARGIN / 2, MARGIN / 2, width - MARGIN, height - MARGIN);

		// Aborts drawing if no elements are present
		if (values.length < 1) {
			return;
		}

		// Aborts graph drawing if width is too small...
		if (width < 80) {
			return;
		}

		// Find the range for x

		double xRange = maxx-minx;

		String xLabel="";


		// Find maximum value for y
		double maxy = 0;
		for (int i = 0; i < values.length; i++) {
			double currenty = values[i];
			if (currenty > maxy && !Double.isInfinite(currenty)) {
				maxy = currenty;
			}
		}

		// Correct zero maxy value, to avoid division per zero in ystep
		if (maxy == 0) {
			maxy = 1;
		}

		//Get text bounds
		FontMetrics metric = g.getFontMetrics();
		Rectangle2D xtextBound = metric.getStringBounds("XXXX", g);
		Rectangle2D ytextBound = metric.getStringBounds((formatNumber(maxy)), g);
		Rectangle2D xLabelBound = metric.getStringBounds(xLabel, g);

		// Find initial position
		x0 = (int) Math.ceil(ytextBound.getWidth()) + 2 + MARGIN;
		y0 = height - (int) Math.ceil(xtextBound.getHeight()) - 12 - MARGIN;

		xstep = (width - x0 - MARGIN - xtextBound.getWidth() / 2) / xRange;
		ystep = (y0 - MARGIN) / maxy;

		// Draws axis and captions
		g.setColor(COLOR_AXIS);
		// Y axis
		g.drawLine(x0, y0, x0, getY(maxy));
		int halfHeight = (int) Math.floor(ytextBound.getHeight() / 2);
		int yNum = (int) Math.floor((y0 - getY(maxy)) / (ytextBound.getHeight() + 2));
		// Draws caption for y axis
		for (int i = 0; i <= yNum; i++) {
			g.drawLine(x0, getY(maxy / yNum * i), x0 - 2, getY(maxy / yNum * i));
			g.drawString(decimalFormat2.format(maxy / yNum * i), MARGIN, getY(maxy / yNum * i) + halfHeight);
		}
		g.setColor(COLOR_AXIS);

		// X axis
		g.drawLine(x0, y0, getX(maxx), y0);

		double maxAvailableXWidth = width - x0 - MARGIN - xtextBound.getWidth() / 2;
		int xNum = (int) Math.floor(maxAvailableXWidth / (xtextBound.getWidth() + 12));
		// Draws caption for x axis
		for (int i = 0; i <= xNum; i++) {
			String label = formatNumber(minx + xRange / xNum * i);
			int halfWidth = (int) Math.floor(metric.getStringBounds(label,g).getWidth() / 2);
			g.drawLine(getX(minx + xRange / xNum * i), y0,getX(minx + xRange / xNum * i) ,y0+2 );
			g.drawString(label,getX(minx + xRange / xNum * i) - halfWidth, height - MARGIN - 12);
		}
		// Draws measure unit on X axis
		g.drawString(xLabel, width - (int)xLabelBound.getWidth() - MARGIN/2 - 1, height - MARGIN/2 - 1);

		// Draw chart series

		switch (type) {
		case StatisticalOutputsWindow.TYPE_DENSITY_LINE:
			for (int i = 0; i < values.length -1 ; i++) {
				double yValue = values[i];
				double nextYValue = values[i+1];
				double xValue = getXValue(i);
				double nextXValue = getXValue(i+1);
				//draw	
				g.setColor(COLOR_DRAW);
				g.drawLine(getX(xValue),getY(yValue),getX(nextXValue),getY(nextYValue));
			}
			break;

		case StatisticalOutputsWindow.TYPE_HISTOGRAM:
			for (int i = 0; i < values.length ; i++) {
				double yValue = values[i];
				double xValue = getXValue(i);
				// draw
				g.setColor(COLOR_DRAW);
				g.drawLine(getX(xValue),getY(yValue),getX(xValue + intwidth),getY(yValue));
				g.drawLine(getX(xValue),getY(yValue),getX(xValue),y0);
				g.drawLine(getX(xValue+ intwidth),getY(yValue),getX(xValue+ intwidth),y0);
			}
			break;
		case StatisticalOutputsWindow.TYPE_DISTRIBUTION:
			for (int i = 0; i < values.length -1 ; i++) {
				double yValue = values[i];
				double nextYValue = values[i+1];
				double xValue = getXValue(i);
				double nextXValue = getXValue(i+1);
				//draw	
				g.setColor(COLOR_DRAW);
				g.drawLine(getX(xValue),getY(yValue),getX(nextXValue),getY(nextYValue));
			}
			break;	
		}

	}

	/**
	 * Returns X coordinate for the screen of a point, given its value
	 * @param value value of point X
	 * @return X coordinate on the screen
	 */
	private int getX(double value) {
		return (int) Math.round(x0 + (value-minx) * xstep);
	}

	/**
	 * Returns Y coordinate for the screen of a point, given its value
	 * @param value value of point Y
	 * @return Y coordinate on the screen
	 */
	private int getY(double value) {
		return (int) Math.round(y0 - value * ystep);
	}

	/**
	 * Reads the X value for the chart
	 * @param value the measure value variable
	 * @param index the index
	 * @return the X value
	 */
	private double getXValue(int index) {
		switch (type) {
		case StatisticalOutputsWindow.TYPE_DENSITY_LINE:
			return minx + intwidth*(index+0.5);
		case StatisticalOutputsWindow.TYPE_HISTOGRAM:
			return minx + intwidth*(index);
		case StatisticalOutputsWindow.TYPE_DISTRIBUTION:
			return minx + intwidth*(index+0.5);
		default :
			return minx + intwidth*(index+0.5);
		}
	}


	/**
	 * Formats a number to string to be shown as label of the graph
	 * @param value number to be converted
	 * @return value converted into string
	 */
	private String formatNumber(double value) {
		if (value == 0) {
			return "0.00";
		} 
		else if (value < 10) {
			return decimalFormat2.format(value);
		} 
		else if (value < 100) {
			return decimalFormat3.format(value);
		}
		else  {
			return decimalFormat4.format(value);
		} 
	}



	private void rightClick(MouseEvent ev) {
		popup.show(this, ev.getX(), ev.getY());
	}

	protected class PlotPopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		public JMenuItem saveAs;
		public PlotPopupMenu() {
			saveAs = new JMenuItem("Save as...");
			this.add(saveAs);
			addListeners();
		}
		public void addListeners() {
			saveAs.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					ExportDialog export = new ExportDialog();
					export.showExportDialog(DistributionDensityGraph.this,"Export view as ...", DistributionDensityGraph.this,"Export");
				}});

		}

	} 

	public void mouseClicked(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			rightClick(ev);
		} else {

		}
	}


}

