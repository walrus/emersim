package jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.Control.Main;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * LUP Decomposition algorithm based on pseudocode found in Cormen.
 * @author Jack Bradshaw 
 */
public class LUP_Decomposition {
	
	/**
	 * Basis for the model
	 */
	private CoMoMBasis basis;
	
	/**
	 * Starting position of the matrix
	 */
	private Position position;
	
	/**
	 * Decomposed Matrix
	 */
	private BigRational[][] A_prime;
	
	/**
	 * Permutation P
	 */
	private int[] P;
	
	/**
	 * Dimension of square matrix
	 */
	private int size;
	
	/**
	 * 
	 * @param basis
	 * @param position
	 * @param A
	 * @param in_place true if the Decomposition should happen in place
	 * @throws InconsistentLinearSystemException
	 */
	public LUP_Decomposition(CoMoMBasis basis, Position position, BigRational[][] A, boolean in_place) throws InconsistentLinearSystemException {
		
		//store basis
		this.basis = basis;
		
		//store the position of the matrix
		this.position = position;
		
		//store size of matrix A
		size = A.length;
		
		if (in_place && !Main.verbose) {
			A_prime = A;
		} else {
			//A_prime is a copy of A, where decomposition will be performed in place
			A_prime = new BigRational[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j ++) {
					A_prime[i][j] = A[i][j].copy(); //TODO copy()?
				}
			}
		}
		
		//The permutation P is initially the identity
		P = new int[size];
		for (int i = 0; i < size; i++) {
			P[i] = i;
		}
			
		//Compute the in place decomposition
		decompose();
		
		//MiscFunctions.printMatrix(A_prime);
		//MiscFunctions.printMatrix(P);
	}
	
	/**
	 * Computes the LU decomposition of A in place in A_prime
	 * @throws InconsistentLinearSystemException If matrix is singular
	 */
	private void decompose() throws InconsistentLinearSystemException {
		//System.out.println("A_prime: ");
		//MiscFunctions.printMatrix(A_prime);
		BigRational p;
		BigRational abs_A;
		int k_prime = -1;
		
		for (int k = 0; k < size; k++) {
			p = BigRational.ZERO;
			for (int i = k; i < size; i++) {
				abs_A = A_prime[i][k].abs();
				if (abs_A.greaterThan(p)) {
					p = abs_A;
					k_prime = i;
				}
			}
			if ( p.isZero()) {
				throw new InconsistentLinearSystemException("LUP Decomposition failed: Singular Matrix");
			}
			P_exchange(k, k_prime);
			for (int i = 0; i < size; i++) {
				A_prime_exchange(k, k_prime, i);
			}
			for (int i = k + 1; i < size; i++) {
				A_prime[i][k] = A_prime[i][k].divide(A_prime[k][k]);
				for (int j = k + 1; j < size; j++) {
					A_prime[i][j] = A_prime[i][j].subtract(A_prime[i][k].multiply(A_prime[k][j]));
				}
			}
		}
	}
	
	/**
	 * swaps elements of permutation matrix P
	 * @param i position of first element
	 * @param j position of second element
	 */
	private void P_exchange(int i, int j) {
		int temp = P[i];
		P[i] = P[j];
		P[j] = temp;
	}
	
	/**
	 * Swaps element at (row_1,  col) with element at (row_2, col) in matrix A_prime
	 * @param row_1
	 * @param row_2
	 * @param col
	 */
	private void A_prime_exchange(int row_1, int row_2, int col) {
		BigRational temp = A_prime[row_1][col];
		A_prime[row_1][col] = A_prime[row_2][col];
		A_prime[row_2][col] = temp;		
	}	
		
	/**
	 * Solve method called by XMicroBlock, uses basis values and temporary basis rhs
	 * @param rhs
	 */
	public void solve(BigRational[] rhs) {
		//BigRational[] x = new BigRational[size];
		BigRational[] y = new BigRational[size];
		
		//Forward substitution
		for (int i = 0; i < size; i++) {
			BigRational sum = BigRational.ZERO;
			for (int j = 0; j <= i - 1; j++) {
				sum = sum.add(A_prime[i][j].multiply(y[j]));
			}
			y[i] = rhs[position.row + P[i]].subtract(sum);
		}
		
		//Backward substitution
		for (int i = size - 1; i >= 0; i--) {
			BigRational sum = BigRational.ZERO;
			for (int j = i + 1; j < size; j++) {
				sum = sum.add(A_prime[i][j].multiply(basis.getNewValue(position.row + j)));
			}
			basis.setValue(y[i].subtract(sum), position.row + i);
			basis.setValue(basis.getNewValue(position.row + i).divide(A_prime[i][i]), position.row + i);
		}		
	}
	
	/**
	 * Solve Method used for unit testing.
	 * Solves for x in Ax = b
	 * @param b
	 * @return x
	 */
	public BigRational[] test_solve(BigRational[] b) {
		BigRational[] x = new BigRational[size];
		BigRational[] y = new BigRational[size];
		
		//Forward substitution
		for (int i = 0; i < size; i++) {
			BigRational sum = BigRational.ZERO;
			for (int j = 0; j <= i - 1; j++) {
				sum = sum.add(A_prime[i][j].multiply(y[j]));
			}
			y[i] = b[P[i]].subtract(sum);
		}
		
		//Backward substitution
		for (int i = size - 1; i >= 0; i--) {
			BigRational sum = BigRational.ZERO;
			for (int j = i + 1; j < size; j++) {
				sum = sum.add(A_prime[i][j].multiply(x[j]));
			}
			x[i] = y[i].subtract(sum);
			x[i] = x[i].divide(A_prime[i][i]);
		}
		return x;
	}
}
