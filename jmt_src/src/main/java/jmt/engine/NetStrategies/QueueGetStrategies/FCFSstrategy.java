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

package jmt.engine.NetStrategies.QueueGetStrategies;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.QueueGetStrategy;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobInfoList;

/**
 * This class implements a FCFS strategy (First Come First Served).
 * @author Francesco Radaelli
 */
public class FCFSstrategy implements QueueGetStrategy {

	/** Implements the FCFS strategy.
	 * @param queue Job queue.
	 * @return First job in the queue.
	 * @throws NetException
	 */
	public Job get(JobInfoList queue) throws NetException {
		return queue.removeFirst().getJob();
	}

	/** Implements the FCFS strategy.
	 * @param queue Job queue.
	 * @param jobClass Job class.
	 * @return First job of specified class in the queue.
	 * @throws NetException
	 */
	public Job get(JobInfoList queue, JobClass jobClass) throws NetException {
		return queue.removeFirst(jobClass).getJob();
	}

	/* (non-Javadoc)
	 * @see jmt.common.AutoCheck#check()
	 */
	public boolean check() {
		return true;
	}

}
