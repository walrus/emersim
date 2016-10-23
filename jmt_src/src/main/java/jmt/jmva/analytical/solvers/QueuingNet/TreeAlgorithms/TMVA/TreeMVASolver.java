package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.QNSolver;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ComplexityEvaluator;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePlanter;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePrinter;

/**
 * Tree-MVA implementation based on Hoyme and Tucci papers from 1985.
 * @author Ben Homer, 2014.
 */
public class TreeMVASolver extends QNSolver {

	/** The number of threads to use when running the solver. */
	private int numThreads;
	
	/** The tree planter instance. */
	private TreePlanter treePlanter;

	/** The instantiated solver (may be a sequential MVA solver,
	 * a single-threaded TMVA solver, or a multi-threaded TMVA solver depending on settings). */
	private IThreadedTMVASolver solver;
	
	/** If true then sequential MVA used instead of TMVA. */
	boolean useJMTsequentialMVA;
	
	/**
	 * Constructor.
	 * @param qnm The model to solve.
	 * @param numThreads The number of threads to use.
	 * @param useSequentialMVA Whether to use sequential MVA.
	 * @throws InternalErrorException
	 */
	public TreeMVASolver(QNModel qnm, int numThreads, boolean useJMTsequentialMVA) throws InternalErrorException {
		super(qnm);
		this.numThreads = numThreads;
		this.useJMTsequentialMVA = useJMTsequentialMVA;
		Printer.create();
		TreePrinter treePrinter = new TreePrinter();
		ClassCoverageUtils ccu = ClassCoverageUtils.create(qnm);
		ComplexityEvaluator cm = new TMVAComplexityEvaluator(qnm, ccu);
		treePlanter = new TreePlanter(qnm, ccu, cm, treePrinter);
		TMVACore tmvaCore = new TMVACore(qnm, ccu);
		
		if (useJMTsequentialMVA) {
			solver = new StandardMVASolver(qnm, ccu, true);
		} else if (numThreads > 1) {
			solver = new MultiThreadTMVASolver(tmvaCore, numThreads);
		} else {
			solver = new SingleThreadTMVASolver(tmvaCore);
		}
	}
	
    /** 
     * Prints a short welcome message that says which solver is used. 
     */
    @Override
    public void printWelcome() {
        System.out.println("Using Tree-MVA. Multi-core=" + (numThreads > 1));
    }
	
    /**
     * Computes and stores the performance measures (mean throughputs and mean
     * queue lengths for the current model.
     * @throws InternalErrorException Thrown when any computation fails
     */
	public void computePerformanceMeasures() throws InternalErrorException {
		if (!treePlanter.runTreePlantingPhase()) {
			throw new InternalErrorException(
					"Early termination, suitable tree could not be planted. A sequential algorithm may be better for this network.");
		}
		
		totalTimer.start();
		Node root = treePlanter.treeRoot();
		MVAResults res = solver.solve(root, qnm.N);
		if (res == null) {
			throw new InternalErrorException("Error computing MVA results.");
		}
		
		BigRational[] X = new BigRational[qnm.R];
		BigRational[][] Q = new BigRational[qnm.M][qnm.R];
		int ri = 0;
		int ki = 0;
		for (int r = 0; r < qnm.R; r++) {
			boolean rResult = root.getAllCoveredClasses().contains(r);
			X[r] = rResult ? new BigRational(res.X[ri]) : BigRational.ZERO;
			ki = 0;
			for (int k = 0; k < qnm.M; k++) {
				boolean kResult = root.stations.contains(k);
				Q[k][r] = rResult && kResult ? new BigRational(res.Q[ki][ri]) : BigRational.ZERO;
				if (kResult) ki++;
			}
			if (rResult) ri++;
		}
		
		totalTimer.pause();
		qnm.setPerformanceMeasures(Q, X);
		printSummary();
	}
	
	/**
	 * Prints a summary of the tree MVA solution, including the model
	 * solved and the time splits for various parts of the algorithm.
	 */
	private void printSummary() {
		if (Config.VERBOSE) {
			System.out.println("\n --- Tree MVA Summary ---");
			qnm.prettyPrint();
			System.out.println("\n --- Time splits ---");
			System.out.println("Plant mode: " + treePlanter.getPlantModeUsed());
			System.out.println("No. threads: " + numThreads);
			System.out.println("Sequential MVA: " + useJMTsequentialMVA);
			System.out.println("Tree Planting: " + treePlanter.treePlantTimer.getPrettyInterval());
			System.out.println("Performance Measures: " + totalTimer.getPrettyInterval());
			long total = 
					treePlanter.treePlantTimer.getInterval() + 
					totalTimer.getInterval();
			System.out.println("Total Time: " + total + " msec");
			System.out.println("\n --- END Tree MVA Summary ---\n");
		}
	}
}