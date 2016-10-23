package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;

/**
 * The solver interface for tree MVA solvers.
 * @author Ben Homer, 2014.
 */
public interface IThreadedTMVASolver {
	
	/**
	 * Solves the network rooted at the specified node.
	 * @param node The root node of the tree.
	 * @param N The population vector.
	 * @return The MVAResults object containing the final performance measures.
	 */
	MVAResults solve(Node node, PopulationVector N);	
}
