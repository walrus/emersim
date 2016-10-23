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

package jmt.engine.NetStrategies.RoutingStrategies;

import jmt.engine.NetStrategies.RoutingStrategy;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeList;

/**
 * This class implements a round robin strategy: all the output nodes
 * are chosen in sequence, one at each time; when all the nodes have been chosen,
 * the sequence restarts.
 * @author Francesco Radaelli, Bertoli Marco
 * Modified by Bertoli Marco to correct behaviour with closed classes and sinks
 */
public class RoundRobinStrategy extends RoutingStrategy {

	private int CLOSED_CLASS = JobClass.CLOSED_CLASS;
	private int Counter;

	public RoundRobinStrategy() {
		Counter = 0;
	}

	/**
	 * Gets the output node, into which the job must be routed, using a round
	 * robin strategy.
	 * @param nodes the list of output nodes
	 * @param jobClass class of current job to be routed
	 * @return The selected node.
	 */
	@Override
	public NetNode getOutNode(NodeList nodes, JobClass jobClass) {
		NetNode out;
		if (nodes.size() == 0) {
			return null;
		} else {
			if (Counter == nodes.size()) {
				Counter = 0;
			}
			out = nodes.get(Counter++);

			// Skips sinks for closed classes
			if (jobClass.getType() == CLOSED_CLASS) {
				int totalLoops = 0; // Counts whenever a full loop has been performed
				while (out.isSink()) {
					if (Counter == nodes.size()) {
						Counter = 0;
						if (totalLoops++ > 0) {
							return null;
						}
					}
					out = nodes.get(Counter++);
				}
			}
			return out;
		}
	}

}
