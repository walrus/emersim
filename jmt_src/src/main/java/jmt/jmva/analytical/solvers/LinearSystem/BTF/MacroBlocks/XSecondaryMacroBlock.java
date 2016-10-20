package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class extends SecondaryMacroBlock to specify behavior specific to the Secondary Macro Blocks of the X Block.
 * 
 * This includes inserting the correct coefficients for given equations and computing its size and position.
 * 
 * @author Jack Bradshaw
 */
public class XSecondaryMacroBlock extends SecondaryMacroBlock {

	public XSecondaryMacroBlock(QNModel qnm, CoMoMBasis basis,
			Position position, MacroBlock block_1, MacroBlock block_2) throws BTFMatrixErrorException {
		super(qnm, basis, position, block_1, block_2);
	}
	
	public  XSecondaryMacroBlock(XSecondaryMacroBlock full_block, int current_class) {
		super(full_block, current_class);
	}
	
	@Override
	protected SecondaryMacroBlock subBlockCopy(int current_class) {
		return new XSecondaryMacroBlock(this, current_class);
	}
	
	/**
	 * Inserts CE at the given row (of the overall matrix)
	 * @param row
	 * @param row
	 * @param queue 
	 * @return 
	 * @throws InternalErrorException 
	 */
	@Override
	public int addCE(int row, PopulationChangeVector n, int queue) throws InternalErrorException {
		
		int row_of_block = row - position.row;		
			
		int col;
		
		for (int s = 1; s <= qnm.R - 1; s++) {
			
			n.plusOne(s);				
			
			col = basis.indexOf(n, queue);				
			n.restore();
				
			col -= position.col;			
			
			//Column is in this block
			if (col >= 0 && col < size.col) {							
				matrix.write( row_of_block , col, qnm.getDemandAsBigRational(queue - 1, s - 1).negate());
			}					
		}
		return row;		
	}

	@Override
	public int addPC(int row, PopulationChangeVector n, int _class)
			throws BTFMatrixErrorException, InternalErrorException {
		//Do Nothing
		return row;
	}

	@Override
	protected Position computeDivisions(MacroBlock block_1, MacroBlock block_2) {
		return new Position(block_1.numberOfMicroBlocks(), block_2.numberOfMicroBlocks());
	}

	@Override
	protected Position computeDimensions(MacroBlock block_1, MacroBlock block_2) {
		return new Position(block_1.size().row, block_2.size().col);
	}

}
