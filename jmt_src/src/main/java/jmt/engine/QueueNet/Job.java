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

/**
 *	This class implements a generic job of a queue network.
 * 	@author Francesco Radaelli, Marco Bertoli
 */
public class Job implements Cloneable {

	// bitwise data, if the job have ever been as job class
	private long classesBitwise;

	//counter used to generate id
	private static int counter;
	//job ID
	private int Id;

	//Class of this job
	private JobClass jobClass;
	//used to compute residence time: born time is reset when a job enters a station
	private double BornTime;
	//used to compute system response time
	protected double systemEnteringTime;

	/*
	This fields are used with blocking region.
	The presence of an input station, in fact, modifies the route of some jobs:
	instead of being processed directly by the destination node, they are first
	redirected to the region input station (which check the capability of the
	blocking region) and then returned to the destination node, using the
	informations contained in this object.
	 */

	//true if this job has been redirected
	private boolean redirected = false;
	// the original destination of the job message
	private NetNode originalDestinationNode = null;

	private double serviceTime = 0.0;

	/** Creates a new instance of Job.
	 *  @param jobClass Reference to the class of the job.
	 */
	public Job(JobClass jobClass) {
		//setClass(jobClass);
		this.jobClass = jobClass;
		// Job Id is used only for logging
		this.Id = counter++;

		resetSystemEnteringTime();
	}

	/** Gets the class of this job.
	 * @return Value of property Class.
	 */
	public JobClass getJobClass() {
		return jobClass;
	}

	/** Sets born time.*/
	public void born() {
		BornTime = NetSystem.getTime();
	}

	/** Gets born time.
	 * @return Born time.
	 */
	public double getBornTime() {
		return BornTime;
	}

	/** Resets born time of the job. */
	void reborn() {
		BornTime = NetSystem.getTime();
	}

	/** Gets job id: job id is used mainly for logging.
	 * @return Job Id.
	 */
	public int getId() {
		return Id;
	}

	public void resetSystemEnteringTime() {
		systemEnteringTime = NetSystem.getTime();
		resetClass();
	}

	public double getSystemEnteringTime() {
		return systemEnteringTime;
	}

	public void setSystemEnteringTime(double time) {
		systemEnteringTime = time;
	}

	/**
	 * Tells whether this job has been redirected (used for blocking regions)
	 * @return true if the job has been redirected
	 */
	public boolean isRedirected() {
		return redirected;
	}

	/**
	 * Sets <tt>redirected</tt> attribute
	 * @param redirected true to mark the job as redirected
	 */
	public void setRedirected(boolean redirected) {
		this.redirected = redirected;
	}

	/**
	 * Gets the destination node of this redirected job
	 * @return the destination node, if this job has been redirected, null otherwise
	 */
	public NetNode getOriginalDestinationNode() {
		if (redirected) {
			return originalDestinationNode;
		} else {
			return null;
		}
	}

	/**
	 * Sets the destination node of a redirected job and sets <tt>redirected</tt> to true
	 * @param originalDestinationNode the destination node
	 */
	public void setOriginalDestinationNode(NetNode originalDestinationNode) {
		this.originalDestinationNode = originalDestinationNode;
		redirected = true;
	}

	public static void resetCounter() {
		counter = 0;
	}

	/**
	 * Setter for field JobClass
	 * @param newClass the new value of JobClass
	 */
	public void setClass(JobClass newClass) {
		jobClass = newClass;
		classesBitwise |= newClass.getIdBitwise();
		//Long.toBinaryString(classesBitwise);
	}

	/** Adds a class to job history
	 * @param newClass the JobClass to add
	 */
	public void addClass(JobClass newClass) {
		classesBitwise |= newClass.getIdBitwise();
		//Long.toBinaryString(classesBitwise);
	}

	/**
	 * Reset bitwise 
	 */
	private void resetClass() {
		classesBitwise = jobClass.getIdBitwise();
	}

	/** 
	 * If the job have ever been as jobClass
	 * @param jobClass the class of the job if the job has ever been the class
	 * @return true if the job currently of given class or previously in this class
	 */
	public boolean hasBeenClass(JobClass jobClass) {
		return hasBeenClass(jobClass.getIdBitwise());
	}

	/** 
	 * If the job have ever been as jobClass id
	 * @param jobClassId the class of the job if the job has ever been the class
	 * @return true if the job currently of given class or previously in this class
	 */
	public boolean hasBeenClass(int jobClassId) {
		long idBitwise = JobClass.getIdBitwise(jobClassId);
		return  hasBeenClass(idBitwise);
	}

	/** 
	 * If the job have ever been as jobClass as idBitwise
	 * @param jobClassId the class of the job if the job has ever been the class
	 * @return true if the job currently of given class or previously in this class
	 */
	private boolean hasBeenClass(long idBitwise) {
		return  (classesBitwise & idBitwise) > 0 ;
	}

	public double getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(double time) {
		serviceTime = time;
	}
}
