package jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.B2MacroBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.MacroBlock;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the B2 Block of the B matrix.
 * 
 * The Factory Methods newMacroBlock and subBlockCopy are implemented to return the correct 
 * block type in the parallel hierarchy.
 * 
 * Type 1 Macro Blocks are selected for lower class linear systems
 * 
 * @author Jack Bradshaw
 */
public class B2Block extends TopLevelBlock {

	public B2Block(QNModel qnm, CoMoMBasis basis)
			throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, new Position(MiscFunctions.binomialCoefficient(qnm.M + qnm.R - 1 , qnm.M) * qnm.M, 0));	
		selection_policy = new TypeOneBlocks(qnm, this, current_class);
	}
	
	public B2Block(QNModel qnm, CoMoMBasis basis, Position position) //TODO two
			throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position);
		selection_policy = new TypeOneBlocks(qnm, this, current_class);		
	}
	
	public B2Block(B2Block full_block, int current_class) {
		super(full_block, current_class);		
	}

	@Override
	protected TopLevelBlock subBlockCopy(int current_class) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		return new B2Block(this, current_class);
	}
	
	@Override
	protected MacroBlock newMacroBlock(Position block_position, int h) throws InternalErrorException, InconsistentLinearSystemException {
		return new B2MacroBlock(qnm, basis, block_position, h);
	}
	
	public void update(int current_class_population) {
		for (int i = 0; i < macro_blocks.length; i++) {
			((B2MacroBlock) macro_blocks[i]).update(current_class_population);
		}
	}
}
