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

import java.util.ArrayList;
import java.util.List;

import jmt.engine.dataAnalysis.Measure;
import jmt.engine.simEngine.Simulation;

/**
 * This class represents a blocking region, that is a region with a finite capacity.
 * <br>
 * The constraint can be global (the constraint is shared, i.e. relative to the total
 * occupation of jobs, without considering their own job class)
 * or relative to each class (in this case it may happen that the
 * jobs of one class are blocked, while the job of another one are not blocked).
 * <br>
 * If other jobs arrive when the region has already reached one or more constraints,
 * the jobs may be blocked or dropped. Each class can decide its own drop strategy,
 * which will be used when a constraint (global or class) is violated.
 * <br>
 * Each class can have a different weight, i.e. jobs of different class may require
 * different capacity.
 *
 *
 * @author Stefano Omini
 *
 */
public class BlockingRegion {

	public static final String REGION_GROUP_FCFS_STRATEGY = "FCFS";
	public static final String REGION_GROUP_HPS_STRATEGY = "HPS";
	public static final String REGION_GROUP_HPS_LCFS_STRATEGY = "HPS_LCFS";
	public static final String REGION_GROUP_HPS_FCFS_STRATEGY = "HPS_FCFS";

	/** The name of the blocking region */
	protected String name;

	/** the simulation */
	protected Simulation sim;
	/** the queue network the region belongs to */
	protected QueueNetwork network;

	/** the name of the nodes contained in the blocking region */
	protected String[] regionNodeNames;
	/** the nodes contained in the blocking region */
	protected NetNode[] regionNodes;

	/** reference to the fictitious station which controls arrivals to the blocking region */
	protected NetNode inputStation;

	//-----------------------------CONSTRAINTS--------------------------------------------\\
	//data structures are duplicated to consider both global and class constraints

	/** the maximum number of jobs that can be at the same time inside the region */
	protected double maxCapacity;

	protected double maxMemory;
	/** the maximum number of jobs of each class that can be at the same time inside the region
	 *  (-1 if the class has not a class constraint)
	 */
	protected double[] maxCapacityPerClass;

	/** the number of jobs that are currently inside the region */
	protected double actualOccupation;
	/** the number of jobs of each class that are currently inside the region */
	protected double[] actualOccupationPerClass;

	protected double actualMemoryOccupation;

	/** True if there are already maxJobs inside the region */
	protected boolean globallyBlocked;
	/** For each class, true if there are already maxJobsPerClass inside the region */
	protected boolean[] classBlocked;

	/** For each class, true if jobs in excess must be dropped */
	protected boolean[] dropClass;

	/** true if there is a global constraint */
	protected boolean globalConstraintDefined;

	/** true if there is a global memory constraint */
	protected boolean globalMemoryConstraintDefined;
	/** true if the specified class has a class constraint */
	protected boolean classConstraintDefined[];

	//-----------------------------------------------------------------------------------\\

	//---------------------------CLASS WEIGHTS------------------------//

	//true if global constraint must be evaluated as a weighted sum
	// sum(job_classC * token_for_each_job_classC) < maxToken
	private boolean weightedConstraintDefined = false;

	//class c weight: number of token required by each class c job
	protected double[] classWeights;

	protected double[] classSizes;

	//---------------------------end CLASS WEIGHTS---------------------//

	private boolean sizeConstraintDefined = false;

	//size constraint
	//private boolean isThreadType = false;
	//private double maxSize = 10;
	//private double usedSize = 0;
	//private double[] sizePerClass = {5,2,1};
	//private double[] capacityPerClass = {10,10,5};

	private Measure weightMeasure, sizeMeasure;
	private double lastTimeModified;
	private double lastWeightOccupation, lastSizeOccupation;

	protected boolean groupConstraintDefined;
	protected List<GroupInfo> groupInfo_FCFS;
	protected List<GroupInfo> groupInfo_HPS;
	protected boolean groupHPSActive;
	protected String groupHPSStrategy;

	/**
	 * Creates a blocking region with both global constraint and class constraints
	 * @param regionName the regionName of the blocking region.
	 * @param maxCapacity the global constraint (-1 means no constraint)
	 * @param maxCapacityPerClass the max capacity allowed for each class (-1 means no constraint for
	 * that class). If null, no class constraints are defined.
	 * @param drop for each class, specifies if jobs in excess must be blocked or dropped (even
	 * if the specified class is not class constrained, the corresponding drop property is important,
	 * because there is also a global constraint which can be violated)
	 * @param simul the simulation to which the blocking region belongs
	 * @param regionNodeNames the names of the nodes contained in the blocking region.
	 */
	public BlockingRegion(String regionName, double maxCapacity, double[] maxCapacityPerClass, boolean[] drop, Simulation simul, String[] regionNodeNames) {
		//No control on input dimensions.

		//-----------------REGION PROPERTIES----------------------//
		//region name
		this.name = regionName;
		//class number
		int classNumber = simul.getClasses().length;

		//sets owner simulation
		sim = simul;
		//before sim.initialize() method, network has not been set yet
		network = null;
		//before sim.initialize() method, net nodes has not been set yet
		this.regionNodeNames = regionNodeNames;
		this.regionNodes = null;

		//------------------GLOBAL CONSTRAINT--------------------//
		if (maxCapacity == -1) {
			//no global constraint
			this.globalConstraintDefined = false;
			this.maxCapacity = -1;
		} else {
			//global constraint
			this.globalConstraintDefined = true;
			this.maxCapacity = maxCapacity;
		}

		//init actual occupation
		this.actualOccupation = 0;
		this.actualMemoryOccupation = 0;
		//init region status
		this.globallyBlocked = false;

		//------------------CLASS CONSTRAINT--------------------//
		classConstraintDefined = new boolean[classNumber];

		if (maxCapacityPerClass == null) {
			//no class constraints defined
			this.maxCapacityPerClass = null;

			for (int c = 0; c < classNumber; c++) {
				//no class constraint
				classConstraintDefined[c] = false;
			}

		} else {
			//class constraint defined
			this.maxCapacityPerClass = maxCapacityPerClass;

			for (int c = 0; c < classNumber; c++) {
				if (maxCapacityPerClass[c] == -1) {
					//no constraints on this class
					classConstraintDefined[c] = false;
				} else {
					classConstraintDefined[c] = true;
				}
			}
		}

		//init actual occupation per class
		this.actualOccupationPerClass = new double[classNumber];
		this.classBlocked = new boolean[classNumber];

		for (int c = 0; c < classNumber; c++) {
			//init occupation
			actualOccupationPerClass[c] = 0;
			//init class status
			this.classBlocked[c] = false;
		}

		//------------------WEIGHTED CONSTRAINT--------------------//
		weightedConstraintDefined = false;
		classWeights = new double[classNumber];

		//if no weights are defined, suppose 1 job = 1 token for each class
		for (int c = 0; c < classNumber; c++) {
			//init weights
			classWeights[c] = 1.0;
		}

		//------------------DROP PROPERTIES-----------------------//
		dropClass = new boolean[classNumber];

		for (int c = 0; c < classNumber; c++) {
			dropClass[c] = drop[c];
		}
	}

	// WEIGHTED CONSTRAINTS
	/**
	 * Creates a blocking region with both global constraint and class constraints
	 * @param regionName the regionName of the blocking region.
	 * @param maxCapacity the global constraint (-1 means no constraint)
	 * @param maxCapacityPerClass the max capacity allowed for each class (-1 means no constraint for
	 * that class). If null, no class constraints are defined.
	 * @param drop for each class, specifies if jobs in excess must be blocked or dropped (even
	 * if the specified class is not class constrained, the corresponding drop property is important,
	 * because there is also a global constraint which can be violated)
	 * @param simul the simulation to which the blocking region belongs
	 * @param regionNodeNames the names of the nodes contained in the blocking region.
	 */
	public BlockingRegion(String regionName, double maxCapacity, double maxMemory, double[] maxCapacityPerClass, double[] classSizes, double[] classWeights, boolean[] drop,
			String[] groupNames, double[] maxCapacityPerGroup, String[] groupStrategies, List<List<Integer>> groupClassIndexLists, Simulation simul, String[] regionNodeNames) {
		//No control on input dimensions.

		//-----------------REGION PROPERTIES----------------------//
		//region name
		this.name = regionName;
		//class number
		int classNumber = simul.getClasses().length;

		//sets owner simulation
		sim = simul;
		//before sim.initialize() method, network has not been set yet
		network = null;
		//before sim.initialize() method, net nodes has not been set yet
		this.regionNodeNames = regionNodeNames;
		this.regionNodes = null;

		//------------------GLOBAL CONSTRAINT--------------------//
		if (maxCapacity == -1) {
			//no global constraint
			this.globalConstraintDefined = false;
			this.maxCapacity = -1;
		} else {
			//global constraint
			this.globalConstraintDefined = true;
			this.maxCapacity = maxCapacity;
		}

		if (maxMemory == -1) {
			this.globalMemoryConstraintDefined = false;
			this.maxMemory = -1;
		} else {
			this.globalMemoryConstraintDefined = true;
			this.maxMemory = maxMemory;
		}

		//init actual occupation
		this.actualOccupation = 0;

		this.actualMemoryOccupation = 0;
		//init region status
		this.globallyBlocked = false;

		//------------------CLASS CONSTRAINT--------------------//
		classConstraintDefined = new boolean[classNumber];

		if (maxCapacityPerClass == null) {
			//no class constraints defined
			this.maxCapacityPerClass = null;

			for (int c = 0; c < classNumber; c++) {
				//no class constraint
				classConstraintDefined[c] = false;
			}

		} else {
			//class constraint defined
			this.maxCapacityPerClass = maxCapacityPerClass;

			for (int c = 0; c < classNumber; c++) {
				if (maxCapacityPerClass[c] == -1) {
					//no constraints on this class
					classConstraintDefined[c] = false;
				} else {
					classConstraintDefined[c] = true;
				}
			}
		}

		//init actual occupation per class
		this.actualOccupationPerClass = new double[classNumber];
		this.classBlocked = new boolean[classNumber];

		for (int c = 0; c < classNumber; c++) {
			//init occupation
			actualOccupationPerClass[c] = 0;
			//init class status
			this.classBlocked[c] = false;
		}

		//------------------WEIGHTED CONSTRAINT--------------------//
		weightedConstraintDefined = true;
		//copy class weights
		this.classWeights = classWeights;

		//------------------MEMORY CONSTRAINT---------------------//
		sizeConstraintDefined = true;
		this.classSizes = classSizes;

		//------------------DROP PROPERTIES-----------------------//
		dropClass = new boolean[classNumber];
		dropClass = drop;

		//------------------GROUP CONSTRAINT----------------------//
		if (groupNames.length == 0) {
			groupConstraintDefined = false;
		} else {
			groupConstraintDefined = true;
		}

		groupInfo_FCFS = new ArrayList<GroupInfo>();
		groupInfo_HPS = new ArrayList<GroupInfo>();
		for (int i = 0; i < groupNames.length; i++) {
			if (groupStrategies[i].equals(REGION_GROUP_FCFS_STRATEGY)) {
				groupInfo_FCFS.add(new GroupInfo(groupNames[i], maxCapacityPerGroup[i], groupStrategies[i], groupClassIndexLists.get(i)));
			}
			if (groupStrategies[i].contains(REGION_GROUP_HPS_STRATEGY)) {
				groupInfo_HPS.add(new GroupInfo(groupNames[i], maxCapacityPerGroup[i], groupStrategies[i], groupClassIndexLists.get(i)));
			}
		}
		if (groupInfo_HPS.size() > 0) {
			groupHPSStrategy = groupInfo_HPS.get(0).strategy;
		}
		groupHPSActive = false;
		calcHPSGroupActive();
	}

	//-----------------------CONSTRAINTS METHODS---------------------------\\

	/** True if there is a global constraint */
	public boolean hasGlobalConstraint() {
		return globalConstraintDefined;
	}

	/** True if the class is subjected to a class constraint */
	public boolean hasClassConstraint(JobClass jobClass) {
		return classConstraintDefined[jobClass.getId()];
	}

	/** True if the region has a global weighted constraint (constraint is shared,
	 * but different classes have different weights)
	 * @return True if the region has a global weighted constraint
	 */
	public boolean hasWeightedConstraint() {
		return weightedConstraintDefined;
	}

	public boolean hasSizeConstraint() {
		return sizeConstraintDefined;
	}

	public boolean hasGroupConstraint() {
		return groupConstraintDefined;
	}

	/** Gets the number of jobs currently inside the blocking region */
	public double getActualOccupation() {
		return actualOccupation;
	}

	/** Gets the number of jobs of the specified class currently inside the blocking region
	 * @param jobClass the specified class
	 */
	public double getActualOccupation(JobClass jobClass) {
		if (jobClass != null) {
			return actualOccupationPerClass[jobClass.getId()];
		} else {
			return 0;
		}
	}

	private void calcHPSGroupActive() {
		if (!groupConstraintDefined) {
			groupHPSActive = false;
			return;
		} else if (groupInfo_HPS.size() == 0) {
			groupHPSActive = false;
			return;
		} else if (groupInfo_FCFS.size() == 0) {
			groupHPSActive = true;
			return;
		}

		groupHPSActive = true;
		for (int i = 0; i < groupInfo_FCFS.size(); i++) {
			if (groupInfo_FCFS.get(i).actualOccupation.size() < groupInfo_FCFS.get(i).maxCapacity) {
				groupHPSActive = false;
				break;
			}
		}
	}

	public boolean isHPSGroupActive() {
		calcHPSGroupActive();
		return groupHPSActive;
	}

	public String getHPSStrategy() {
		if (groupHPSStrategy.contains(REGION_GROUP_HPS_STRATEGY)) {
			return groupHPSStrategy;
		} else {
			return REGION_GROUP_HPS_LCFS_STRATEGY;
		}
	}

	/** Increases both the number of total jobs currently inside the region
	 * and also the number of jobs of the specified class currently inside
	 * the region.
	 */
	public void increaseOccupation(JobClass jobClass) {
		int c = jobClass.getId();
		actualOccupation += classWeights[c];
		actualMemoryOccupation += classSizes[c];
		actualOccupationPerClass[c] += classWeights[c];

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}
		lastTimeModified = currentTime;
		return;
	}

	/** Decreases the number of jobs currently inside the region. */
	public void decreaseOccupation(JobClass jobClass) {
		int c = jobClass.getId();
		actualOccupation -= classWeights[c];
		actualMemoryOccupation -= classSizes[c];
		actualOccupationPerClass[c] -= classWeights[c];

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}
		lastTimeModified = currentTime;
		return;
	}

	/** Gets the drop property relative to a class constraint: if
	 * true, jobs in excess of this class will be dropped */
	public boolean getClassDrop(JobClass jobClass) {
		return dropClass[jobClass.getId()];
	}

	/** Gets the drop property relative to a class constraint: if
	 * true, jobs in excess of this class will be dropped */
	public boolean getClassDrop(int classIndex) {
		return dropClass[classIndex];
	}

	/**
	 * Tells whether the region is blocked for this class or
	 * has enough place for a job of this class.
	 * @return true if blocked, false if other capacity is available
	 */
	public boolean isBlocked(JobClass jobClass) {
		//double currentOccupation = 0.0;
		//double currentMemory = 0.0;
		//double[] currentOccupationPerClass = new double[classWeights.length];
		//for (int i = 0; i < regionNodes.length; i++) {
		//	for (int j= 0; j < maxCapacityPerClass.length; j++) {
		//		try {
		//			JobInfoList tempList = regionNodes[i].getJobInfoList();
		//			currentOccupation += (tempList.getJobsInPerClass(j) - tempList.getJobsOutPerClass(j))* classWeights[j];
		//			currentMemory += (tempList.getJobsInPerClass(j) - tempList.getJobsOutPerClass(j))* classSizes[j];
		//			currentOccupationPerClass[j] = (tempList.getJobsInPerClass(j) - tempList.getJobsOutPerClass(j))* classWeights[j];
		//			if (currentOccupation != actualOccupation) {
		//				System.out.println(currentOccupation);
		//				System.out.println(actualOccupation);
		//				System.out.println("--------");
		//			}
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//	}
		//}

		int id = jobClass.getId();

		if (globalConstraintDefined) {
			if ((actualOccupation + classWeights[id]) > maxCapacity) {
				//if ((currentOccupation + classWeights[id]) > maxCapacity) {
				//globally blocked
				return true;
				//}
			}
		}

		if (globalMemoryConstraintDefined) {
			if ((actualMemoryOccupation + classSizes[id]) > maxMemory) {
				//if ((currentMemory + classSizes[id]) > maxMemory) {
				return true;
				//}
			}
		}

		if (classConstraintDefined[jobClass.getId()]) {
			if ((actualOccupationPerClass[id] + classWeights[id]) > maxCapacityPerClass[id]) {
				//class blocked
				//if ((currentOccupationPerClass[id] + classWeights[id]) > maxCapacityPerClass[id]) {
				return true;
				//}
			}
		}

		if (groupConstraintDefined) {
			boolean isBlocked = true;
			if (!groupHPSActive) {
				for (int i = 0; i < groupInfo_FCFS.size(); i++) {
					if (groupInfo_FCFS.get(i).classIndexList.contains(id)) {
						if (groupInfo_FCFS.get(i).actualOccupation.size() < groupInfo_FCFS.get(i).maxCapacity) {
							isBlocked = false;
						}
					}
				}
			} else {
				for (int i = 0; i < groupInfo_HPS.size(); i++) {
					if (groupInfo_HPS.get(i).classIndexList.contains(id)) {
						if (groupInfo_HPS.get(i).actualOccupation.size() < groupInfo_HPS.get(i).maxCapacity) {
							isBlocked = false;
						}
					}
				}
			}
			return isBlocked;
		}

		return false;
	}

	//-------------------- end  CONSTRAINTS METHODS---------------------------\\

	/** Gets the name of the blocking region */
	public String getName() {
		return name;
	}

	/**Gets the names of the region nodes */
	public String[] getRegionNodeNames() {
		return regionNodeNames;
	}

	/**
	 * Finds and sets the netNode objects using the node names
	 */
	private void findRegionNodes() {
		//nodes of the region must be found, using their names
		network = sim.getNetwork();
		regionNodes = new NetNode[regionNodeNames.length];
		for (int i = 0; i < regionNodeNames.length; i++) {
			//TODO: check wrong names...?
			regionNodes[i] = network.getNode(regionNodeNames[i]);
		}
	}

	/**
	 * Tells whether a node is contained in the blocking region
	 * @param nodeName the name of the node
	 * @return true if the specified node is contained in the region
	 */
	public boolean belongsToRegion(String nodeName) {
		for (String regionNodeName : regionNodeNames) {
			if (regionNodeName.equalsIgnoreCase(nodeName)) {
				//the specified node is contained in the blocking region
				return true;
			}
		}
		//the specified node is not contained in the blocking region
		return false;
	}

	/**
	 * Tells whether a node is contained in the blocking region
	 * @param node the NetNode object
	 * @return true if the specified node is contained in the region
	 */
	public boolean belongsToRegion(NetNode node) {
		if (network == null) {
			//if network has not been set
			//nodes of the region must be found, using their names
			findRegionNodes();
		}
		for (NetNode regionNode : regionNodes) {
			if (regionNode == node) {
				//the specified node is contained in the blocking region
				return true;
			}
		}
		//the specified node is not contained in the blocking region
		return false;
	}

	/**
	 * Returns a node, if contained in the blocking region
	 * @param nodeName the name of the node
	 * @return the NetNode object if the specified node is contained in the region,
	 * null otherwise
	 */
	public NetNode getRegionNode(String nodeName) {
		if (network == null) {
			//before searching a particular node for the first time,
			//nodes of the region must be found, using their names
			findRegionNodes();
		}

		for (int i = 0; i < regionNodeNames.length; i++) {
			if (regionNodeNames[i].equalsIgnoreCase(nodeName)) {
				//the specified node is contained in the blocking region
				//returns the corresponding NetNode object
				return regionNodes[i];
			}
		}
		//the specified node is not contained in the blocking region
		//returns null
		return null;
	}

	/**
	 * Gets the fictitious station that controls arrivals to the blocking region
	 * @return the fictitious station that controls arrivals to the blocking region
	 */
	public NetNode getInputStation() {
		return inputStation;
	}

	/**
	 * Sets the input station of the blocking region
	 * @param inputStation the input station of the blocking region
	 */
	public void setInputStation(NetNode inputStation) {
		this.inputStation = inputStation;
	}

	/**
	 * @return Returns the classWeights.
	 */
	public double[] getClassWeights() {
		return classWeights;
	}

	/**
	 * @return Returns the maxCapacity.
	 */
	public double getMaxCapacity() {
		return maxCapacity;
	}

	//public boolean isThreadType() {
	//	return isThreadType;
	//}
	//
	//public void setThreadType(boolean isThreadType) {
	//	this.isThreadType = isThreadType;
	//}
	//
	//public double getMaxSize() {
	//	return maxSize;
	//}
	//
	//public void setMaxSize(double maxSize) {
	//	this.maxSize = maxSize;
	//}
	//
	//public double getUsedSize() {
	//	return usedSize;
	//}
	//
	//public void setUsedSize(double usedSize) {
	//	this.usedSize = usedSize;
	//}
	//
	//public double[] getSizePerClass() {
	//	return sizePerClass;
	//}
	//
	//public void setSizePerClass(double[] sizePerClass) {
	//	this.sizePerClass = sizePerClass;
	//}
	//
	//public double[] getCapacityPerClass() {
	//	return capacityPerClass;
	//}
	//
	//public void setCapacityPerClass(double[] capacityPerClass) {
	//	this.capacityPerClass = capacityPerClass;
	//}

	public void setWeightMeasure(Measure wm) {
		this.weightMeasure = wm;
	}
	public void setSizeMeasure(Measure sm) {
		this.sizeMeasure = sm;
	}

	public void increaseOccupation(JobClass jobClass, int i) {
		int c = jobClass.getId();
		actualOccupation += classWeights[c] * i;
		actualMemoryOccupation += classSizes[c] * i;
		actualOccupationPerClass[c] += classWeights[c] * i;

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}
		lastTimeModified = currentTime;
		return;
	}

	public void decreaseOccupation(JobClass jobClass, int i) {
		int c = jobClass.getId();
		actualOccupation -= classWeights[c] * i;
		actualMemoryOccupation -= classSizes[c] * i;
		actualOccupationPerClass[c] -= classWeights[c] * i;

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}
		lastTimeModified = currentTime;
		return;
	}

	/** Increases both the number of total jobs currently inside the region
	 * and also the number of jobs of the specified class and group currently
	 * inside the region without groupIndex.
	 */
	public void increaseOccupation(Job job) {
		int c = job.getJobClass().getId();
		actualOccupation += classWeights[c];
		actualMemoryOccupation += classSizes[c];
		actualOccupationPerClass[c] += classWeights[c];

		if (groupConstraintDefined) {
			if (!groupHPSActive) {
				for (int i = 0; i < groupInfo_FCFS.size(); i++) {
					if (groupInfo_FCFS.get(i).classIndexList.contains(job.getJobClass().getId())) {
						if (groupInfo_FCFS.get(i).actualOccupation.size() < groupInfo_FCFS.get(i).maxCapacity) {
							groupInfo_FCFS.get(i).actualOccupation.add(job);
						}
					}
				}
			} else {
				for (int i = 0; i < groupInfo_HPS.size(); i++) {
					if (groupInfo_HPS.get(i).classIndexList.contains(job.getJobClass().getId())) {
						if (groupInfo_HPS.get(i).actualOccupation.size() < groupInfo_HPS.get(i).maxCapacity) {
							groupInfo_HPS.get(i).actualOccupation.add(job);
						}
					}
				}
			}
			groupHPSActive = false;
		}

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}	
		lastTimeModified = currentTime;
		return;
	}

	/** Decreases the number of jobs currently inside the region. */
	public void decreaseOccupation(Job job) {
		int c = job.getJobClass().getId();
		actualOccupation -= classWeights[c];
		actualMemoryOccupation -= classSizes[c];
		actualOccupationPerClass[c] -= classWeights[c];

		if (groupConstraintDefined) {
			boolean groupInfo_FCFS_blocked = true;
			for (int i = 0; i < groupInfo_FCFS.size(); i++) {
				if (groupInfo_FCFS.get(i).actualOccupation.contains(job)) {
					groupInfo_FCFS.get(i).actualOccupation.remove(job);
					groupInfo_FCFS_blocked = false;
					break;
				}
			}
			if (groupInfo_FCFS_blocked) {
				for (int i = 0; i < groupInfo_HPS.size(); i++) {
					if (groupInfo_HPS.get(i).actualOccupation.contains(job)) {
						groupInfo_HPS.get(i).actualOccupation.remove(job);
						break;
					}
				}
			}
		}

		double currentTime = NetSystem.getTime();
		if (weightMeasure != null) {
			weightMeasure.update(lastWeightOccupation, currentTime - lastTimeModified);
			lastWeightOccupation = actualOccupation;
		}
		if (sizeMeasure != null) {
			sizeMeasure.update(lastSizeOccupation, currentTime - lastTimeModified);
			lastSizeOccupation = actualMemoryOccupation;
		}
		lastTimeModified = currentTime;
		return;
	}

	protected class GroupInfo {

		public String name;
		public double maxCapacity;
		public String strategy;
		protected List<Integer> classIndexList;
		protected List<Job> actualOccupation;

		public GroupInfo(String name, double maxCapacity, String strategy, List<Integer> classIndexList) {
			this.name = name;
			this.maxCapacity = maxCapacity;
			this.strategy = strategy;
			this.classIndexList = new ArrayList<Integer>();
			this.actualOccupation = new ArrayList<Job>();
			for (int i = 0; i < classIndexList.size(); i++) {
				this.classIndexList.add(classIndexList.get(i));
			}
		}

	}

}
