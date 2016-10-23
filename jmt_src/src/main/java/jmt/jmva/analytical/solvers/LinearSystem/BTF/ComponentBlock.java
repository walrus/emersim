package jmt.jmva.analytical.solvers.LinearSystem.BTF;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

/**
 * This class defines fields that all component blocks of the linear system must have, and provides the interface 
 * they must all implement.
 * @author Jack Bradshaw
 */
public abstract class ComponentBlock {
	
	/**
	 * The model under consideration.
	 */
	protected QNModel qnm;
	
	/**
	 * The basis of the model under consideration.
	 */
	protected CoMoMBasis basis;
	
	/**
	 * The coordinate of the top left corner of the block 
	 */
	protected Position position;	
	
	/**
	 * The dimensions of the block; in the form (rows, columns)
	 */
	protected Position size;
	
	/**
	 * The number of columns that have been output from the current row 
	 * being printed 
	 */
	protected static int cols_printed;	
	
	/**
	 * The current class being considered.
	 */
	protected int current_class;	
	
	/**
	 * The base constructor for any ComponentBlock
	 * @param qnm The Queueing Network to be considered
	 * @param basis The associate basis of the Model
	 * @param position The position of the top left corner of the block  
	 */
	protected ComponentBlock(QNModel qnm, CoMoMBasis basis, Position position) {
		
		this.qnm = qnm;
		this.basis = basis;
		this.position = position;	
		this.size = new Position(-1, -1);
		
		//Default value for current class since the matrix is only constructed once, for the final class.
		current_class = qnm.R;
	}
	
	/**
	 * Copy Constructor - Shallow Copy
	 * Returns a block with the same QNModel, Basis and position and sets is current working class.
	 * @param block The block to copy
	 * @param The current class under consideration
	 */
	protected ComponentBlock(ComponentBlock block, int current_class) {
		this.position = block.position;
		this.basis = block.basis;
		this.qnm = block.qnm;
		this.size = new Position(-1, -1);
		this.current_class = current_class;
	}

	/**
	 * @return The first column contained in this block
	 */
	public int getStartingCol() {		
		return position.col;
	}
	
	/**
	 * @return The first row contained in this block
	 */
	public int getStartingRow() {		
		return position.row;
	}
	
	/**
	 * @return The Dimensions of the block, in the form (rows, columns)
	 */
	public Position size() {
		return size.copy();
	}
	
	/**
	 * Prints a line of the block if the contains the <code>row</code>.
	 * The number of columns printed for the current row is incremented accordingly.
	 * @param row The absolute row of the overall matrix to be printed.
	 */
	public abstract void printRow2(int row);
	
	/**
	 * Starts a new line whilst printing the matrices.
	 */
	public static void newLine() {
		cols_printed = 0;
		System.out.print("\n");
	}
	
	/**
	 * Adds the appropriate coefficients of a Convolution Expression to the block
	 * @param position Carries information on where to insert the equation depending on the context 
	 * (can be interpreted differently by subclasses)
	 * @param n The Population Change Vector n to which the equation relates
	 * @param queue  The queue k to which the equation relates; 1 <= k <= M
	 * @return The row at which the equation was inserted
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 */
	public abstract int addCE(int position, PopulationChangeVector n, int queue) throws BTFMatrixErrorException, InternalErrorException;
	
	/**
	 * Adds the appropriate coefficients of a Population Constraint to the block
	 * @param position Carries information on where to insert the equation depending on the context 
	 * (can be interpreted differently by subclasses)
	 * @param n The Population Change Vector n to which the equation relates
	 * @param _class  The class r to which the equation relates; 1 <= r <= R
	 * @return The row at which the equation was inserted
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 */
	public abstract int addPC(int position, PopulationChangeVector n, int _class) throws BTFMatrixErrorException, InternalErrorException;

	/**
	 * Carries out the appropriate matrix multiplication operation for the block
	 * @param input Input vector
	 * @param result Result vector
	 */
	public abstract void multiply(BigRational[] result) throws BTFMatrixErrorException;
	
	/**
	 * Makes the block carry out its role in the solution of the linear system
	 * @param rhs The intermediate basis used during the computation of the solution
	 * @throws BTFMatrixErrorException
	 * @throws OperationNotSupportedException
	 * @throws InconsistentLinearSystemException
	 * @throws InternalErrorException
	 */
	public abstract void solve(BigRational[] rhs) throws BTFMatrixErrorException, OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException;
}
