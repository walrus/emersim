package jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks;

import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.MacroBlock;

/**
 * A policy to select the Type 2(a) Macro Blocks for lower class linear systems
 * 
 * @author Jack Bradshaw
 */
public class TypeTwoABlocks extends MacroBlockSelectionPolicy{

	protected TypeTwoABlocks(QNModel qnm, TopLevelBlock full_block, int currnet_class) {
		super(qnm, full_block);		
	}

	@Override
	protected MacroBlock[] selectMacroBlocks(int current_class) {
		
		int number_of_macro_blocks = 0;		
		if (current_class < qnm.R) {
			number_of_macro_blocks = (current_class < qnm.M ? current_class: qnm.M);
		}
		
		//Take required macro blocks
		MacroBlock[] macro_blocks = new MacroBlock[number_of_macro_blocks];
		for (int i = 0; i < macro_blocks.length; i++) {
			macro_blocks[i] = full_block.macro_blocks[i + 1].subBlock(current_class);
		}
		return macro_blocks;
	}
}