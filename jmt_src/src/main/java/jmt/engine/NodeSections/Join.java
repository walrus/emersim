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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.JoinStrategies.GuardJoin;
import jmt.engine.NetStrategies.JoinStrategies.NormalJoin;
import jmt.engine.NetStrategies.JoinStrategy;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.ForkJob;
import jmt.engine.QueueNet.GlobalJobInfoList;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.LinkedJobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.QueueNet.SimConstants;
import jmt.engine.dataAnalysis.Measure;

/**
 * <p>Title: Join</p>
 * <p>Description: This class is a Join input section, used to re-assemble previously
 * forked jobs. Jobs are kept until all fragments are collected. If a non-fragmented
 * job is received, it is simply routed to service section.</p>
 *
 * @author Bertoli Marco
 *         Date: 13-mar-2006
 *         Time: 16.19.58
 * 
 * Modified by J. Shuai & M. Cazzoli, implemented join strategies
 */
public class Join extends InputSection {

	/** Data structure used to store received tasks for each job */
	private HashMap<Job, Integer> jobs;
	private HashMap<Job, Integer[]> required;
	private HashMap<Job, Integer> allJobs;
	private HashMap<Job, Boolean> hasFired;
	private HashMap<Job, Integer> counter;
	private HashMap<Job, List<ForkJob>> forkJobs;

	private JoinStrategy[] joinStrategies;

	private JobClassList jobClasses;
	private JobInfoList nodeJobsList;
	private GlobalJobInfoList netJobsList;
	private JobInfoList FJList;

	// --- Constructors -----------------------------------------------------------------------------
	/**
	 * Constructs a new Join
	 */
	public Join() {
		// Disables automatic handling of jobinfolists
		super(false);
		jobs = new HashMap<Job, Integer>();
		required = new HashMap<Job, Integer[]>();
		allJobs = new HashMap<Job, Integer>();
		hasFired = new HashMap<Job, Boolean>();
		forkJobs = new HashMap<Job, List<ForkJob>>();
		counter = new HashMap<Job, Integer>();
	}

	public Join(JoinStrategy[] joinStrategies) {
		this();
		this.joinStrategies = joinStrategies;
	}

	// ----------------------------------------------------------------------------------------------

	@Override
	protected void nodeLinked(NetNode node) {
		jobClasses = getJobClasses();
		nodeJobsList = node.getJobInfoList();
	};

	/**
	 * Assembles split tasks and sends an EVENT_JOIN to reference fork when done
	 *
	 * @param message message to be processed.
	 * @throws NetException if something goes wrong
	 * @return message processing result.
	 */
	@Override
	protected int process(NetMessage message) throws NetException {
		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
			netJobsList = getOwnerNode().getQueueNet().getJobInfoList();
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
				int needed;
				Integer[] neededPerClass = null;
				boolean isFired;
				if (allJobs.containsKey(fJob.getForkedJob())) {
					totalForked = allJobs.get(fJob.getForkedJob());
					needed = jobs.get(fJob.getForkedJob());
					neededPerClass = required.get(fJob.getForkedJob());
					isFired = hasFired.get(fJob.getForkedJob());
				} else {
					totalForked = fJob.getForkedNumber();
					needed = joinStrategies[fJob.getForkedJob().getJobClass().getId()].getRequiredNum();
					if (joinStrategies[fJob.getForkedJob().getJobClass().getId()] instanceof GuardJoin) {
						neededPerClass = ((GuardJoin) joinStrategies[fJob.getForkedJob().getJobClass().getId()]).getRequiredPerClass();
						needed = -1;
					}
					required.put(fJob.getForkedJob(), neededPerClass);
					isFired = false;

					if (myRegion != null) {
						if (joinStrategies[fJob.getForkedJob().getJobClass().getId()] instanceof NormalJoin) {
							counter.put(fJob.getForkedJob(), fJob.getForkedNumber());
						} else {
							counter.put(fJob.getForkedJob(), joinStrategies[fJob.getForkedJob().getJobClass().getId()].getRequiredNum());
						}
					}
				}
				totalForked--;

				// If needed is zero, all pieces has been retrieved and job can be forwarded
				if (isFired) {
					JobInfo i = nodeJobsList.lookFor(fJob);
					if (i != null) {
						nodeJobsList.remove(i);
					}
					if (myRegion != null) {
						//myRegion.decreaseOccupation(fJob.getJobClass());
						myRegion.decreaseOccupation(fJob);
					}
				} else {
					needed--;
					boolean isGuardTrue = false;
					if (neededPerClass != null) {
						neededPerClass[fJob.getJobClass().getId()]--;
						isGuardTrue = true;
						for (Integer i: neededPerClass) {
							isGuardTrue = isGuardTrue && (i <= 0);
						}
					}
					jobs.put(fJob.getForkedJob(), needed);
					hasFired.put(fJob.getForkedJob(), false);

					boolean allArrived = (joinStrategies[fJob.getForkedJob().getJobClass().getId()] instanceof NormalJoin) && (totalForked == 0);
					if (needed == 0 || allArrived || isGuardTrue) {
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

						// Sends job forward
						sendForward(fJob.getForkedJob(), 0.0);
						hasFired.put(fJob.getForkedJob(), true);
						// Notify fork node (to support blocking)
						send(NetEvent.EVENT_JOIN, fJob.getForkedJob(), 0.0, NodeSection.OUTPUT, fJob.getReferenceFork().getOwnerNode());

						if (getOwnerNode().getSection(NodeSection.OUTPUT) instanceof Fork) {
							if (FJList != null) {
								FJList.add(new JobInfo(fJob.getForkedJob()));
							}
						}
					}
				}

				if (totalForked == 0) {
					jobs.remove(fJob.getForkedJob());
					required.remove(fJob.getForkedJob());
					allJobs.remove(fJob.getForkedJob());
					hasFired.remove(fJob.getForkedJob());
					forkJobs.remove(fJob.getForkedJob());
				} else {
					allJobs.put(fJob.getForkedJob(), totalForked);
				}
			} else {
				// If this is not a fork job, sends it forward
				sendForward(job, 0.0);
				if (getOwnerNode().getSection(NodeSection.OUTPUT) instanceof Fork) {
					if (FJList != null) {
						FJList.add(new JobInfo(job));
					}
				}
			}
			break;

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_JOIN:
			Job j = (Job) message.getData();
			JobInfo jData = null;
			if (FJList != null) {
				jData = FJList.lookFor(j);
			}
			if (jData != null && FJList != null) {
				FJList.remove(jData);
				netJobsList.updateResponseSum(jData);
			}
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

	public int getDeTimes(Job job) {
		return counter.remove(job);
	}

	public BlockingRegion getMyRegion() {
		return myRegion;
	}

	@Override
	public void analyzeJoin(int name, JobClass jobClass, Measure measurement) throws NetException {
		switch (name) {
			case SimConstants.UTILIZATION:
				nodeJobsList.analyzeUtilizationJoin(jobClass, measurement);
			default:
				throw new NetException(this, EXCEPTION_MEASURE_DOES_NOT_EXIST, "required analyzer does not exist!");
		}
	}

	@Override
	public void analyzeFJ(int name, JobClass jobClass, Measure measurement) throws NetException {
		if (FJList == null) {
			FJList = new LinkedJobInfoList(jobClasses.size(), true);
		}
		switch (name) {
		case SimConstants.FORK_JOIN_RESPONSE_TIME:
			FJList.analyzeResponseTime(jobClass, measurement);
			break;
		case SimConstants.FORK_JOIN_NUMBER_OF_JOBS:
			FJList.analyzeQueueLength(jobClass, measurement);
			break;
		default:
			throw new NetException(this, EXCEPTION_MEASURE_DOES_NOT_EXIST, "required analyzer does not exist!");
		}
	}

}
