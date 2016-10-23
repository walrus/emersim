package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA;

/** Class for storing MVA performance measure results.
 *  @author Ben Homer, 2014
 */
public class MVAResults {
	/** The per class throughputs. */
	double[] X;
	
	/** The per station per class mean queue lengths. */
	double[][] Q;
	
	/** The per station per class response times. */
	double[][] R;
	
	/**
	 * Constructor.
	 * @param K The number of station/queues.
	 * @param C The number of classes.
	 */
	public MVAResults(int K, int C) {
		X = new double[C];
		Q = new double[K][C];
		R = new double[K][C];
	}
}
