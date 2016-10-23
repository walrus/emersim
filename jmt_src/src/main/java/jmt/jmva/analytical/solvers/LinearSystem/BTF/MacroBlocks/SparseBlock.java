package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.Basis;
import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class implements the blocks that make up the SparseBlockMatrx objects.
 * 
 * A linear data structure is used to reduce the memory requirement.
 * 
 * @author Jack Bradshaw
 *
 */
public class SparseBlock {

	private BigRational[] values;
	private int[] columns;
	
	private Basis basis;
	
	private int rows;
	private int cols;
	
	private Position position;
		
	public SparseBlock(CoMoMBasis basis, Position position, int rows, int cols) {
		values = new BigRational[rows];
		columns = new int[rows];
		
		this.position = position;
		
		this.basis = basis;
		
		this.rows = rows;
		this.cols = cols;
	}

	public void write(int row, int col, BigRational value) {
		values[row] = value.copy();
		columns[row] = col;
	}

	public BigRational get(int row, int col) {
		if (columns[row] == col) {
			return values[row];
		} else {
			return BigRational.ZERO;
		}
	}
	
	public void multiply(BigRational[] result) {
		 for (int i = 0; i < rows; i++) {
			 result[position.row + i] = result[position.row + i].add(basis.getNewValue(position.col + columns[i]).multiply(values[i]));
		 }
	} 

	public void solve(BigRational[] rhs) {
		for (int i = 0; i < rows; i++) {
			 rhs[position.row + i] = rhs[position.row + i].add((basis.getNewValue(position.col + columns[i]).multiply(values[i])).negate());
		} 		
	}

}
