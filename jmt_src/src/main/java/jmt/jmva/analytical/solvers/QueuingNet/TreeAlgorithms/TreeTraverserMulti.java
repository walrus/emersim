package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;

/**
 * Tree traverser abstract class which implements generic multi-threaded tree traversal, leaving the 
 * actual node computations to the extending class.
 * @author Ben Homer, 2014.
 */
public abstract class TreeTraverserMulti {
	
	/** The task executor which holds the thread pool. */
	protected ExecutorService taskExecutor;
	
	/**
	 * Constructor.
	 * @param numThreads The number of threads to use for computation.
	 */
	public TreeTraverserMulti(int numThreads) {
		initThreads(numThreads);
	}
	
	/** 
	 * Initialises the thread pool.
	 * @param numThreads The number of threads to use.
	 */
	private void initThreads(int numThreads) {
		// Determine max no. of threads to use based on available cores.
		int maxThreads = Runtime.getRuntime().availableProcessors();
		if (numThreads > maxThreads) {
			numThreads = maxThreads;
		}
		taskExecutor = Executors.newFixedThreadPool(numThreads);
	}
	
	/**
	 * Runs a recursive multi-threaded traversal and waits for the result.
	 * @param node The node from which to start the traversal.
	 * @param pv The population vector to use for node computations.
	 */
	public void traverse(Node node, PopulationVector pv) {
		Future<?> f = recursiveTraverseMultiCore(node, pv);
		waitForFuture(f);
	}
	
	/**
	 * Recursively traverses a tree from the specified node in a post-order
	 * fashion, performing computations at each node.
	 * @param node The node the traversal starts from.
	 * @param pv The population vector to use for node computations.
	 * @return A Future object which represents the result of the asynchronous computation
	 * at the specified node.
	 */
	private Future<?> recursiveTraverseMultiCore(Node node, PopulationVector pv) {
		Future<?>[] childThreads = new Future[2];
		if (node.leftChild() != null) {
			childThreads[0] = recursiveTraverseMultiCore(node.leftChild(), pv);
		}
		if (node.rightChild() != null) {
			childThreads[1] = recursiveTraverseMultiCore(node.rightChild(), pv);
		}
		
		// Wait for child threads to complete before proceeding.
		for (Future<?> f : childThreads) {
			waitForFuture(f);
		}
		
		Thread t;
		if (node.isLeaf()) {
			t = getComputeLeafNodeThread(node, pv);
		} else {
			t = getSubnetNodeThread(node, pv);
		}
		
		Future<?> f = taskExecutor.submit(t);
		return f;
	}
	
	/**
	 * Waits for a Future operation to complete.
	 * @param f The future object to wait on.
	 */
	private void waitForFuture(Future<?> f) {
		try {
			if (f != null) {
				f.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Abstract method which an extending class will override to define leaf node computations.
	 * @param node The leaf node.
	 * @param pv The population vector to use for the computation.
	 * @return A Thread which will run the leaf node computation.
	 */
	public abstract Thread getComputeLeafNodeThread(Node node, PopulationVector pv);
	
	/**
	 * Abstract method which an extending class will override to define subnetwork node computations.
	 * @param node The subnetwork node.
	 * @param pv The population vector to use for the computation.
	 * @return A Thread which will run the subnetwork node computation.
	 */
	public abstract Thread getSubnetNodeThread(Node node, PopulationVector pv);
}