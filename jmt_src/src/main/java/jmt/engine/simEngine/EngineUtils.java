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

package jmt.engine.simEngine;

import jmt.engine.QueueNet.SimConstants;

/**
 * Utility class used by the simulation engine
 * @author Marco Bertoli
 *
 */
public abstract class EngineUtils {

	/**
	 * Returns true if a measure is inverse or false otherwise
	 * @param measureType the type of measure
	 * @return true if it is inverse, false otherwise.
	 */
	public static boolean isInverseMeasure(int measureType) {
		return measureType == SimConstants.THROUGHPUT
				|| measureType == SimConstants.SYSTEM_THROUGHPUT
				|| measureType == SimConstants.DROP_RATE
				|| measureType == SimConstants.SYSTEM_DROP_RATE
				|| measureType == SimConstants.SYSTEM_POWER
				|| measureType == SimConstants.THROUGHPUT_PER_SINK;
	}

}
