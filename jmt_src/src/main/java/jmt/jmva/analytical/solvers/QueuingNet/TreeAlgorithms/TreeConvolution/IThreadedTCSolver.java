package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.Utilities.Timer;

/**
 * The solver interface for tree convolution solvers.
 * @author Ben Homer, 2014.
 */
public interface IThreadedTCSolver {
	
	/** Timer for computing normalisation constant. */
	Timer normalisationConstantTimer = new Timer();
	
	/** Timer for computing throughputs. */
	Timer throughputTimer = new Timer();
	
	/** Timer for computing mean queue lengths. */
	Timer queueLengthTimer = new Timer();
	
	/** Compute G using postorder tree traversal. 
	 * @param node The current node to compute the normalization constant G for.
	 * @param pv The population vector to be used during the computation.
	 * @returns The normalisation constant G at the given node, calculated using the
	 * specified population vector.
	 */
	BigRational computeG(Node node, PopulationVector pv);
	
	/** 
	 * Calculates per class throughputs using methods outlined in LamLien83 paper.
	 * @param p The full population vector.
	 * @return The per class throughputs as an array of BigRationals.
	 */
	BigRational[] calculateThroughputs(PopulationVector p);
	
	/** 
	 * Calculates per class and per station mean queue lengths using methods outlined in LamLien83 paper.
	 * @param p The full population vector.
	 * @return The per class per station mean queue lengths as a 2D array of BigRationals.
	 */
	BigRational[][] calculateMeanQueueLengths(PopulationVector p);
}
