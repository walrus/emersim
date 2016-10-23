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

package jmt.gui.common.forkStrategies;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CombFork extends ForkStrategy {
	private Map<Object, Double> outProbabilities = new HashMap<Object, Double>();

	@Override
	public String getName() {
		description = "The tasks will be generated in a subset of the output links "
				+ "(one on each output link of the subset). The dimension of the "
				+ "subset is defined by a probability distribution, the output links "
				+ "that will form the subset are selected randomly.";
		return "Random Subset";
	}

	@Override
	public Map<Object, Double> getOutDetails() {
		return outProbabilities;
	}

	@Override
	public ForkStrategy clone() {
		CombFork cf = new CombFork();
		cf.outProbabilities = new HashMap<Object, Double>(outProbabilities);
		return cf;
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.ForkStrategies.CombFork";
	}

	@Override
	public boolean isModelStateDependent() {
		return false;
	}

	@Override
	public void removeStation(Object stationKey) {
		outProbabilities.remove(Integer.toString(outProbabilities.size()));
	}

	@Override
	public void addStation(String stationName, Object stationKey, Object classKey, Vector<Object> classKeys) {
		outProbabilities.put(Integer.toString(outProbabilities.size() + 1), new Double(0.0));
	}

}
