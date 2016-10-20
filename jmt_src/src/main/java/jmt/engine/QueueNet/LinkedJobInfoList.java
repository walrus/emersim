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

package jmt.engine.QueueNet;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/** This class implements a job info list based on a linked list.
 * @author Francesco Radaelli, Stefano Omini.
 * 
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for the capturing the 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class.
 */
public class LinkedJobInfoList implements JobInfoList {

	/** Required property is not available*/
	public static final int PROPERTY_NOT_AVAILABLE = 0x0001;

	//contain JobInfo objects
	private LinkedList<JobInfo> list;

	private LinkedList<JobInfo> listPerClass[];

	//arrivals and completions
	private int jobsIn;

	private int jobsOut;

	private int jobsInPerClass[];

	private int jobsOutPerClass[];

	private double busyTime;

	private double busyTimePerClass[];

	private double lastJobOutTime;

	private double lastJobInTime;

	private double lastJobDropTime;

	private double lastJobOutTimePerClass[];

	private double lastJobInTimePerClass[];

	private double lastJobDropTimePerClass[];

	private Measure utilization ;

	private Measure utilizationPerClass[];

	private Measure utilizationJoin ;

	private Measure utilizationPerClassJoin[];

	private Measure responseTime;

	private Measure responseTimePerClass[];

	private Measure residenceTime;

	private Measure residenceTimePerClass[];

	private Measure queueLength;

	private Measure queueLengthPerClass[];

	private InverseMeasure dropRate;

	private InverseMeasure dropRatePerClass[];

	private Measure responseTimePerSink;

	private Measure responseTimePerSinkPerClass[];

	private InverseMeasure throughput;

	private InverseMeasure throughputPerClass[];

	private InverseMeasure throughputPerSink;

	private InverseMeasure throughputPerSinkPerClass[];

	/** The number of servers to estimate Utilization measure on multiserver environments. */
	private int serverNumber = 1;

	/** Creates a new JobInfoList instance.
	 * @param numberOfJobClasses number of job classes.
	 * @param save True to create and use a list to add/remove
	 * each job which arrives/departs, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public LinkedJobInfoList(int numberOfJobClasses, boolean save) {
		if (save) {
			list = new LinkedList<JobInfo>();
			listPerClass = new LinkedList[numberOfJobClasses];
			for (int i = 0; i < numberOfJobClasses; i++) {
				listPerClass[i] = new LinkedList<JobInfo>();
			}
		}

		jobsInPerClass = new int[numberOfJobClasses];
		jobsOutPerClass = new int[numberOfJobClasses];
		busyTimePerClass = new double[numberOfJobClasses];
		lastJobInTimePerClass = new double[numberOfJobClasses];
		lastJobOutTimePerClass = new double[numberOfJobClasses];
		lastJobDropTimePerClass = new double[numberOfJobClasses];
	}

	/**---------------------------------------------------------------------
	 *-------------------- "GET" METHODS -----------------------------------
	 *---------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#size()
	 */
	public int size() throws jmt.common.exception.NetException {
		if (list != null) {
			return list.size();
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#size(jmt.engine.QueueNet.JobClass)
	 */
	public int size(JobClass jobClass) throws jmt.common.exception.NetException {
		if (listPerClass != null) {
			return listPerClass[jobClass.getId()].size();
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsIn()
	 */
	public int getJobsIn() {
		return jobsIn;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsInPerClass(jmt.engine.QueueNet.JobClass)
	 */
	public int getJobsInPerClass(JobClass jobClass) {
		return jobsInPerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsInPerClass()
	 */
	public int[] getJobsInPerClass() {
		return jobsInPerClass;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsOut()
	 */
	public int getJobsOut() {
		return jobsOut;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsOutPerClass(jmt.engine.QueueNet.JobClass)
	 */
	public int getJobsOutPerClass(JobClass jobClass) {
		return jobsOutPerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobsOutPerClass()
	 */
	public int[] getJobsOutPerClass() {
		return jobsOutPerClass;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getBusyTime()
	 */
	public double getBusyTime() throws jmt.common.exception.NetException {
		if (list != null) {
			return busyTime;
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getBusyTimePerClass(jmt.engine.QueueNet.JobClass)
	 */
	public double getBusyTimePerClass(JobClass jobClass) throws jmt.common.exception.NetException {
		if (listPerClass != null) {
			return busyTimePerClass[jobClass.getId()];
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobInTime()
	 */
	public double getLastJobInTime() {
		return lastJobInTime;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobInTimePerClass(jmt.engine.QueueNet.JobClass)
	 */
	public double getLastJobInTimePerClass(JobClass jobClass) {
		return lastJobInTimePerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobOutTime()
	 */
	public double getLastJobOutTime() {
		return lastJobOutTime;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobDropTime()
	 */
	public double getLastJobDropTime() {
		return lastJobDropTime;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobOutTimePerClass(jmt.engine.QueueNet.JobClass)
	 */
	public double getLastJobOutTimePerClass(JobClass jobClass) {
		return lastJobOutTimePerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastJobDropTimePerClass(jmt.engine.QueueNet.JobClass)
	 */
	public double getLastJobDropTimePerClass(JobClass jobClass) {
		return lastJobDropTimePerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastModifyTime()
	 */
	public double getLastModifyTime() {
		if (lastJobOutTime >= lastJobInTime && lastJobOutTime >= lastJobDropTime) {
			return lastJobOutTime;
		} else if (lastJobInTime >= lastJobOutTime && lastJobInTime >= lastJobDropTime) {
			return lastJobInTime;
		} else {
			return lastJobDropTime;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getLastModifyTimePerClass(jmt.engine.QueueNet.JobClass)
	 */
	public double getLastModifyTimePerClass(JobClass jobClass) {
		if (lastJobOutTimePerClass[jobClass.getId()] >= lastJobInTimePerClass[jobClass.getId()]
				&& lastJobOutTimePerClass[jobClass.getId()] >= lastJobDropTimePerClass[jobClass.getId()]) {
			return lastJobOutTimePerClass[jobClass.getId()];
		} else if (lastJobInTimePerClass[jobClass.getId()] >= lastJobOutTimePerClass[jobClass.getId()]
				&& lastJobInTimePerClass[jobClass.getId()] >= lastJobDropTimePerClass[jobClass.getId()]) {
			return lastJobInTimePerClass[jobClass.getId()];
		} else {
			return lastJobDropTimePerClass[jobClass.getId()];
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#lookFor(jmt.engine.QueueNet.Job)
	 */
	public JobInfo lookFor(Job job) throws jmt.common.exception.NetException {
		if (listPerClass == null) {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
		//creates an iterator for the job class list of the job class of the specified job
		//ARIF: changed because possibly class switch has already changed the job class
		ListIterator<JobInfo> it = list.listIterator();//PerClass[job.getJobClass().getId()].listIterator();
		JobInfo jobInfo;
		while (it.hasNext()) {
			jobInfo = it.next();
			if (jobInfo.getJob() == job) {
				return jobInfo;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#getJobList()
	 */
	public List<JobInfo> getJobList() {
		return list;
	}

	/**---------------------------------------------------------------------
	 *-------------------- "ADD" AND "REMOVE" METHODS ----------------------
	 *---------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#add(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean add(JobInfo jobInfo) {
		if (list != null) {
			updateAdd(jobInfo);
			listPerClass[jobInfo.getJob().getJobClass().getId()].add(jobInfo);
			list.add(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#addFirst(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean addFirst(JobInfo jobInfo) {
		if (list != null) {
			updateAdd(jobInfo);
			listPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			list.addFirst(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#addLast(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean addLast(JobInfo jobInfo) {
		if (list != null) {
			updateAdd(jobInfo);
			listPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			list.addLast(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#add(int, jmt.engine.QueueNet.JobInfo)
	 */
	public boolean add(int index, JobInfo jobInfo) {
		if (list != null) {
			updateAdd(jobInfo);
			JobClass jobClass = jobInfo.getJob().getJobClass();
			ListIterator<JobInfo> it = list.listIterator();
			int indexPerClass = 0;
			for (int i = 0; i < index; i++) {
				if (it.next().getJob().getJobClass() == jobClass) {
					indexPerClass++;
				}
			}
			listPerClass[jobClass.getId()].add(indexPerClass, jobInfo);
			it.add(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#add(int, jmt.engine.QueueNet.JobInfo, boolean)
	 */
	public boolean add(int index, JobInfo jobInfo, boolean isClassHead) {
		if (list != null) {
			updateAdd(jobInfo);
			if (isClassHead) {
				listPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			} else {
				listPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			}
			list.add(index, jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/** Removes a job info from the list and updates the measures related to
	 * throughput, utilization and response time.
	 * @param jobInfo reference to job info to be removed.
	 * @param position 0 to remove from a random location, 1 from head, 2 from tail.
	 * @param perClassPosition 0 to remove from a random location, 1 from head, 2 from tail.
	 * @return True if the job has been removed (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	private boolean doRemove(JobInfo jobInfo, int position, int perClassPosition) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			updateThroughput(job);
			updateUtilization(jobClass);
			updateUtilizationJoin(jobClass);

			//NEW
			//@author Stefano Omini
			updateQueueLength(jobClass);
			updateResidenceTime(jobInfo);
			//end NEW

			updateResponseTime(jobInfo);

			finalRemove(jobInfo, listPerClass[c], perClassPosition);
			finalRemove(jobInfo, list, position);
			lastJobOutTimePerClass[c] = lastJobOutTime = NetSystem.getTime();
			double time = lastJobOutTime - jobInfo.getTime();
			jobsOut++;
			jobsOutPerClass[c]++;
			busyTime += time;
			busyTimePerClass[c] += time;

			return true;
		} else {
			return false;
		}
	}

	private void finalRemove(JobInfo what, LinkedList<JobInfo> list, int position) {
		switch (position) {
		case 1:
			list.removeFirst();
			break;
		case 2:
			list.removeLast();
			break;
		default:
			list.remove(what);
			break;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#remove(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean remove(JobInfo jobInfo) throws jmt.common.exception.NetException {
		return doRemove(jobInfo, 0, 0);
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#removeFirst()
	 */
	public JobInfo removeFirst() throws jmt.common.exception.NetException {
		if (list != null) {
			JobInfo jobInfo = list.getFirst();
			if (jobInfo != null) {
				doRemove(jobInfo, 1, 1);
				return jobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#removeFirst(jmt.engine.QueueNet.JobClass)
	 */
	public JobInfo removeFirst(JobClass jobClass) throws jmt.common.exception.NetException {
		if (list != null) {
			int c = jobClass.getId();
			JobInfo jobInfo = listPerClass[c].getFirst();
			if (jobInfo != null) {
				doRemove(jobInfo, 0, 1);
				return jobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#removeLast()
	 */
	public JobInfo removeLast() throws jmt.common.exception.NetException {
		if (list != null) {
			JobInfo jobInfo = list.getLast();
			if (jobInfo != null) {
				doRemove(jobInfo, 2, 2);
				return jobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#removeLast(jmt.engine.QueueNet.JobClass)
	 */
	public JobInfo removeLast(JobClass jobClass) throws jmt.common.exception.NetException {
		if (list != null) {
			int c = jobClass.getId();
			JobInfo jobInfo = listPerClass[c].getLast();
			if ((jobInfo != null)) {
				doRemove(jobInfo, 0, 2);
				return jobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**---------------------------------------------------------------------
	 *---------------- "ANALYZE" AND "UPDATE" METHODS ----------------------
	 *---------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeUtilization(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeUtilization(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (utilizationPerClass == null) {
				utilizationPerClass = new Measure[listPerClass.length];
			}
			utilizationPerClass[jobClass.getId()] = measurement;
		} else {
			utilization = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeUtilizationJoin(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeUtilizationJoin(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (utilizationPerClassJoin == null) {
				utilizationPerClassJoin = new Measure[listPerClass.length];
			}
			utilizationPerClassJoin[jobClass.getId()] = measurement;
		} else {
			utilizationJoin = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeResponseTime(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeResponseTime(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (responseTimePerClass == null) {
				responseTimePerClass = new Measure[listPerClass.length];
			}
			responseTimePerClass[jobClass.getId()] = measurement;
		} else {
			responseTime = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeDropRate(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.InverseMeasure)
	 */
	public void analyzeDropRate(JobClass jobClass, InverseMeasure measurement) {
		if (jobClass != null) {
			if (dropRatePerClass == null) {
				dropRatePerClass = new InverseMeasure[listPerClass.length];
			}
			dropRatePerClass[jobClass.getId()] = measurement;
		} else {
			dropRate = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeThroughput(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.InverseMeasure)
	 */
	public void analyzeThroughput(JobClass jobClass, InverseMeasure measurement) {
		if (jobClass != null) {
			if (throughputPerClass == null) {
				throughputPerClass = new InverseMeasure[listPerClass.length];
			}
			throughputPerClass[jobClass.getId()] = measurement;
		} else {
			throughput = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeResidenceTime(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeResidenceTime(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (residenceTimePerClass == null) {
				residenceTimePerClass = new Measure[listPerClass.length];
			}
			residenceTimePerClass[jobClass.getId()] = measurement;
		} else {
			residenceTime = measurement;
		}
	}

	public void analyzeResponseTimePerSink(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (responseTimePerSinkPerClass == null) {
				responseTimePerSinkPerClass = new Measure[listPerClass.length];
			}
			responseTimePerSinkPerClass[jobClass.getId()] = measurement;
		} else {
			responseTimePerSink = measurement;
		}		
	}

	public void analyzeThroughputPerSink(JobClass jobClass, InverseMeasure measurement) {
		if (jobClass != null) {
			if (throughputPerSinkPerClass == null) {
				throughputPerSinkPerClass = new InverseMeasure[listPerClass.length];
			}
			throughputPerSinkPerClass[jobClass.getId()] = measurement;
		} else {
			throughputPerSink = measurement;
		}		
	}

	/**
	 * Updates Response time measure
	 * <br>Author: Bertoli Marco
	 * @param JobInfo current JobInfo
	 */
	private void updateResponseTime(JobInfo jobInfo) {
		int c = jobInfo.getJob().getJobClass().getId();
		double arriveTime = jobInfo.getTime();
		if (responseTimePerClass != null) {
			Measure m = responseTimePerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - arriveTime, 1.0);
			}
		}
		if (responseTime != null) {
			responseTime.update(NetSystem.getTime() - arriveTime, 1.0);
		}
	}

	private void updateUtilization(JobClass jobClass) {
		double divisor = serverNumber;
		if (utilizationPerClass != null) {
			int c = jobClass.getId();
			Measure m = utilizationPerClass[c];
			if (m != null) {
				m.update(listPerClass[c].size() / divisor, NetSystem.getTime() - getLastModifyTimePerClass(jobClass));
			}
		}
		if (utilization != null) {
			utilization.update(list.size() / divisor, NetSystem.getTime() - getLastModifyTime());
		}
	}

	private void updateUtilizationJoin(JobClass jobClass) {
		if (utilizationPerClassJoin != null) {
			int c = jobClass.getId();
			Measure m = utilizationPerClassJoin[c];
			if (m != null) {
				if (listPerClass[c].size() > 0) {
					m.update(1, NetSystem.getTime() - getLastModifyTimePerClass(jobClass));
				} else {
					m.update(0, NetSystem.getTime() - getLastModifyTimePerClass(jobClass));
				}
			}
		}
		if (utilizationJoin != null) {
			if (list.size() > 0) {
				utilizationJoin.update(1, NetSystem.getTime() - getLastModifyTime());
			} else {
				utilizationJoin.update(0, NetSystem.getTime() - getLastModifyTime());
			}
		}
	}

	private void updateResidenceTime(JobInfo jobInfo) {
		int c = jobInfo.getJob().getJobClass().getId();
		double arriveTime = jobInfo.getTime();
		if (residenceTimePerClass != null) {
			Measure m = residenceTimePerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - arriveTime, 1.0);
			}
		}
		if (residenceTime != null) {
			residenceTime.update(NetSystem.getTime() - arriveTime, 1.0);
		}
	}

	private void updateDropRate(JobClass jobClass) {
		int c = jobClass.getId();
		if (dropRatePerClass != null) {
			Measure m = dropRatePerClass[c];
			if (m != null) {
				// Inverse measure must be used to compute drop rate
				m.update(NetSystem.getTime() - getLastJobDropTimePerClass(jobClass), 1.0);
			}
		}
		if (dropRate != null) {
			dropRate.update(NetSystem.getTime() - getLastJobDropTime(), 1.0);
		}
	}

	private void updateThroughput(Job job) {
		int c = job.getJobClass().getId();
		if (throughputPerClass != null) {
			Measure m = throughputPerClass[c];
			if (m != null) {
				// new sample is the inter-departures time (1/throughput)
				// Inverse measure must be used to compute throughput
				m.update(NetSystem.getTime() - getLastJobOutTimePerClass(job.getJobClass()), 1.0);
			}
		}
		if (throughput != null) {
			throughput.update(NetSystem.getTime() - getLastJobOutTime(), 1.0);
		}
	}

	private void updateAdd(JobInfo jobInfo) {
		Job job = jobInfo.getJob();
		JobClass jobClass = job.getJobClass();
		int c = jobClass.getId();

		updateUtilization(jobClass);
		updateUtilizationJoin(jobClass);
		updateQueueLength(jobClass);

		lastJobInTimePerClass[c] = lastJobInTime = NetSystem.getTime();
		jobsIn++;
		jobsInPerClass[c]++;

	}

	public void updateResponseTimePerSink(JobInfo jobInfo) {
		int c = jobInfo.getJob().getJobClass().getId();
		if (responseTimePerSinkPerClass != null) {
			Measure m = responseTimePerSinkPerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - jobInfo.getJob().getSystemEnteringTime(), 1.0);
			}
		}
		if (responseTimePerSink != null) {
			responseTimePerSink.update(NetSystem.getTime() - jobInfo.getJob().getSystemEnteringTime(), 1.0);
		}
	}

	public void updateThroughputPerSink(JobInfo jobInfo) {
		int c = jobInfo.getJob().getJobClass().getId();
		if (throughputPerSinkPerClass != null) {
			InverseMeasure m = throughputPerSinkPerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - getLastJobOutTimePerClass(jobInfo.getJob().getJobClass()), 1.0);
			}
		}
		if (throughputPerSink != null) {
			throughputPerSink.update(NetSystem.getTime() - getLastJobOutTime(), 1.0);
		}
	}

	//NEW
	//@author Stefano Omini
	//modified 21/5/2004

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeQueueLength(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeQueueLength(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (queueLengthPerClass == null) {
				queueLengthPerClass = new Measure[listPerClass.length];
			}
			queueLengthPerClass[jobClass.getId()] = measurement;
		} else {
			queueLength = measurement;
		}
	}

	/**
	 * WARNING: updateQueueLength is implemented exactly as updateUtilization: the
	 * difference is that in the former case the resident jobs counted
	 * ( ListPerClass[c].size() ) are all the jobs in the node, in the latter case
	 * are only the jobs in the service sections.
	 * This difference must be guaranteed at upper level (in Simulation class) where
	 * "analyze" methods are called
	 */
	private void updateQueueLength(JobClass jobClass) {
		if (queueLengthPerClass != null) {
			int c = jobClass.getId();
			Measure m = queueLengthPerClass[c];
			if (m != null) {
				m.update(listPerClass[c].size(), NetSystem.getTime() - getLastModifyTimePerClass(jobClass));
			}
		}
		if (queueLength != null) {
			queueLength.update(list.size(), NetSystem.getTime() - getLastModifyTime());
		}
	}

	//END NEW

	//NEW
	//@author Stefano Omini

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#removeJob(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean removeJob(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			updateResponseTimePerSink(jobInfo);
			updateThroughputPerSink(jobInfo);

			listPerClass[c].remove(jobInfo);
			list.remove(jobInfo);
			lastJobOutTimePerClass[c] = lastJobOutTime = NetSystem.getTime();

			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#redirectJob(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean redirectJob(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			listPerClass[c].remove(jobInfo);
			list.remove(jobInfo);
			//the job has been redirected: it should not be counted
			jobsIn--;
			jobsInPerClass[c]--;

			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#dropJob(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean dropJob(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			updateDropRate(jobClass);

			listPerClass[c].remove(jobInfo);
			list.remove(jobInfo);
			lastJobDropTimePerClass[c] = lastJobDropTime = NetSystem.getTime();

			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#produceJob(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean produceJob(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			listPerClass[c].add(jobInfo);
			list.add(jobInfo);

			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#consumeJob(jmt.engine.QueueNet.JobInfo)
	 */
	public boolean consumeJob(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (list != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			listPerClass[c].remove(jobInfo);
			list.remove(jobInfo);

			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#setServerNumber(int)
	 */
	public void setServerNumber(int serverNumber) {
		this.serverNumber = serverNumber;
	}

	public int getJobsInPerClass(int id) {
		return jobsInPerClass[id];
	}

	public int getJobsOutPerClass(int id) {
		return jobsOutPerClass[id];
	}

}
