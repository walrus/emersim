package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class extends SecondaryMacroBlock to specify behavior specific to the Secondary Macro Blocks of the Y Block.
 * 
 * This includes inserting the correct coefficients for given equations and computing its size and position.
 * 
 * @author Jack Bradshaw
 */
public class YSecondaryMacroBlock extends SecondaryMacroBlock {

	public YSecondaryMacroBlock(QNModel qnm, CoMoMBasis basis,
			Position position, MacroBlock block_1, MacroBlock block_2) throws BTFMatrixErrorException {
		super(qnm, basis, position, block_1, block_2);		
	}

	public  YSecondaryMacroBlock(YSecondaryMacroBlock full_block, int current_class) {
		super(full_block, current_class);
	}

	
	@Override
	protected SecondaryMacroBlock subBlockCopy(int current_class) {
		return new YSecondaryMacroBlock(this, current_class);
	}
	
	@Override
	public int addCE(int position, PopulationChangeVector n, int queue)
			throws BTFMatrixErrorException, InternalErrorException {
		//Do Nothing
		return position;
	}

	@Override
	public int addPC(int row, PopulationChangeVector n, int _class)
			throws BTFMatrixErrorException, InternalErrorException {
				
		int row_of_block = row - position.row;		
		if (row_of_block < 0 || row_of_block >= size.row) throw new BTFMatrixErrorException("Row: " + row + " not in micro block, when considering n:" + n +" class: " + _class);
		
		n.minusOne(_class);
		int col =  basis.indexOf(n, 0);
		int col_of_block = col - position.col;	
		
		if (col_of_block >= 0 && col_of_block < size.col) { //column is in this block				
			matrix.write(row_of_block, col_of_block, qnm.N.getAsBigRational(_class - 1).subtract(n.getAsBigRational(_class - 1)));		
		}
		n.restore();
		
		return row;
	}

	@Override
	protected Position computeDivisions(MacroBlock block_1, MacroBlock block_2) {
		return new Position(block_2.numberOfMicroBlocks(), block_1.numberOfMicroBlocks());
	}

	@Override
	protected Position computeDimensions(MacroBlock block_1, MacroBlock block_2) {
		return new Position(block_2.size().row, block_1.size().col);
	}

}
