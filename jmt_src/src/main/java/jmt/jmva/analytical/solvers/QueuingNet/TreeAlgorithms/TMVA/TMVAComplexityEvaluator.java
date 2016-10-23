package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import java.util.Arrays;
import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.QNModel.QueueType;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ComplexityEvaluator;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * Class responsible for evaluating the complexity of the tree MVA algorithm if
 * it were to be run on a given tree representing a network.
 * @author Ben Homer, 2014.
 */
public class TMVAComplexityEvaluator extends ComplexityEvaluator {
	
	/**
	 * Constructor.
	 * @param qnm The queueing network model we are trying to solve.
	 * @param ccu The ClassCoverageUtils object used for calculating the
	 * class coverage at certain nodes in the planted tree. 
	 */
	public TMVAComplexityEvaluator(QNModel qnm, ClassCoverageUtils ccu) {
		super(qnm, ccu);
	}

	/**
	 * Determines if the TMVA for the specified tree has better complexity characteristics than
	 * the standard/sequential MVA algorithm for the same network.
	 * @param root The root node of the tree to test.
	 * @param pv The per class population vector of the network.
	 * @return A boolean indicating whether the tree has an acceptable expected complexity.
	 */
	public boolean treeHasAcceptableComplexity(Node root, PopulationVector pv) {
		stdComplexity = getStandardAlgorithmComplexity(pv);
		treeComplexity = getTreeComplexity(root, pv);
		sparcityOfDemandsMatrix = getSparcityOfDemandsMatrix();
		perClassSparsityMeasure = getPerClassSparsityIndex();
		//fcfToPcRatio = calculateFcsToPcsRatio(root);
		
		if (Config.TERMINATE_IF_HIGH_COMPLEXITY && treeComplexity.timeComplexity > stdComplexity.timeComplexity) {
			return false;
		}
		
		printSummary();
		return true;
	}
	
	/**
	 * Gets the complexity of a leaf node computation in the TMVA algorithm.
	 * @param node The leaf node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getLeafNodeComplexity(Node node, PopulationVector pv) {
		return new ComplexityBundle(0, 0);
	}

	/**
	 * Gets the complexity of a subnetwork node computation in the TMVA algorithm.
	 * @param node The subnetwork node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getSubnetNodeComplexity(Node node, PopulationVector pv) {
		TreeSet<Integer> fcs = node.fcs;
		TreeSet<Integer> pcs = node.pcs;
		
		int Asum = 0;
		for (int c : fcs) {
			Asum += (qnm.N.get(c) + 1);
		}
		
		int Bsum = 0;
		for (int c : pcs) {
			Bsum += (qnm.N.get(c) + 1);
		}
		
		int A = fcs.size();
		int singlePermutationComplexity = fcs.size() == 0 ? 2 : 2 * A * Asum;
		int time =  Bsum * singlePermutationComplexity;
		
		// TODO: equation for space complexity not given in papers.
		int space = 0;
		
		return new ComplexityBundle(time, space);
	}

	/**
	 * Gets the complexity of running standard/sequential MVA algorithm over a network
	 * with the specified population vector.
	 * @param pv The network's population vector.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getStandardAlgorithmComplexity(PopulationVector pv) {
		int qdepStations = 0;
		int fixedRateStations = 0;
		for (int k = 0; k < qnm.M; k++) {
			QueueType t = qnm.getQueueType(k);
			if (t.equals(QueueType.LI)) fixedRateStations++;
			else qdepStations++;
		}
		
		// Time complexity
		int time = 0;
		int a = 0;
		for (int j = 0; j < qdepStations; j++) {
			a += (j + fixedRateStations) * MiscFunctions.binomialCoefficient(qdepStations, j);
		}
		
		int b = 0;
		for (int c = 0; c < qnm.R; c++) {
			b += (qnm.N.get(c) + 1);
		}
		
		if (a == 0) { a = qnm.M; }
		time = a * qnm.R * b;
		
		// Space complexity
		int f = (int)Math.floor(qdepStations/2);
		int space = qnm.R * (qnm.M + f) * MiscFunctions.binomialCoefficient(qdepStations, f) * (1 + b);
		
		return new ComplexityBundle(time, space);
	}

	/**
	 * Prints a summary of the complexity evaluators calculations.
	 */
	@Override
	protected void printSummary() {
		Printer.out.println("--- TMVA Complexity Evaluation ---");
		getMachineCapabilities();
		if (stdComplexity != null) {
			Printer.out.println("Std. MVA: Time = " + stdComplexity.timeComplexity 
					+ " Space = " + stdComplexity.spaceComplexity);
		}
		if (treeComplexity != null) {
			Printer.out.println("Tree convolution: Time = " + treeComplexity.timeComplexity 
					+ " Space = " + treeComplexity.spaceComplexity);
		}
		Printer.out.println("Per class sparsity: " + Arrays.toString(perClassSparsityMeasure));
		Printer.out.println("Sparsity of demands matrix: " + sparcityOfDemandsMatrix);
		Printer.out.println("Ratio of FCS to PCS: " + fcfToPcRatio);
		Printer.out.println("--- END Complexity Evaluation ---");
	}
}