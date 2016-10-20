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

package jmt.engine.NodeSections;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import jmt.common.exception.NetException;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.LinkedJobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.SimConstants;
import jmt.engine.dataAnalysis.Measure;

/**
 * This class implements the queue of a region input station, that is
 * the node which controls the access to a blocking region.
 *
 * @author Stefano Omini
 *
 * Modifief by Bertoli Marco 31-03-2006 (my birthday!!!) 
 * 
 * Modified by Ashanka (Oct 2009):
 * Desc: FCR(Finite Capacity Region) is a blocking region and it was measuring wrong 
 * 		 values for the Performance Indices. This was because the Jobs in FCR were not
 * 		 considering the time spent in the Queuing Stations in the FCR and simply reporting
 *       time spent in Queuing in FCR. This behavior is incorrect as Performance Indices for 
 *       FCR should also contain the time spent in the FCR and not only the time spent queuing.
 *       
 *       Approach taken presently to rectify is to delay the capture of the performance indices 
 *       until the job is out of the FCR. So the job dropping are manually handled for Blocking region
 *       at Node level.
 */
public class BlockingQueue extends InputSection {

	public static final boolean DEBUG = false;

	/** Property Identifier: infinite. */
	public static final int PROPERTY_ID_INFINITE = 0x0101;
	/** Property Identifier: drop.*/
	public static final int PROPERTY_ID_DROP = 0x0102;
	/** Property Identifier: size.*/
	public static final int PROPERTY_ID_SIZE = 0x0103;
	/** Property Identifier: Waiting request.*/
	public static final int PROPERTY_ID_WAITING_REQUESTS = 0x0104;
	/** Property Identifier: Queue get strategy.*/
	public static final int PROPERTY_ID_GET_STRATEGY = 0x0105;
	/** Property Identifier: Queue put strategy.*/
	public static final int PROPERTY_ID_PUT_STRATEGY = 0x0106;

	//coolStart is true if there are no waiting jobs when the queue is started
	private boolean coolStart;

	private int size;

	private BlockingRegion blockingRegion;

	/** For each class, true if jobs in excess must be dropped */
	protected boolean[] classDrop;

	/** For each class, true if jobs in excess must be dropped */
	protected double[] classWeights;

	private int droppedJobs;
	private int[] droppedJobsPerClass;

	//job info list of the owner node: used to remove job after drop
	private JobInfoList jobsList_node;

	/**
	 * Creates a new instance of infinite BlockingQueue.
	 */
	public BlockingQueue(BlockingRegion myRegion) {
		super(true);
		coolStart = true;
		//infinite queue
		this.size = -1;
		//sets the blocking region owner of this blocking queue
		this.blockingRegion = myRegion;
	}

	@Override
	protected void nodeLinked(NetNode node) {
		// Sets netnode dependent properties
		jobsList = new LinkedJobInfoList(getJobClasses().size(), true);
		jobsList_node = node.getJobInfoList();

		//copies drop properties from blocking region
		//and initializes dropped jobs
		classDrop = new boolean[getJobClasses().size()];
		for (int i = 0; i < classDrop.length; i++) {
			classDrop[i] = blockingRegion.getClassDrop(i);
		}
		droppedJobs = 0;
		droppedJobsPerClass = new int[getJobClasses().size()];
		Arrays.fill(droppedJobsPerClass, 0);
	}

	@Override
	public int getIntSectionProperty(int id) throws NetException {
		switch (id) {
		case PROPERTY_ID_SIZE:
			return size;
		default:
			return super.getIntSectionProperty(id);
		}
	}

	/**
	 * Gets, if exists, the first job in queue that is not class blocked and has an higher priority.
	 * @return the first job in queue that is not class blocked; null otherwise.
	 */
	private Job getNextNotBlockedJob() {
		if (jobsList == null) {
			return null;
		}

		List<JobInfo> jobList = jobsList.getJobList();
		ListIterator<JobInfo> iterator = jobList.listIterator();
		boolean hasGroupConstraint = blockingRegion.hasGroupConstraint();
		boolean isHPSGroupActive = blockingRegion.isHPSGroupActive();
		Job nextJob = null;
		int nextJobPriority = 0;
		while (iterator.hasNext()) {
			JobInfo info = iterator.next();
			JobClass jobClass = info.getJob().getJobClass();
			if (hasGroupConstraint) {
				if (nextJob == null || nextJobPriority < jobClass.getPriority() || (nextJobPriority == jobClass.getPriority()
						&& blockingRegion.getHPSStrategy().equals(BlockingRegion.REGION_GROUP_HPS_LCFS_STRATEGY))) {
					if (!blockingRegion.isBlocked(jobClass)) {
						nextJob = info.getJob();
						nextJobPriority = jobClass.getPriority();
						if (!isHPSGroupActive) {
							break;
						}
					}
				}
			} else {
				if (nextJob == null || nextJobPriority < jobClass.getPriority()) {
					if (!blockingRegion.isBlocked(jobClass)) {
						nextJob = info.getJob();
						nextJobPriority = jobClass.getPriority();
					}
				}
			}
		}
		return nextJob;
	}

	/** This method implements a blocking queue
	 * @param message message to be processed.
	 * @throws NetException
	 */
	@Override
	protected int process(NetMessage message) throws NetException {
		Job job;
		Job notBlockedJob;
		JobClass jobClass;

		switch (message.getEvent()) {

		case NetEvent.EVENT_START:

			//the input station receives the "start" event because it has no nodes in input
			//then it is marked as reference node

			//NEW
			//@author Stefano Omini

			//checks whether there are jobs in queue
			if (jobsList.size() == 0) {
				//no jobs in queue
				coolStart = true;
				break;
			}

			//get the first job which is not blocked (if exists)
			notBlockedJob = getNextNotBlockedJob();

			if (notBlockedJob == null) {
				//all jobs in queue are class blocked
				//nothing else to do
			} else {
				//get class
				//jobClass = notBlockedJob.getJobClass();
				//forward job and increase occupation
				forward(notBlockedJob);
				blockingRegion.increaseOccupation(notBlockedJob);
			}
			break;

		case NetEvent.EVENT_JOB:

			//EVENT_JOB
			job = message.getJob();

			//no ack must be sent to the source of message (the redirecting queue of a node
			//inside the region)
			jobClass = job.getJobClass();

			//this job has been received by an internal node
			//the region input station will have to send it back
			NetNode realDestination = message.getSource();

			//adds a JobInfo object after modifying the job with redirection informations
			job.setOriginalDestinationNode(realDestination);
			jobsList.add(new JobInfo(job));

			//checks whether the region is blocked for this class
			if (blockingRegion.isBlocked(jobClass)) {
				//the region is already blocked
				//the job must be dropped?
				if (classDrop[jobClass.getId()] == true) {
					//drop job
					drop(job);
				}
				//otherwise the job remains blocked in queue
				break;
			}

			//the region is not blocked for this class
			//checks whether the queue is empty (coolstart = true) or not

			if (coolStart) {
				// If coolStart is true, this job is sent immediately to the next
				// section and coolStart set to false.
				forward(job);
				blockingRegion.increaseOccupation(job);

				coolStart = false;
			} else {
				//coolStart is false: this means that the jobs in queue are class blocked,
				//otherwise they would have been already sent to the region till it would
				//become globally or class blocked

				//the job is sent to service section

				//OLD
				/*Job firstNotBlocked = getFirstNotBlockedJob();
				if (firstNotBlocked != null) {
					forward(firstNotBlocked);
					if (DEBUG) {
						System.out.println("SendJobIn");
					}

					blockingRegion.increaseOccupation(jobClass);
				}*/
				forward(job);
				if (DEBUG) {
					System.out.println("SendJobIn");
				}

				blockingRegion.increaseOccupation(job);
			}
			break;

			//the BlockingQueue has almost the same behaviour with the events
			//EVENT_ACK and EVENT_JOB_OUT_OF_REGION

		case NetEvent.EVENT_ACK:

			//checks whether there are jobs in queue
			if (jobsList.size() == 0) {
				//no jobs in queue
				coolStart = true;
				break;
			}

			//search for the first job which is not blocked (if exists)
			//and forward it
			notBlockedJob = getNextNotBlockedJob();

			if (notBlockedJob != null) {
				//jobClass = notBlockedJob.getJobClass();
				forward(notBlockedJob);
				blockingRegion.increaseOccupation(notBlockedJob);
			} else {
				//all jobs in queue are blocked
				//nothing else to do
			}
			break;

		case NetEvent.EVENT_JOB_OUT_OF_REGION:

			//FCR Bug fix:
			//The dropping of the job was postponed as the time 
			//spent in the Queuing station was not taken in consideration.
			//Secondly point to be noted is that I am 
			//only dropping Jobs at Node level as the node section jobs 
			//are automatically dropped but the node level jobs are
			//dropped manually.
			Job localJob = (Job) message.getData();
			JobInfo jobInfo = jobsList_node.lookFor(localJob);

			//remove only when you find a job in the list.
			if (jobInfo != null) {
				jobsList_node.remove(jobInfo);
			}

			//checks whether there are jobs in queue
			if (jobsList.size() == 0) {
				//no jobs in queue
				coolStart = true;
				break;
			}

			//search for the first job which is not blocked (if exists)
			//and forward it
			notBlockedJob = getNextNotBlockedJob();

			if (notBlockedJob != null) {
				//jobClass = notBlockedJob.getJobClass();
				forward(notBlockedJob);
				blockingRegion.increaseOccupation(notBlockedJob);
			} else {
				//all jobs in queue are blocked
				//nothing else to do
			}
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

	private void forward(Job job) throws NetException {
		sendForward(job, 0.0);
	}

	/**
	 * Drop the specified job, removing it from the job info lists and increases
	 * the counters of dropped jobs
	 * @param job the job to be dropped
	 * @throws NetException
	 */
	private void drop(Job job) throws NetException {
		int c = job.getJobClass().getId();
		droppedJobs++;
		droppedJobsPerClass[c]++;

		JobInfo jobInfo = jobsList.lookFor(job);
		if (jobInfo != null) {
			//removes job from the jobInfoList of the node section
			jobsList.dropJob(jobInfo);
		}

		if (jobsList_node == null) {
			jobsList_node = getOwnerNode().getJobInfoList();
		}
		JobInfo jobInfo_node = jobsList_node.lookFor(job);
		if (jobInfo_node != null) {
			//removes job from the jobInfoList of the node
			jobsList_node.dropJob(jobInfo_node);
		}
		// Removes job from global jobInfoList - Bertoli Marco
		getOwnerNode().getQueueNet().getJobInfoList().dropJob(job);
	}

	/**
	 * Gets the total number of dropped jobs
	 * @return the total number of dropped jobs
	 */
	public int getDroppedJobs() {
		return droppedJobs;
	}

	/**
	 * Gets the number of dropped jobs for the specified class
	 * @param classIndex the index of the job class
	 * @return the number of dropped jobs for the specified class
	 */
	public int getDroppedJobPerClass(int classIndex) {
		return droppedJobsPerClass[classIndex];
	}

	@Override
	public void analyzeFCR(int name, Measure measurement) throws NetException {
		switch (name) {
		case SimConstants.FCR_TOTAL_WEIGHT:
			blockingRegion.setWeightMeasure(measurement);
			break;
		case SimConstants.FCR_MEMORY_OCCUPATION:
			blockingRegion.setSizeMeasure(measurement);
			break;
		default:
			throw new NetException(this, EXCEPTION_MEASURE_DOES_NOT_EXIST, "required analyzer does not exist!");
		}
	}

}
