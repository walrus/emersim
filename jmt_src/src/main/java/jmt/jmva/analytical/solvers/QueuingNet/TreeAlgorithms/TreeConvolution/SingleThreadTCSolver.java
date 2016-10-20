package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import java.math.BigDecimal;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreePlanter;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeTraverser;

/**
 * Tree Convolution solver that uses a single thread.
 * @author Ben Homer, 2014.
 */
public class SingleThreadTCSolver extends TreeTraverser implements IThreadedTCSolver  {

	/** The queueing network model to solve. */
	private QNModel qnm;
	
	/** The convolution layer to delegate to. */
	private ConvolutionLayer cl;
	
	/** The tree planter. */
	private TreePlanter tp;
	
	/** The most recently calculated mean throughputs for each class. */
	private BigRational[] X;
	
	/**
	 * Constructor.
	 * @param qnm The queueing network model to solve.
	 * @param cl The convolution layer to delegate to.
	 * @param tp The tree planter.
	 */
	public SingleThreadTCSolver(QNModel qnm, ConvolutionLayer cl, TreePlanter tp) {
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
		if (Config.RECURSIVE) {
			this.recursiveTraverse(node, pv);
		} else {
			this.iterativeTraverse(node, pv);
		}
		normalisationConstantTimer.pause();
		BigDecimal res = node.getG();
		Printer.out.println("G(N) = " + res);
		return new BigRational(res);
	}

	/** 
	 * Computes results at leaf node by delegating to the convolution layer.
	 * @param node The leaf node.
	 * @param pv The population vector to use for the computation.
	 */
	@Override
	public void initLeafNode(Node node, PopulationVector pv) {
		cl.computeLeafNodeGArray(node, pv);
	}
	
	/** 
	 * Computes results at subnetwork node by delegating to the convolution layer.
	 * @param node The subnetwork node.
	 * @param pv The population vector to use for the computation.
	 */
	@Override
	public void computeSubnetNode(Node node, PopulationVector pv) {
		cl.computeSubnetNodeGArray(node, pv);
	}
	
	/** 
	 * Calculates per class throughputs using methods outlined in LamLien83 paper.
	 * @param p The full population vector.
	 * @return The per class throughputs as an array of BigRationals.
	 */
	@Override
	public BigRational[] calculateThroughputs(PopulationVector p) {
		X = new BigRational[qnm.R];
		Printer.out.println("--- Calculating throughputs ---");
		throughputTimer.start();
		for (int s = 0; s < qnm.R; s++) {
			double totD = 0;
			for (int k = 0; k < qnm.M; k++) {
				totD += qnm.getDemand(k, s);
			}
			if (totD == 0) {
				X[s] = BigRational.ZERO;
				continue;
			}
			
			p.minusOne(s + 1);
			BigRational newG = cl.computeGNminusOneMethod1(tp.rootClone(), s, p);
			X[s] = newG.divide(qnm.getNormalisingConstant());
			p.restore();
		}
		
		throughputTimer.pause();
		Printer.out.println("--- END Calculating throughputs ---");
		return X;
	}
	
	/** 
	 * Calculates per class and per station mean queue lengths using methods outlined in LamLien83 paper.
	 * @param p The full population vector.
	 * @return The per class per station mean queue lengths as a 2D array of BigRationals.
	 */
	@Override
	public BigRational[][] calculateMeanQueueLengths(PopulationVector p) {
		BigRational[][] Q = new BigRational[qnm.M][qnm.R];
		Printer.out.println("--- Calculating mean queue lengths ---");
		queueLengthTimer.start();

		tp.printCurrentTree();
		
		for (int m = 0; m < qnm.M; m++) {
			
			switch (qnm.getQueueType(m)) {
			
			// Case: Queue-dependent station, DELAY/infinite server.
			case DELAY:
				// TODO: not quite right, needs multiplying by service time - currently cant access this.
				for (int r = 0; r < qnm.R; r++) {
					Q[m][r] = X[r].multiply(qnm.getDemandAsBigRational(m, r));
				}
				break;
			
			// Case: Queue-dependent station, with general service rate function.
			// (This will never actually be accessed from JMT since multi-class models
			// do not allow general queue-depended stations)
			// Currently not supported by QNModel data structure, needs access to service times
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
					Q[m] = cl.computeGmPlusStoreSingle(cloneParent, p, m);
				} else {
					/* Calculates the values of Gm+(N - 1_k) for each station m and class k,
					 * calculating for all classes k in single pass, stores multiple g arrays at each node. */
					Q[m] = cl.computeGmPlusStoreMultiple(cloneParent, p, m);
				}
				break;
				
			default:
				throw new IllegalStateException("Unsupported station type.");
			}
		}
		
		Printer.out.println("--- END Calculating mean queue lengths ---");
		queueLengthTimer.pause();
		return Q;
	}
}
