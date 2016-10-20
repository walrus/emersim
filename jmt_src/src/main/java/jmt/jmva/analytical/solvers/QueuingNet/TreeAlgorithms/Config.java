package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.math.MathContext;

import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePlanter.PlantMode;

/**
 * Configuration values for tree algorithms.
 * @author Ben Homer, 2014.
 */
public class Config {

	/** Each step of the algorithm is printed to the console if true. */
	public static final boolean VERBOSE = false;
	
	/** All verbose outputs are written to log file if true. */
	public static final boolean LOG = false;
	
	/** The context to use for BigDecimal calculations. */
	public static final MathContext Context = MathContext.DECIMAL64;
	
	/** TREE PLANTING CONSTS **/
	
	/** If true the tree planter tries several heuristics in the tree planting stage
	 * to find a tree with the best complexity characteristics. */
	public static boolean COMPARE_ALL_HEURISTICS = true;
	
	/** Determines which complexities are checked when comparing heuristics,
	 * when COMPARE_ALL_HEURISTICS is true, as follows:
	 * 0 = best time complexity
	 * 1 = best space complexity
	 * 2 = best overall/combined complexity (space and time)
	 * 3 = based on number of partially covered classes in whole tree.
	 */
	public static int COMPLEXITY_CHECK_LEVEL = 3;
	
	/** The tree planting mode to use if COMPARE_ALL_HEURISTICS is false. */
	public static PlantMode TREE_PLANT_MODE = PlantMode.SIMPLE;
	
	/** The max number of tree plant attempts allowed before the
	 * algorithm terminates in a failed state. */
	public static final int MAX_TREE_PLANT_ATTEMPTS = 3;
	
	/** Specifies whether the algorithm should terminate if the complexity manager
	 *  repeatedly decides that the tree algorithm is not the best approach, given
	 *  the tree planted in the 'tree planting' stage of the algorithm. */
	public static boolean TERMINATE_IF_HIGH_COMPLEXITY = false;
	
	/** GENERAL CONSTRAINTS **/
	
	/** Specifies the number of threads to be used when running the tree algorithms.
	 *  N.B. only used from JMVA options panel. */
	public static int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	
	/** Indicates whether a recursive or iterative approach is used to traverse the tree. */
	public static boolean RECURSIVE = false;

	/** TREE CONVOLUTION - CORE ALGORITHM CONSTANTS **/
	
	/** Indicates whether mean queue lengths should be computed individually/sequentially
	 * for each station m and class k, or if g arrays for all classes k should be calculated and
	 * stored in a tree node simultaneously. For detail see Lam/Lien paper sec. 4. */
	public static final boolean Q_COMPUTE_SEQUENTIALLY = true;
	
	/** Indicates whether throughputs should be computed individually/sequentially. */
	public static final boolean X_COMPUTE_SEQUENTIALLY = true;
	
	/** If true then uses the feedback filtering approach to compute the g array at nodes where both children
	 * are leaves, and at least one of them is a fixed-rate station. 
	 * N.B. Leave this as true for good performance on networks with large populations.
	 */
	public static final boolean USE_FEEDBACK_FILTERING = true;
	
	/** The feedback filtering approach is only attempted if the overlapped population between the child stations
	 *  if greater than the specified value. */
	public static final int FEEDBACK_FILTERING_OVERLAP_POP_THRESHOLD = 1;
	
	/** Prints the current config state. */
	public static void print() {
		Printer.out.println(" --- CONFIG ---");
		Printer.out.println("VERBOSE: " + VERBOSE);
		Printer.out.println("LOG: " + LOG);
		Printer.out.println("COMPARE_ALL_HEURISTICS: " + COMPARE_ALL_HEURISTICS);
		Printer.out.println("COMPLEXITY_CHECK_LEVEL: " + COMPLEXITY_CHECK_LEVEL);
		Printer.out.println("TREE_PLANT_MODE: " + TREE_PLANT_MODE);
		Printer.out.println("MAX_TREE_PLANT_ATTEMPTS: " + MAX_TREE_PLANT_ATTEMPTS);
		Printer.out.println("TERMINATE_IF_HIGH_COMPLEXITY: " + TERMINATE_IF_HIGH_COMPLEXITY);
		Printer.out.println("NUM_THREADS: " + NUM_THREADS);
		Printer.out.println("RECURSIVE: " + RECURSIVE);
		Printer.out.println("Q_COMPUTE_SEQUENTIALLY: " + Q_COMPUTE_SEQUENTIALLY);
		Printer.out.println("X_COMPUTE_SEQUENTIALLY: " + X_COMPUTE_SEQUENTIALLY);
		Printer.out.println("USE_FEEDBACK_FILTERING: " + USE_FEEDBACK_FILTERING);
		Printer.out.println("FEEDBACK_FILTERING_OVERLAP_POP_THRESHOLD: " + FEEDBACK_FILTERING_OVERLAP_POP_THRESHOLD);
	}
}