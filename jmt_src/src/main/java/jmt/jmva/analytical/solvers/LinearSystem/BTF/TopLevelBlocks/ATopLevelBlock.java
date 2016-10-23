package jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.MacroBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.SecondaryMacroBlock;

/**
 * This class extends TopLevelBlock to also include a list of SecondaryMacroBlocks. 
 * This is required of the two blocks of the A matrix: XBlock and YBlock
 * 
 * @author Jack Bradshaw
 */
public abstract class ATopLevelBlock extends TopLevelBlock {

	protected SecondaryMacroBlock[] sec_macro_blocks;
	
	/**
	 * Constructor - First phase of creation, does not initialise MacroBlocks or SecondaryMacroBlocks
	 * @see ATopLevelBlock#initialise() initialise()
	 * @see ATopLevelBlock#initialiseSecondaryMacroBlocks() initialiseSecondaryMacroBlocks()
	 * @param qnm
	 * @param basis
	 * @param position
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected ATopLevelBlock(QNModel qnm, CoMoMBasis basis, Position position)
			throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position);	
	}		
	
	/**
	 * Copy Constructor	 
	 * @throws BTFMatrixErrorException 
	 */
	protected ATopLevelBlock(ATopLevelBlock full_block, int current_class) throws BTFMatrixErrorException {
		super(full_block, current_class);		
	}

	/**
	 * Builder method that encapsulates the creation of sub-ATopLevelBlocks
	 * Fills list of MacroBlocks as super class.
	 * Fills list of SecondaryMacroBlocks.
	 * 
	 * @param current_class The class for which the copy is created
	 * @return A shallow copy of calling block, containing the correct macro blocks for the current_class
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public TopLevelBlock subBlock(int current_class) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		
		//Create shallow copy of full block and take macro_blocks
		ATopLevelBlock sub_block = (ATopLevelBlock) super.subBlock(current_class);
		
		//Take required secondary macro blocks
		sub_block.sec_macro_blocks = new SecondaryMacroBlock[sub_block.macro_blocks.length - 1];
		for (int i = 0; i < sub_block.sec_macro_blocks.length; i++) {
			sub_block.sec_macro_blocks[i] = sec_macro_blocks[i].subBlock(current_class, sub_block.macro_blocks[i], sub_block.macro_blocks[i + 1]);
		}
		
		return sub_block;
	}
	
	/**
	 * initialise() overridden to further create list of SecondaryMacroBlocks
	 */
	@Override
	public void initialise() throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		
		//Initialise macro_blocks as super class
		super.initialise();
		
		//Initialise sec_macro_blocks
		initialiseSecondaryMacroBlocks();
	}

	/**
	 * Creates list of contained SecondaryMacroBlocks
	 * @throws BTFMatrixErrorException
	 */
	private void initialiseSecondaryMacroBlocks() throws BTFMatrixErrorException {
		sec_macro_blocks = new SecondaryMacroBlock[macro_blocks.length - 1];
		//Instantiate secondary macro blocks		
		for (int h = 0; h < macro_blocks.length - 1; h++) {				
			newSecondaryMacroBlock(h, macro_blocks[h], macro_blocks[h+1]);			
		}
	}
	
	@Override
	public int addCE(int position, PopulationChangeVector n, int queue) throws BTFMatrixErrorException, InternalErrorException {	
		
		int block = findMacroBlock(position);		
		
		int row_inserted_at =  macro_blocks[block].addCE(position, n, queue);
		
		if (block < sec_macro_blocks.length) {
			sec_macro_blocks[block].addCE(row_inserted_at, n, queue);
		}	
		return row_inserted_at;
	}
	
	/**
	 * Factory Method for Secondary Macro Blocks
	 * 
	 * To be overridden by subclasses in order to create the appropriate
	 * subclass of SecondaryMacroBlock in the parallel hierarchy. 
	 * 
	 * @param h
	 * @param block_1
	 * @param block_2
	 * @throws BTFMatrixErrorException
	 */
	protected abstract void newSecondaryMacroBlock(int h, MacroBlock block_1, MacroBlock block_2) throws BTFMatrixErrorException;
	
	/*
	 * OVERRIDEN METHODS FROM COMPONENT BLOCK...
	 */
	
	@Override
	public int addPC(int position, PopulationChangeVector n, int _class) throws BTFMatrixErrorException, InternalErrorException {
		
		//Locate macro block that contains the leading constant
		int block = findMacroBlock(position);		
		
		//add equation to macro block 
		int row_inserted_at =  macro_blocks[block].addPC(position, n, _class);	
		
		//add equation to secondary macro block
		if (block > 0) {
			sec_macro_blocks[block - 1].addPC(row_inserted_at, n, _class);
		}
		
		return row_inserted_at;
	}
	
	@Override
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {
		
		//multiply macro_blocks
		super.multiply(result);
		
		//Also multiply sec_macro_blocks
		for (int i = 0; i < sec_macro_blocks.length; i++) {
			sec_macro_blocks[i].multiply(result);
		}
	}
	
	@Override
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException, OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {
		
		//Need to solve in bottom to top order, with secondary macro blocks interleaved
		
		//First solve last, alone macro block
		macro_blocks[macro_blocks.length - 1].solve(rhs);
		
		//Now, solve secondary macro block, marco block pairs in reverse order:
		for (int i =  sec_macro_blocks.length - 1; i >= 0; i--) {
			sec_macro_blocks[i].solve(rhs);
			macro_blocks[i].solve(rhs);
		}
	}
}
