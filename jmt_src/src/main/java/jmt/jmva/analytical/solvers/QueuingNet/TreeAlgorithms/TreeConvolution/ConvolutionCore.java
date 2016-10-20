package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.QNModel.QueueType;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Config;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/** 
 * Implements the lowest level convolution operations, as outlined in the LamLien83 paper.
 * @author Ben Homer, 2014.
 */
public class ConvolutionCore {

	/** The queueing network we are trying to solve. */
	protected QNModel qnm;
	
	/** The ClassCoverageUtils instance. */
	private ClassCoverageUtils ccu;
	
	/** A Map in which to prestore factorial results. */
	private Map<Integer, BigDecimal> factorial = null;
	
	/**
	 * Constructor.
	 * @param qnm The queueing network we are trying to solve.
	 * @param ccu A ClassCoverageUtils instance.
	 */
	public ConvolutionCore(QNModel qnm, ClassCoverageUtils ccu) {
		this.qnm = qnm;
		this.ccu = ccu;
		initialise();
	}
	
	/**
	 * Initialises the factorials map by precomputing all factorial
	 * values that will be required in later computations.
	 * This is necessary for faster computation of initial conditions.
	 */
	private void initialise() {
		try {
			factorial = MiscFunctions.computeFactorialsAsBigDecimal(qnm.N.max());
		} catch (OperationNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Computes the g-array at the specified leaf node by implementing Eq. (9) from Lam/Lien paper. 
	 * @param n The leaf node.
	 * @param popVec The population vector to use during the computation.
	 * @param backup Whether to store the results in a backup g-array map.
	 */ 
	public void computeLeafNodeGArray(Node n, PopulationVector popVec, boolean backup) {
		if (n.stations.size() != 1) {
			return;
		}

		TreeSet<Integer> pc = n.pcs;
		TreeSet<Integer> fc = n.fcs;

		// Only single stations at node since leaf node.
		int m = n.stations.first();
		
		// Only need to store partial arrays as paper describes.
		PopulationVector p = ccu.contract(popVec, ccu.all, pc);
		
		PopulationVector orig = p.copy();
		Printer.out.println("Computing leaf g array for m = " + m + " pcs = " + pc.toString() 
				+ " popVec = " + popVec.toString());
		while (p != null) {
			BigDecimal res = BigDecimal.ONE;
			
			if (qnm.getQueueType(m) == QueueType.DELAY) {
				res = leafNodeDelayInitalCondition(p, popVec, n);
			} 
			else {
				int sumik = 0;
				int sumNk = 0;
	
				for (int k : fc) {
					int Nk = popVec.get(k);
					sumNk += Nk;
					if (Nk >= 0) {
						BigDecimal curVal = qnm.getDemandAsBigDecimal(m, k).pow(Nk);
						curVal = curVal.divide(factorial.get(Nk), Config.Context);
						res = res.multiply(curVal,Config.Context);
					} else if (Nk < 0) {
						res = BigDecimal.ZERO;
					}
				}
	
				int idx = 0;
				for (int k : pc) {
					int ik = p.get(idx);
					idx++;
					sumik += ik;
					if (ik >= 0) {
						BigDecimal curVal = qnm.getDemandAsBigDecimal(m, k).pow(ik);
						curVal = curVal.divide(factorial.get(ik), Config.Context);
						res = res.multiply(curVal,Config.Context);
					} else if (ik < 0) {
						res = BigDecimal.ZERO;
					}
				}
	
				int nm = sumik + sumNk;
				BigDecimal nmFac = MiscFunctions.computeFactorialAsBigDecimal(nm);
				res = res.multiply(nmFac, Config.Context);
			}
			Printer.out.println(p.toString() + " : " + res);
			n.store(popVec, p, res, backup);
			p = ccu.nextPermutation(p, orig);
		}
	}
	
	/** 
	 * Computes the g-array at the specified leaf node representing a delay station.
	 * @param n The leaf node representing a delay station.
	 * @param popVec The population vector to use during the computation.
	 */ 
	protected BigDecimal leafNodeDelayInitalCondition(PopulationVector p, PopulationVector popVec, Node n) {
		BigDecimal res = BigDecimal.ONE;

		for (int k : n.fcs) {
			int Nk = popVec.get(k);
			if (Nk >= 0) {
				BigDecimal curVal = qnm.getDelayAsBigDecimal(k).pow(Nk);
				curVal = curVal.divide(factorial.get(Nk), Config.Context);
				res = res.multiply(curVal,Config.Context);
			} else if (Nk < 0) {
				res = BigDecimal.ZERO;
			}
		}

		int idx = 0;
		for (int k : n.pcs) {
			int ik = p.get(idx);
			idx++;
			if (ik >= 0) {
				BigDecimal curVal = qnm.getDelayAsBigDecimal(k).pow(ik);
				curVal = curVal.divide(factorial.get(ik), Config.Context);
				res = res.multiply(curVal,Config.Context);
			} else if (ik < 0) {
				res = BigDecimal.ZERO;
			}
		}
		
		return res;
	}

	/**
	 * Computes the g-array at the specified subnet (non-leaf) node.
	 * @param node The subnet node.
	 * @param popVec The population vector to use during the computation.
	 * @param backup Whether to store the results in a backup g-array map.
	 */
	public void computeSubnetNodeGArray(Node node, PopulationVector popVec, boolean backup) {
		// Only need to store partial arrays as paper describes.
		PopulationVector p = ccu.contract(popVec, ccu.all, node.pcs);
		
		Printer.out.println("Computing intermediate node result for combination: " + node.leftChild().stations.toString() 
				+ " + " + node.rightChild().stations.toString() + "\npcs = " + node.pcs.toString() 
				+ " fcs = " + node.fcs.toString() + " popVec = " + popVec.toString());
		
		if (Config.USE_FEEDBACK_FILTERING && node.childrenAreLeaves()
			&& node.childrenSuitableForFeedbackFiltering(qnm)) {
			//&& node.childrenOverlappedPcsHaveLargePopulation(popVec)) {
			feedbackFilter(node, p, popVec.copy(), backup); // same as g{childNodes}(i) in table V of Lam/Lien paper
		}
		else {
			stdConvolution(node, p, popVec.copy(), backup); // same as g{childNodes}(i) in table V of Lam/Lien paper
		}
	}
	
	/** 
	 * Computes g-arrays at a node using using the standard
	 * convolution equation(equ (4) from the LamLien83 paper).
	 * @param parentNode The node to compute the g-array for.
	 * @param parentPcPops The parent populations for partially covered classes.
	 * @param popVec The max population vector to use in the calculation.
	 * @param backup Whether to store the results in a backup g-array map.
	 */
	private void stdConvolution(Node parentNode, PopulationVector p, PopulationVector popVec, boolean backup) {
		PopulationVector origp = p.copy();
		PopulationVector origPopVec = popVec.copy();
		Node leftChild = parentNode.leftChild();
		Node rightChild = parentNode.rightChild();
		PopulationVector iminusj;
		
		// Find the intersection of partially covered classes in the child nodes.
		TreeSet<Integer> commonPcs = new TreeSet<Integer>(leftChild.pcs);
		commonPcs.retainAll(rightChild.pcs);
		
		while (p != null) {
			
			// Set the max values in popVec to same as parent population
			// vector for corresponding partially covered classes.
			int x = 0;
			for (int pc : parentNode.pcs) {
				popVec.set(pc, p.get(x));
				x++;
			}
			
			// Get the maximum population for each partially covered class in commonPcs.
			PopulationVector commonPopVec = ccu.contract(popVec, ccu.all, commonPcs);
			
			PopulationVector maxes = commonPopVec.copy();
			BigDecimal res = BigDecimal.ZERO;
	
			while (commonPopVec != null) {
				// Do subtraction only on common partially covered class populations.
				iminusj = maxes.subVec(commonPopVec);
				
				// Expand population vector to necessary size depending on child partially covered classes.
				PopulationVector jFull = ccu.expand(commonPopVec, commonPcs, popVec, leftChild.pcs);
				PopulationVector iminusjFull = ccu.expand(iminusj, commonPcs, popVec, rightChild.pcs);
				
				BigDecimal l, r;
				if (backup) {
					l = (leftChild.backupContains(origPopVec)) 
							? leftChild.getBackupGarr(origPopVec, jFull) : leftChild.getGarr(jFull);
					r = (rightChild.backupContains(origPopVec))
							? rightChild.getBackupGarr(origPopVec, iminusjFull) : rightChild.getGarr(iminusjFull);
				} else {
					l = leftChild.getGarr(jFull);
					r = rightChild.getGarr(iminusjFull);
				}
				
				if (l == null || r == null) {
					commonPopVec = ccu.nextPermutation(commonPopVec, maxes);
					continue;
				}
				
				res = res.add(l.multiply(r, Config.Context));
				commonPopVec = ccu.nextPermutation(commonPopVec, maxes);
			}
	
			parentNode.store(origPopVec, p, res, backup);
			Printer.out.println(p.toString() + " : " + res);
			p = ccu.nextPermutation(p, origp);
		}
	}
	
	/**
	 * Implements equation (6) from Lam/Lien for combining two leaf node stations,
	 * more efficient than normal convolution if one is a fixed-rate station.
	 * @param parentNode The node to compute the g-array for.
	 * @param parentPcMax The parent population max for partially covered classes.
	 * @param popVec The max population vector to use in the calculation.
	 * @param backup Whether to store the results in a backup g-array map.
	 * */
	public void feedbackFilter(Node parentNode, PopulationVector parentPcMax, PopulationVector popVec, boolean backup) {
		Node uNode; // u - fixed rate
		Node vNode; // v
		
		if (qnm.getQueueType(parentNode.leftChild().stations.first()) == QNModel.QueueType.LI) {
			uNode = parentNode.leftChild();
			vNode = parentNode.rightChild();
		} else { // Right child must be fixed rate LI station.
			uNode = parentNode.rightChild();
			vNode = parentNode.leftChild();
		}
		
		PopulationVector origPopVec = popVec.copy();
		
		TreeSet<Integer> pu = new TreeSet<Integer>(uNode.fcs);
		pu.addAll(uNode.pcs);
		TreeSet<Integer> pv = new TreeSet<Integer>(vNode.pcs);
		if (uNode.stations.equals(vNode.stations)) {
			// Must be calculating mean queue lengths with cloned child node.
			// So, according to Lam/Lien, pu == pv
			pv.addAll(vNode.fcs);
		}
		
		// Find the union of pu and pv.
		TreeSet<Integer> pcUnion = new TreeSet<Integer>(pu);
		pcUnion.addAll(pv);
		
		TreeSet<Integer> pcSub = new TreeSet<Integer>(pu);
		pcSub.removeAll(pv);
		
		// Start i_uv from 0 vector and work upwards to goal vector.
		PopulationVector goal = ccu.contract(popVec, ccu.all, pcUnion);
		PopulationVector i_uv = new PopulationVector(0, goal.size());
		
		HashMap<PopulationVector, BigDecimal> G_uv = new HashMap<PopulationVector, BigDecimal>();
				
		// Define g_{u,v}(0vec) = 1
		G_uv.put(new PopulationVector(0, pcUnion.size()), BigDecimal.ONE);
		int u = uNode.stations.first();
		
		while (i_uv != null) {
			
			// Calculate sum.
			BigDecimal sum = BigDecimal.ZERO;
			for (int k : pu) {
				int kIdx = ccu.getIndex(k, pcUnion);
				if (i_uv.get(kIdx) == 0) {
					// If i_k == 0 result is 0, therefore skip.
					continue;
				}
				
				i_uv.minusOne(kIdx + 1);
				BigDecimal gm = G_uv.get(i_uv);
				BigDecimal demand = qnm.getDemandAsBigDecimal(u, k);
				sum = sum.add(gm.multiply(demand,Config.Context));
				i_uv.restore();
			}
			
			PopulationVector subPop = ccu.contract(i_uv, pcUnion, pcSub);
			
			BigDecimal res;
			if (subPop.isZeroVector()) {
				// Delta = 1
				PopulationVector vVec = ccu.contract(i_uv, pcUnion, vNode.pcs);
				BigDecimal g_v = vNode.Gmap.get(vVec);
				res = g_v.add(sum);
			} else {
				// Delta = 0
				res = sum;
			}
			
			G_uv.put(i_uv, res);
			i_uv = ccu.nextPermutationUpwards(i_uv, goal);
		}
		
		// Reduce G_uv to get solutions.
		PopulationVector p = parentPcMax.copy();
		while (p != null) {
			
			PopulationVector guvVec = new PopulationVector(0, pcUnion.size());
			int idx = 0;
			int pIdx = 0;
			
			for (int k : pcUnion) {
				if (parentNode.pcs.contains(k)) {
					guvVec.set(idx, p.get(pIdx));
					pIdx++;
				}
				else if (parentNode.fcs.contains(k)) {
					guvVec.set(idx, popVec.get(k));
				}
				idx++;
			}
			
			BigDecimal res = G_uv.get(guvVec);
			parentNode.store(origPopVec, p, res, backup);
			Printer.out.println(p.toString() + " : " + res);
			p = ccu.nextPermutation(p, parentPcMax);
		}
	}
}