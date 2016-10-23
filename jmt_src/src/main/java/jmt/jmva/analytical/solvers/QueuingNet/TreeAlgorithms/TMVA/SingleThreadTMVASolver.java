package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeTraverser;

/**
 * Single-threaded TMVA solver implementation.
 * @author Ben Homer, 2014.
 */
public class SingleThreadTMVASolver extends TreeTraverser implements IThreadedTMVASolver {

	/** The TMVA core implementation. */
	private TMVACore tmvaCore;
	
	/**
	 * Constructor.
	 * @param tmvaCore The TMVA core implementation.
	 */
	public SingleThreadTMVASolver(TMVACore tmvaCore) {
		this.tmvaCore = tmvaCore;
	}
	
	/**
	 * Solves the network rooted at the specified node.
	 * @param node The root node of the tree.
	 * @param N The population vector.
	 * @return The MVAResults object containing the final performance measures.
	 */
	@Override
	public MVAResults solve(Node node, PopulationVector N) {
		Printer.out.println("\n--- STARTING TREE-MVA ---");
		if (Config.RECURSIVE) {
			this.recursiveTraverse(node, N);
		} else {
			this.iterativeTraverse(node, N);
		}

		return node.getMVARes(N);
	}
	
	/**
	 * Method to run at leaf nodes in the TMVA tree.
	 * @param node The leaf node.
	 * @param pv The population vector.
	 */
	@Override
	public void initLeafNode(Node node, PopulationVector pv) {
		tmvaCore.initLeafNode(node);
	}

	/**
	 * Method to run at the parent's of leaf nodes in the TMVA tree.
	 * @param node The leaf node parent.
	 * @param pv The population vector.
	 */
	@Override
	public void computeSubnetNode(Node node, PopulationVector pv) {
		tmvaCore.computeSubnetNode(node);
	}
}
