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
import jmt.engine.QueueNet.GlobalJobInfoList;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.random.engine.RandomEngine;

/**
 * This class implements a class switch.
 * 
 * @author Sebatiano Spicuglia, Arif Canakoglu
 */
public class ClassSwitch extends ServiceSection {

	private int switchedJobs;
	private int[] switchedJobsPerClass;
	private RandomEngine engine;

	private Float[][] matrix;

	public ClassSwitch(Object matrix[]) {
		super();
		this.matrix = new Float[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			Float[] row = (Float[]) matrix[i];
			for (int j = 0; j < row.length; j++) {
				this.matrix[i][j] = row[j];
			}
		}
		switchedJobs = 0;
		switchedJobsPerClass = new int[matrix.length];
		Arrays.fill(switchedJobsPerClass, 0);
		engine = RandomEngine.makeDefault();
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		Job job;
		job = message.getJob();

		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
			break;

		case NetEvent.EVENT_ACK:
			sendBackward(message.getEvent(), message.getData(), 0.0);
			break;

		case NetEvent.EVENT_JOB:
			int jobClassIn = message.getJob().getJobClass().getId();
			int jobClassOut = chooseOutClass(matrix[jobClassIn]);
			//Find out class by using the matrix
			JobClass outClass = getJobClasses().get(jobClassOut);

			//Update counters
			if (!job.hasBeenClass(outClass)) {
				switchedJobs++;
				switchedJobsPerClass[jobClassOut]++;
			}

			sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);

			//remove job from node list
			JobInfoList local = getOwnerNode().getJobInfoList();

			GlobalJobInfoList global = getOwnerNode().getQueueNet().getJobInfoList();
			global.performJobClassSwitch(job, outClass);
			sendForward(message.getEvent(), job, 0.0);

			local.remove(local.lookFor(job));
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

	/**
	 * It choose randomly a position of @row.
	 * Let SUM the sum of all elements of @row
	 * The probability that this method chooses an
	 * index i is row[i]/SUM.
	 * @param row
	 * @return
	 */
	private int chooseOutClass(Float[] row) {
		float sum;
		float random;
		int i;

		sum = 0;
		for (i = 0; i < row.length; i++) {
			sum += row[i];
		}

		random = (float) (engine.raw() * sum);
		for (i = 0; i < row.length; i++) {
			random -= row[i];
			if (random <= 0)
				return i;
		}
		return i-1;
	}

	/**
	 * Gets the total number of switched jobs
	 * @return the total number of switched jobs
	 */
	public int getSwitchedJobs() {
		return switchedJobs;
	}

	/**
	 * Gets the number of switched jobs for the specified class
	 * @param classIndex the index of the job class
	 * @return the number of switched jobs for the specified class
	 */
	public int getSwitchedJobsPerClass(int classIndex) {
		return switchedJobsPerClass[classIndex];
	}

}
