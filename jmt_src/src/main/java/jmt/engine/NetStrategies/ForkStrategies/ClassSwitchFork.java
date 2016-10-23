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

package jmt.engine.NetStrategies.ForkStrategies;

import java.util.HashMap;
import java.util.Map;
import jmt.engine.NetStrategies.ForkStrategy;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeList;
import jmt.engine.QueueNet.NodeListWithJobNum;

/**
 *
 * @author mattia
 */
public class ClassSwitchFork extends ForkStrategy {

	private ClassJobNum jobsPerClass[];
	private String nodeNames[];
	private NetNode[] nodes;

	public ClassSwitchFork(ClassJobNum jobsPerClass[]) {
		this.jobsPerClass = jobsPerClass;
		nodeNames = new String[jobsPerClass.length];
		for (int i = 0; i < jobsPerClass.length; i++) {
			ClassJobNum c = jobsPerClass[i];
			nodeNames[i] = c.getName();
		}
	}

	@Override
	public NodeListWithJobNum getOutNodes(NodeList nodes, JobClass jobClass) {
		NodeListWithJobNum nl = new NodeListWithJobNum();
		if (this.nodes == null) {
			//it is the first execution of this method: find the NetNode objects
			//corresponding to the nodeNames
			findNodes();
		}
		for (int i = 0; i < jobsPerClass.length; i++) {
			ClassJobNum c = jobsPerClass[i];
			Map<JobClass, Integer> map = new HashMap<JobClass, Integer>();
			for (int j = 0; j < c.getClasses().length; j++) {
				map.put(findClass(c.getClasses()[j]), c.getNumbers()[j]);
			}
			nl.add(this.nodes[i], map);
		}
		return nl;
	}

	private boolean findNodes() {
		nodes = new NetNode[nodeNames.length];
		for (int i = 0; i < nodeNames.length; i++) {
			nodes[i] = NetSystem.getNode(nodeNames[i]);
			if (nodes[i] == null) {
				//the passed name does not correspond to a node of the model
				return false;
			}
		}
		return true;
	}

	private JobClass findClass(String name) {
		return nodes[0].getJobClasses().get(name);
	}

}
