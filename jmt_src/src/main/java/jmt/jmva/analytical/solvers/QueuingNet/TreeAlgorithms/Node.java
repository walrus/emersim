package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils.Covered;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA.MVAResults;

/** Class representing a tree node. 
 * @author Ben Homer, 2014.
 */
public class Node {
	
	/** The stations this node represents. */
	public TreeSet<Integer> stations;
	
	/** The weight of this node (used during tree planting). */
	public int weight;
	
	/** The marked flag. */
	public boolean marked;
	
	/** This node's parent node. */
	protected Node parent;
	
	/** This node's left child. */
	protected Node leftChild;
	
	/** This node's right child. */
	protected Node rightChild;
	
	/** This node's set of partially covered classes.
	 * Stored locally to avoid constant recomputation.
	 * TreeSet used to maintain consistent ordering (ascending).
	 */
	public TreeSet<Integer> pcs = new TreeSet<Integer>(); 
	
	/** This node's set of fully covered classes. */
	public TreeSet<Integer> fcs = new TreeSet<Integer>();
	
	/** Set of all (fully and partially) covered classes. */
	private TreeSet<Integer> allCovered;
	
	/** Tree Convolution related **/
	
	/** The partially covered g-array for this node. */
	public HashMap<PopulationVector, BigDecimal> Gmap;
	
	/** In certain cases when we store multiple Gmaps at a node, backupGmaps can be used
	 *  to store these extra Gmaps, else this remains unused.
	 */
	public HashMap<PopulationVector, HashMap<PopulationVector, BigDecimal>> backupGmaps;
	
	
	/** Tree MVA related **/
	
	public HashMap<PopulationVector, MVAResults> mvaRes;

	/** Constructor. */
	public Node() {
		this.stations = new TreeSet<Integer>();
		
		this.Gmap = new HashMap<PopulationVector, BigDecimal>();
		this.backupGmaps = new HashMap<PopulationVector, HashMap<PopulationVector, BigDecimal>>();
		
		this.mvaRes = new HashMap<PopulationVector, MVAResults>();
	}
	
	/** Returns the parent node. */
	public Node parent() {
		return parent;
	}
	
	/** Returns the left child node. */
	public Node leftChild() {
		return leftChild;
	}
	
	/** Returns the right child node. */
	public Node rightChild() {
		return rightChild;
	}
	
	/** Sets the left child node. 
	 *  @param leftChild The left child node.
	 */
	public void setLeftChild(Node leftChild) {
		this.leftChild = leftChild;
		if (this.leftChild != null) {
			this.leftChild.parent = this;
		}
	}
	
	/** Sets the right child node. 
	 *  @param rightChild The right child node.
	 */
	public void setRightChild(Node rightChild) {
		this.rightChild = rightChild;
		if (this.rightChild != null) {
			this.rightChild.parent = this;
		}
	}
	
	/** Sets this parent.
	 * @param parent The parent of this node.
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/** Recalculates this node's weight. */
	public void recalculateWeight() {
		weight = ClassCoverageUtils.inst().calculateSubnetWeight(this);
	}

	/** Recalculates both the partially and fully covered classes for this node. */
	public void recalculateCoveredClasses() {
		pcs = ClassCoverageUtils.inst().getCoveredClasses(Covered.PARTIALLY, stations);
		fcs = ClassCoverageUtils.inst().getCoveredClasses(Covered.FULLY, stations);
	}
	
	/**
	 * Gets set of both fully and partially covered classes at this node.
	 * @param forceRecompute Whether to force a recomputation of the covered classes.
	 * @return The set of all covered classes.
	 */
	public TreeSet<Integer> getAllCoveredClasses() {
		if (allCovered == null) {
			allCovered = new TreeSet<Integer>(pcs);
			allCovered.addAll(fcs);
		}
		return allCovered;
	}

	/**
	 * Merges this node with another node by combining the stations.
	 * (N.B. does not recalculate partially covered classes).
	 * @param s The node to merge with.
	 * @return The merged node.
	 */
	public Node merge(Node s) {
		Node merged = new Node();
		merged.stations.addAll(this.stations);
		merged.stations.addAll(s.stations);
		return merged;
	}

	/**
	 * Determines whether this node is a leaf node.
	 * @return A boolean indicating whether this node is a leaf node.
	 */
	public boolean isLeaf() {
		return leftChild == null && rightChild == null;
	}
	
	/**
	 * Determines whether both children of this node are leaves.
	 * @return A boolean indicating whether both children are leaves.
	 */
	public boolean childrenAreLeaves() {
		return leftChild != null && leftChild.isLeaf() 
			&& rightChild != null && rightChild.isLeaf();
	}
	
	/**
	 * Retrieves the stored MVA results for a specified population vector.
	 * @param N The population vector.
	 * @return The MVA results as an MVAResults object.
	 */
	public MVAResults getMVARes(PopulationVector N) {
		ClassCoverageUtils ccu = ClassCoverageUtils.inst();
		PopulationVector nred = ccu.contract(N, ccu.all, this.getAllCoveredClasses());
		return this.mvaRes.get(nred);
	}
	
	/**
	 * Stores a nromalisation constant locally.
	 * @param backupVector The backup population vector to store the value at.
	 * @param key The population vector to use as a key.
	 * @param value The actual value to store.
	 * @param backup Whether to use the backup store or single g-array store.
	 */
	public void store(PopulationVector backupVector, PopulationVector key, BigDecimal value, boolean backup) {
		if (!backup || backupVector == null) {
			Gmap.put(key, value);
		} else {
			addBackupGmap(backupVector, key, value);
		}
	}
	
	/**
	 * Adds a g-array value to the backup store.
	 * @param backupVector The backup population vector to store the value at.
	 * @param key The population vector to use as a key.
	 * @param value The actual value to store.
	 */
	private void addBackupGmap(PopulationVector backupVector, PopulationVector key, BigDecimal value) {
		HashMap<PopulationVector, BigDecimal> b = backupGmaps.get(backupVector);
		if (b == null) {
			b = new HashMap<PopulationVector, BigDecimal>();
		}
		b.put(key, value);
		backupGmaps.put(backupVector, b);
	}
	
	/** Gets a g-array values from the single g-array store at the specified population vector.
	 * @param p The population vector.
	 * @return The g-array value.
	 */
	public BigDecimal getGarr(PopulationVector p) {
		if (Gmap.containsKey(p)) {
			return Gmap.get(p);
		}
		return null;
	}
	
	/** Gets a g-array values from the backup g-array store using the specified
	 * backup and key population vectors.
	 * @param p1 The backup population vector.
	 * @param p2 The key population vector.
	 * @return The g-array value.
	 */
	public BigDecimal getBackupGarr(PopulationVector p1, PopulationVector p2) {
		return backupGmaps.get(p1).get(p2);
	}
	
	/**
	 * Checks whether the backup g-array store contains the population vector.
	 * @param p The population vector.
	 * @return Boolean indicating whether backup g-array store contains the population vector.
	 */
	public boolean backupContains(PopulationVector p) {
		return backupGmaps.containsKey(p);
	}
	
	/** Returns the first value in the g-array
	 * (at the root this will be the normalisation constant).
	 * @return The normalisation constant.
	 */
	public BigDecimal getG() {
		return Gmap.values().iterator().next();
	}
	
	/** Returns the first value in the backup g-array for specified population vector
	 * (at the root this will be the normalisation constant).
	 * @return The normalisation constant.
	 */
	public BigDecimal getBackupG(PopulationVector p) {
		return backupGmaps.get(p).values().iterator().next();
	}
	
	/** Checks whether this node is suitable for feedback filtering.
	 * This will be true if:
	 * 1. Both children are leaf nodes.
	 * 2. At least one is a fixed-rate station.
	 * @param qnm The queueing network model being solved.
	 * @return A boolean indicating whether this node is suitable for feedback filtering. 
	 */
	public boolean childrenSuitableForFeedbackFiltering(QNModel qnm) {
		if (!childrenAreLeaves()) {
			return false;
		}
		
		boolean oneIsFixedRate = qnm.getQueueType(leftChild.stations.first()) == QNModel.QueueType.LI 
				|| qnm.getQueueType(rightChild.stations.first()) == QNModel.QueueType.LI;
		
		return oneIsFixedRate;
	}
	
	/** Checks whether this node's children have a large population overlap in their partially covered classes.
	 * This is defined to be true if the overlap is greater than the FEEDBACK_FILTERING_OVERLAP_POP_THRESHOLD
	 * constant defined in Config.
	 * @param popVec The full population vector.
	 * @return A boolean indicating whether the population overlap is large.
	 */
	public boolean childrenOverlappedPcsHaveLargePopulation(PopulationVector popVec) {
		TreeSet<Integer> overlapped = new TreeSet<Integer>(leftChild.pcs);
		overlapped.retainAll(rightChild.pcs);
		
		for (int cls : leftChild.pcs) {
			if (overlapped.contains(cls) && popVec.get(cls) > Config.FEEDBACK_FILTERING_OVERLAP_POP_THRESHOLD) {
				return true;
			}
		}
		
		return false;
	}
	
	/** Clears g-arrays recursively from this node downwards in the tree. */
	public void clearGmap() {
		this.Gmap.clear();
		if (this.leftChild != null) {
			this.leftChild.clearGmap();
		}

		if (this.rightChild != null) {
			this.rightChild.clearGmap();
		}
	}

	/** Creates a clone of this node. 
	 *  @return The cloned node.
	 */
	public Node clone() {
		Node n = new Node();
		for (int i : stations) {
			n.stations.add(i);
		}

		for (int i : pcs) {
			n.pcs.add(i);
		}

		for (int i : fcs) {
			n.fcs.add(i);
		}

		for (PopulationVector pv : Gmap.keySet()) {
			n.Gmap.put(pv, Gmap.get(pv));
		}
		
		for (PopulationVector p1 : backupGmaps.keySet()) {
			for (PopulationVector p2 : backupGmaps.get(p1).keySet()) {
				n.backupGmaps.put(p1, backupGmaps.get(p2));
			}
		}

		n.weight = weight;
		n.marked = marked;
		//n.parent = (parent == null) ? null : parent.clone();
		Node lc = (leftChild == null) ? null : leftChild.clone();
		n.setLeftChild(lc);
		Node rc = (rightChild == null) ? null : rightChild.clone();
		n.setRightChild(rc);
		return n;
	}

	/** Prints a summary of this node. */
	public void print() {
		Printer.out.print(stations + ":{" + pcs + "} {" + fcs + "}");
	}
	
	/** Prints the local g-array. */
	public void printGmap() {
		if (Config.VERBOSE) {
			for (PopulationVector p : Gmap.keySet()) {
				System.out.println(p.toString() + " : " + Gmap.get(p));
			}
		}
	}
}