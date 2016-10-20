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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.SemaphoreStrategy;
import jmt.engine.NetStrategies.SemaphoreStrategies.NormalSemaphore;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.ForkJob;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;

/**
 * <p>Title: Semaphore</p>
 * <p>Description: This class is a Semaphore input section, used to stop a
 * percentage of forked tasks. Tasks are kept until they reach the threshold. If
 * a non-fragmented job is received, it is simply routed to service section.</p>
 *
 * @author Vitor S. Lopes
 *         Date: 25-jul-2016
 *         Time: 11.37.58
 */
public class Semaphore extends InputSection {

	/** Data structure used to store received tasks for each job */	
	private HashMap<Job, Integer> required;
	private HashMap<Job, Integer> aggregated;
	private HashMap<Job, Integer> allJobs;
	private HashMap<Job, Boolean> hasFired;
	private HashMap<Job, Integer> counter;
	private HashMap<Job, List<ForkJob>> forkJobs;

	private SemaphoreStrategy[] semaphoreStrategies;

	private JobInfoList nodeJobsList;

	// --- Constructors -----------------------------------------------------------------------------

	/**
	 * Constructs a new Semaphore
	 */
	public Semaphore() {
		// Disables automatic handling of jobinfolist
		super(false);
		required = new HashMap<Job, Integer>();
		aggregated = new HashMap<Job, Integer>();
		allJobs = new HashMap<Job, Integer>();
		hasFired = new HashMap<Job, Boolean>();
		forkJobs = new HashMap<Job, List<ForkJob>>();
		counter = new HashMap<Job, Integer>();
	}

	public Semaphore(SemaphoreStrategy[] semaphoreStrategies) {
		this();
		this.semaphoreStrategies = semaphoreStrategies;
	}

	// ----------------------------------------------------------------------------------------------

	@Override
	protected void nodeLinked(NetNode node) {
		nodeJobsList = node.getJobInfoList();
	};

	/**
	 * @param message message to be processed.
	 * @throws NetException if something goes wrong
	 * @return message processing result.
	 */
	@Override
	protected int process(NetMessage message) throws NetException {
		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
			break;

		case NetEvent.EVENT_JOB:
			Job job = message.getJob();
			send(NetEvent.EVENT_ACK, job, 0.0, message.getSourceSection(), message.getSource());

			if (job instanceof ForkJob) {
				ForkJob fJob = (ForkJob) job;
				List<ForkJob> fJobs = forkJobs.get(fJob.getForkedJob());
				if (fJobs == null) {
					fJobs = new ArrayList<ForkJob>();
					forkJobs.put(fJob.getForkedJob(), fJobs);
					JobInfo temp = new JobInfo(job);
					jobsList.add(temp);
					jobsList.remove(temp);
				}
				fJobs.add(fJob);

				int totalForked;
				Integer neededPerClass = null;
				Integer totalAggregated = null;
				boolean isFired;
				if (allJobs.containsKey(fJob.getForkedJob())) {
					totalForked = allJobs.get(fJob.getForkedJob());                                               
					neededPerClass = required.get(fJob.getForkedJob());
					totalAggregated = aggregated.get(fJob.getForkedJob());
					isFired = hasFired.get(fJob.getForkedJob());
				} else {
					totalForked = fJob.getForkedNumber();
					neededPerClass = ((NormalSemaphore) semaphoreStrategies[fJob.getForkedJob().getJobClass().getId()]).getSemaphoreThres();
					if (neededPerClass == 0) {
						neededPerClass = 1;
					}
					if (neededPerClass > totalForked) {
						neededPerClass = totalForked;
					}
					totalAggregated = neededPerClass;

					required.put(fJob.getForkedJob(), neededPerClass);
					aggregated.put(fJob.getForkedJob(), totalAggregated);
					isFired = false;

					if (myRegion != null) {
						counter.put(fJob.getForkedJob(), fJob.getForkedNumber());
						counter.put(fJob.getForkedJob(), semaphoreStrategies[fJob.getForkedJob().getJobClass().getId()].getSemaphoreThres());
					}
				}
				totalForked--;

				if (isFired) {
					JobInfo i = nodeJobsList.lookFor(fJob);
					if (i != null) {
						nodeJobsList.remove(i);
					}
					if (myRegion != null) {
						//myRegion.decreaseOccupation(fJob.getJobClass());
						myRegion.decreaseOccupation(fJob);
					}
					sendForward(job, 0.0);
				} else {
					boolean isGuardTrue = false;
					if (neededPerClass != null) {
						neededPerClass--;
						if (neededPerClass <= 0) {
							isGuardTrue = true;
						}
					}
					hasFired.put(fJob.getForkedJob(), false);
					required.put(fJob.getForkedJob(), neededPerClass);

					if (isGuardTrue) {
						for (ForkJob j : forkJobs.get(fJob.getForkedJob())) {
							JobInfo i = nodeJobsList.lookFor(j);
							if (i != null) {
								nodeJobsList.remove(i);
							}
							if (myRegion != null) {
								JobInfo ji = myRegion.getInputStation().getJobInfoList().lookFor(j);
								if (ji != null) {
									myRegion.getInputStation().getJobInfoList().remove(ji);
								}
							}
						}
						if (myRegion != null) {
							myRegion.getInputStation().getJobInfoList().remove(new JobInfo(fJob.getForkedJob()));
						}

						for (int n = 0; n < totalAggregated; n++) {
							sendForward(job, 0.0);
							hasFired.put(fJob.getForkedJob(), true);
						}
					}
				}

				if (totalForked == 0) {
					required.remove(fJob.getForkedJob());
					aggregated.remove(fJob.getForkedJob());
					allJobs.remove(fJob.getForkedJob());
					hasFired.remove(fJob.getForkedJob());
					forkJobs.remove(fJob.getForkedJob());
				} else {
					allJobs.put(fJob.getForkedJob(), totalForked);
				}
			} else {
				sendForward(job, 0.0);
			}
			break;

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_JOB_RELEASE:
			break;

		case NetEvent.EVENT_JOB_FINISH:
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		// Everything was okay
		return MSG_PROCESSED;
	}

	private boolean redirectionON;
	private BlockingRegion myRegion;
	private NetNode regionInputStation;

	public void addRegion(BlockingRegion region) {
		redirectionON = true;
		myRegion = region;
		regionInputStation = myRegion.getInputStation();
	}

	public BlockingRegion getMyRegion() {
		return myRegion;
	}

	public int getDeTimes(Job job) {
		return counter.remove(job);
	}

}
