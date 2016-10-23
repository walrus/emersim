/**
 * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.engine.NodeSections;

import java.util.Arrays;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.TransitionStrategies.TransitionVector;
import jmt.engine.QueueNet.DataPacket;
import jmt.engine.QueueNet.GlobalJobInfoList;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;

/**
 * <p>Title: Firing</p>
 * <p>Description: This class implements the firing section.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class Firing extends OutputSection {

	private TransitionVector[] firingOutcomes;
	private JobInfoList nodeJobsList;
	private GlobalJobInfoList netJobsList;
	private int[] releasedJobsPerClass;
	private int producedJobs;
	private int[] producedJobsPerClass;

	/**
	 * Creates a new instance of the firing section.
	 */
	public Firing(TransitionVector[] firingOutcomes) {
		super(true);
		this.firingOutcomes = firingOutcomes;
	}

	@Override
	protected void nodeLinked(NetNode node) {
		nodeJobsList = node.getJobInfoList();
		JobClassList jobClasses = getJobClasses();
		releasedJobsPerClass = new int[jobClasses.size()];
		Arrays.fill(releasedJobsPerClass, 0);
		for (int i = 0; i < releasedJobsPerClass.length; i++) {
			for (String nodeName : firingOutcomes[i].keySet()) {
				releasedJobsPerClass[i] += firingOutcomes[i].getValue(nodeName).intValue();
			}
		}
		producedJobs = 0;
		producedJobsPerClass = new int[jobClasses.size()];
		Arrays.fill(producedJobsPerClass, 0);
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		Object data = message.getData();

		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
		{
			netJobsList = getOwnerNode().getQueueNet().getJobInfoList();
			break;
		}

		case NetEvent.EVENT_JOB:
			return MSG_NOT_PROCESSED;

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_FIRING:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			double totalLifetime = ((Double) ((DataPacket) data).getData(DataPacket.FIELD_TOTAL_LIFETIME)).doubleValue();
			int classIndex = jobClass.getId();
			double avgEnteringTime = NetSystem.getTime() - totalLifetime / releasedJobsPerClass[classIndex];
			for (String nodeName : firingOutcomes[classIndex].keySet()) {
				int releasedJobs = firingOutcomes[classIndex].getValue(nodeName).intValue();
				DataPacket packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(releasedJobs));
				send(NetEvent.EVENT_JOB_RELEASE, packet, 0.0, NodeSection.INPUT, NetSystem.getNode(nodeName));

				for (int i = 0; i < releasedJobs; i++) {
					Job job = new Job(jobClass);
					job.setSystemEnteringTime(avgEnteringTime);
					JobInfo jobInfo = new JobInfo(job);
					jobsList.produceJob(jobInfo);
					nodeJobsList.produceJob(jobInfo);
					netJobsList.produceJob(job);
					send(job, 0.0, NetSystem.getNode(nodeName));
				}

				packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(releasedJobs));
				send(NetEvent.EVENT_JOB_FINISH, packet, 0.0, NodeSection.INPUT, NetSystem.getNode(nodeName));
				producedJobs += releasedJobs;
				producedJobsPerClass[classIndex] += releasedJobs;
			}
			break;
		}

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

	/**
	 * Gets the total number of produced jobs
	 */
	public int getProducedJobs() {
		return producedJobs;
	}

	/**
	 * Gets the number of produced jobs for the specified class
	 */
	public int getProducedJobsPerClass(int jobClass) {
		return producedJobsPerClass[jobClass];
	}

}
