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

import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/**
 * <p><b>Name:</b> PSJobInfoList</p> 
 * <p><b>Description:</b> 
 * A wrapper around JobInfoList to be used by processor sharing. All the estimates are manually set, with the exception
 * of Throughput and Drop rates that are handled by the inner list (as they are unaffected by the Processor Sharing 
 * behavior)
 * </p>
 * <p><b>Date:</b> 10/nov/2009
 * <b>Time:</b> 08.33.59</p>
 * @author Bertoli Marco [marco.bertoli@neptuny.com]
 * @version 3.0
 */
public class PSJobInfoList implements JobInfoList {

	/** Wrapped JobInfoList */
	private JobInfoList list;

	private int numberOfJobClasses;
	private int size;

	private int sizePerClass[];

	//arrivals and completions
	private int jobsIn;

	private int jobsOut;

	private int jobsInPerClass[];

	private int jobsOutPerClass[];

	private double busyTime;

	private double busyTimePerClass[];

	private double lastJobOutTime;

	private double lastJobInTime;

	private double lastJobOutTimePerClass[];

	private double lastJobInTimePerClass[];

	private Measure utilization;

	private Measure utilizationPerClass[];

	private Measure responseTime;

	private Measure responseTimePerClass[];

	private Measure residenceTime;

	private Measure residenceTimePerClass[];

	private Measure queueLength;

	private Measure queueLengthPerClass[];

	private InverseMeasure throughput;

	private InverseMeasure throughputPerClass[];

	/** The number of servers to estimate Utilization measure on multiserver environments. */
	private int serverNumber = 1;

	public PSJobInfoList(int numberOfJobClasses, boolean save) {
		list = new LinkedJobInfoList(numberOfJobClasses, save);

		// Initialize all the arrays
		sizePerClass = new int[numberOfJobClasses];
		jobsInPerClass = new int[numberOfJobClasses];
		jobsOutPerClass = new int[numberOfJobClasses];
		busyTimePerClass = new double[numberOfJobClasses];
		lastJobInTimePerClass = new double[numberOfJobClasses];
		lastJobOutTimePerClass = new double[numberOfJobClasses];
	}

	public boolean add(JobInfo jobInfo) {
		return list.add(jobInfo);
	}

	public boolean addFirst(JobInfo jobInfo) {
		return list.addFirst(jobInfo);
	}

	public boolean addLast(JobInfo jobInfo) {
		return list.addLast(jobInfo);
	}

	public boolean add(int index, JobInfo jobInfo) {
		return list.add(index, jobInfo);
	}

	public boolean add(int index, JobInfo jobInfo, boolean isClassTail) {
		return list.add(index, jobInfo, isClassTail);
	}

	/**--------------------------------------------------------
	 *---------------- "ANALYZE" METHODS ----------------------
	 *--------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeUtilization(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeUtilization(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (utilizationPerClass == null) {
				utilizationPerClass = new Measure[numberOfJobClasses];
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
		list.analyzeUtilizationJoin(jobClass, measurement);
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeResponseTime(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeResponseTime(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (responseTimePerClass == null) {
				responseTimePerClass = new Measure[numberOfJobClasses];
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
		list.analyzeDropRate(jobClass, measurement);
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeThroughput(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.InverseMeasure)
	 */
	public void analyzeThroughput(JobClass jobClass, InverseMeasure measurement) {
		if (jobClass != null) {
			if (throughputPerClass == null) {
				throughputPerClass = new InverseMeasure[numberOfJobClasses];
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
				residenceTimePerClass = new Measure[numberOfJobClasses];
			}
			residenceTimePerClass[jobClass.getId()] = measurement;
		} else {
			residenceTime = measurement;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#analyzeQueueLength(jmt.engine.QueueNet.JobClass, jmt.engine.dataAnalysis.Measure)
	 */
	public void analyzeQueueLength(JobClass jobClass, Measure measurement) {
		if (jobClass != null) {
			if (queueLengthPerClass == null) {
				queueLengthPerClass = new Measure[numberOfJobClasses];
			}
			queueLengthPerClass[jobClass.getId()] = measurement;
		} else {
			queueLength = measurement;
		}
	}

	public double getBusyTime() throws NetException {
		return busyTime;
	}

	public double getBusyTimePerClass(JobClass jobClass) throws NetException {
		return busyTimePerClass[jobClass.getId()];
	}

	public List<JobInfo> getJobList() {
		return list.getJobList();
	}

	public int getJobsIn() {
		return jobsIn;
	}

	public int getJobsInPerClass(JobClass jobClass) {
		return jobsInPerClass[jobClass.getId()];
	}

	public int getJobsOut() {
		return jobsOut;
	}

	public int getJobsOutPerClass(JobClass jobClass) {
		return jobsOutPerClass[jobClass.getId()];
	}

	public double getLastJobDropTime() {
		return list.getLastJobDropTime();
	}

	public double getLastJobDropTimePerClass(JobClass jobClass) {
		return list.getLastJobDropTimePerClass(jobClass);
	}

	public double getLastJobInTime() {
		return lastJobInTime;
	}

	public double getLastJobInTimePerClass(JobClass jobClass) {
		return lastJobInTimePerClass[jobClass.getId()];
	}

	public double getLastJobOutTime() {
		return lastJobOutTime;
	}

	public double getLastJobOutTimePerClass(JobClass jobClass) {
		return lastJobOutTimePerClass[jobClass.getId()];
	}

	public double getLastModifyTime() {
		if (lastJobOutTime >= lastJobInTime && lastJobOutTime >= list.getLastJobDropTime()) {
			return lastJobOutTime;
		} else if (lastJobInTime >= lastJobOutTime && lastJobInTime >= list.getLastJobDropTime()) {
			return lastJobInTime;
		} else {
			return list.getLastJobDropTime();
		}
	}

	public double getLastModifyTimePerClass(JobClass jobClass) {
		if (lastJobOutTimePerClass[jobClass.getId()] >= lastJobInTimePerClass[jobClass.getId()]
				&& lastJobOutTimePerClass[jobClass.getId()] >= list.getLastJobDropTimePerClass(jobClass)) {
			return lastJobOutTimePerClass[jobClass.getId()];
		} else if (lastJobInTimePerClass[jobClass.getId()] >= lastJobOutTimePerClass[jobClass.getId()]
				&& lastJobInTimePerClass[jobClass.getId()] >= list.getLastJobDropTimePerClass(jobClass)) {
			return lastJobInTimePerClass[jobClass.getId()];
		} else {
			return list.getLastJobDropTimePerClass(jobClass);
		}
	}

	public JobInfo lookFor(Job job) throws NetException {
		return list.lookFor(job);
	}

	public boolean remove(JobInfo jobInfo) throws NetException {
		return list.remove(jobInfo);
	}

	public boolean removeJob(JobInfo jobInfo) throws NetException {
		return list.removeJob(jobInfo);
	}

	public boolean redirectJob(JobInfo jobInfo) throws NetException {
		return list.redirectJob(jobInfo);
	}

	public boolean dropJob(JobInfo jobInfo) throws NetException {
		return list.dropJob(jobInfo);
	}

	public boolean produceJob(JobInfo jobInfo) throws NetException {
		return list.produceJob(jobInfo);
	}

	public boolean consumeJob(JobInfo jobInfo) throws NetException {
		return list.consumeJob(jobInfo);
	}

	public JobInfo removeFirst() throws NetException {
		return list.removeFirst();
	}

	public JobInfo removeFirst(JobClass jobClass) throws NetException {
		return list.removeFirst(jobClass);
	}

	public JobInfo removeLast() throws NetException {
		return list.removeLast();
	}

	public JobInfo removeLast(JobClass jobClass) throws NetException {
		return list.removeLast(jobClass);
	}

	public int size() {
		return size;
	}

	public int size(JobClass jobClass) {
		return sizePerClass[jobClass.getId()];
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#setServerNumber(int)
	 */
	public void setServerNumber(int serverNumber) {
		this.serverNumber = serverNumber;
	}

	public void psJobIn(JobClass jobClass, double time) {
		size++;
		sizePerClass[jobClass.getId()]++;
		jobsIn++;
		jobsInPerClass[jobClass.getId()]++;
		lastJobInTime = time;
		lastJobInTimePerClass[jobClass.getId()] = time;
	}

	public void psJobOut(JobClass jobClass, double time) {
		size--;
		sizePerClass[jobClass.getId()]--;
		jobsOut++;
		jobsOutPerClass[jobClass.getId()]++;
		lastJobOutTime = time;
		lastJobOutTimePerClass[jobClass.getId()] = time;
	}

	public void psUpdateUtilization(JobClass jobClass, double multiplier, double time) {
		double lastModify = Math.max(getLastJobInTime(), getLastJobOutTime());
		double lastModifyPerClass = Math.max(getLastJobInTimePerClass(jobClass), getLastJobOutTimePerClass(jobClass));
		double value = size();
		double valuePerClass = size(jobClass);
		update(utilization, value * multiplier / serverNumber, time - lastModify);
		update(utilizationPerClass, jobClass, valuePerClass * multiplier / serverNumber, time - lastModifyPerClass);
		update(queueLength, value * multiplier, time - lastModify);
		update(queueLengthPerClass, jobClass, valuePerClass * multiplier, time - lastModifyPerClass);
	}

	public void psUpdateBusyTimes(JobClass jobClass, double busyTime) {
		int id = jobClass.getId();
		update(residenceTime, busyTime, 1.0);
		update(residenceTimePerClass, jobClass, busyTime, 1.0);
		update(responseTime, busyTime, 1.0);
		update(queueLengthPerClass, jobClass, busyTime, 1.0);
		this.busyTime += busyTime;
		busyTimePerClass[id] += busyTime;
	}

	public void psUpdateThroughput(JobClass jobClass) {
		update(throughput, NetSystem.getTime() - getLastJobOutTime(), 1.0);
		update(throughputPerClass, jobClass, NetSystem.getTime() - getLastJobOutTimePerClass(jobClass), 1.0);
	}

	/**
	 * Updates a measure if it is not null
	 * @param m the measure, may be null
	 * @param value the value
	 * @param weight the weight
	 */
	private void update(Measure m, double value, double weight) {
		if (m != null) {
			m.update(value, weight);
		}
	}

	/**
	 * Updates a perclass measure if it is not null
	 * @param m the measure, may be null
	 * @param value the value
	 * @param weight the weight
	 */
	private void update(Measure[] m, JobClass jobClass, double value, double weight) {
		if (m != null && m[jobClass.getId()] != null) {
			m[jobClass.getId()].update(value, weight);
		}
	}

	/**
	 * @return the internal jobInfoList to be used by Queue class.
	 */
	public JobInfoList getInternalList() {
		return list;
	}

	public void analyzeResponseTimePerSink(JobClass jobClass, Measure measurement) {
		list.analyzeResponseTimePerSink(jobClass, measurement);
	}

	public void analyzeThroughputPerSink(JobClass jobClass, InverseMeasure measurement) {
		list.analyzeThroughputPerSink(jobClass, measurement);
	}

	public int getJobsInPerClass(int id) {
		return jobsInPerClass[id];
	}

	public int getJobsOutPerClass(int id) {
		return jobsOutPerClass[id];
	}

}
