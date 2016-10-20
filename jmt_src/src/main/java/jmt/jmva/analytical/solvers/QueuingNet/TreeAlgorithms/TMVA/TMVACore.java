package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import java.util.HashMap;
import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;

/**
 * Core TMVA algorithm implementation, based on theory from Tucci's 1985 paper and Hoyme's 1986 paper.
 * @author Ben Homer, 2014.
 */
public class TMVACore {
	
	/** The queueing network model to solve. */
	private QNModel qnm;
	
	/** The class coverage utilities instance. */
	private ClassCoverageUtils ccu;
	
	/** Sequential MVA solver for solving node's whose parent's are both leaves. */
	private StandardMVASolver sequentialMVASolver;
	
	/** Constructor.
	 * @param qnm The queueing network model to solve.
	 * @param ccu The class coverage utilities instance.
	 */
	public TMVACore(QNModel qnm, ClassCoverageUtils ccu) {
		this.qnm = qnm;
		this.ccu = ccu;
		sequentialMVASolver = new StandardMVASolver(qnm, ccu, false);
	}
	
	/**
	 * Initialises the specified leaf node, using the formulas in Hoyme's paper.
	 * @param node The leaf node.
	 */
	public void initLeafNode(Node node) {
		// Only need to initialise leaf node if it is the only station in the network
		// or it has a parent node that has one leaf child and one non-leaf child.
		// Case when parent has two leaf children is handled by sequential MVA at parent node.
		Node parent = node.parent();
		if (parent != null && parent.childrenAreLeaves()) return;
		
		int station = node.stations.iterator().next();
		TreeSet<Integer> cs = node.getAllCoveredClasses();
		
		PopulationVector pcvec = new PopulationVector(0, cs.size());
		PopulationVector pcmax = ccu.contract(qnm.N, ccu.all, cs); 
		
		Printer.out.println("\nInit leaf node: station = " + station + " pcs = " 
				+ node.pcs + " cs = " + node.getAllCoveredClasses());
		
		int nsum, nr;
		double demand, mul, val;
		
		while (pcvec != null) {
			PopulationVector n = pcvec;
			nsum = n.sum();
			
			Printer.out.println("--- N = " + n.toString() + " ---");
			
			MVAResults res = new MVAResults(1, cs.size());
			int ri = 0;
			for (int r : cs) {
				nr = n.get(ri);
				demand = qnm.getDemand(station, r);
				mul = (double)nsum/(double)nr;
				val = nr == 0 ? 0 : demand * mul;
				res.X[ri] = val == 0 ? 0 : 1/val;
				res.Q[0][ri] = nr;
				ri++;
			}
			
			node.mvaRes.put(n, res);
			pcvec = ccu.nextPermutationUpwards(pcvec, pcmax);
		}
	}
	
	/**
	 * Computes the TMVA results at a subnet node.
	 * @param node The subnet node.
	 */
	public void computeSubnetNode(Node ab) {
		Node a = ab.leftChild();
		Node b = ab.rightChild();
		// Use sequential MVA a parent of two leaf nodes as Tucci suggests.
		if (ab.childrenAreLeaves()) {
			sequentialMVASolver.solve(ab, qnm.N);
			// Clear out child node results, not required anymore.
			a.mvaRes.clear();
			b.mvaRes.clear();
			return;
		}
		
		TreeSet<Integer> ab_cs = ab.getAllCoveredClasses();
		TreeSet<Integer> a_cs = a.getAllCoveredClasses();
		TreeSet<Integer> b_cs = b.getAllCoveredClasses();
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(a.pcs);
		commonPcs.addAll(b.pcs); 
		
		Printer.out.println("\nAggregate stations: " 
				+ a.stations.toString() + " + " + b.stations.toString()
			    + " pcs = " + ab.pcs.toString() + " cs = " + ab_cs.toString()
				+ "\na : pcs = " + a.pcs.toString() + " cs = " + a.getAllCoveredClasses().toString() 
				+ " b : pcs " + b.pcs.toString() + " cs = " + b.getAllCoveredClasses().toString());
	
		PopulationVector n_ab;
		PopulationVector npc = new PopulationVector(0, commonPcs.size());
		PopulationVector npcmax = ccu.contract(qnm.N, ccu.all, commonPcs);
		
		// NB this has to be local rather than global so that multi-threaded solver works correctly.
		// (i.e. so threads do not share pStore values).
		HashMap<PTuple, Double> pStore = new HashMap<PTuple, Double>();
		int ri, rx;
		double wa, wb, q;
		int ma, mb, ms;
		boolean stationInA;
		
		while (npc != null) {
			n_ab = ccu.expand(npc, commonPcs, qnm.N, ab_cs);
			
			Printer.out.println("--- N = " + n_ab.toString() + " ---");
			
			MVAResults resAB = new MVAResults(ab.stations.size(), ab_cs.size());
			
			// Do this first, all updates to resAB will automatically update in mvaRes.
			ab.mvaRes.put(n_ab, resAB);
						
			ri = 0;
			for (int r : commonPcs) {
				wa = wb = 0;
				if (a_cs.contains(r)) {
					wa = residenceTimeGeneric(a, true, r, npc, pStore);
				}
				if (b_cs.contains(r)) {
					wb = residenceTimeGeneric(b, false, r, npc, pStore);
				}
				
				rx = ccu.convertIndex(ri, commonPcs, ab_cs);
				resAB.X[rx] = this.subnetThroughput6(rx, n_ab, wa, wb);; 
				ri++;
			}
			

			ri = 0;
			for (int r : ab_cs) {
				// Adjust throughputs
				int rab = ccu.getIndex(r, ab_cs);
				resAB.X[rab] = equ10a(ab, -1, r, npc, true, a_cs.contains(r), pStore);
				
				// Adjust mean queue lengths
				ma = mb = 0;
				for (int m : ab.stations) {
					stationInA = a.stations.contains(m);
					ms = (stationInA) ? ma : mb;
					q = 0;
					if ((stationInA && a_cs.contains(r)) || 
					   (!stationInA && b_cs.contains(r))) {
					  q = equ10a(ab, ms, r, npc, false, stationInA, pStore);
					}
					resAB.Q[ma+mb][ri] = q;
					if (stationInA) ma++; else mb++;
				}
				ri++;
			}
			
			npc = ccu.nextPermutationUpwards(npc, npcmax);
		}
		
		// Clear out child node results, not required anymore.
		a.mvaRes.clear();
		b.mvaRes.clear();
	}
	
	/** Implements equations 3a/b and 5 from Hoyme's paper for computing waiting/response times.
	 * @param child The node to compute response times at.
	 * @param isLeftChild Whether the node is the left child or not relative to its parent.
	 * @param r The class ID.
	 * @param n_ab The population vector for common pcs in the parent node.
	 * @param pStore The pFunc store for speeding up calls to pFunc.
	 * @return The residence/wait time. 
	 */
	private double residenceTimeGeneric(Node child, boolean isLeftChild, int r, PopulationVector n_ab, HashMap<PTuple, Double> pStore) {
		if (child.isLeaf()) {
			return this.computeResidenceTimeAtLeaf(child, isLeftChild, r, n_ab);
		}
		Node ab = child.parent();
		Node b = ab.rightChild();
		TreeSet<Integer> ab_cs = ab.getAllCoveredClasses();
		TreeSet<Integer> child_cs = child.getAllCoveredClasses();
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(ab.leftChild().pcs);
		commonPcs.addAll(ab.rightChild().pcs); 
		
		int r_child = ccu.getIndex(r, child_cs);
		int r_ab = ccu.getIndex(r, commonPcs);
		
		boolean kred, nred;
		int kr;
		double x, pb, res;
		PopulationVector k_child, k_;
		double sum = 0;
		
		PopulationVector k = new PopulationVector(0, n_ab.size());
		
		while (k != null) {
			kr = k.get(r_ab);
			if (kr == 0) { k = ccu.nextPermutationUpwards(k, n_ab); continue; }
			
			PopulationVector k1 = ccu.expand(k, commonPcs, qnm.N, ab_cs);
			k_child = ccu.contract(k1, ab_cs, child_cs);
			x = getRes(child, k_child).X[r_child];
			if (x == 0) { k = ccu.nextPermutationUpwards(k, n_ab); continue; }
			
			kred = nred = false;
			if (isLeftChild) {
				k_ = this.subtract(n_ab, k);
			} else {
				if (k.get(r_ab) > 0) { k.minusOne(r_ab+1); kred = true; }
				k_ = k;
			}
			if (n_ab.get(r_ab) > 0) { n_ab.minusOne(r_ab+1); nred = true; }
			pb = pFunc(b, k_, n_ab, pStore);
			
			res = (kr / x) * pb;
			sum += res;
			if (kred) k.restore();
			if (nred) n_ab.restore();
			
			k = ccu.nextPermutationUpwards(k, n_ab);
		}
		
		return sum;
	}
	
	/**
	 * Implements equation 6 in Hoyme's paper for computing throughputs at the parent node.
	 * @param r The class ID.
	 * @param n The population vector.
	 * @param wa The left child response/wait time.
	 * @param wb The right child response/wait time.
	 * @return The parent throughpu for the specified class.
	 */
	private double subnetThroughput6(int r, PopulationVector n, double wa, double wb) {
		double den = wa + wb;
		if (den == 0) {
			return 0;
		}
		int nr = n.get(r);
		if (nr == 0) nr++;
		return nr/den;
	}
	
	/**
	 * Implements equations 8/9 in Hoyme's paper for computing the marginal probability function.
	 * @param b The left node (relative to parent).
	 * @param k_ab The population vector for common pcs in the left node.
	 * @param n_ab The population vector for common pcs in the parent node.
	 * @param pStore The pFunc store/cache for speeding up calls to pFunc (i.e. if results already computed,
	 * can return it immediately).
	 * @return The marginal probability.
	 *
	 * N.B. Must be called with right-hand child (i.e. b) so that pStore optimisation works.
	 */
	private double pFunc(Node b, PopulationVector k_ab, PopulationVector n_ab, HashMap<PTuple, Double> pStore)  {
		int r = 0;
		if (k_ab.isZeroVector() && n_ab.isZeroVector()) {
			return 1;
		}
		
		// If pFunc already computed for this node, return stored result.
		PTuple p = new PTuple(k_ab, n_ab);
		if (pStore.containsKey(p)) {
			return pStore.get(p);
		}
		
		try {
			if (k_ab.isZeroVector()) {
				// Choose r s.t. n_r > 0
				r = n_ab.findFirstNonZeroElement();
			} else {
				// Choose r s.t. k_r > 0
				r = k_ab.findFirstNonZeroElement();
			}
		} catch (InternalErrorException e) {
			e.printStackTrace();
			System.exit(-1);
			return -1;
		}
		
		Node ab = b.parent();
		Node a = ab.leftChild();
		TreeSet<Integer> ab_cs = ab.getAllCoveredClasses();
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(a.pcs);
		commonPcs.addAll(b.pcs); 
		
		PopulationVector n_oChild = null;
		
		TreeSet<Integer> b_cs = b.getAllCoveredClasses();
		TreeSet<Integer> a_cs = a.getAllCoveredClasses();
		
		double x_ab = 0;
		PopulationVector n1 = ccu.expand(n_ab, commonPcs, qnm.N, ab_cs);
		int r1 = ccu.convertIndex(r, commonPcs, ab_cs);
		x_ab = getRes(ab, n1).X[r1];
		if (x_ab == 0) { return 0; }
		
		double x_child = 0;
		boolean kr = false;
		boolean nr = false;
		
		if (k_ab.isZeroVector()) {
			n_oChild = ccu.contract(n1, ab_cs, a_cs);
			int ro = ccu.convertIndex(r1, ab_cs, a_cs);
			x_child = ro < 0 ? 0 : getRes(a, n_oChild).X[ro];
			if (x_child == 0) return 0;
		} else {
			PopulationVector k1 = ccu.expand(k_ab, commonPcs, qnm.N, ab_cs);
			PopulationVector k_child = ccu.contract(k1, ab_cs, b_cs);
			int rc = ccu.convertIndex(r1, ab_cs, b_cs);
			x_child = rc < 0 ? 0 : getRes(b, k_child).X[rc];
			if (x_child == 0) return 0;
			if (k_ab.get(r) > 0) { k_ab.minusOne(r+1); kr = true; }
		}
		
		if (n_ab.get(r) > 0) { n_ab.minusOne(r+1); nr = true; }
		double pf = pFunc(b, k_ab, n_ab, pStore);
		
		double res = (x_ab/x_child) * pf;
		if (nr) n_ab.restore();
		if (kr) k_ab.restore();
		
		// Store result for future use.
		pStore.put(p, res);
		return res;
	}
	
	/**
	 * Implements equation 10a/b in Hoyme paper (equ. 18 in Tucci paper). Allows conversion of
	 * performance measures so that they are not conditioned by lower levels in the tree.
	 * @param ab The parent node.
	 * @param i The station ID (unused if convertX is true).
	 * @param r The class ID.
	 * @param n_ab The population vector for the common pcs.
	 * @param convertX If true the equ. 10 is applied to the throughputs (X), 
	 *                 else it is applied to the mean queue lengths (Q).
	 * @param useA Whether to use the left child (A) or the right child (B).
	 * @param pStore The store of pFunc values to make pFunc calculations faster.
	 * @return The converted performance measure for the specified parameters.
	 */
	private double equ10a(Node ab, int i, int r, PopulationVector n_ab, boolean convertX, boolean useA, HashMap<PTuple, Double> pStore) {
		Node child = (useA) ? ab.leftChild() : ab.rightChild();
		Node b = ab.rightChild();
		
		TreeSet<Integer> ab_cs = ab.getAllCoveredClasses();
		TreeSet<Integer> child_cs = child.getAllCoveredClasses();
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(ab.leftChild().pcs);
		commonPcs.addAll(ab.rightChild().pcs); 
		
		int r_child = ccu.getIndex(r, child_cs);
		
		PopulationVector k = new PopulationVector(0, n_ab.size());
		
		double sum = 0;
		double q, pf, res;
		PopulationVector k_child, kp;
		
		while (k != null) {
			PopulationVector k_ab = ccu.expand(k, commonPcs, qnm.N, ab_cs);
			k_child = ccu.contract(k_ab, ab_cs, child_cs);
			q = convertX ? getRes(child, k_child).X[r_child] : getRes(child, k_child).Q[i][r_child];
			if (q == 0) { k = ccu.nextPermutationUpwards(k, n_ab); continue; }
			
			// N.B. This is to ensure pFunc is always called on b child, so results can be stored to save time.
			kp = useA ? this.subtract(n_ab, k) : k;
			pf = pFunc(b, kp, n_ab, pStore);
			res = q * pf;
			sum += res;
			k = ccu.nextPermutationUpwards(k, n_ab);
		}
		
		return sum;
	}
	
	
	/** Implements computing of waiting/response times at leaf node. More efficient than for subnetwork nodes.
	 * @param leaf The leaf node to compute response times at.
	 * @param isLeftChild Whether the node is the left child or not relative to its parent.
	 * @param r The class ID.
	 * @param n_ab The population vector for common pcs in the parent node.
	 * @return The residence/wait time at the leaf node.
	 */
	private double computeResidenceTimeAtLeaf(Node leaf, boolean isLeftNode, int r, PopulationVector n_ab) {
		int station = leaf.stations.iterator().next();
		Node ab = leaf.parent();
		TreeSet<Integer> ab_cs = ab.getAllCoveredClasses();
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(ab.leftChild().pcs);
		commonPcs.addAll(ab.rightChild().pcs); 
		
		int r_ab = ccu.getIndex(r, commonPcs);
		int m_ab = ccu.getIndex(station, ab.stations);
		
		boolean nred = false;
		if (n_ab.get(r_ab) > 0) { n_ab.minusOne(r_ab+1); nred = true; }
		
		if (!nred) {
			return 0;
		}
		
		PopulationVector n_full = ccu.expand(n_ab, commonPcs, qnm.N, ab_cs);
		MVAResults res = this.getRes(ab, n_full);
		double qs[] = res.Q[m_ab];
		double q = 0 ;
		for (int c = 0; c < qs.length; c++) {
			q += qs[c];
		}
		if (nred) n_ab.restore();
		return qnm.getDemand(station, r) * (1 + q);
	}

	/**
	 * Wrapper function for accessing MVAResults stored at a tree node.
	 * @param node The node to access the MVA results at.
	 * @param n The population vector to get results for.
	 * @return The MVAResults stored at the node.
	 */
	private MVAResults getRes(Node node, PopulationVector n) {
		try {
			MVAResults res = node.mvaRes.get(n);
			if (res == null) {
				throw new Exception("ERROR ACCESSING MVA RESULT: " + n.toString());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	/**
	 * Subtracts two population vectors, ensuring that the resulting vector
	 * contains only integers greater than or equal to 0.
	 * @param p1 The first vector.
	 * @param p2 The second vector.
	 * @return The results of p1 - p2.
	 */
	private PopulationVector subtract(PopulationVector p1, PopulationVector p2) {
		return this.removeNegatives(p1.subVec(p2));
	}
	
	/**
	 * Removes all negative values from a vector by replacing them with 0.
	 * @param p The vector.
	 * @return The vector with no negative values.
	 */
	private PopulationVector removeNegatives(PopulationVector p) {
		for (int i = 0; i < p.size(); i++) {
			if (p.get(i) < 0) {
				p.set(i, 0);
			}
		}
		
		return p;
	}
	
	/**
	 * PopulationVector tuple implementation.
	 * Used for pFunc cache/store to avoid having to use a multi-layered HashMap.
	 */
	private class PTuple  {
		/** The first vector. */
		private PopulationVector k;
		
		/** The second vector. */
		private PopulationVector n;
		
		/**
		 * Constructor.
		 * @param k The first vector.
		 * @param n The second vector.
		 */
		public PTuple(PopulationVector k, PopulationVector n) {
			this.k = k;
			this.n = n;
		}
		
		/**
		 * Determines if another object is equal to this PTuple.
		 * @param o The other object to check.
		 * @param Whether the other object is equal to this PTuple.
		 */
		@Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof PTuple)) return false;
	        PTuple key = (PTuple) o;
	        return k.equals(key.k) && n.equals(key.n);
	    }

		/**
		 * Creates a hash code for this PTuple.
		 * @return The hash code for this PTuple.
		 */
	    @Override
	    public int hashCode() {
	        int result = k.hashCode();
	        result = 31 * result + n.hashCode();
	        return result;
	    }
	}
}
