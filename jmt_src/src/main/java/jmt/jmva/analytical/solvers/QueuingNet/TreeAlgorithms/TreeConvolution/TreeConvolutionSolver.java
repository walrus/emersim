/**
 * Copyright (C) 2014, Ben Homer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.QNSolver;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ComplexityEvaluator;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePlanter;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePrinter;

/**
 * Class that implements the Tree Convolution algorithm outlined in the paper
 * "A Tree Convolution Algorithm for the Solution of Queueing Networks" (Lam/Lien 1983).
 * Allows solution of sparse networks which are intractable using the sequential convolution algorithm.
 * @author Ben Homer, 2014.
 */
public class TreeConvolutionSolver extends QNSolver {

	/** The number of threads to use when running the algorithm. */
	private int numThreads;
	
	/** The TreePlanter responsible for planting the tree initially. */
	private TreePlanter treePlanter;
	
	/** The single or multi-threaded solver, responsible for calculating the normalisation
	 *  constant and performance measures. */
	private IThreadedTCSolver solver;

	/**
	 * Constructor.
	 * @param qnm The queueing network model to solve.
	 * @param numThreads The number of threads to solve the model with.
	 * @throws InternalErrorException
	 */
	public TreeConvolutionSolver(QNModel qnm, int numThreads) throws InternalErrorException {
		super(qnm);
		this.numThreads = numThreads;
		Printer.create();
		TreePrinter treePrinter = new TreePrinter();
		ClassCoverageUtils ccu = ClassCoverageUtils.create(qnm);
		ConvolutionCore cc = new ConvolutionCore(qnm, ccu);
		ConvolutionLayer cl = new ConvolutionLayer(cc);
		ComplexityEvaluator ce = new TCComplexityEvaluator(qnm, ccu);
		treePlanter = new TreePlanter(qnm, ccu, ce, treePrinter);
		
		if (numThreads > 1) {
			solver = new MultiThreadTCSolver(qnm, cl, treePlanter, numThreads);
		} else {
			solver = new SingleThreadTCSolver(qnm, cl, treePlanter);
		}
	}
	
    /**
     * Prints a short welcome message that says which solver is used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using Tree-Convolution. Multi-core=" + (numThreads > 1));
    }

	/**
	 * Computes G, the normalisation constant, by first running the preprocessor stage (tree planting and complexity evaluation)
	 * and then doing a postorder traversal of the tree to calculate G at the root node.d
	 */
	@Override
	public void computeNormalisingConstant() throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
		if (!treePlanter.runTreePlantingPhase()) {
			throw new InternalErrorException(
					"Early termination, suitable tree could not be planted. A sequential algorithm may be better for this network.");
		}
		
		BigRational G = solver.computeG(treePlanter.treeRoot(), qnm.N);
		if (G == null) {
			throw new InternalErrorException("Error computing G(N).");
		}
		
		qnm.setNormalisingConstant(G);
		this.G = G;
	}

	/**
	 * Computes the throughput and mean queue length performance measures from which all other performance measures can be derived.
	 */
	@Override
	public void computePerformanceMeasures() throws InternalErrorException {
		PopulationVector p = qnm.N.copy();
		totalTimer.start();
		BigRational[] X = solver.calculateThroughputs(p);
		BigRational[][] Q = solver.calculateMeanQueueLengths(p);
		totalTimer.pause();
		qnm.setPerformanceMeasures(Q, X);
		printSummary();
	}
	
	/**
	 * Prints a summary of the tree convolution solution, including the model solved and the time splits for various parts of the algorithm.
	 */
	private void printSummary() {
		if (Config.VERBOSE) {
			System.out.println("\n --- Tree Convolution Summary ---");
			qnm.prettyPrint();
			System.out.println("\n --- Time splits ---");
			System.out.println("Plant mode: " + treePlanter.getPlantModeUsed());
			System.out.println("No. threads: " + numThreads);
			System.out.println("Tree Planting: " + treePlanter.treePlantTimer.getPrettyInterval());
			System.out.println("Normalization Constant: " + IThreadedTCSolver.normalisationConstantTimer.getPrettyInterval());
			System.out.println("Performance Measures: X: " + IThreadedTCSolver.throughputTimer.getPrettyInterval()
					+ " Q: " + IThreadedTCSolver.queueLengthTimer.getPrettyInterval() + " Tot: " + totalTimer.getPrettyInterval());
			long total = 
					treePlanter.treePlantTimer.getInterval() + 
					IThreadedTCSolver.normalisationConstantTimer.getInterval() +
					totalTimer.getInterval();
			System.out.println("Total Time: " + total + " msec");
			System.out.println("\n --- END Tree Convolution Summary ---\n");
		}
	}
}