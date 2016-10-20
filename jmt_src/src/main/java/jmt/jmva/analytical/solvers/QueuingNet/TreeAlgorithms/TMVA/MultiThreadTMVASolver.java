package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeTraverserMulti;

/**
 * Tree MVA solver that uses multiple threads.
 * @author Ben Homer, 2014.
 */
public class MultiThreadTMVASolver extends TreeTraverserMulti implements IThreadedTMVASolver {
	
	/** The TMVA core implementation. */
	private TMVACore tmvaCore;
	
	/** Constructor.
	 * @param tmvaCore The TMVA core implementation.
	 * @param numThreads The number of threads to use.
	 */
	public MultiThreadTMVASolver(TMVACore tmvaCore, int numThreads) {
		super(numThreads);
		this.tmvaCore = tmvaCore;
	}
	
	/**
	 * Solves the network rooted at the specified node using TMVA.
	 * @param node The root node.
	 * @param N The population vector.
	 */
	@Override
	public MVAResults solve(Node node, PopulationVector N) {
		Printer.out.println("\n--- STARTING MULTI-CORE TREE-MVA ---");
		this.traverse(node, N);
		MVAResults res = node.getMVARes(N);
		return res;
	}
	
	/** Computes results at a leaf node by creating a suitable thread to run the computation.
	 * @param node The leaf node under consideration.
	 * @param pv The population vector to use for the computation.
	 * @return The thread which will carry out the computation.
	 */
	@Override
	public Thread getComputeLeafNodeThread(Node node, PopulationVector pv) {
		return new ComputeLeafNodeThread(node);
	}

	/** Computes results at a subnetwork node by creating a suitable thread to run the computation.
	 * @param node The subnetwork node under consideration.
	 * @param pv The population vector to use for the computation.
	 * @return The thread which will carry out the computation.
	 */
	@Override
	public Thread getSubnetNodeThread(Node node, PopulationVector pv) {
		return new ComputeSubnetNodeThread(node);
	}
	
	/** Thread for calculating leaf node MVA results. */
	private class ComputeLeafNodeThread extends Thread {
		/** The leaf node. */
		private Node node;
		
		/**
		 * Constructor.
		 * @param node The leaf node.
		 */
		public ComputeLeafNodeThread(Node node) {
			this.node = node;
		}
		
		/** Runs the leaf node MVA computation. */
	    public void run() {
	    	tmvaCore.initLeafNode(node);
	    }
	}
	
	/** Thread for calculating subnetwork node MVA results. */
	private class ComputeSubnetNodeThread extends Thread {
		/** The subnetwork node. */
		private Node node;
		
		/**
		 * Constructor.
		 * @param node The subnet node.
		 */
		public ComputeSubnetNodeThread(Node node) {
			this.node = node;
		}
		
		/** Runs the subnet node MVA computation. */
	    public void run() {
	    	tmvaCore.computeSubnetNode(node);
	    }
	}
}