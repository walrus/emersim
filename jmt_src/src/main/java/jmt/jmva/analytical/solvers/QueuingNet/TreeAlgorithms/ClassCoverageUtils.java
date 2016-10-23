package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;

/**
 * Implements functionality related to class coverage.
 * @author Ben Homer, 2014.
 */
public class ClassCoverageUtils {
	
	/**
	 * Enum to represent the 3 types of class coverage: fully-covered, partially-covered and non-covered,
	 * as defined in the LamLien83 paper.
	 */
	public enum Covered { FULLY, PARTIALLY, NON };

	/** The queueing network model we are trying to solve. */
	private QNModel qnm;
	
	/** Set containing all classes. */
	public TreeSet<Integer> all;
	
	/** The local static instance. */
	private static ClassCoverageUtils ccu;
	
	/**
	 * Private constructor.
	 * @param qnm The queueing network model we are trying to solve.
	 */
	private ClassCoverageUtils(QNModel qnm) {
		this.qnm = qnm;
		all = new TreeSet<Integer>();
		for (int r = 0; r < qnm.R; r++) {
			all.add(r);
		}
	}
	
	/**
	 * Creates a local instance of a ClassCoverageUtils object to be used.
	 * @param qnm The queueing network model we are trying to solve.
	 * @return The local ClassCoverageUtils instance.
	 */
	public static ClassCoverageUtils create(QNModel qnm) {
		ccu = new ClassCoverageUtils(qnm);
		return ccu;
	}
	
	/**
	 * Gets the local ClassCoverageUtils instance.
	 * @return The local ClassCoverageUtils instance.
	 */
	public static ClassCoverageUtils inst() {
		return ccu;
	}
	
	/**
	 * With respect to a subset of stations (SUBNET),
	 * a) a class of customers is fully covered if the stations that the class visits are a subset of SUBNET,
	 * b) a class of customers is noncovered if the intersection between the stations that the class visits and SUBNET is empty,
	 * c) a class of customers is partially covered if only some of the stations that the class visits are in SUBNET.
	 * @param cls The class of customers.
	 * @param subnet The subnetwork of stations.
	 * @return An enum describing whether the class is fully, non or partially covered with respect to the subnetwork.
	 */
	public Covered getClassCoverage(int cls, TreeSet<Integer> subnet) {
		TreeSet<Integer> stationsVisitedByClass = stationsVisitedByClass(cls);
		int count = 0;
		for (int k : stationsVisitedByClass) {
			if (!subnet.contains(k)) {
				count++;
			}
		}

		if (count == stationsVisitedByClass.size()) {
			return Covered.NON;
		} else if (count > 0) {
			return Covered.PARTIALLY;
		}
		return Covered.FULLY;
	}

	/** 
	 * Gets the classes which are covered by subnet, with specified level of coverage.
	 * @param cover The level of coverge we are looking for.
	 * @param subnet The subnetwork of stations to examine.
	 * */
	public TreeSet<Integer> getCoveredClasses(Covered cover, TreeSet<Integer> subnet) {
		TreeSet<Integer> classList = new TreeSet<Integer>();
		for (int c = 0; c < qnm.R; c++) {
			if (getClassCoverage(c, subnet) == cover) {
				classList.add(c);
			}
		}

		return classList;
	}

	/**
	 * Gets the stations which are visited by a certain class of customers.
	 * (This corresponds to CENTERS(k) in the Lam/Lien 1983 paper).
	 * @param cls The class of customers.
	 * @return The stations which are visited by the specified class.
	 */
	private TreeSet<Integer> stationsVisitedByClass(int cls) {
		TreeSet<Integer> st = new TreeSet<Integer>();
		for (int k = 0; k < qnm.M; k++) {
			if (qnm.getDemand(k, cls) > 0) {
				st.add(k);
			}
		}
		return st;
	}
	
	/** 
	 * Calculates the weight of a subnetwork by implementing equation (10) from the LamLien83 paper.
	 * @param n The tree node to calculate the subnet weight at (subnet consists of all descendents).
	 * @return The weight of the subnetwork.
	 * */
	public int calculateSubnetWeight(Node n) {
		int weight = 0;
		TreeSet<Integer> partiallyCoveredClasses = n.pcs;
		for (int c = 0; c < partiallyCoveredClasses.size(); c++) {
			TreeSet<Integer> stationsVisitedByClass = stationsVisitedByClass(c);
			stationsVisitedByClass.removeAll(n.stations);
			weight += stationsVisitedByClass.size();
		}

		return weight;
	}

	/**
	 * Determines whether two subnetworks are overlapped for a particular class.
	 * @param cls The class to check.
	 * @param subnet1 The first subnetwork of stations.
	 * @param subnet2 The second subnetwork of stations.
	 * @return A boolean indicating whether the two subnetworks are overlapped for the specified class.
	 */
	@SuppressWarnings("unused")
	private boolean isOverlapped(int cls, TreeSet<Integer> subnet1, TreeSet<Integer> subnet2) {
		Covered c1 = getClassCoverage(cls, subnet1);
		Covered c2 = getClassCoverage(cls, subnet2);
		return c1 == Covered.PARTIALLY && c2 == Covered.PARTIALLY;
	}

	/**
	 * Gets the overlapped classes for the given subnetworks.
	 * @param subnet1 The first subnetwork of stations.
	 * @param subnet2 The second subnetwork of stations.
	 * @return The set of overlapped classes.
	 */
	@SuppressWarnings("unused")
	private TreeSet<Integer> getOverlappedClasses(TreeSet<Integer> subnet1, TreeSet<Integer> subnet2) {
		TreeSet<Integer> c1 = getCoveredClasses(Covered.PARTIALLY, subnet1);
		TreeSet<Integer> c2 = getCoveredClasses(Covered.PARTIALLY, subnet2);
		c1.retainAll(c2);
		return c1;
	}
	
	/**
	 * Contracts a population vector to the necessary size depending on partially covered classes in smallPcs.
	 * @param largePopVec The population vector containing populations for each class in largePcs.
	 * @param largePcs The set of partially covered classes corresponding to the largePopVec.
	 * @param smallPcs The set of partially covered classes we want to find populations for.
	 * @return The population vector containing populations for each class in smallPcs.
	 */
	public PopulationVector contract(
		PopulationVector largePopVec,
		TreeSet<Integer> largePcs,
		TreeSet<Integer> smallPcs) {
			
		PopulationVector contracted = new PopulationVector(0, smallPcs.size());
		int smallIdx = 0;
		int largeIdx = 0;
		for (int k : largePcs) {
			if (smallPcs.contains(k)) {
				contracted.set(smallIdx, largePopVec.get(largeIdx));
				smallIdx++;
			}
			largeIdx++;
		}
		
		return contracted;
	}
	
	/**
	 * Expands a population vector to the necessary size depending on partially covered classes in newPcs.
	 * @param oldPopVec The population vector.
	 * @param oldPcs The old set of partially covered classes.
	 * @param newPopVec The population vector containing max values for all classes in newPcs.
	 * @param newPcs The news set of partially covered classes.
	 * @return
	 */
	public PopulationVector expand(
			PopulationVector oldPopVec,
			TreeSet<Integer> oldPcs,
			PopulationVector newPopVec,
			TreeSet<Integer> newPcs) {
		
		Integer[] expanded = new Integer[newPcs.size()];
		int n = 0;
		int m = 0;
		int val = 0;
		for (Integer pcClass : newPcs) {
			if (oldPcs.contains(pcClass)) {
				val = oldPopVec.get(m);
				m++;
			} else {
				val = newPopVec.get(pcClass);
			}
			
			expanded[n] = val;
			n++;
		}
		
		return new PopulationVector(expanded);
	}
	
	/** Gets the index of the specified class in a set of classes.
	 * @param cls The class to find the index of.
	 * @param pcs The set of classes to find the class in.
	 * @return The index.
	 */
	public int getIndex(int cls, TreeSet<Integer> pcs) {
		int count = 0;
		for (int p : pcs) {
			if (p == cls) {
				return count;
			}
			count++;
		}
		
		return -1;
	}
	
	/**
	 * Finds the index of a class in one set (oldPcs) then calculates the classes
	 * index in the new set (newPcs).
	 * @param idx The index of the class.
	 * @param oldPcs The set to find the class in initially.
	 * @param newPcs The set for which an index is calculated.
	 * @return The index of the class in newPcs.
	 */
	public int convertIndex(int idx, TreeSet<Integer> oldPcs, TreeSet<Integer> newPcs) {
		int i = 0;
		int cls = -1;
		for (int p : oldPcs) {
			if (idx == i) {
				cls = p;
				break;
			}
			i++;
		}
		
		i = 0;
		for (int p : newPcs) {
			if (p == cls) {
				return i; 
			}
			i++;
		}
		
		return -1;
	}
	
	/**
	 * Returns the next permutation of a population vector to be used in the
	 * convolution computation, given the current vector and the original start vector.
	 * @param pv The current permutation of the vector.
	 * @param orig The original/start permutation of the vector.
	 * @return The next permutation of the vector.
	 */
    public PopulationVector nextPermutation(PopulationVector pv, PopulationVector orig) {
    	if (pv.isZeroVector()) {
 			return null;
 		}
    	
    	PopulationVector npv = pv.copy();
 		for (int i = pv.size()-1; i >= 0; i--) {
 			int curr = npv.get(i);
 			if (curr > 0) {
 				npv.set(i, curr-1);
 				break;
 			}
 			npv.set(i, orig.get(i));
 		}
 		
 		return npv;
    }
    
	/**
	 * Returns the next permutation of a population vector to be used in the
	 * convolution computation, given the current vector and the original start vector.
	 * This version counts upwards instead of down.
	 * @param pv The current permutation of the vector.
	 * @param orig The original/start permutation of the vector.
	 * @return The next permutation of the vector.
	 */
    public PopulationVector nextPermutationUpwards(PopulationVector pv, PopulationVector orig) {
    	if (pv.equals(orig)) {
 			return null;
 		}
    	
    	PopulationVector npv = pv.copy();
 		for (int i = pv.size()-1; i >= 0; i--) {
 			int curr = npv.get(i);
 			if (curr != orig.get(i)) {
 				npv.set(i, curr+1);
 				break;
 			}
 			npv.set(i, 0);
 		}
 		
 		return npv;
    }
}