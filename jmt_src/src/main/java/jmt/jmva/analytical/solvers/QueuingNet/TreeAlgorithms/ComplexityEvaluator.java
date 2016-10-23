package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;

/**
 * Abstract class representing complexity evaluator for calculating the complexity of a tree algorithm if
 * it were to be run on a given tree representing a network.
 * @author Ben Homer, 2014.
 */
public abstract class ComplexityEvaluator {
	
	/** The queueing network model we are trying to solve. */
	protected QNModel qnm;
	
	/** The ClassCoverageUtils object used for calculating the
	 * class coverage at certain nodes in the planted tree.  */
	protected ClassCoverageUtils ccu;
	
	/** Complexity of running standard/sequential version of algorithm over specified model. */
	protected ComplexityBundle stdComplexity;
	
	/** Complexity of running tree version of algorithm over specified model, for a given tree. */
	protected ComplexityBundle treeComplexity;
	
	/** The sparsity of the network demand matrix. */
	protected double sparcityOfDemandsMatrix;
	
	/** The per class mean sparsity. */
	protected double[] perClassSparsityMeasure;
	
	/** The ratio of fully covered classes to partially covered classes for a given tree. */
	protected double fcfToPcRatio;
	
	/**
	 * Constructor.
	 * @param qnm The queueing network model we are trying to solve.
	 * @param ccu The ClassCoverageUtils object used for calculating the
	 * class coverage at certain nodes in the planted tree. 
	 */
	public ComplexityEvaluator(QNModel qnm, ClassCoverageUtils ccu) {
		this.qnm = qnm;
		this.ccu = ccu;
	}
	
	/**
	 * Determines if the tree algorithm for the specified tree has better complexity characteristics than
	 * the standard/sequential algorithm for the same network.
	 * @param root The root node of the tree to test.
	 * @param pv The per class population vector of the network.
	 * @return A boolean indicating whether the tree has an acceptable expected complexity.
	 */
	public abstract boolean treeHasAcceptableComplexity(Node root, PopulationVector pv);
	
	/**
	 * Gets the complexity of running standard/sequential algorithm over a network with the specified population vector.
	 * @param pv The network's population vector.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	protected abstract ComplexityBundle getStandardAlgorithmComplexity(PopulationVector pv);
	
	/**
	 * Gets the complexity of running the tree algorithm over the specified tree.
	 * @param node The tree root.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	protected ComplexityBundle getTreeComplexity(Node node, PopulationVector pv) {
		ComplexityBundle res = new ComplexityBundle(0, 0);
		if (node.leftChild() != null) {
			res.add(getTreeComplexity(node.leftChild(), pv));
		}
		if (node.rightChild() != null) {
			res.add(getTreeComplexity(node.rightChild(), pv));
		}

		if (node.isLeaf()) {
			res.add(getLeafNodeComplexity(node, pv));
		} else {
			res.add(getSubnetNodeComplexity(node, pv));
		}

		return res;
	}
	
	/**
	 * Determines which tree from a list has the best complexity characteristics.
	 * @param roots The list of tree roots.
	 * @param pv The population vector of the network.
	 * @return The index of the tree which has the best complexity characteristics.
	 */
	public int getTreeWithBestComplexity(Node[] roots, PopulationVector pv) {
		ComplexityBundle best = new ComplexityBundle(Integer.MAX_VALUE, Integer.MAX_VALUE);
		int numPcsBest = Integer.MAX_VALUE;
		
		int bestTree = 0;
		for (int i = 0; i < roots.length; i++) {
			ComplexityBundle c = getTreeComplexity(roots[i].clone(), pv);
			int numPcs = calculateNumberOfPcs(roots[i]);
			boolean isNewBest = false;
			switch (Config.COMPLEXITY_CHECK_LEVEL) {
			// Best time complexity
			case 0:
				isNewBest = c.hasBetterTimeComplexity(best);
				break;
			// Best space complexity
			case 1:
				isNewBest = c.hasBetterSpaceComplexity(best);
				break;
			// Best combined space and time complexity
			case 2: 
				isNewBest = c.hasBetterCombinedComplexity(best);
				break;
			// Based on number of pcs in trees.
			case 3: 
				isNewBest = numPcs < numPcsBest;
				break;
			default:
				isNewBest = c.hasBetterTimeComplexity(best);
			}
			
			if (isNewBest) {
				best = c;
				numPcsBest = numPcs;
				bestTree = i;
			}
		}
		
		return bestTree;
	}
	
	/**
	 * Gets the complexity of a leaf node computation in the tree algorithm.
	 * @param node The leaf node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	protected abstract ComplexityBundle getLeafNodeComplexity(Node node, PopulationVector pv);
	
	/**
	 * Gets the complexity of a subnetwork node computation in the tree algorithm.
	 * @param node The subnetwork node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	protected abstract ComplexityBundle getSubnetNodeComplexity(Node node, PopulationVector pv);
	
	/**
	 * Prints a summary of the complexity evaluators calculations.
	 */
	protected abstract void printSummary();
	
	/**
	 * Prints out information about machine's spec.
	 */
	public void getMachineCapabilities() {
		// No. off cores available to JVM.
		Printer.out.println("Available cores: " + Runtime.getRuntime().availableProcessors());
		int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        Printer.out.println(" --- JVM Heap statistics (MB) ---");
        Printer.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        Printer.out.println("Free Memory:" + runtime.freeMemory() / mb);
        Printer.out.println("Total Memory:" + runtime.totalMemory() / mb);
        Printer.out.println("Max Memory:" + runtime.maxMemory() / mb);
		Printer.out.println("");
	}
	
	/**
	 * Calculates the sparsity of the network's demand matrix.
	 * @return The sparsity measure on scale from 0 to 1.
	 */
	protected double getSparcityOfDemandsMatrix() {
		int zeroCount = 0;
		for (int k = 0; k < qnm.M; k++) {
			for (int c = 0; c < qnm.R; c++) {
				if (qnm.getDemand(k, c) == 0) zeroCount++;
			}
		}
		
		return (double)zeroCount / (double)(qnm.M * qnm.R);
	}
	
	/**
	 * Gets the ratio of fully covered classes to partially covered classes
	 * in the network.
	 * @param root The root node.
	 * @return The ratio of FCF to PCS.
	 */
	protected double calculateFcsToPcsRatio(Node root) {
		int[] t = getPcsAndFcsTotals(root);
		
		double p = (double) t[0];
		double f = (double) t[1];
		
		// Ratio of fcs to pcs
		return f / p;
	}
	
	protected int calculateNumberOfPcs(Node root) {
		int[] t = getPcsAndFcsTotals(root);
		return t[0];
	}
	
	protected double[] getPerClassSparsityIndex() {
		double[] perClassSparsity = new double[qnm.R];
		for (int r = 0; r < qnm.R; r++) {
			for (int m = 0; m < qnm.M; m++) {
				perClassSparsity[r] += qnm.getDemand(m, r) > 0 ? 1 : 0;
			}
			perClassSparsity[r] /= qnm.M;
		}
		
		return perClassSparsity;
	}
	
	/**
	 * Recurses over the tree to get FCS and PCS totals.
	 * @param node The root ndoe.
	 * @return The total FCS and PCS.
	 */
	private int[] getPcsAndFcsTotals(Node node) {
		int[] res = new int[2];
		if (node.leftChild() != null) {
			int[] l = getPcsAndFcsTotals(node.leftChild());
			res[0] += l[0];
			res[1] += l[1];
		}
		if (node.rightChild() != null) {
			int[] r = getPcsAndFcsTotals(node.rightChild());
			res[0] += r[0];
			res[1] += r[1];
		}

		res[0] += node.pcs.size();
		res[1] += node.fcs.size();
		
		return res;
	}
	
	/**
	 * Internal class for storing and comparing time and space complexities.
	 * @author Ben Homer, 2014.
	 */
	protected class ComplexityBundle {
		/** The time complexity. */
		public int timeComplexity;
		
		/** The space complexity. */
		public int spaceComplexity;
		
		/**
		 * Constructor.
		 * @param timeComplexity The time complexity.
		 * @param spaceComplexity The space complexity.
		 */
		public ComplexityBundle(int timeComplexity, int spaceComplexity) {
			this.timeComplexity = timeComplexity;
			this.spaceComplexity = spaceComplexity;
		}
		
		/**
		 * Adds this ComplexityBundle (time and space) to another
		 * and returns the result.
		 * @param c The ComplexityBundle the add the local space and time complexities to.
		 * @return The combined ComplexityBundle.
		 */
		public ComplexityBundle add(ComplexityBundle c) {
			this.timeComplexity += c.timeComplexity;
			this.spaceComplexity += c.spaceComplexity;
			return this;
		}
		
		/**
		 * Determines if the local ComplexityBundle has better time complexity
		 * than another specified ComplexityBundle.
		 * @param c The ComplexityBundle to check against.
		 * @return A boolean indication whether the local ComplexityBundle has better time complexity
		 * than the other specified ComplexityBundle.
		 */
		public boolean hasBetterTimeComplexity(ComplexityBundle c) {
			return this.timeComplexity < c.timeComplexity;
		}
		
		/**
		 * Determines if the local ComplexityBundle has better space complexity
		 * than another specified ComplexityBundle.
		 * @param c The ComplexityBundle to check against.
		 * @return A boolean indication whether the local ComplexityBundle has better space complexity
		 * than the other specified ComplexityBundle.
		 */
		public boolean hasBetterSpaceComplexity(ComplexityBundle c) {
			return this.spaceComplexity < c.spaceComplexity;
		}
		
		/**
		 * Determines if the local ComplexityBundle has better time and space complexity
		 * than another specified ComplexityBundle.
		 * @param c The ComplexityBundle to check against.
		 * @return A boolean indication whether the local ComplexityBundle has better time
		 * and space complexity than the other specified ComplexityBundle.
		 */
		public boolean hasBetterCombinedComplexity(ComplexityBundle c) {
			return (this.timeComplexity + this.spaceComplexity) < (c.timeComplexity + c.spaceComplexity);
		}
	}
}
