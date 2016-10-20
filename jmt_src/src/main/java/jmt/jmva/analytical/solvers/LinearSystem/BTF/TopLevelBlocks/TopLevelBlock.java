package jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.ComponentBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks.MacroBlock;

/**
 * This class is a superclass for the five main components of the BTF linear system: X, Y, B1, B2 and C.
 * It is composed of MacroBlocks and implements the methods that intialise and handle them.
 * 
 * A MacroBlockSelectionPolicy must be specified by any concrete implementation, this will
 * be used to select which MacroBlocks to use in lower class linear systems.
 * 
 * @author Jack Bradshaw
 *
 */
public abstract class TopLevelBlock extends ComponentBlock {

	/**
	 * The list of Macro Blocks contained in the TopLevelBlock
	 */
	protected MacroBlock[] macro_blocks;
	
	/**
	 * Encapsulated policy for selecting macro blocks when 
	 * creating sub-blocks for lower classes
	 */
	protected MacroBlockSelectionPolicy selection_policy;
	
	
	/**
	 * Constructor - First phase of construction
	 * 
	 * Initialises everything up to but not including the list of macro blocks
	 * @see TopLevelBlock#initialise() initialise()
	 * 
	 * @param qnm The Model under consideration
	 * @param basis The corresponding basis for the model
	 * @param position The starting position (top right hand corner) of the block
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected TopLevelBlock(QNModel qnm, CoMoMBasis basis, Position position) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position);		
	}
		
	/**
	 * Copy Constructor	 
	 */
	protected TopLevelBlock(TopLevelBlock full_block, int current_class) {
		super(full_block, current_class);
		
		this.current_class = current_class;		
	}
	
	/**
	 * Second Phase of Construction
	 * 
	 * This method intialises the list component macro blocks.
	 *  
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public void initialise() throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		
		//Calculate the number of macro blocks the top level block will contain
		int number_of_macro_blocks = (qnm.R - 1 < qnm.M ? qnm.R - 1: qnm.M) + 1;
		
		macro_blocks = new MacroBlock[number_of_macro_blocks];
		
		//The size of the block
		size = new Position(0,0);
		
		//The starting position of the next macro block to be added
		Position block_position = position.copy();
		
		for (int h = 0; h < number_of_macro_blocks; h++) {	
			
			//add new macro block
			macro_blocks[h] = newMacroBlock(block_position.copy(), h);
			
			//initialise the macro block
			macro_blocks[h].initialise();
			
			//increase block_position by the size of newly added macro block
			block_position.add(macro_blocks[h].size());
			
			///increase the size of the block
			size.add(macro_blocks[h].size());
		}
		
		//Store the size of the block
		size = block_position;
	}	
	
	/**
	 * Factory Method for Macro Blocks
	 * 
	 * To be overridden by subclasses in order to create the appropriate
	 * subclass of MacroBlock in the parallel hierarchy. 
	 * 
	 * i.e. XBlocks are composed of XMacroBlocks
	 * 
	 * @param position Starting position for the new macro block
	 * @param h Number of non-zeros associated with the macro block
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected abstract MacroBlock newMacroBlock(Position position, int h) throws InternalErrorException, InconsistentLinearSystemException;
	
	/**
	 * Factory Method for sub-block creation
	 * 
	 * This method is to be overridden by subclasses to instantiate a copy
	 * of the current block of correct type
	 * 
	 * @param current_class The class for which the copy is created
	 * @return A shallow copy of calling block, for the current_class. Without macro blocks.
	 * 
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected abstract TopLevelBlock subBlockCopy(int current_class) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException;
	
	/**
	 * Builder method that encapsulates the creation of sub-TopLevelBlocks
	 * 
	 * @param current_class The class for which the copy is created
	 * @return A shallow copy of calling block, containing the correct macro blocks for the current_class
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public TopLevelBlock subBlock(int current_class) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		
		//Create a shallow copy of the current full block
		TopLevelBlock sub_block = subBlockCopy(current_class);
		
		//Assign the correct macro blocks according to the selection policy
		sub_block.macro_blocks = selection_policy.selectMacroBlocks(current_class);
		
		return sub_block;
	}	
	
	
	/**
	 * Finds the macro which contains a specified column (row) 
	 * @param position The number of the column (row)
	 * @return The index of containing MacroBlock in the list <code>macro_blocks</code>
	 * @throws BTFMatrixErrorException
	 */
	protected int findMacroBlock(int position) throws BTFMatrixErrorException {

		//Invalid position passed:
		if (position < 0) throw new  BTFMatrixErrorException("Trying to find macro block containing index: " + position);
		
		int block = 0;
		
		//Linear Search to locate containing MacroBlock //TODO binary search
		for (int i = 1; i < macro_blocks.length; i++) {
			if (position >= (macro_blocks[i].getStartingRow())) {
				block++;
			}
		}
		
		//Invalid positon passed, size of block overshot!
		if (block >=  macro_blocks.length) throw new  BTFMatrixErrorException("Ran out of macro blocks! position = " + position);
		
		return block;
	}
	
	/*
	 * OVERRIDEN METHODS FROM COMPONENT BLOCK...
	 */
	
	@Override
	public int addCE(int position, PopulationChangeVector n, int queue) throws BTFMatrixErrorException, InternalErrorException {
		int block = findMacroBlock(position);
		
		return  macro_blocks[block].addCE(position, n, queue);		
	}
	
	@Override
	public int addPC(int position, PopulationChangeVector n, int _class) throws BTFMatrixErrorException, InternalErrorException {
		int block = findMacroBlock(position);		
		
		return  macro_blocks[block].addPC(position, n, _class);			
	}
	
	@Override
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {
		
		for (int i = 0; i < macro_blocks.length; i++) {
			macro_blocks[i].multiply(result);
		}
	}
	
	@Override
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException, OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {
		for (int i = 0; i < macro_blocks.length; i++) {
			macro_blocks[i].solve(rhs);
		}
	}
	
	@Override
	public void printRow2(int row) {
		for (int i = 0; i < macro_blocks.length; i++) {
			macro_blocks[i].printRow2(row);
		}
	}	
}
