package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.ComponentBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class is an abstract super class the implements the common featrues of the SecondaryMacroBlocks
 * that are found in the X and Y blocks of the A matrix.
 * 
 * methods for their initialisation and sub copying are implemented.
 * 
 * The matrix is represented by a SparseBlockMatrix.
 * 
 * @author Jack Bradshaw
 */
public abstract class SecondaryMacroBlock extends ComponentBlock {

	//protected BlockMatrix matrix;
	protected SparseBlockMatrix matrix;
	
	protected SecondaryMacroBlock(QNModel qnm, CoMoMBasis basis, Position position, MacroBlock block_1, MacroBlock block_2) throws BTFMatrixErrorException {
		super(qnm, basis, position);
		initialise(block_1, block_2);
	}
	
	/**
	 * Constructor for lower classes
	 * @param full_block
	 * @param current_class
	 */
	protected  SecondaryMacroBlock(SecondaryMacroBlock full_block, int current_class) {
		super(full_block, current_class);		
	}
	
	private void initialise(MacroBlock block_1, MacroBlock block_2) throws BTFMatrixErrorException {
		size = computeDimensions(block_1, block_2);
		Position divisions = computeDivisions(block_1, block_2);
		//matrix = new BlockMatrix(basis, position, size, divisions);
		matrix = new SparseBlockMatrix(basis, position, size, divisions);
	}
		
	protected abstract Position computeDivisions(MacroBlock block_1, MacroBlock block_2);

	protected abstract Position computeDimensions(MacroBlock block_1, MacroBlock block_2);

	@Override
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {
		matrix.multiply(result);
	}
	
	@Override
	public void printRow2(int row) {
		int row_to_print = row - position.row;
		if (row_to_print >= 0 && row_to_print < size.row) {
			//print white space offset
			for (int i = cols_printed; i < position.col; i++) {
				System.out.format("%2s ", " ");	
				cols_printed++;
			}
			matrix.printRow(row);
			cols_printed += matrix.cols;
		}
	}
	
	@Override
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException {
		matrix.solve(rhs);		
	}

	public SecondaryMacroBlock subBlock(int current_class, MacroBlock macro_block_1, MacroBlock macro_block_2) {
		
		//Create a shallow copy of the current full block
		SecondaryMacroBlock sub_block = subBlockCopy(current_class);
		
		//Take the required 'top corner' of the full block's matrix
		Position divisions = computeDivisions(macro_block_1, macro_block_2);
		
		//sub_block.matrix = new BlockMatrix(matrix, divisions);
		sub_block.matrix = new SparseBlockMatrix(matrix, divisions);
		
		sub_block.size = matrix.getSize();
		
		return sub_block;
	}

	protected abstract SecondaryMacroBlock subBlockCopy(int current_class);

}
