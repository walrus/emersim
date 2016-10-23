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
import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.QueueGetStrategy;
import jmt.engine.NetStrategies.QueuePutStrategy;
import jmt.engine.QueueNet.BlockingRegion;
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
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.random.engine.RandomEngine;

/**
 * <p>Title: Store</p>
 * <p>Description: This class implements the store section.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class Store extends InputSection {

	public static final String FINITE_DROP = "drop";
	public static final String FINITE_BLOCK = "BAS blocking";
	public static final String FINITE_WAITING = "waiting queue";

	private int capacity;
	private boolean[] drop;
	private boolean[] block;
	private QueueGetStrategy getStrategy;
	private QueuePutStrategy putStrategies[];

	private JobClassList jobClasses;
	private JobInfoList nodeJobsList;
	private GlobalJobInfoList netJobsList;
	private boolean waitingRelease;
	private boolean acceptedRelease;
	private int droppedJobs;
	private int[] droppedJobsPerClass;

	//--------------------BLOCKING REGION PROPERTIES--------------------//
	//true if the redirection behaviour is turned on
	private boolean redirectionON;
	//the blocking region that the owner node of this section belongs to
	private BlockingRegion myRegion;
	//the input station of the blocking region
	private NetNode regionInputStation;
	//------------------------------------------------------------------//

	/**
	 * Creates a new instance of the store section.
	 */
	public Store(Integer capacity, String[] dropRules, QueueGetStrategy getStrategy, QueuePutStrategy putStrategies[]) {
		super(false);
		this.capacity = capacity.intValue();
		drop = new boolean[dropRules.length];
		block = new boolean[dropRules.length];
		for (int i = 0; i < dropRules.length; i++) {
			if (dropRules[i].equals(FINITE_DROP)) {
				drop[i] = true;
				block[i] = false;
			} else if (dropRules[i].equals(FINITE_BLOCK)) {
				drop[i] = false;
				block[i] = true;
			} else if (dropRules[i].equals(FINITE_WAITING)) {
				drop[i] = false;
				block[i] = false;
			} else {
				drop[i] = true;
				block[i] = false;
			}
		}
		this.getStrategy = getStrategy;
		this.putStrategies = putStrategies;
		redirectionTurnOFF();
	}

	/**
	 * Tells if the redirection behaviour is turned on.
	 */
	public boolean isRedirectionON() {
		return redirectionON;
	}

	/**
	 * Turns off the redirection behaviour.
	 */
	public void redirectionTurnOFF() {
		redirectionON = false;
		myRegion = null;
		regionInputStation = null;
	}

	/**
	 * Turns on the redirection behaviour.
	 */
	public void redirectionTurnON(BlockingRegion region) {
		redirectionON = true;
		myRegion = region;
		regionInputStation = myRegion.getInputStation();
	}

	@Override
	protected void nodeLinked(NetNode node) {
		jobClasses = getJobClasses();
		nodeJobsList = node.getJobInfoList();
		waitingRelease = false;
		acceptedRelease = false;
		droppedJobs = 0;
		droppedJobsPerClass = new int[jobClasses.size()];
		Arrays.fill(droppedJobsPerClass, 0);
	}

	/**
	 * Preloads the specified numbers of jobs for each class.
	 */
	public void preloadJobs(int[] jobsPerClass) throws NetException {
		int totalJobs = 0;
		for (int i = 0; i < jobsPerClass.length; i++) {
			totalJobs += jobsPerClass[i];
		}
		Job[] jobPermutation = new Job[totalJobs];
		int index = 0;
		for (int i = 0; i < jobsPerClass.length; i++) {
			for (int j = 0; j < jobsPerClass[i]; j++) {
				jobPermutation[index] = new Job(jobClasses.get(i));
				index++;
			}
		}

		RandomEngine randomEngine = RandomEngine.makeDefault();
		for (int i = jobPermutation.length - 1; i > 0; i--) {
			int j = (int) Math.floor(randomEngine.raw() * (i + 1));
			Job job = jobPermutation[i];
			jobPermutation[i] = jobPermutation[j];
			jobPermutation[j] = job;
		}

		netJobsList = getOwnerNode().getQueueNet().getJobInfoList();
		for (int i = 0; i < jobPermutation.length; i++) {
			Job job = jobPermutation[i];
			JobInfo jobInfo = new JobInfo(job);
			putStrategies[job.getJobClass().getId()].put(job, jobsList, this);
			nodeJobsList.add(jobInfo);
			netJobsList.addJob(job);
		}
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
			List<JobInfo> preloadedJobList = jobsList.getJobList();
			for (JobInfo jobInfo : preloadedJobList) {
				JobClass jobClass = jobInfo.getJob().getJobClass();
				DataPacket packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(jobsList.size(jobClass)));
				sendForward(NetEvent.EVENT_JOB_CHANGE, packet, 0.0);
			}
			break;
		}

		case NetEvent.EVENT_JOB:
		{
			if (redirectionON && source != regionInputStation && !myRegion.belongsToRegion(source)) {
				redirect(job, 0.0, NodeSection.INPUT, regionInputStation);
				send(NetEvent.EVENT_ACK, job, 0.0, sourceSection, source);
				break;
			}

			if (capacity < 0 || jobsList.size() < capacity) {
				JobClass jobClass = job.getJobClass();
				putStrategies[jobClass.getId()].put(job, jobsList, this);
				send(NetEvent.EVENT_ACK, job, 0.0, sourceSection, source);

				if (waitingRelease) {
					acceptedRelease = true;
				} else {
					DataPacket packet = new DataPacket();
					packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
					packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(jobsList.size(jobClass)));
					sendForward(NetEvent.EVENT_JOB_CHANGE, packet, 0.0);
				}
			} else {
				// We only support drop in the initial implementation.
				sendAckAfterDrop(job, 0.0, sourceSection, source);
				netJobsList.dropJob(job);
				if (redirectionON) {
					//myRegion.decreaseOccupation(job.getJobClass());
					myRegion.decreaseOccupation(job);
					send(NetEvent.EVENT_JOB_OUT_OF_REGION, job, 0.0, NodeSection.INPUT, regionInputStation);
				}
				droppedJobs++;
				droppedJobsPerClass[job.getJobClass().getId()]++;
			}
			break;
		}

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_JOB_REQUEST:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			int requestedJobs = ((Integer) ((DataPacket) data).getData(DataPacket.FIELD_JOB_NUMBER)).intValue();
			if (requestedJobs > jobsList.size(jobClass)) {
				return MSG_NOT_PROCESSED;
			}
			for (int i = 0; i < requestedJobs; i++) {
				sendForward(getStrategy.get(jobsList, jobClass), 0.0);
			}

			DataPacket packet = new DataPacket();
			packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
			packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(requestedJobs));
			sendForward(NetEvent.EVENT_JOB_FINISH, packet, 0.0);

			packet = new DataPacket();
			packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
			packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(jobsList.size(jobClass)));
			sendForward(NetEvent.EVENT_JOB_CHANGE, packet, 0.0);
			break;
		}

		case NetEvent.EVENT_JOB_RELEASE:
		{
			waitingRelease = true;
			break;
		}

		case NetEvent.EVENT_JOB_FINISH:
		{
			waitingRelease = false;
			if (acceptedRelease) {
				JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
				DataPacket packet = new DataPacket();
				packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
				packet.setData(DataPacket.FIELD_JOB_NUMBER, Integer.valueOf(jobsList.size(jobClass)));
				sendForward(NetEvent.EVENT_JOB_CHANGE, packet, 0.0);
			}
			acceptedRelease = false;
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
	 * Gets the total number of dropped jobs
	 */
	public int getDroppedJobs() {
		return droppedJobs;
	}

	/**
	 * Gets the number of dropped jobs for the specified class
	 */
	public int getDroppedJobPerClass(int classIndex) {
		return droppedJobsPerClass[classIndex];
	}

}
