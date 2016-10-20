package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import java.math.BigDecimal;
import java.util.Arrays;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;

/**
 * Layer inbetween the single and multi-threaded solvers and the convolution core.
 * @author Ben Homer, 2014.
 */
public class ConvolutionLayer {
	
	/** The queueing network model. */
	private QNModel qnm;
	
	/** The convolution core implementation. */
	private ConvolutionCore cc;
	
	/**
	 * Constructor.
	 * @param cc The convolution core class.
	 */
	public ConvolutionLayer(ConvolutionCore cc) {
		this.cc = cc;
		this.qnm = cc.qnm;
	}
	
	/** 
	 * Wraps the computation of leaf node g-arrays so that the solvers
	 * do not need direct access to the convolution core.
	 * @param n The leaf node.
	 * @param popVec The population vector to use during the computation.
	 * @param backup Whether to store the results in a backup g-array map.
	 */
	public void computeLeafNodeGArray(Node node, PopulationVector popVec) {
		// TODO: for now assuming backup is false.
		cc.computeLeafNodeGArray(node, popVec, false);
	}
	
	/** Wraps the computation of subnet node g arrays so that the solvers
	 * do not need direct access to the convolution core.
	 */
	public void computeSubnetNodeGArray(Node node, PopulationVector popVec) {
		cc.computeSubnetNodeGArray(node, popVec, false);
	}
	
	/** Computes the normalization constant G(N - 1_k) for the specified class k,
	 * required to calculate the chain throughputs.
	 * Method 1 outlined in sec. 4.3 in Lam/Lien paper.
	 * @param k The class k.
	 */
	public BigRational computeGNminusOneMethod1(Node root, int k, PopulationVector pv) {
		// The node at which class k becomes fully covered.
		Node NODE = findNodeAtWhichFirstFullyCovered(root, k);
		if (NODE == null) {
			return BigRational.ZERO;
		}
		
		return new BigRational(computeGFromNodeBottomUp(NODE, pv));
	}
	
	/**
	 * Calculates Gm+(n-1_k) using first method from section 4.2 in Lam/Lien paper. This is
	 * a required normalization constant when computing mean queue lengths and is carried out
	 * on a tree in which station m has been cloned.
	 * @param cloneParent The node which is the parent of the two cloned children.
	 * @param p The population vector to use during the computation.
	 * @param m The station/queue which has been cloned.
	 * @return An array containing the Gm+ normalisation constants for each class.
	 */
	public BigRational[] computeGmPlusStoreSingle(Node cloneParent, PopulationVector p, int m) {
		BigRational[] res = new BigRational[qnm.R];
		Arrays.fill(res, BigRational.ZERO);
		BigRational G = qnm.getNormalisingConstant();
		
		/* Optimised leaf node calculation.
		 * 1) Avoid computing same Gmap twice, since nodes are clones of each other.
		 * 2) Only recompute leaf node Gmap if it has been marked because its partially 
		 * covered classes set has changed. 
		 * 3) If we do need to recompute the leaf node Gmap, we only calculate values once for full population,
		 * the results for full population are then reused in subsequent computations to save time. */ 
		if (cloneParent.leftChild().marked) {
			cc.computeLeafNodeGArray(cloneParent.leftChild(), p, false);
		}
		cloneParent.rightChild().Gmap = cloneParent.leftChild().Gmap;
		
		for (int r : cloneParent.leftChild().pcs) {
			p.minusOne(r + 1);
			BigDecimal newG = computeGFromNodeBottomUp(cloneParent, p);
			BigDecimal demand = qnm.getDemandAsBigDecimal(m, r);
			res[r] = new BigRational(demand.multiply(newG, Config.Context)).divide(G);
			p.restore();
		}
		
		// For fully covered classes only need to compute for maximum population.
		for (int r : cloneParent.leftChild().fcs) {
			res[r] = new BigRational(p.get(r));
		}
		return res;
	}
	
	/** 
	 * Calculates Gm+(n-1_k) by storing multiple g arrays, one for each partially covered class in pc.
	 * @param cloneParent The node which is the parent of the two cloned children.
	 * @param p The population vector to use during the computation.
	 * @param m The station/queue which has been cloned.
	 * @return An array containing the Gm+ normalisation constants for each class.
	 *  */
	public BigRational[] computeGmPlusStoreMultiple(Node cloneParent, PopulationVector p, int m) {
		BigRational[] res = new BigRational[qnm.R];
		Arrays.fill(res, BigRational.ZERO);
		PopulationVector[] ps = new PopulationVector[qnm.R];
		for (int r : cloneParent.pcs) {
			p.minusOne(r + 1);
			ps[r] = p.copy();
			p.restore();
		}
		
		/* Optimised leaf node calculation.
		 * 1) Avoid computing same Gmap twice, since nodes are clones of each other.
		 * 2) Only recompute leaf node Gmap if it has been marked because its partially 
		 * covered classes set has changed. 
		 * 3) If we do need to recompute the leaf node Gmap, we only calculate values once for full population,
		 * the results for full population are then reused in subsequent computations to save time. */
		// N.B. This implementation uses more space (since each backupGmap contains full population results)
		// but saves some time (possibly).
		if (cloneParent.leftChild().marked) {
			cc.computeLeafNodeGArray(cloneParent.leftChild(), p, false);
		}
		for (int r : cloneParent.pcs) {
			cloneParent.leftChild().backupGmaps.put(ps[r], cloneParent.leftChild().Gmap);
			cloneParent.rightChild().backupGmaps = cloneParent.leftChild().backupGmaps;
		}
		
		Node cloneParentOrig = cloneParent.clone();
		
		while (true) {
			for (int r : cloneParentOrig.pcs) {
				cc.computeSubnetNodeGArray(cloneParent, ps[r], true);
			}
			
			if (cloneParent.parent() == null) {
				BigRational G = qnm.getNormalisingConstant();
				for (int r : cloneParentOrig.pcs) {
					BigDecimal newG = cloneParent.getBackupG(ps[r]);
					BigDecimal demand = qnm.getDemandAsBigDecimal(m, r);
					res[r] = new BigRational(demand.multiply(newG, Config.Context)).divide(G);
				}
				
				for (int r : cloneParentOrig.fcs) {
					res[r] = new BigRational(p.get(r));
				}
				
				return res;
			}
			
			// Clear up backup gmaps when no longer needed to save space.
			cloneParent.leftChild().backupGmaps.clear();
			cloneParent.rightChild().backupGmaps.clear();
			cloneParent = cloneParent.parent();
		}
	}
	
	/** Computes normalisation constant G by only carrying out the
	 * convolutions between the specified node and the root.
	 * This is required during performance measure calculation.
	 * @param node The node from which to start the calculation.
	 * @param pv The population vector to use during the computation.
	 */
	private BigDecimal computeGFromNodeBottomUp(Node node, PopulationVector pv) {
		if (node.isLeaf()) {
			// TODO: fix equ 15 which will be faster to compute, started implementing below.
			//computeLeafNodeGArrayScaling(node, pv, k);
			cc.computeLeafNodeGArray(node, pv, false);
		} else {
			cc.computeSubnetNodeGArray(node, pv, false);
		}
		
		if (node.parent() != null) {
			return computeGFromNodeBottomUp(node.parent(), pv);
		}

		return node.getG();
	}
	
	/** Finds the node/station at which class k first becomes fully covered. */
	private Node findNodeAtWhichFirstFullyCovered(Node node, int k) {
		if (!node.fcs.contains(k)) {
			Printer.out.println("Node does not fully covered class " + k);
		}
		
		if (node.leftChild() != null && node.leftChild().fcs.contains(k)) {
			return findNodeAtWhichFirstFullyCovered(node.leftChild(), k);
		}
		
		if (node.rightChild() != null && node.rightChild().fcs.contains(k)) {
			return findNodeAtWhichFirstFullyCovered(node.rightChild(), k);
		}
		
		return node;
	}
	
	/** Implements Eq. (15) from Lam/Lien paper. */
	// TODO: doesn't seem to work, using normal leaf calculation for time being.
	/*public void computeLeafNodeGArrayScaling(Node n, PopulationVector popVec, int k) {
		if (n.stations.size() != 1) {
			return;
		}

		TreeSet<Integer> pc = n.pcs;
		TreeSet<Integer> fc = n.fcs;

		// Inputs
		// Only single stations at node since leaf node.
		int m = (int)n.stations.toArray()[0];
		// Only need to store partial arrays as paper describes.
		Integer[] ps = new Integer[pc.size()];
		int i = 0;
		for (int pcClass : pc) {
			ps[i] = popVec.get(pcClass);
			i++;
		}
		
		PopulationVector p = new PopulationVector(ps);
		PopulationVector orig = p.copy();

//		int sumNk = 0;
//		int sumik = 0;
//		for (int cls : fc) {
//			int Nk = popVec.get(cls); // qnm.getPopulationVector().get(k);
//			sumNk += Nk;
//		}
		
		System.out.println("Computing leaf g array for m = " + m);
//		for (PopulationVector p : pvectors) {
		while (p != null) {
			BigRational res;

			int sumik = 0;
			int sumNk = 0;

//			for (int c : fc) {
//				int Nk = popVec.get(k); // qnm.getPopulationVector().get(k);
				sumNk += popVec.get(k);
//			}

//			int idx = 0;
//			for (int c : pc) {
				int ik = p.get(0);
//				idx++;
				sumik += ik;
//			}

//			int nm = sumik + sumNk;

			BigRational nm = new BigRational(sumNk + sumik) ;//sumik + sumNk);
//			int k = popVec.size()-1;
			BigRational Nk = new BigRational(popVec.get(k));
//			BigRational nm = new BigRational(popVec.get(m));
			BigRational pmk = qnm.getDemandAsBigRational(m, k);
			res = Nk.divide(nm.multiply(pmk));
			
			res = res.multiply(n.Gmap.get(p));
			n.Gmap.put(p, res);
			p = nextPermutation(p, orig);
		}
	}*/
}