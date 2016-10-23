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
import java.util.ListIterator;

import jmt.common.exception.NetException;
import jmt.engine.NodeSections.BlockingQueue;
import jmt.engine.NodeSections.ClassSwitch;
import jmt.engine.NodeSections.Enabling;
import jmt.engine.NodeSections.Firing;
import jmt.engine.NodeSections.Fork;
import jmt.engine.NodeSections.Queue;
import jmt.engine.NodeSections.Store;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.dataAnalysis.TempMeasure;

/**
 * This class implements a queue network.
 * @author Francesco Radaelli, Bertoli Marco (Added support for global measures)
 */
public class QueueNetwork {

	/* Symbolic name for reference node in the network: it should be used
	 * within broadcast communication.
	 */
	public static final int REFERENCE_NODE = 0x0001;

	/* Symbolic name for normal node in the network: it should be used
	 * within broadcast communication.
	 */
	public static final int NODE = 0x0002;

	/** Behaviour ID: Aborts the simulation when a measure has been obtained.*/
	public final static int BEHAVIOUR_ABORT = 0x001;
	/** Behaviour ID: Stops the simulation when a measure has been obtained.*/
	public final static int BEHAVIOUR_STOP = 0x002;
	/** Behaviour ID: Continues the simulation when a measure has been obtained.*/
	public final static int BEHAVIOUR_CONTINUE = 0x003;
	/** Behaviour ID: Continues and waits other measures when a measure has been obtained.*/
	public final static int BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_ABORT = 0x004;
	/** Behaviour ID: Continues and waits other measures when a measure has been obtained.*/
	public final static int BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_STOP = 0x005;

	/** State ID: Initial state, the network is ready to be started.*/
	public final static int STATE_READY = 0x0001;
	/** State ID: The network is running.*/
	public final static int STATE_RUNNING = 0x0002;
	/** State ID: The network has been stopped.*/
	public final static int STATE_STOPPED = 0x0003;
	/** State ID: The network has been aborted.*/
	public final static int STATE_ABORTED = 0x0004;
	/** State ID: The network is in final state.*/
	public final static int STATE_FINAL = 0x0005;

	/** Measure ID: queue length */
	public final static int QUEUE_LENGTH = 0x001;
	/** Measure ID: residence time */
	public final static int RESIDENCE_TIME = 0x002;
	/** Measure ID: residence time */
	public final static int SERVICE_TIME = 0x003;

	private NodeList referenceNodes;

	private NodeList nodes;

	private LinkedList<Measure> measures;

	private JobClassList jobClasses;

	private String name;

	private int behaviour;

	private int state;

	private GlobalJobInfoList jobInfoList;

	private boolean taskWarningShown;

	/** Creates a new instance of QueueNetwork. */
	public QueueNetwork(String name) {
		nodes = new NodeList();
		referenceNodes = new NodeList();
		jobClasses = new JobClassList();
		this.name = name;
		measures = new LinkedList<Measure>();
		behaviour = BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_STOP;
		state = STATE_READY;
		taskWarningShown = false;
	}

	/** Adds a generic node to the network. If the node has no inputs the node
	 * is set as reference node.
	 * @param node node to be added.
	 */
	public void addNode(NetNode node) throws jmt.common.exception.NetException {
		nodes.add(node);
		if (node.getInputNodes().size() == 0) {
			//if no input nodes are present, it is presumed that this node
			//must be a source of jobs
			referenceNodes.add(node);
		}
		node.setNetwork(this);
	}

	/** Adds a node to the network and forces it to be a reference node.
	 * @param node node to be added.
	 */
	public void addReferenceNode(NetNode node) throws jmt.common.exception.NetException {
		nodes.add(node);
		referenceNodes.add(node);
		node.setNetwork(this);
	}

	/** Adds a new job class to the network.
	 *  @param jobClass Job class to be added.
	 */
	public void addJobClass(JobClass jobClass) {
		jobClasses.add(jobClass);
		jobClass.setId(jobClasses.indexOf(jobClass));
	}

	/**
	 * Gets the list of the queue network nodes.
	 * @return Queue network nodes.
	 */
	public NodeList getNodes() {
		return nodes;
	}

	/**
	 * Gets the NetNode with the specified name.
	 *
	 * @param name the name of the node
	 * @return the specified node
	 */
	public NetNode getNode(String name) {
		return nodes.get(name);
	}

	/**
	 * Gets the JobClass with the specified name.
	 *
	 * @param name the name of the JobClass
	 * @return the job class. Null if it does not exist.
	 */
	public JobClass getJobClass(String name) {
		return jobClasses.get(name);
	}

	/**Gets the specified measure from the node with the specified name.
	 *
	 * @param nodeName name of the node
	 * @param measureID the measure id (chosen from QUEUE_LENGTH, RESIDENCE_TIME, SERVICE_TIME)
	 * @return the measure
	 * @throws jmt.common.exception.NetException
	 */
	public double getMeasure(String nodeName, int measureID) throws jmt.common.exception.NetException {
		double measure = 0;
		boolean found = false;
		switch (measureID) {
		case QUEUE_LENGTH:
			measure = nodes.get(nodeName).getDoubleNodeProperty(NetNode.PROPERTY_ID_RESIDENT_JOBS);
			found = true;
			break;
		case RESIDENCE_TIME:
			measure = nodes.get(nodeName).getDoubleNodeProperty(NetNode.PROPERTY_ID_RESIDENCE_TIME);
			found = true;
			break;
		case SERVICE_TIME:
			measure = nodes.get(nodeName).getSection(NodeSection.SERVICE).getDoubleSectionProperty(NodeSection.PROPERTY_ID_RESIDENCE_TIME);
			found = true;
			break;
		}
		if (found) {
			return measure;
		} else {
			throw new jmt.common.exception.NetException(this, 0, "measure not available");
		}
	}

	/** Gets the list of the queue network reference nodes.
	 * @return Queue network reference nodes.
	 */
	public NodeList getReferenceNodes() {
		return referenceNodes;
	}

	/** Gets the list of the queue network job classes.
	 * @return Queue network job classes.
	 */
	public JobClassList getJobClasses() {
		return jobClasses;
	}

	/** Gets network name
	 * @return Network name.
	 */
	public String getName() {
		return name;
	}

	/** Adds a new measure to the network.
	 * @param measure Reference to the measure to be added.
	 */
	public void addMeasure(Measure measure) {
		// If GlobalJobInfoList is not set, creates it
		// (at this point all classes should be declared)
		csExists();
		if (jobInfoList == null) {
			jobInfoList = new GlobalJobInfoList(jobClasses.size(), csExists());
		}

		//sets the reference to network
		measure.setNetwork(this);
		measures.add(measure);
	}

	private boolean csExists() {
		boolean csExists = false;
		ListIterator<NetNode> it = nodes.listIterator();
		while (it.hasNext()) {
			NetNode node = it.next();
			NodeSection serviceSection = null;
			try {
				serviceSection = node.getSection(NodeSection.SERVICE);
			} catch (NetException e) {
				// NEVER ENTERS
				e.printStackTrace();
			}
			if (serviceSection != null && serviceSection instanceof ClassSwitch) {
				csExists = true;
				break;
			}
		}
		return csExists;
	}

	/**
	 * Returns Global jobInfoList associated with this queue network. This is used
	 * to calculate global measures
	 * @return Global JobInfoList of this network
	 */
	public GlobalJobInfoList getJobInfoList() {
		return jobInfoList;
	}

	/**
	 * Gets the measures
	 */
	public LinkedList<Measure> getMeasures() {
		return measures;
	}

	/**
	 * Gets the network behaviour (see behaviour constants).
	 *
	 */
	public int getBehaviour() {
		return behaviour;
	}

	/**
	 * Sets the network behaviour (see behaviour constants).
	 */
	public void setBehaviuor(int behaviour) {
		this.behaviour = behaviour;
	}

	/**
	 * Sets the network state (see state constants).
	 */
	void setState(int state) {
		this.state = state;
	}

	/**
	 * Gets the network state (see state constants).
	 */
	public int getState() {
		if (state == STATE_FINAL || state == STATE_READY) {
			return state;
		}
		boolean flag = false;
		ListIterator<NetNode> nodeList = nodes.listIterator();
		NetNode node;
		while (nodeList.hasNext()) {
			node = nodeList.next();
			if (node.isRunning()) {
				flag = true;
			}
		}
		if (flag) {
			return state;
		} else {
			state = STATE_FINAL;
			return STATE_FINAL;
		}
	}

	/**
	 * Get the total number of dropped jobs in the whole network
	 * @return the total number of dropped jobs in the whole network
	 */
	public int getDroppedJobs() {
		//dropped jobs
		int dropped = 0;
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection inputSection;
			try {
				inputSection = node.getSection(NodeSection.INPUT);
				if (inputSection instanceof Queue) {
					dropped += ((Queue) inputSection).getDroppedJobs();
				} else if (inputSection instanceof Store) {
					dropped += ((Store) inputSection).getDroppedJobs();
				} else if (inputSection instanceof BlockingQueue) {
					dropped += ((BlockingQueue) inputSection).getDroppedJobs();
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return dropped;
	}

	/**
	 * Get the total number of dropped jobs in the whole network for the specified class
	 * @param jobClass the job class
	 * @return the total number of dropped jobs in the whole network for the specified class
	 */
	public int getDroppedJobs(JobClass jobClass) {
		//dropped jobs
		int dropped = 0;
		int classIndex = jobClass.getId();
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection inputSection;
			try {
				inputSection = node.getSection(NodeSection.INPUT);
				if (inputSection instanceof Queue) {
					dropped += ((Queue) inputSection).getDroppedJobPerClass(classIndex);
				} else if (inputSection instanceof Store) {
					dropped += ((Store) inputSection).getDroppedJobPerClass(classIndex);
				} else if (inputSection instanceof BlockingQueue) {
					dropped += ((BlockingQueue) inputSection).getDroppedJobPerClass(classIndex);
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return dropped;
	}

	/**
	 * Gets the total number of switched jobs in the whole network
	 * @param jobClass the job class
	 * @return the total number of switched jobs in the whole network
	 */
	public int getSwitchedJobs() {
		//switched jobs
		int switched = 0;
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection section;
			try {
				section = node.getSection(NodeSection.SERVICE);
				if (section instanceof ClassSwitch) {
					switched += ((ClassSwitch) section).getSwitchedJobs();
				}
				section = node.getSection(NodeSection.OUTPUT);
				if (section instanceof Fork) {
					switched += ((Fork) section).getSwitchedJobs();
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return switched;
	}

	/**
	 * Gets the total number of switched jobs in the whole network for the specified class
	 * @param jobClass the job class
	 * @return the total number of switched jobs in the whole network for the specified class
	 */
	public int getSwitchedJobs(JobClass jobClass) {
		//switched jobs
		int switched = 0;
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection section;
			try {
				section = node.getSection(NodeSection.SERVICE);
				if (section instanceof ClassSwitch) {
					switched += ((ClassSwitch) section).getSwitchedJobsPerClass(jobClass.getId());
				}
				section = node.getSection(NodeSection.OUTPUT);
				if (section instanceof Fork) {
					switched += ((Fork) section).getSwitchedJobsPerClass(jobClass.getId());
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return switched;
	}

	/**
	 * Gets the total number of produced jobs in the whole network
	 * @param jobClass the job class
	 * @return the total number of produced jobs in the whole network
	 */
	public int getProducedJobs() {
		//produced jobs
		int produced = 0;
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection section;
			try {
				section = node.getSection(NodeSection.INPUT);
				if (section instanceof Enabling) {
					produced -= ((Enabling) section).getConsumedJobs();
				}
				section = node.getSection(NodeSection.OUTPUT);
				if (section instanceof Firing) {
					produced += ((Firing) section).getProducedJobs();
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return produced;
	}

	/**
	 * Gets the total number of produced jobs in the whole network for the specified class
	 * @param jobClass the job class
	 * @return the total number of produced jobs in the whole network for the specified class
	 */
	public int getProducedJobs(JobClass jobClass) {
		//produced jobs
		int produced = 0;
		for (int i = 0; i < nodes.size(); i++) {
			NetNode node = nodes.get(i);
			NodeSection section;
			try {
				section = node.getSection(NodeSection.INPUT);
				if (section instanceof Enabling) {
					produced -= ((Enabling) section).getConsumedJobsPerClass(jobClass.getId());
				}
				section = node.getSection(NodeSection.OUTPUT);
				if (section instanceof Firing) {
					produced += ((Firing) section).getProducedJobsPerClass(jobClass.getId());
				}
			} catch (NetException ne) {
				continue;
			}
		}
		return produced;
	}

	/**
	 * Aborts all the measures linked to the queue network
	 */
	public void abortAllMeasures() {
		LinkedList<Measure> measures = this.getMeasures();
		TempMeasure tmp;
		for (Measure m: measures) {
			tmp = new TempMeasure(m);
			tmp.refreshMeasure();
			tmp.abort();
		}
	}

	public boolean isTaskWarningShown() {
		return taskWarningShown;
	}

	public void setTaskWarningShown(boolean taskWarningShown) {
		this.taskWarningShown = taskWarningShown;
	}

}
