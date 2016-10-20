package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

import java.util.HashMap;
import java.util.TreeSet;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.QNModel.QueueType;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Node;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.Printer;

/**
 * Implementation of standard/sequential MVA algorithm.
 * @author Ben Homer, 2014.
 * N.B. This was adapted from the MVA solver in JMT, but uses
 * the updated data structures provided within the JCoMoM package.
 */
public class StandardMVASolver implements IThreadedTMVASolver {

	/** The model the solve. */
	private QNModel qnm;
	
	/** The class coverage utilities instance. */
	private ClassCoverageUtils ccu;
	
	/** Boolean indicating whether to use JMT's MVA algorithm or not. */
	//private boolean useJMT_MVA;
	
	/**
	 * Constructor.
	 * @param qnm The model to solve.
	 * @param ccu The class coverage utilities instance.
	 */
	public StandardMVASolver(QNModel qnm, ClassCoverageUtils ccu, boolean useJMT_MVA) {
		this.qnm = qnm;
		this.ccu = ccu;
		//this.useJMT_MVA = useJMT_MVA;
	}
	
	/**
	 * Solves the network rooted at the specified node.
	 * @param node The root node of the tree.
	 * @param N The population vector for the full network.
	 * @return The MVAResults object containing the final performance measures.
	 */
	public MVAResults solve(Node node, PopulationVector N) {
		/*if (useJMT_MVA) {
			return JMTsequentialMVA(node, N);
		} else */
			return sequentialMVA(node, N);
	}
	
	public MVAResults sequentialMVA(Node node, PopulationVector N) {
		int K = node.stations.size();
		int C = node.getAllCoveredClasses().size();
		TreeSet<Integer> cs = node.getAllCoveredClasses();
		int ci = 0;
		
		Node ab = node;
		Printer.out.println("\nSTDMVA: Start aggregate computation stations: " 
				+ node.stations.toString()
				+ " + " + " pcs = " + ab.pcs.toString() + " cs = " + cs.toString());
		
		HashMap<PopulationVector, double[]> Q_agg = new HashMap<PopulationVector, double[]>();
		MVAResults lastRes = null;
		
		PopulationVector zeros = new PopulationVector(0, C);
		double[] qk = new double[K];
		for (int k = 0; k < K; k++) {
			qk[k] = 0;
		}
		Q_agg.put(zeros, qk);
		
		PopulationVector np = new PopulationVector(0, C);
		PopulationVector npmax = ccu.contract(N, ccu.all, cs);
		while (np != null) {
			
			MVAResults res = new MVAResults(K, C);
			Printer.out.println("--- N = " + np.toString() + " ---");
			
			// Response times.
			ci = 0;
			for (int c : cs) {
				//double sum = 0;
				int ki = 0;
				for (int k : node.stations) {
					if (np.get(ci) > 0) {
						if (qnm.getQueueType(k) == QueueType.LI) {
							np.minusOne(ci+1);
							res.R[ki][ci] = qnm.getDemand(k, c) * (1 + Q_agg.get(np)[ki]); // Assume queueing station for now
							np.restore();
						} else { // Delay stations
							res.R[ki][ci] = qnm.getDemand(k, c);
						}
					}
					else {
						res.R[ki][ci] = 0;
					}
					//sum += res.R[ki][ci];
					ki++;
				}
				ci++;
				//Printer.out.println("R" + c + " = " + sum);
			}
			//Printer.out.println("R: " + Arrays.deepToString(res.R));
			
			// Throughputs.
			ci = 0;
			for (int c : cs) {
				int nc = np.get(ci);
				if (nc == 0) {
					res.X[ci] = 0;
					ci++;
					continue;
				}
				
				double Rsum = 0;
				for (int k = 0; k < K; k++) {
					Rsum += res.R[k][ci];
				}
				double den = Rsum;
				if (den == 0) {
					res.X[ci] = 0;
				} else {
					res.X[ci] = nc / den;
				}
				Printer.out.println("X" + c + " = " + res.X[ci]);
				ci++;
			}
			
			// Queue lengths.
			double[] qs = new double[K];
			for (int k = 0; k < K; k++) {
				double sum = 0;
				for (int c = 0; c < C; c++) {
					res.Q[k][c] = res.X[c] * res.R[k][c];
					sum += res.Q[k][c];
				}
				
				qs[k] = sum;
			}
			
			Q_agg.put(np, qs);
				
			node.mvaRes.put(np, res);
			lastRes = res;
			
			np = ccu.nextPermutationUpwards(np, npmax);
		}
				
		return lastRes;
	}
	
	/** 
	 * Calls JMTs implementation of MVA and converts result to MVAResult object.
	 * N.B. Not used.
	public MVAResults JMTsequentialMVA(Node node, PopulationVector N) {
		int K = node.stations.size();
		int C = node.getAllCoveredClasses().size();
		SolverMultiClosedMVA jmtMVA = new SolverMultiClosedMVA(C, K);
		String[] n = new String[K];
		int[] t = new int[K];
		double s[][][] = new double[K][C][1];
		double v[][] = new double[K][C];
		int pop[] = new int[C];
		for (int i = 0; i < K; i++) {
			n[i] = "s" + i;
			t[i] = qnm.getQueueType(i).ordinal();
			for (int j = 0; j < C; j++) {
				double d = qnm.getDemand(i, j);
				s[i][j][0] = d;
				v[i][j] = d > 0 ? 1.0 : 0.0;
			}
		}

		for (int j = 0; j < C; j++) {
			pop[j] = qnm.N.get(j);
		}
		
		jmtMVA.input(n, t, s, v, pop);
		jmtMVA.solve();
		
		MVAResults res = new MVAResults(K, C);
		res.X = jmtMVA.getClsThroughput();
		res.Q = jmtMVA.getQueueLen();
		res.R = jmtMVA.getResTime();
		return res;
	}
	 */
}
