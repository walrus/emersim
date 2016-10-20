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

package jmt.gui.common.distributions;

import javax.swing.ImageIcon;

import jmt.gui.common.JMTImageLoader;

/**
 * <p>Title: Erlang distribution</p>
 * <p>Description: Erlang distribution data structure</p>
 * 
 * @author Bertoli Marco
 *         Date: 6-lug-2005
 *         Time: 14.40.47
 */
public class Erlang extends Distribution {
	/**
	 * Construct a new Erlang Distribution
	 */
	public Erlang() {
		super("Erlang", "jmt.engine.random.Erlang", "jmt.engine.random.ErlangPar", "Erlang distribution");
		hasMean = true;
		hasC = true;
		isNestable = true;
	}

	/**
	 * Used to set parameters of this distribution.
	 * @return distribution parameters
	 */
	@Override
	protected Parameter[] setParameters() {
		// Creates parameter array
		Parameter[] parameters = new Parameter[2];
		// Sets parameter lambda
		//ARIF: Left parameters name as alpha and r in order to have back-compatibility of the XML files.
		parameters[0] = new Parameter("alpha", "\u03BB", Double.class, new Double(0.8));
		// Checks value of lambda must greater then 0
		parameters[0].setValueChecker(new ValueChecker() {
			public boolean checkValue(Object value) {
				Double d = (Double) value;
				if (d.doubleValue() > 0) {
					return true;
				} else {
					return false;
				}
			}
		});

		// Sets parameter k
		//ARIF: Left parameters name as alpha and r in order to have back-compatibility of the XML files.
		parameters[1] = new Parameter("r", "k", Long.class, new Long(4));
		// Checks value of k must be greater then 0
		parameters[1].setValueChecker(new ValueChecker() {
			public boolean checkValue(Object value) {
				Long d = (Long) value;
				if (d.longValue() > 0) {
					return true;
				} else {
					return false;
				}
			}
		});

		return parameters;
	}

	/**
	 * Set illustrating figure in distribution panel
	 * @return illustrating figure
	 */
	@Override
	protected ImageIcon setImage() {
		return JMTImageLoader.loadImage("Erlang");
	}

	/**
	 * Returns this distribution's short description
	 * @return distribution's short description
	 */
	@Override
	public String toString() {
		return "erl(" + FormatNumber(((Double) parameters[0].getValue()).doubleValue()) + "; "
				+ FormatNumber(((Long) parameters[1].getValue()).longValue()) + ")";
	}

	/**
	 * Sets the mean for this distribution
	 * @param value mean value
	 */
	@Override
	public void setMean(double value) {
		setCM(value, c);
	}

	/**
	 * Sets the variation coefficient C for this distribution
	 * @param value variation coefficient C value
	 */
	@Override
	public void setC(double value) {
		setCM(mean, value);
	}

	/**
	 * Sets Mean and C values
	 * @param mean mean value
	 * @param c c value
	 */
	protected void setCM(double mean, double c) {
		// lambda = 1 / (c*c*mean) && k = 1 / (c*c)
		// Backups old parameters to restore them upon a false result
		Object oldr = getParameter("r").getValue();
		Object olda = getParameter("alpha").getValue();
		
		Long k = new Long(Math.round(new Double(1 / (c * c))));
		c = 1 / Math.sqrt((k).doubleValue());
		Double lambda = new Double(k/mean);
		
		
		if (getParameter("r").setValue(k) && getParameter("alpha").setValue(lambda)) {
			this.mean = mean;
			this.c = c;
		} else {
			getParameter("r").setValue(oldr);
			getParameter("alpha").setValue(olda);
		}
	}

	/**
	 * This method is called whenever a parameter changes and <code>hasC</code> or
	 * <code>hasMean</code> are true
	 */
	@Override
	public void updateCM() {
		mean = ((Long) getParameter("r").getValue()).longValue() / ((Double) getParameter("alpha").getValue()).doubleValue();
		c = 1 / Math.sqrt(((Long) getParameter("r").getValue()).doubleValue());
	}
}
