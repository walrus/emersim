package jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the Micro Blocks of the B1 block of the B matrix.
 * @author Jack Bradshaw
 */
public class B1MicroBlock extends MicroBlock {

	private int[][] array;
	
	public B1MicroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) {
		super(qnm, basis, position, h);		
		computeDimensions();
		array = new int[size.row][2];
	}
	
	@Override
	protected void initialiseDataStructures() {
		array = new int[size.row][2];		
	}

	public B1MicroBlock(B1MicroBlock micro_block, int current_class) {
		super(micro_block, current_class);
		this.array = micro_block.array;
	}
	
	@Override
	protected MicroBlock subBlockCopy(int current_class) {
		return new B1MicroBlock(this, current_class);
	}

	@Override
	protected void computeDimensions() {
		size.row = MiscFunctions.binomialCoefficient(qnm.M, h) * qnm.M;
		size.col = 0;
	}

	@Override
	public int addCE(int row, PopulationChangeVector n, int queue)
			throws BTFMatrixErrorException, InternalErrorException {
		
		int col = basis.indexOf(n, queue);	
		int insertion_row = row;	
		insertion_row -= position.row;
			
		array[insertion_row][0] = queue;
		array[insertion_row][1] = col;
		
		return row;	
	}

	@Override
	public int addPC(int row, PopulationChangeVector n, int _class)
			throws BTFMatrixErrorException, InternalErrorException {
		
		int insertion_row = row;	
		insertion_row -= position.row;
		
		array[insertion_row][0] = 0;
		array[insertion_row][1] = 0;
		return row;
	}

	@Override
	public void printRow2(int row) {
		int row_to_print = row - position.row;		
		if (row_to_print >= 0 && row_to_print < size.row) {
			//print whitespace offset
			for (int i = 0; i < array[row_to_print][1]; i++) {
				System.out.format("%2s ", " ");
			}
			BigRational value = getValue(row_to_print);
			if (!value.equals(BigRational.ZERO)) {
				System.out.format("%2s ", getValue(row_to_print));
			}
		}

	}
			
	private BigRational multiplyRow(int index) {
		if (array[index][0] != 0) {
			return basis.getOldValue(getCol(index)).multiply(getValue(index));  //TODO copy()?
		} else {
			return BigRational.ZERO;
		}
	}
	 
	@Override
	public void multiply(BigRational[] result)
			throws BTFMatrixErrorException {
		
		for (int i = 0; i < size.row; i++) {
			result[position.row + i] = multiplyRow(i);
		}
	}
	
	private  BigRational getValue(int row) {
		if (array[row][0] == 0) return BigRational.ZERO;
		return qnm.getDemandAsBigRational(array[row][0] - 1, current_class - 1);
	}
	
	private int getCol(int row) {
		if (array[row][0] == 0) return 0;
		return array[row][1];
	}

	@Override
	public void solve(BigRational[] rhs) {
		for (int i = 0; i < size.row; i++) {
			rhs[position.row + i] = multiplyRow(i);			
		}		
	}
}
