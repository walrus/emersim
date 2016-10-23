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

import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/**
 * <p>Title: Global Job Info List</p>
 * <p>Description: This class provides a global "job info list" to be used during
 * simulation to compute global measures. This is designed to be associated to a
 * simulation object.</p>
 *
 * @author Bertoli Marco
 *         Date: 8-mar-2006
 *         Time: 12.27.42
 *         
 *  Modified by Ashanka (Feb 2010)
 *  Desc: Modified the logic of System Power calculation. Earlier the logic was to capture the 
 *  	  response time (rn) of each job when it was entering the sink and to capture the time 
 *  	  of throughput (xn) for each job. Then x1/r1 ... x2/r2 .... x3/r3...
 *        was sent for System Power Simulation Engine. This logic was modified into :
 *        x1/r1 .. [x1/r1 + (x1+x2)/(r1+r2)]..[x1/r1 + (x1+x2)/(r1+r2) + (x1+x2+x3)/(r1+r2+r3)]..           
 */
public class GlobalJobInfoList {

	private int classNum;
	private boolean hasClassSwitch;

	private double lastJobOutTime;
	private double[] lastJobOutTimePerClass;

	private double lastJobDropTime;
	private double[] lastJobDropTimePerClass;

	private double lastModifyNumber;
	private double[] lastModifyNumberPerClass;

	private double responseSumWithCS;
	private double[] responseSumPerClassWithCS;

	private int jobs;
	private int[] jobsPerClass;

	private Measure responseTimeWOCS;
	private Measure[] responseTimePerClassWOCS;

	private Measure responseTimeWithCS;
	private Measure[] responseTimePerClassWithCS;

	private Measure jobNum;
	private Measure[] jobNumPerClass;

	private InverseMeasure dropRate;
	private InverseMeasure[] dropRatePerClass;

	private InverseMeasure throughput;
	private InverseMeasure[] throughputPerClass;

	private InverseMeasure systemPower;
	private InverseMeasure[] systemPowerPerClass;

	//Variables for the System Power calculation: New Modified version.
	double systemPowerSamples;
	double systemPowerSamplesClass[];

	double sampling_SystemResponseSum;
	double sampling_SystemThroughputSum;
	double samplingClass_SystemThroughputSum[];

	/**
	 * Creates a new GlobalJobInfoList
	 * @param classNum number of classes in current network model
	 */
	public GlobalJobInfoList(int classNum, boolean hasClassSwitch) {
		initialize(classNum, hasClassSwitch);
	}

	/**
	 * Resets this info list
	 * @param classNum number of classes in current network model
	 */
	private void initialize(int classNum, boolean hasClassSwitch) {
		this.classNum = classNum;
		this.hasClassSwitch = hasClassSwitch;
		lastJobOutTime = 0.0;
		lastJobOutTimePerClass = new double[classNum];
		lastJobDropTime = 0.0;
		lastJobDropTimePerClass = new double[classNum];
		lastModifyNumber = 0.0;
		lastModifyNumberPerClass = new double[classNum];
		responseSumWithCS = 0.0;
		responseSumPerClassWithCS = new double[classNum];
		jobs = 0;
		jobsPerClass = new int[classNum];

		responseTimeWOCS = null;
		responseTimePerClassWOCS = null;
		responseTimeWithCS = null;
		responseTimePerClassWithCS = null;
		jobNum = null;
		jobNumPerClass = null;
		dropRate = null;
		dropRatePerClass = null;
		throughput = null;
		throughputPerClass = null;
		systemPower = null;
		systemPowerPerClass = null;

		systemPowerSamples = 0.0;
		systemPowerSamplesClass = new double[classNum];

		sampling_SystemResponseSum = 0.0;
		sampling_SystemThroughputSum = 0.0;
		samplingClass_SystemThroughputSum = new double[classNum];
	}

	// --- Methods to be called on job events ---------------------------------------------
	/**
	 * This method MUST be called each time a new job is added to the network
	 * @param job identifier of added job
	 */
	public void addJob(Job job) {
		job.resetSystemEnteringTime();
		updateJobNumber(job);
		// Updates job number data structures
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
		jobs++;
		jobsPerClass[job.getJobClass().getId()]++;
	}

	/**
	 * This method MUST be called each time a job is removed from the network
	 * @param job identifier of removed job
	 */
	public void removeJob(Job job) {
		updateResponseTime(job);
		updateThroughput(job);
		updateJobNumber(job);
		updateSystemPower(job);

		// Updates jobs number and throughput data structures
		jobs--;
		jobsPerClass[job.getJobClass().getId()]--;
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();

		for (int index = 0; index < classNum; index++) {
			if (job.hasBeenClass(index)) {
				lastJobOutTimePerClass[job.getJobClass().getId()] = NetSystem.getTime();
			}
		}
		lastJobOutTime = NetSystem.getTime();
	}

	/**
	 * This method MUST be called each time a job cycles in its reference station
	 * @param job identifier of cycling job
	 */
	public void recycleJob(Job job) {
		updateResponseTime(job);
		updateThroughput(job);
		updateJobNumber(job);
		updateSystemPower(job);

		// Updates jobs number and throughput data structures
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();

		for (int index = 0; index < classNum; index++) {
			if (job.hasBeenClass(index)) {
				lastJobOutTimePerClass[index] = NetSystem.getTime();
			}
		}
		lastJobOutTime = NetSystem.getTime();

		job.resetSystemEnteringTime();
	}

	/**
	 * This method COULD be called to notify that a JOB exists
	 * @param job identifier of existing job
	 */
	public void existJob(Job job) {
		updateJobNumber(job);
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
	}

	/**
	 * This method must be called each time a job is dropped by a queue, a store or
	 * a blocking region
	 * @param job identifier of dropped job
	 */
	public void dropJob(Job job) {
		updateJobNumber(job);
		// Updates dropped jobs and drop percentage measure
		updateDropRate(job);

		// Updates jobs number and drop rate data structures
		jobs--;
		jobsPerClass[job.getJobClass().getId()]--;
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();

		lastJobDropTimePerClass[job.getJobClass().getId()] = lastJobDropTime = NetSystem.getTime();
	}

	/**
	 * This method MUST be called each time a job is produced by a transition
	 * @param job identifier of produced job
	 */
	public void produceJob(Job job) {
		updateJobNumber(job);
		// Updates job number data structure only
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
		jobs++;
		jobsPerClass[job.getJobClass().getId()]++;
	}

	/**
	 * This method MUST be called each time a job is consumed by a transition
	 * @param job identifier of consumed job
	 */
	public void consumeJob(Job job) {
		updateJobNumber(job);
		// Updates job number data structure only
		jobs--;
		jobsPerClass[job.getJobClass().getId()]--;
		lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
	}

	// ------------------------------------------------------------------------------------

	// --- Methods to specify measures to be analyzed -------------------------------------

	/**
	 * Analyzes System Response Time for a specific job class or for every class
	 * @param jobClass specified job class. If null measure will be job independent
	 * @param measure reference to a Measure object
	 */
	public void analyzeResponseTime(JobClass jobClass, Measure measure) {
		if (hasClassSwitch) {
			if (jobClass != null) {
				if (responseTimePerClassWithCS == null) {
					responseTimePerClassWithCS = new Measure[classNum];
				}
				responseTimePerClassWithCS[jobClass.getId()] = measure;
			} else {
				responseTimeWithCS = measure;
			}
		} else {
			if (jobClass != null) {
				if (responseTimePerClassWOCS == null) {
					responseTimePerClassWOCS = new Measure[classNum];
				}
				responseTimePerClassWOCS[jobClass.getId()] = measure;
			} else {
				responseTimeWOCS = measure;
			}
		}
	}

	/**
	 * Analyzes System Number of Jobs for a specific job class or for every class
	 * @param jobClass specified job class. If null measure will be job independent
	 * @param Measure reference to a Measure object
	 */
	public void analyzeJobNumber(JobClass jobClass, Measure Measure) {
		if (jobClass != null) {
			if (jobNumPerClass == null) {
				jobNumPerClass = new Measure[classNum];
			}
			jobNumPerClass[jobClass.getId()] = Measure;
		} else {
			jobNum = Measure;
		}
	}

	/**
	 * Analyzes System Throughput for a specific job class or for every class
	 * @param jobClass specified job class. If null measure will be job independent
	 * @param Measure reference to a Measure object
	 */
	public void analyzeThroughput(JobClass jobClass, Measure Measure) {
		if (jobClass != null) {
			if (throughputPerClass == null) {
				throughputPerClass = new InverseMeasure[classNum];
			}
			throughputPerClass[jobClass.getId()] = (InverseMeasure) Measure;
		} else {
			throughput = (InverseMeasure) Measure;
		}
	}

	/**
	 * Analyzes Drop Rate for a specific job class or for every class
	 * @param jobClass specified job class. If null measure will be job independent
	 * @param Measure reference to a Measure object
	 */
	public void analyzeDropRate(JobClass jobClass, Measure Measure) {
		if (jobClass != null) {
			if (dropRatePerClass == null) {
				dropRatePerClass = new InverseMeasure[classNum];
			}
			dropRatePerClass[jobClass.getId()] = (InverseMeasure) Measure;
		} else {
			dropRate = (InverseMeasure) Measure;
		}
	}

	// ------------------------------------------------------------------------------------

	// --- Methods to update measures -----------------------------------------------------

	/**
	 * Updates System Response Time measures.
	 * @param job current job
	 */
	private void updateResponseTime(Job job) {
		// with class switch
		if (responseTimePerClassWithCS != null) {
			for (int index = 0; index < classNum; index++) {
				if (job.hasBeenClass(index)) {
					Measure m = responseTimePerClassWithCS[index];
					if (m != null) {
						m.update(responseSumPerClassWithCS[index], 1.0);
						responseSumPerClassWithCS[index] = 0;
					}
				}
			}
		}
		if (responseTimeWithCS != null) {
			responseTimeWithCS.update(responseSumWithCS, 1.0);
			responseSumWithCS = 0;
		}

		// without class switch
		if (responseTimePerClassWOCS != null) {
			Measure m = responseTimePerClassWOCS[job.getJobClass().getId()];
			if (m != null) {
				m.update(NetSystem.getTime() - job.getSystemEnteringTime(), 1.0);
			}
		}
		if (responseTimeWOCS != null) {
			responseTimeWOCS.update(NetSystem.getTime() - job.getSystemEnteringTime(), 1.0);
		}
	}

	/**
	 * update the times in response time with class switch
	 * 
	 * @param jobInfo info of the job
	 */
	public void updateResponseSum(JobInfo jobInfo) {
		if (hasClassSwitch) {
			// skip forked job
			if (jobInfo.getJob() instanceof ForkJob)
				return;
			int jobClassId = jobInfo.getJob().getJobClass().getId();
			double time = NetSystem.getTime() - jobInfo.getTime();
			responseSumWithCS += time;
			responseSumPerClassWithCS[jobClassId] += time;
		}
	}

	/**
	 * Updates System Job Number measures.
	 * @param job current job
	 */
	private void updateJobNumber(Job job) {
		if (jobNumPerClass != null) {
			int index = job.getJobClass().getId();
			Measure m = jobNumPerClass[index];
			if (m != null) {
				m.update(jobsPerClass[index], NetSystem.getTime() - lastModifyNumberPerClass[index]);
			}
		}
		if (jobNum != null) {
			jobNum.update(jobs, NetSystem.getTime() - lastModifyNumber);
		}
	}

	/**
	 * Updates System Throughput measures.
	 * @param job current job
	 */
	private void updateThroughput(Job job) {
		if (throughputPerClass != null) {
			for (int index = 0; index < classNum; index++) {
				if (job.hasBeenClass(index)) {
					InverseMeasure m = throughputPerClass[index];
					if (m != null) {
						m.update(NetSystem.getTime() - lastJobOutTimePerClass[index], 1.0);
					}
				}
			}
		}
		if (throughput != null) {
			throughput.update(NetSystem.getTime() - lastJobOutTime, 1.0);
		}
	}

	/**
	 * Updates System Drop Rate measures.
	 * @param job current job
	 */
	private void updateDropRate(Job job) {
		if (dropRatePerClass != null) {
			int index = job.getJobClass().getId();
			Measure m = dropRatePerClass[index];
			if (m != null) {
				m.update(NetSystem.getTime() - lastJobDropTimePerClass[index], 1.0);
			}
		}
		if (dropRate != null) {
			dropRate.update(NetSystem.getTime() - lastJobDropTime, 1.0);
		}
	}

	/**
	 * Analyzes System Power for a specific job class or for every class
	 * @param jobClass specified job class. If null measure will be job independent
	 * @param Measure reference to a Measure object
	 */
	public void analyzeSystemPower(JobClass jobClass, Measure Measure) {
		if (jobClass != null) {
			if (systemPowerPerClass == null) {
				systemPowerPerClass = new InverseMeasure[classNum];
			}
			systemPowerPerClass[jobClass.getId()] = (InverseMeasure) Measure;
		} else {
			systemPower = (InverseMeasure) Measure;
		}
	}

	/**
	 * Updates System Throughput measures.
	 * @param job current job
	 */
	private void updateSystemPower(Job job) {
		sampling_SystemThroughputSum = sampling_SystemThroughputSum + (NetSystem.getTime() - lastJobOutTime);
		sampling_SystemResponseSum   = sampling_SystemResponseSum + (NetSystem.getTime() - job.getSystemEnteringTime());
		systemPowerSamples = systemPowerSamples + 1;
		if (systemPowerPerClass != null) {
			int index = job.getJobClass().getId();
			InverseMeasure m = systemPowerPerClass[index];
			samplingClass_SystemThroughputSum[index] = samplingClass_SystemThroughputSum[index] + NetSystem.getTime() - lastJobOutTimePerClass[index];
			systemPowerSamplesClass[index] = systemPowerSamplesClass[index] + 1;
			if (m != null) {
				double temp = (sampling_SystemResponseSum/systemPowerSamples) * (samplingClass_SystemThroughputSum[index] / systemPowerSamplesClass[index]);
				m.update(temp, 1.0);
			}
		}
		if (systemPower != null) {
			double tmp = (sampling_SystemResponseSum/systemPowerSamples) * (sampling_SystemThroughputSum / systemPowerSamples);
			systemPower.update(tmp, 1.0);
		}
	}

	// ------------------------------------------------------------------------------------

	/**
	 * It changes the class of @job, the new class will be @newClass.
	 * It also updates the performance indices.
	 * @param job the job you want to switch
	 * @param newClass the new class of @job
	 */
	public void performJobClassSwitch(Job job, JobClass newClass) {
		// Get the identifiers of old and new classes
		int oldClassId = job.getJobClass().getId();
		int newClassId = newClass.getId();

		// Updates old class measure (if not null)
		if (jobNumPerClass != null && jobNumPerClass[oldClassId] != null) {
			jobNumPerClass[oldClassId].update(jobsPerClass[oldClassId], NetSystem.getTime() - lastModifyNumberPerClass[oldClassId]);
		}
		lastModifyNumberPerClass[oldClassId] = NetSystem.getTime();

		// Updates new class measure (if not null)
		if (jobNumPerClass != null && jobNumPerClass[newClassId] != null) {
			jobNumPerClass[newClassId].update(jobsPerClass[newClassId], NetSystem.getTime() - lastModifyNumberPerClass[newClassId]);
		}
		lastModifyNumberPerClass[newClassId] = NetSystem.getTime();

		// Switches job class
		job.setClass(newClass);
		jobsPerClass[oldClassId]--;
		jobsPerClass[newClassId]++;
	}

}
