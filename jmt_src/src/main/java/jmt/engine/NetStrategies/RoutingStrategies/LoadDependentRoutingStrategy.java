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

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.RoutingStrategy;
import jmt.engine.QueueNet.*;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Ashanka
 * Date: 9/4/11
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDependentRoutingStrategy extends RoutingStrategy {

	private LoadDependentRoutingParameter[] parameters;

	public LoadDependentRoutingStrategy(LoadDependentRoutingParameter[] params) {
		Arrays.sort(params);
		this.parameters = params;
	}

	/**
	 * This method should be overridden to implement a specific strategy.
	 *
	 * @param nodes    List of nodes.
	 * @param jobClass class of current job to be routed
	 * @return Selected node.
	 */
	@Override
	public NetNode getOutNode(NodeList nodes, JobClass jobClass) {
		return nodes.get(1);  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public NetNode getOutNode(NodeSection section, JobClass jobClass) {
		NetNode outNode = null;
		try {
			int jobs = section.getOwnerNode().getIntNodeProperty(NetNode.PROPERTY_ID_RESIDENT_JOBS,jobClass);
			int index = Arrays.binarySearch(parameters, new Integer(jobs));
			if (index < 0) {
				index = -index - 2;
			}
			outNode = parameters[index].getOutNode();
		}catch (NetException ne) {

		}
		return outNode;
	}

}
