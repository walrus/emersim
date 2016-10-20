package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

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
import jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks.MicroBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.TopLevelBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.TypeOneBlocks;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class is a superclass for the Macro Blocks of the five main components of the BTF linear system: X, Y, B1, B2 and C.
 * A MacroBlock is composed of MicroBlocks and implements the methods that intialise and handle them.
 * 
 * A MicroBlockSelectionPolicy must be specified by any concrete implementation, this will
 * be used to select which MicroBlocks are to use in lower class linear systems.
 * 
 * @author Jack Bradshaw
 *
 */
public abstract class MacroBlock extends ComponentBlock{

	/**
	 * Array of contained micro blocks
	 */
	protected MicroBlock[] micro_blocks;
	
	/**
	 * Encapsulated policy for selecting micro blocks when 
	 * creating sub-blocks for lower classes
	 */
	protected MicroBlockSelectionPolicy selection_policy;
	
	/**
	 * Number of non-zeros in n associated to macro block
	 */
	protected int h;	
	
	/**
	 * Constructor - First phase of construction
	 * 
	 * Initialises everything up to but not including the list of micro blocks
	 * @see MacroBlock#initialise() initialise()
	 * 
	 * @param qnm The Model under consideration
	 * @param basis The corresponding basis for the model
	 * @param position The starting position (top right hand corner) of the block
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected MacroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) throws InternalErrorException, InconsistentLinearSystemException {
		super(qnm, basis, position);
		this.h = h;		
	}	

	/**
	 * Second Phase of Construction
	 * 
	 * This method initialises the list component micro blocks.
	 *  
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public void initialise() throws InternalErrorException, InconsistentLinearSystemException {
		
		//Calculate the nunber of micro blocks this macro block will contain
		int number_of_micro_blocks = MiscFunctions.binomialCoefficient(qnm.R - 1, h);
	
		micro_blocks = new MicroBlock[number_of_micro_blocks];
	
		//The size of the macro block
		size = new Position(0,0);
		
		//The starting position of the next micro block to be added 
		Position block_position = position.copy();
	
		//Instantiate micro blocks
		for (int i = 0; i < number_of_micro_blocks; i++) {
			
			//insert new micro block
			micro_blocks[i] = newMicroBlock(block_position.copy(), h);
			
			//Initialise micro block
			micro_blocks[i].initialise();
			
			//increase block_position by the size of newly added micro block
			block_position.add(micro_blocks[i].size());
			
			///increase the size of the macro block
			size.add(micro_blocks[i].size());		
		}
	}
	
	/**
	 * Factory Method for Micro Blocks
	 * 
	 * To be overridden by subclasses in order to create the appropriate
	 * subclass of MacroBlock in the parallel hierarchy. 
	 * 
	 * i.e. XMacroBlocks are composed of XMicroBlocks
	 * 
	 * @param block_position Starting position for the new macro block
	 * @param h Number of non-zeros associated with the macro block
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	protected abstract MicroBlock newMicroBlock(Position block_position, int h) throws InternalErrorException, InconsistentLinearSystemException;
	
	/**
	 * Copy Constructor
	 * 
	 * @param full_block Block to be copied
	 * @param current_class The class for which the copy is being created
	 */
	protected MacroBlock(MacroBlock full_block, int current_class) {
		super(full_block, current_class);
		
		this.current_class  = current_class;
		this.h = full_block.h;
		this.size = full_block.size; //TODO really?		
	}
		
	/**
	 * Builder method that encapsulates the creation of sub-MacroBlock
	 * 
	 * @param current_class The class for which the copy is being created
	 * @return A shallow copy of calling block, containing the correct micro blocks for the current_class
	 */
	public MacroBlock subBlock(int current_class) {
		
		//Create Shallow copy of full block
		MacroBlock sub_block = subBlockCopy(current_class);
		
		//Take required micro blocks as determined by selection policy
		sub_block.micro_blocks = selection_policy.selectMicroBlocks(current_class);
		
		return sub_block;
	}
	
	/**
	 * Factory Method for sub-block creation
	 * 
	 * This method is to be overridden by subclasses to instantiate a copy
	 * of the current block (of the correct type)
	 * 
	 * For instance, a copy of an XMacroBlocks needs to be a XMacroBlock
	 * 
	 * @param current_class The class for which the copy is created
	 * @return A shallow copy of calling block, for the current_class. Without micro blocks.
	 */
	protected abstract MacroBlock subBlockCopy(int current_class);

	/**
	 * Finds the micro which contains a specified column (row) 
	 * @param position The number of the column (row)
	 * @return The index of containing MicroBlock in the list <code>micro_blocks</code>
	 * @throws BTFMatrixErrorException
	 */
	private int findMicroBlock(int position) throws BTFMatrixErrorException {
		
		//Invalid position passed:
		if (position < 0) throw new  BTFMatrixErrorException("Trying to find macro block containing index: " + position);
		
		int block = 0;
		
		//Linear Search to locate containing MacroBlock //TODO binary search
		for (int i = 1; i < micro_blocks.length; i++) {
			if (position >= (micro_blocks[i].getStartingRow())) {
				block++;
			}
		}
		//Invalid position passed, size of block overshot!
		if (block >=  micro_blocks.length) throw new  BTFMatrixErrorException("Ran out of micro blocks! position = " + position);
		
		return block;
	}
	
	/**
	 * @return The number of micro blocks contain in this macro block
	 */
	protected int numberOfMicroBlocks() {
		return micro_blocks.length;
	}
	
	/*
	 * OVERRIDEN METHODS FROM COMPONENT BLOCK...
	 */
	
	@Override
	public int addCE(int position, PopulationChangeVector n, int queue) throws BTFMatrixErrorException, InternalErrorException {
		
		//find micro block 
		int block = findMicroBlock(position);
		
		//add equation to micro block
		int row_inserted_at =  micro_blocks[block].addCE(position, n, queue);	
		
		return row_inserted_at;
	}
	
	@Override
	public int addPC(int position, PopulationChangeVector n, int _class) throws BTFMatrixErrorException, InternalErrorException {
		
		//find micro block 
		int block = findMicroBlock(position);
		
		//add equation to micro block
		int row_inserted_at = micro_blocks[block].addPC(position, n, _class);
		
		return row_inserted_at;
	}
	
	@Override
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {
		//multiply all micro blocks
		for (int i = 0; i < micro_blocks.length; i++) {
			micro_blocks[i].multiply(result);
		}		
	}
	
	@Override
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException, OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {		
		//solve all micro blocks
		for (int i = 0; i <  micro_blocks.length ; i++) {
			micro_blocks[i].solve(rhs);
		}
	}

	@Override
	public void printRow2(int row) {
		//print all micro blocks
		for (int i = 0; i < micro_blocks.length; i++) {
			micro_blocks[i].printRow2(row);
		}
	}
}
