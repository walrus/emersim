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
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;

/**
 * <p>Title: Enabling</p>
 * <p>Description: This class implements the enabling section.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class Enabling extends InputSection {

	private TransitionVector[] enablingConditions;
	private TransitionVector[] inhibitingConditions;
	private TransitionVector[] inputSituations;
	private GlobalJobInfoList netJobsList;
	private int[] enablingDegrees;
	private int unfinishedRequests;
	private double totalLifetime;
	private int consumedJobs;
	private int[] consumedJobsPerClass;

	/**
	 * Creates a new instance of the enabling section.
	 */
	public Enabling(TransitionVector[] enablingConditions, TransitionVector[] inhibitingConditions) {
		super(true);
		this.enablingConditions = enablingConditions;
		this.inhibitingConditions = inhibitingConditions;
	}

	@Override
	protected void nodeLinked(NetNode node) {
		JobClassList jobClasses = getJobClasses();
		inputSituations = new TransitionVector[jobClasses.size()];
		for (int i = 0; i < inputSituations.length; i++) {
			inputSituations[i] = new TransitionVector();
			for (String nodeName : enablingConditions[i].keySet()) {
				inputSituations[i].setValue(nodeName, Integer.valueOf(0));
			}
		}
		enablingDegrees = new int[jobClasses.size()];
		Arrays.fill(enablingDegrees, 0);
		unfinishedRequests = 0;
		totalLifetime = 0.0;
		consumedJobs = 0;
		consumedJobsPerClass = new int[jobClasses.size()];
		Arrays.fill(consumedJobsPerClass, 0);
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		Object data = message.getData();
		Job job = message.getJob();
		NetNode source = message.getSource();
		byte sourceSection = message.getSourceSection();

		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
		{
			netJobsList = getOwnerNode().getQueueNet().getJobInfoList();
			break;
		}

		case NetEvent.EVENT_JOB:
		{
			totalLifetime += NetSystem.getTime() - job.getSystemEnteringTime();
			sendAckAfterConsume(job, 0.0, sourceSection, source);
			netJobsList.consumeJob(job);
			break;
		}

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_JOB_CHANGE:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			int presentJobs = ((Integer) ((DataPacket) data).getData(DataPacket.FIELD_JOB_NUMBER)).intValue();
			int classIndex = jobClass.getId();
			inputSituations[classIndex].setValue(source.getName(), Integer.valueOf(presentJobs));
			int enablingDegree = -1;
			for (String name : inputSituations[classIndex].keySet()) {
				int availableJobs = inputSituations[classIndex].getValue(name).intValue();
				int requiredJobsForEnabling = enablingConditions[classIndex].getValue(name).intValue();
				int requiredJobsForInhibiting = inhibitingConditions[classIndex].getValue(name).intValue();
				if (requiredJobsForEnabling > 0) {
					if (enablingDegree < 0) {
						enablingDegree = availableJobs / requiredJobsForEnabling;
					} else {
						enablingDegree = Math.min(enablingDegree, availableJobs / requiredJobsForEnabling);
					}
				}
				if (requiredJobsForInhibiting > 0 && availableJobs >= requiredJobsForInhibiting) {
					enablingDegree = 0;
					break;
				}
			}

			if (enablingDegree >= 0 && enablingDegree != enablingDegrees[classIndex]) {
				DataPacket packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_ENABLING_DEGREE, enablingDegree);
				sendForward(NetEvent.EVENT_ENABLING, packet, 0.0);
				enablingDegrees[classIndex] = enablingDegree;
			}
			break;
		}

		case NetEvent.EVENT_FIRING:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			int classIndex = jobClass.getId();
			if (enablingDegrees[classIndex] <= 0) {
				return MSG_NOT_PROCESSED;
			}
			enablingDegrees[classIndex]--;

			for (String nodeName : enablingConditions[classIndex].keySet()) {
				int requiredJobs = enablingConditions[classIndex].getValue(nodeName).intValue();
				if (requiredJobs > 0) {
					DataPacket packet = new DataPacket();
					packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
					packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(requiredJobs));
					send(NetEvent.EVENT_JOB_REQUEST, packet, 0.0, NodeSection.OUTPUT, NetSystem.getNode(nodeName));
					unfinishedRequests++;
				}
			}
			break;
		}

		case NetEvent.EVENT_JOB_FINISH:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			int requiredJobs = ((Integer) ((DataPacket) data).getData(DataPacket.FIELD_JOB_NUMBER)).intValue();
			int classIndex = jobClass.getId();
			if (unfinishedRequests <= 0) {
				return MSG_NOT_PROCESSED;
			}
			unfinishedRequests--;

			if (unfinishedRequests == 0) {
				DataPacket packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_TOTAL_LIFETIME, Double.valueOf(totalLifetime));
				sendForward(NetEvent.EVENT_FIRING, packet, 0.0);
				totalLifetime = 0.0;
			}
			consumedJobs += requiredJobs;
			consumedJobsPerClass[classIndex] += requiredJobs;
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
	 * Gets the total number of consumed jobs
	 */
	public int getConsumedJobs() {
		return consumedJobs;
	}

	/**
	 * Gets the number of consumed jobs for the specified class
	 */
	public int getConsumedJobsPerClass(int jobClass) {
		return consumedJobsPerClass[jobClass];
	}

}
