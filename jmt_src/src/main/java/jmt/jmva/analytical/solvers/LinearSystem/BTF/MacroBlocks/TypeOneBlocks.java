package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.MicroBlock;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * A policy that selects the type 1 Micro Blocks from the full block for use in lower class linear systesms
 * @author Jack Bradshaw
 */
public class TypeOneBlocks extends MicroBlockSelectionPolicy {

	protected TypeOneBlocks(QNModel qnm, MacroBlock full_block) {
		super(qnm, full_block);		
	}

	@Override
	protected MicroBlock[] selectMicroBlocks(int current_class) {
		
		int number_of_micro_blocks = MiscFunctions.binomialCoefficient(current_class - 1, full_block.h);
		
		//Take required macro blocks
		MicroBlock[] micro_blocks = new MicroBlock[number_of_micro_blocks];
		for (int i = 0; i < micro_blocks.length; i++) {
			micro_blocks[i] = full_block.micro_blocks[i].subBlock(current_class);
		}
		return micro_blocks;
	}

}
