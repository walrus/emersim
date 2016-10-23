package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePlanter;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeTraverserMulti;

/**
 * Tree Convolution solver that uses multiple threads.
 * @author Ben Homer, 2014.
 */
public class MultiThreadTCSolver extends TreeTraverserMulti implements IThreadedTCSolver {
	
	/** The queueing network model to solve. */
	private QNModel qnm;
	
	/** The convolution layer to delegate to. */
	private ConvolutionLayer cl;
	
	/** The tree planter. */
	private TreePlanter tp;
	
	/** The most recently calculated mean throughputs for each class. */
	private BigRational[] X;
	
	/** Constructor.
	 * @param qnm The queueing network model to solve.
	 * @param cl The convolution layer to delegate to.
	 * @param tp The tree planter.
	 */
	public MultiThreadTCSolver(QNModel qnm, ConvolutionLayer cl, TreePlanter tp, int numThreads) {
		super(numThreads);
		this.qnm = qnm;
		this.cl = cl;
		this.tp = tp;
	}
	
	/** Computes G using postorder tree traversal. 
	 * @param node The current node to compute the normalization constant G for.
	 * @param pv The population vector to be used during the computation.
	 * @returns The normalisation constant G at the given node, calculated using the
	 * specified population vector.
	 */
	public BigRational computeG(Node node, PopulationVector pv) {
		Printer.out.println("\nComputing normalization constant.");
		normalisationConstantTimer.start();
		this.traverse(node, pv);
		BigDecimal G = node.getG();
		normalisationConstantTimer.pause();
		Printer.out.println("G(N) = " + G);
		return new BigRational(G);
	}
	
	/** 
	 * Calculates per class throughputs using methods outlined in LamLien83 paper.
	 * @param p The population vector to use during the calculation.
	 * @return The per class throughputs as an array of BigRationals.
	 */
	@Override
	public BigRational[] calculateThroughputs(PopulationVector p) {
		X = new BigRational[qnm.R];
		
		Future<?>[] results = new Future[qnm.R];
		for (int s = 0; s < qnm.R; s++) {
			results[s] = taskExecutor.submit(new ThroughputThread(s, p.copy()));
		}
		
		for (int i = 0; i < qnm.R; i++) {
			try {
				X[i] = (BigRational) results[i].get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return X;
	}
	
	/**
	 * Thread used for calculating throughput for specific class.
	 */
	private class ThroughputThread implements Callable<BigRational> {
		
		/** The class ID. */
		private int s;
		
		/** The population vector to use during the calculation. */
		private PopulationVector p;
		
		/**
		 * Constructor.
		 * @param s The class ID.
		 * @param p The population vector to use during the calculation.
		 */
		public ThroughputThread(int s, PopulationVector p) {
			this.s = s;
			this.p = p;
		}
		
		/**
		 * Computes the throughput for the local class.
		 */
		@Override
		public BigRational call() throws Exception {
			double totD = 0;
			for (int k = 0; k < qnm.M; k++) {
				totD += qnm.getDemand(k, s);
			}
			if (totD == 0) {
				return  BigRational.ZERO;
			}
					
			p.minusOne(s + 1);
			BigRational newG = cl.computeGNminusOneMethod1(tp.rootClone(), s, p);
			BigRational X = newG.divide(qnm.getNormalisingConstant());
			Printer.out.println("Class " + s + ": X = " + X);
			p.restore();
			return X;
		}
	}

	/** 
	 * Calculates per class and per station mean queue lengths using methods outlined in LamLien83 paper.
	 * @param p The population vector to use during the calculation.
	 * @return The per class per station mean queue lengths as a 2D array of BigRationals.
	 */
	@Override
	public BigRational[][] calculateMeanQueueLengths(PopulationVector p) {
		BigRational[][] Q = new BigRational[qnm.M][qnm.R];
		
		Future<?>[] results = new Future[qnm.M];
		for (int m = 0; m < qnm.M; m++) {
			results[m] = taskExecutor.submit(new MeanQueueLengthsThread(m, p.copy()));
		}
		
		for (int i = 0; i < qnm.M; i++) {
			try {
				if (results[i] != null) {
					Q[i] = (BigRational[]) results[i].get();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return Q;
	}
	
	/**
	 * Thread used for calculating mean queue lengths for specific class.
	 */
	private class MeanQueueLengthsThread implements Callable<BigRational[]> {
		
		/** The station/queue ID. */
		private int m;
		
		/** The population vector to use during the calculation. */
		private PopulationVector p;
		
		/** Constructor.
		 * @param m The station/queue ID.
		 * @param p The population vector to use during the calculation.
		 */
		public MeanQueueLengthsThread(int m, PopulationVector p) {
			this.m = m;
			this.p = p;
		}
		
		/** 
		 * Computes the per class throughputs for the local station.
		 */
		@Override
		public BigRational[] call() throws Exception {
			switch (qnm.getQueueType(m)) {
			
			// Case: Queue-dependent station, DELAY/infinite server.
			case DELAY:
				// TODO: not quite right, needs multiplying by service time - currently can't access this.
				BigRational Q[] = new BigRational[qnm.R];
				for (int r = 0; r < qnm.R; r++) {
					Q[r] = X[r].multiply(qnm.getDemandAsBigRational(m, r)); 
				}
				return Q;
			
			// Case: Queue-dependent station, with general service rate function.
			// (This will never actually be accessed from JMT since multi-class models
			// do not allow general queue-depended stations)
			// Currently not supported by QNModel data structure, needs access to service times.
			case LD:
				//Node root = tp.createTreeWithRemovedStation(m);
				throw new UnsupportedOperationException("LD stations not supported.");
				// TODO: implement eq (2) and (14) from Lam/Lien -> need to be able to access service times.
			
			// Case: Fixed-rate service station (LI).
			case LI:
				// Create tree for Gm+ as described in Lam/Lien paper.
				Node cloneParent = tp.createTreeWithClonedStation(m);
				
				if (Config.Q_COMPUTE_SEQUENTIALLY) {
					/* Calculates the values of Gm+(N - 1_k) for each station m and class k
					 * individually, storing a single g array at each node. */
					return cl.computeGmPlusStoreSingle(cloneParent, p, m);
				} else {
					/* Calculates the values of Gm+(N - 1_k) for each station m and class k,
					 * calculating for all classes k in single pass, stores multiple g arrays at each node. */
					return cl.computeGmPlusStoreMultiple(cloneParent, p, m);
				}
				
			default:
				throw new IllegalStateException("Unsupported station type.");
			}
		}
	}

	/** 
	 * Computes results at leaf node by creating an appropriate thread to run the computation.
	 * @param node The leaf node.
	 * @param pv The population vector to use for the computation.
	 * @return The thread which will run the computation.
	 */
	@Override
	public Thread getComputeLeafNodeThread(Node node, PopulationVector pv) {
		return new ComputeLeafNodeThread(node, pv);
	}

	/** 
	 * Computes results at subnetwork node by creating an appropriate thread to run the computation.
	 * @param node The subnetwork node.
	 * @param pv The population vector to use for the computation.
	 * @return The thread which will run the computation.
	 */
	@Override
	public Thread getSubnetNodeThread(Node node, PopulationVector pv) {
		return new ComputeSubnetNodeThread(node, pv);
	}
	
	/** Thread for calculating leaf node g-arrays. */
	private class ComputeLeafNodeThread extends Thread {
		/** The leaf node. */
		private Node node;
		
		/** The population vector to use during the calculation. */
		private PopulationVector pv;
		
		/**
		 * Constructor.
		 * @param node The leaf node.
		 * @param pv The population vector to use during the calculation.
		 */
		public ComputeLeafNodeThread(Node node, PopulationVector pv) {
			this.node = node;
			this.pv = pv;
		}
		
		/** Runs the leaf node g-array computation. */
	    public void run() {
	    	cl.computeLeafNodeGArray(node, pv);
	    }
	}
	
	/** Thread for calculating subnetwork node g-arrays. */
	private class ComputeSubnetNodeThread extends Thread {
		/** The subnetwork node. */
		private Node node;
		
		/** The population vector to use during the calculation. */
		private PopulationVector pv;
		
		/**
		 * Constructor.
		 * @param node The subnet node.
		 * @param pv The population vector to use during the calculation.
		 */
		public ComputeSubnetNodeThread(Node node, PopulationVector pv) {
			this.node = node;
			this.pv = pv;
		}
		
		/** Runs the subnet node g-array computation. */
	    public void run() {
	    	cl.computeSubnetNodeGArray(node, pv);
	    }
	}
}