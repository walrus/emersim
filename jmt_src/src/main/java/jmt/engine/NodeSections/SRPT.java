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

import java.util.PriorityQueue;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.QueueNet.PSJobInfoList;
import jmt.engine.simEngine.RemoveToken;

/**
 * <p><b>Name:</b> PSServer</p> 
 * <p><b>Description:</b> 
 * This class implements a Shortest Remaining Processing Time server.
 * </p>
 * <p><b>Date:</b> 04/ott/2009
 * <b>Time:</b> 13.45.37</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public class SRPT extends ServiceSection {

	/** Property Identifier:  Busy counter. */
	public static final int PROPERTY_ID_BUSY_COUNTER = 0x0101;
	/** Property Identifier:  Max jobs (number of servers). */
	public static final int PROPERTY_ID_MAX_JOBS = 0x0102;
	/** Property Identifier:  Service strategy. */
	public static final int PROPERTY_ID_SERVICE_STRATEGY = 0x0104;

	/**
	 * @uml.property  name="numberOfServers"
	 */
	private int numberOfServers;
	/**
	 * @uml.property  name="waitingAcks"
	 */
	/** This variable is used to implement blocking.*/
	private int waitingAcks;

	/**
	 * @uml.property  name="serviceStrategy"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private ServiceStrategy serviceStrategy[];

	/**
	 * Tells which inner event we are processing
	 */
	private enum PSEvent {
		/**
		 * @uml.property  name="jOB_IN"
		 * @uml.associationEnd  
		 */
		JOB_IN,
		/**
		 * @uml.property  name="jOB_OUT"
		 * @uml.associationEnd  
		 */
		JOB_OUT
	}

	/**
	 * The data structure holdings all the jobs
	 * @uml.property  name="jobs"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="jmt.engine.NodeSections.PSServer$JobData"
	 */
	private PriorityQueue<JobData> jobs;

	/**
	 * @uml.property  name="queueList"
	 * @uml.associationEnd  
	 */
	private PSJobInfoList queueList;
	/**
	 * @uml.property  name="serviceList"
	 * @uml.associationEnd  
	 */
	private PSJobInfoList serviceList;

	// A token to preempt the last message sent when a new job arrives
	/**
	 * @uml.property  name="lastMessageSent"
	 * @uml.associationEnd  
	 */
	private RemoveToken lastMessageSent;
	/**
	 * @uml.property  name="lastMessageSentTime"
	 */
	private double lastMessageSentTime;

	/** Creates a new instance of Server.
	 * @param serverNumber Number of jobs which can be served simultaneously.
	 * @param serviceStrategy Array of service strategies, one per class.
	 * @throws NetException
	 */
	public SRPT(Integer serverNumber, ServiceStrategy serviceStrategy[]) throws NetException {
		// Disable automatic section jobinfolist usage. We will update it manually.
		super(false, true);
		this.serviceStrategy = serviceStrategy;
		this.numberOfServers = serverNumber.intValue();
		jobs = new PriorityQueue<JobData>();
	}

	public SRPT(Integer serverNumber, Integer numberOfVisitsPerClass[], ServiceStrategy serviceStrategy[]) throws NetException {
		this(serverNumber, serviceStrategy);
	}

	@Override
	public int getIntSectionProperty(int id) throws NetException {
		switch (id) {
		case PROPERTY_ID_BUSY_COUNTER:
			return jobs.size();
		case PROPERTY_ID_MAX_JOBS:
			return numberOfServers;
		default:
			return super.getIntSectionProperty(id);
		}
	}

	@Override
	public Object getObject(int id, JobClass jobClass) throws NetException {
		switch (id) {
		case PROPERTY_ID_SERVICE_STRATEGY:
			return serviceStrategy[jobClass.getId()];
		default:
			return super.getObject(id);
		}
	}

	@Override
	public Object getObject(int id) throws NetException {
		switch (id) {
		case PROPERTY_ID_SERVICE_STRATEGY:
			return serviceStrategy;
		default:
			return super.getObject(id);
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.NodeSection#getDoubleSectionProperty(int, jmt.engine.QueueNet.JobClass)
	 */
	@Override
	public double getDoubleSectionProperty(int id, JobClass jobClass) throws NetException {
		if (id == PROPERTY_ID_UTILIZATION) {
			double divisor = numberOfServers;
			return jobsList.getBusyTimePerClass(jobClass) / NetSystem.getTime() / divisor;
		} else {
			return super.getDoubleSectionProperty(id, jobClass);
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.NodeSection#getDoubleSectionProperty(int)
	 */
	@Override
	public double getDoubleSectionProperty(int id) throws NetException {
		if (id == PROPERTY_ID_UTILIZATION) {
			double divisor = numberOfServers;
			return jobsList.getBusyTime() / NetSystem.getTime() / divisor;
		} else {
			return super.getDoubleSectionProperty(id);
		}
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
			break;

		case NetEvent.EVENT_JOB:
			Job job = message.getJob();

			if (isMine(message)) {
				// I sent the message, so it ended servicing
				//updateServiceTimes(lastMessageSentTime);
				lastMessageSent = null;
				// forwards the completed jobs to the output section
				JobData data = jobs.peek();

				data.performService(NetSystem.getTime() - lastMessageSentTime);

				handleJobInfoLists(data, PSEvent.JOB_OUT);
				jobs.poll();
				sendForward(data.getJob(), 0.0);

				waitingAcks++;
			} else {
				if (!jobs.isEmpty()) {
					JobData prev = jobs.peek();
					prev.performService(NetSystem.getTime() - lastMessageSentTime);
				}
				// Check if a job is running
				if (lastMessageSent != null) {
					// We need to preempt the last message

					removeMessage(lastMessageSent);
					//updateServiceTimes(lastMessageSentTime);
					lastMessageSent = null;
				}

				// Estimate the job service time, puts it in the queue and sends a message to itself
				// with the minimum service time of all the jobs to perform processing
				double serviceTime = 0.0;
				if (job.getServiceTime() != 0.0) {
					serviceTime = job.getServiceTime();
				} else {
					serviceTime = serviceStrategy[job.getJobClass().getId()].wait(this);
				}
				//job.setServiceTime(serviceTime);
				JobData data = new JobData(job, serviceTime);
				handleJobInfoLists(data, PSEvent.JOB_IN);
				jobs.add(data);

				// Sends an ACK to the input section as we will always accept new jobs.
				sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
			}
			serviceJobs();
			break;

		case NetEvent.EVENT_ACK:

			//EVENT_ACK
			//If there are no jobs in the service section, message is not processed.
			if (waitingAcks == 0) {
				//it was not waiting for any job
				return NodeSection.MSG_NOT_PROCESSED;
			} else {
				// Avoid ACK to the input section, as we already sent ack
				waitingAcks--;
			}
			serviceJobs();
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

	/**
	 * Service jobs (send a message to itself), if it is not blocked.
	 */
	private void serviceJobs() throws NetException {
		// If all acks were received and there are jobs in internal queue, 
		// send in service the one with the minimum residual service time
		if (waitingAcks == 0 && jobs.size() > 0) {
			// Estimate wait time for the minimum job
			double waitTime = jobs.peek().getResidualServiceTime();
			/*
			if (jobs.size() <= numberOfServers) {
				// No sharing in this case: each job is served by a processor
				waitTime = jobs.peek().getResidualServiceTime();
			} else {
				// Processors are shared among the jobs, so service time will be slow.
				waitTime = jobs.peek().getResidualServiceTime() * jobs.size() / numberOfServers;
			}*/
			lastMessageSent = sendMe(jobs.peek().getJob(), waitTime);
			lastMessageSentTime = NetSystem.getTime();
		}
	}

	/**
	 * Handles the manual update of measures for queue and service sections
	 * @param jobData the jobData
	 * @param event the type of event
	 * @throws NetException
	 */
	private void handleJobInfoLists(JobData jobData, PSEvent event) throws NetException {
		// Retrieve queue PSJobInfoList
		if (queueList == null) {
			queueList =  (PSJobInfoList) getOwnerNode().getSection(NodeSection.INPUT).getObject(PROPERTY_ID_JOBINFOLIST);
		}

		// Computes the percentage of jobs in service and in queue
		double serviceWeigth, queueWeight;
		if (jobs.size() <= numberOfServers) {
			// Everything is in service
			serviceWeigth = 1.0;
		} else {
			serviceWeigth = (double) numberOfServers / jobs.size();
		}
		queueWeight = 1.0 - serviceWeigth;

		// Updates utilization measures
		JobClass jobClass = jobData.getJob().getJobClass();
		queueList.psUpdateUtilization(jobClass, queueWeight, NetSystem.getTime());
		serviceList.psUpdateUtilization(jobClass, serviceWeigth, NetSystem.getTime());

		// Update busy time measures and throughputs
		if (event == PSEvent.JOB_OUT) {
			double queueTime = NetSystem.getTime() - jobData.getEnteringTime() - jobData.getServiceTime();
			// This can happen only because of machine epsilon problems
			if (queueTime < 0.0) {
				queueTime = 0.0;
			}
			queueList.psUpdateBusyTimes(jobClass, queueTime);
			serviceList.psUpdateBusyTimes(jobClass, jobData.getServiceTime());
			queueList.psUpdateThroughput(jobClass);
			serviceList.psUpdateThroughput(jobClass);
		}

		// Finally updates jobIn / jobOut counters
		if (event == PSEvent.JOB_IN) {
			queueList.psJobIn(jobClass, NetSystem.getTime());
			serviceList.psJobIn(jobClass, NetSystem.getTime());
		} else if (event == PSEvent.JOB_OUT) {
			queueList.psJobOut(jobClass, NetSystem.getTime());
			serviceList.psJobOut(jobClass, NetSystem.getTime());
		}
	}

	/**
	 * Updates service times of all the jobs being served by the corresponding amount.
	 * All completed jobs will be sent forward.
	 * @param time the start time in which the job message was sent
	 * @param endedJob the job that was ended. May be null
	 * @throws NetException if something goes wrong.
	 */
	private void updateServiceTimes(double startTime) throws NetException {
		double serviceTime;

		if (jobs.size() <= numberOfServers) {
			// No sharing in this case: each job is served by a processor
			serviceTime = NetSystem.getTime() - startTime;
		} else {
			// Processors are shared among the jobs
			serviceTime = (NetSystem.getTime() - startTime) * numberOfServers / jobs.size();
		}

		for (JobData jd : jobs) {
			jd.performService(serviceTime);
		}
	}

	/**
	 * An inner data structure used to keep job and service time data. The data structure is ordered basing on service times.
	 */
	private static class JobData implements Comparable<JobData> {

		/**
		 * @uml.property  name="job"
		 * @uml.associationEnd  
		 */
		private Job job;
		/**
		 * @uml.property  name="serviceTime"
		 */
		private double serviceTime;
		/**
		 * @uml.property  name="residualServiceTime"
		 */
		private double residualServiceTime;
		/**
		 * @uml.property  name="enteringTime"
		 */
		private double enteringTime;

		/**
		 * Builds a new JobData data structure
		 * @param job the job
		 * @param serviceTime the residual service time
		 */
		public JobData(Job job, double serviceTime) {
			this.job = job;
			this.residualServiceTime = serviceTime;
			this.serviceTime = serviceTime;
			this.enteringTime = NetSystem.getTime();
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(JobData o) {
			if (residualServiceTime < o.residualServiceTime) {
				return -1;
			} else if (residualServiceTime > o.residualServiceTime) {
				return 1;
			} else {
				return 0;
			}
		}

		/**
		 * @return  the job
		 * @uml.property  name="job"
		 */
		public Job getJob() {
			return job;
		}

		/**
		 * @return  the residual service time
		 * @uml.property  name="residualServiceTime"
		 */
		public double getResidualServiceTime() {
			return residualServiceTime;
		}

		/**
		 * @return  the total service time that the job must receive.
		 * @uml.property  name="serviceTime"
		 */
		public double getServiceTime() {
			return serviceTime;
		}

		/**
		 * @return  the time in which the job entered the service section.
		 * @uml.property  name="enteringTime"
		 */
		public double getEnteringTime() {
			return enteringTime;
		}

		/**
		 * Performs service, scaling the residual service time
		 * @param time the service time received
		 */
		public void performService(double time) {
			if (residualServiceTime <=time ) {
				residualServiceTime =0;
			}
			else {
				residualServiceTime = residualServiceTime - time;
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Job id: " + job.getId() + " class: " + job.getJobClass().getName() + " residual time: " + residualServiceTime;
		}

	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.NodeSection#nodeLinked(jmt.engine.QueueNet.NetNode)
	 */
	@Override
	protected void nodeLinked(NetNode node) {
		jobsList = serviceList = new PSJobInfoList(getJobClasses().size(), true);
		jobsList.setServerNumber(numberOfServers);
	}

}
