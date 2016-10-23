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

package jmt.engine.NetStrategies.QueuePutStrategies;

import java.util.Iterator;
import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.QueuePutStrategy;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NodeSection;

/**
 * This class implements a specific queue put strategy: all arriving jobs
 * are ordered according to their priorities. Between jobs with the same
 * priority, the one that comes last is put at the head.
 * @author Stefano Omini
 */
public class HeadStrategyPriority implements QueuePutStrategy {

	public void put(Job job, JobInfoList queue, NodeSection callingSection) throws NetException {
		int priority = job.getJobClass().getPriority();
		job.setServiceTime(0.0);

		List<JobInfo> list = queue.getJobList();
		if (list.size() == 0) {
			queue.addFirst(new JobInfo(job));
			return;
		}

		int currentPriority = 0;
		Iterator<JobInfo> it = list.iterator();
		int index = -1;
		while (it.hasNext()) {
			currentPriority = it.next().getJob().getJobClass().getPriority();
			index++;
			if (currentPriority <= priority) {
				queue.add(index, new JobInfo(job), true);
				return;
			}
		}
		queue.addLast(new JobInfo(job));
	}

	public boolean check() {
		return true;
	}

}
