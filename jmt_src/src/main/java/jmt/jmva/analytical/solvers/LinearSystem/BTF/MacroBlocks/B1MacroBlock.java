package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.B1MicroBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.MicroBlock;

/**
 * This class implements the Macro Blocks of the B1 Block of the B matrix.
 * 
 * The Factory Methods newMicroBlock and subBlockCopy are implemented to return the correct 
 * block type in the parallel hierarchy.
 * 
 * Type 1 Micro Blocks are selected for lower class linear systems
 * 
 * @author Jack Bradshaw
 */
public class B1MacroBlock extends MacroBlock {  

	public B1MacroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) throws InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position, h);
		selection_policy = new TypeOneBlocks(qnm, this); 		
	}

	public B1MacroBlock(MacroBlock full_block, int current_class) {
		super(full_block, current_class); 
	}

	@Override
	protected MacroBlock subBlockCopy(int current_class) {
		return new B1MacroBlock(this, current_class);
	}
	
	@Override
	protected MicroBlock newMicroBlock(Position block_position, int h) {
		return new B1MicroBlock(qnm, basis, block_position, h);
	}
}
