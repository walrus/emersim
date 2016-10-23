package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.MicroBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.YMicroBlock;

/**
 * @author Jack Bradshaw
 */
public class YMacroBlock extends MacroBlock {

	public YMacroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) throws InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position, h);
		selection_policy = new TypeOneBlocks(qnm, this); 
	}

	public YMacroBlock(MacroBlock full_block, int current_class) {
		super(full_block, current_class);
	}

	@Override
	protected MacroBlock subBlockCopy(int current_class) {
		return new YMacroBlock(this, current_class);
	}
	
	@Override
	protected MicroBlock newMicroBlock(Position block_position, int h) throws InternalErrorException {
		return new YMicroBlock(qnm, basis, block_position, h);
	}
}
