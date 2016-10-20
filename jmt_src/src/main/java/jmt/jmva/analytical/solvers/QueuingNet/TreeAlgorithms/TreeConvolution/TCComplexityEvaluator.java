package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ComplexityEvaluator;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils.Covered;

/**
 * Class responsible for evaluating the complexity of the tree convolution algorithm if
 * it were to be run on a given tree representing a network.
 * @author Ben Homer, 2014.
 */
public class TCComplexityEvaluator extends ComplexityEvaluator {
	
	/** Complexity of running standard/sequential version of algorithm over specified model,
	 * when feedback filtering is used. */
	private ComplexityBundle stdComplexityFF;
	
	/**
	 * Constructor.
	 * @param qnm The queueing network model we are trying to solve.
	 * @param ccu The ClassCoverageUtils object used for calculating the
	 * class coverage at certain nodes in the planted tree. 
	 */
	public TCComplexityEvaluator(QNModel qnm, ClassCoverageUtils ccu) {
		super(qnm, ccu);
	}
	
	/**
	 * Determines if the tree convolution algorithm for the specified tree has better complexity
	 * characteristics than the standard/sequential algorithm for the same network.
	 * @param root The root node of the tree to test.
	 * @param pv The per class population vector of the network.
	 * @return A boolean indicating whether the tree has an acceptable expected complexity.
	 */
	public boolean treeHasAcceptableComplexity(Node root, PopulationVector pv) {
		// TODO: include complexity for calculating performance measures as well?
		stdComplexity = getStandardAlgorithmComplexity(pv);
		stdComplexityFF = getStandardConvolutionComplexityFeedbackFiltering(pv);
		treeComplexity = getTreeComplexity(root.clone(), pv);
		sparcityOfDemandsMatrix = getSparcityOfDemandsMatrix();
		//fcfToPcRatio = calculateFcsToPcsRatio(root);
		
		if (Config.TERMINATE_IF_HIGH_COMPLEXITY && treeComplexity.timeComplexity > stdComplexityFF.timeComplexity) {
			return false;
		}
		
		printSummary();
		return true;
	}
	
	/**
	 * Gets the complexity of a leaf node computation in the tree convolution algorithm.
	 * Implements equation (8) in Lam/Lien paper which gives time of
	 * convolution between 2 subnets in tree.
	 * @param node The leaf node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getLeafNodeComplexity(Node n, PopulationVector popVec) {
		TreeSet<Integer> pcUfc = n.pcs;
		pcUfc.addAll(n.fcs);
		int time = 1;
		for (int k : pcUfc) {
			time *= (popVec.get(k) + 1);
		}
		
		time *= 4;
		
		int space = n.pcs.size() + n.fcs.size();
		
		return new ComplexityBundle(time, space);
	}
	
	/**
	 * Gets the complexity of a subnetwork node computation in the tree convolution algorithm.
	 * Implements equation (8) in Lam/Lien paper which gives time of
	 * convolution between 2 subnets in tree.
	 * @param node The subnetwork node.
	 * @param pv The per class population vector of the network.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getSubnetNodeComplexity(Node n, PopulationVector popVec) {
		// Start by computing the overlap and partially covered status of each class.
		Node n1 = n.leftChild();
		Node n2 = n.rightChild();
		TreeSet<Integer> subnet = n1.stations;
		subnet.addAll(n2.stations);
		TreeSet<Integer> overlapped = n1.pcs;
		overlapped.retainAll(n2.pcs);
		TreeSet<Integer> notOverlapped = n1.pcs;
		notOverlapped.addAll(n2.pcs);
		notOverlapped.removeAll(overlapped);
		
		TreeSet<Integer> subnetPc = ccu.getCoveredClasses(Covered.PARTIALLY, subnet);
		
		TreeSet<Integer> p00 = notOverlapped;
		p00.removeAll(subnetPc);
		TreeSet<Integer> p01 = notOverlapped;
		p01.retainAll(subnetPc);
		TreeSet<Integer> p10 = overlapped;
		p10.removeAll(subnetPc);
		TreeSet<Integer> p11 = overlapped;
		p11.retainAll(subnetPc);
		
		TreeSet<Integer> p10up01 = p10;
		p10up01.addAll(p01);
		
		int time = 1;
		for (int k : p10up01) {
			time *= (popVec.get(k) + 1);
		}
		
		for (int k : p11) {
			int Nk = popVec.get(k);
			time *= ((Nk + 2)*(Nk + 1))/2;
		}
		
		int space = n.pcs.size() + n.fcs.size();
		
		return new ComplexityBundle(time, space);
	}
	
	/**
	 * Gets the complexity of running standard convolution over a network with the specified population vector.
	 * @param popVec The network's population vector.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	@Override
	protected ComplexityBundle getStandardAlgorithmComplexity(PopulationVector popVec) {
		int time = 1;
		int space = 1;
		int Nk = 0;
		
		for (int k = 0; k < popVec.size(); k++) {
			Nk = popVec.get(k);
			time *= ((Nk + 1)*(Nk + 2))/2;
			space *= (Nk + 1);
		}
		
		time *= (qnm.M-1);
		space *= 2;
		
		return new ComplexityBundle(time, space);
	}
	
	/**
	 * Gets the complexity if running standard convolution over a network with the specified population vector,
	 * in which feedback filtering is used.
	 * @param popVec The network's population vector.
	 * @return A ComplexityBundle containing the expected time and space complexities.
	 */
	private ComplexityBundle getStandardConvolutionComplexityFeedbackFiltering(PopulationVector popVec) {
		int time = 1;
		int space = 1;
		
		for (int k = 0; k < popVec.size(); k++) {
			space *= (popVec.get(k) + 1);
		}
		
		time = qnm.M * qnm.R * space;
		
		return new ComplexityBundle(time, space);
	}

	/**
	 * Prints a summary of the complexity evaluators calculations.
	 */
	@Override
	protected void printSummary() {
		Printer.out.println("--- Tree Convolution Complexity Evaluation ---");
		getMachineCapabilities();
		if (stdComplexity != null) {
			Printer.out.println("Std. convolution: Time = " + stdComplexity.timeComplexity
					+ " Space = " + stdComplexity.spaceComplexity);
		}
		if (stdComplexityFF != null) {
			Printer.out.println("Std. convolution w/ feedback filtering: Time = " + stdComplexityFF.timeComplexity
					+ " Space = " + stdComplexityFF.spaceComplexity);
		}
		if (treeComplexity != null) {
			Printer.out.println("Tree convolution: Time = " + treeComplexity.timeComplexity
					+ " Space = " + treeComplexity.spaceComplexity);
		}
		Printer.out.println("Sparsity of demands matrix: " + sparcityOfDemandsMatrix);
		Printer.out.println("Ratio of FCS to PCS: " + fcfToPcRatio);
		Printer.out.println("--- END Complexity Evaluation ---");
	}
}