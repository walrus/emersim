package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.MicroBlock;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * A policy that selects the type 2(a) Micro Blocks from the full block for use in lower class linear systesms
 * @author Jack Bradshaw
 */
public class TypeTwoABlocks extends MicroBlockSelectionPolicy {

	protected TypeTwoABlocks(QNModel qnm, MacroBlock full_block) {
		super(qnm, full_block);		
	}

	@Override
	protected MicroBlock[] selectMicroBlocks(int current_class) {
		//The number of micro blocks at the head of the list that we are NOT taking
		int number_of_type1_blocks = 0;
		if (full_block.h < current_class ) {
			number_of_type1_blocks = MiscFunctions.binomialCoefficient(current_class - 1, full_block.h);
		}
		
		//The number of blocks we ARE taking
		int number_of_type2a_blocks = 0;
		if (current_class < qnm.R && full_block.h <= current_class ) {
			number_of_type2a_blocks = MiscFunctions.binomialCoefficient(current_class - 1, full_block.h - 1);
		}
		
		//Take required macro blocks
		MicroBlock[] micro_blocks = new MicroBlock[number_of_type2a_blocks];
		for (int i = 0; i < micro_blocks.length; i++) {
			micro_blocks[i] = full_block.micro_blocks[i + number_of_type1_blocks].subBlock(current_class);
		}
		return micro_blocks;
	}

}
