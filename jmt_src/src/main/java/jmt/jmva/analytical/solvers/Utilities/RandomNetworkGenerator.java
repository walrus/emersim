package jmt.jmva.analytical.solvers.Utilities;

import java.util.Random;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InputFileParserException;


/**
 * Class which can generate models for use when testing/evaluating the algorithms.
 * In particular this class was written to generate queueing model networks for testing
 * the Tree Convolution and TMVA algorithms. 
 * @author Ben Homer, 2014.
 */
public class RandomNetworkGenerator {

	private Bound stations;
	private Bound classes;
	private Bound population;
	private Bound delay;
	private Bound demand;
	private boolean sparse;
	
	private Random rand = new Random();
	
	public RandomNetworkGenerator(boolean sparse, int popMax) {
		this.sparse = sparse;
		stations = new Bound(2, 5);
		classes = new Bound(2, 3);
		population = new Bound(1, popMax);
		delay = new Bound(0, 5);
		demand = new Bound(0, 5);
	}
	
	// TODO: need to look into sparseness aspect.
	// TODO: just assuming sparseness for now, make more general if have time
	public QNModel generateSparseNetwork(
			int stations,
			int classes,
			double sparcityPercentage,
			boolean reusePrevious,
			boolean delays) {
		
		if (reusePrevious) {
			try {
				return new QNModel(QNModel.TempFileName);
			} catch (InputFileParserException e) {
				e.printStackTrace();
			}
		}
		
		int R = classes;
		int M = stations;
		Integer[] N = new Integer[R];
		double[] Z = new double[R];
		int[] T = new int[M];
		Integer[] mults = new Integer[M];
		
		for (int r = 0; r < R; r++) {
			N[r] = rand(population);
			Z[r] = delays ? rand(delay) : 0;
		}
		
		// TODO: come back to this.
//		DoubleMatrix2D D = this.createSparseMatrix(M, R, sparcityPercentage);
//		this.calculateEigenvalues(D);
//		double actualSparcityPercentage = (D.size() - D.cardinality())/D.size();
		
		// TODO: add varying scale factor.
		double[][] Dd = DoubleFactory2D.sparse.random(M, R).toArray();
		double[][] D = new double[M][R];
		double zeroCount = 0;
		for (int m = 0; m < M; m++) {
			for (int r = 0; r < R; r++) {
				if (rand.nextDouble() < sparcityPercentage) {
					D[m][r] = 0;
					zeroCount++;
				} else {
					D[m][r] = (Dd[m][r]*10);
				}
			}
		}
		
		double actualSparcityPercentage = zeroCount/((double)(M*R));
		
		for (int m = 0; m < M; m++) {
			mults[m] = 1;
			T[m] = 1; // LI
		}
		
		try {
			QNModel q = new QNModel(R, M, T, N, Z, mults, D);
		
			System.out.println("Sparse model created.");
			System.out.println("Requested Sparcity Percentage: " + sparcityPercentage);
			System.out.println("Actual sparcity Percentage: " + actualSparcityPercentage);
			q.prettyPrint();
			q.writeToFile();
			return q;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private DoubleMatrix2D createSparseMatrix(int M, int R, double sparcityPercentage) {
		// TODO: add varying scale factor.
		// Create sparse matrix.
		DoubleMatrix2D D = DoubleFactory2D.sparse.random(M, R);
		double[] rowSums = new double[M];
		for (int m = 0; m < M; m++) {
			for (int r = 0; r < R; r++) {
				if (rand.nextDouble() < sparcityPercentage) {
					D.set(m, r, 0);
				}
				rowSums[m] += D.get(m, r);
			}
		}

		// Normalise rows.
		for (int m = 0; m < M; m++) {
			for (int r = 0; r < R; r++) {
				double val = D.get(m, r);
				D.set(m, r, val/rowSums[m]);
			}
		}
		
		return D;
	}
	
	private void calculateEigenvalues(DoubleMatrix2D matrix) {
		EigenvalueDecomposition evd = new EigenvalueDecomposition(matrix);
		DoubleMatrix2D D = evd.getD();
		DoubleMatrix2D V = evd.getV();
		System.out.println("here");
	}
	
	private int rand(Bound bound) {
		return rand.nextInt(bound.max - bound.min) + bound.min;
	}
	
	private class Bound {
		int min;
		int max;
		
		public Bound(int min, int max) {
			this.min = min;
			this.max = max;
		}
	}
}
