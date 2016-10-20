package jmt.jmva.analytical.solvers.LinearSystem.BTF;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.Control.Main;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.LinearSystem;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.B1Block;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.B2Block;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.CBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.XBlock;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.TopLevelBlocks.YBlock;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the BTF linear system, using the idea that the matrices only need to be generated once.
 * @author Jack Bradshaw
 */
public class BTFLinearSystem extends LinearSystem {
	
	/**
	 * Initial Matrix Components (i.e. the full, final class linear system)
	 */
	XBlock x_block;
	YBlock y_block;
	B1Block b1_block;
	B2Block b2_block;
	CBlock c_block;
	
	/**
	 * Current Matrix Components (i.e the linear system for the current class)
	 */
	XBlock x;
	YBlock y;
	B1Block b1;
	B2Block b2;
	CBlock c;
	
	/**
	 * Vector for storing intermediate solution
	 */
	BigRational[] rhs;
	
	/**
	 * Boolean to indicate whether LUP decomposition should happen in place or not.
	 * The latter is useful for debugging and testing purposes, allowing the original matrix to be inspected.
	 */
	private boolean in_place = true;
	
	/**
	 * Constructor
	 * @param qnm The model under study
	 * @param basis The basis of the model
	 * @throws InternalErrorException
	 * @throws BTFMatrixErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public BTFLinearSystem(QNModel qnm, CoMoMBasis basis)
			throws InternalErrorException, BTFMatrixErrorException, InconsistentLinearSystemException {
		
		super(qnm, basis);		
		
		initialise();
	}
	
	/**
	 * Constructor
	 * @param qnm The model under study
	 * @param basis The basis of the model
	 * @param in_place Boolean to specify in the LUP decomposition should be in place or not.
	 * @throws InternalErrorException
	 * @throws BTFMatrixErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public BTFLinearSystem(QNModel qnm, CoMoMBasis basis, boolean in_place)
			throws InternalErrorException, BTFMatrixErrorException, InconsistentLinearSystemException {
		
		super(qnm, basis);		
		this.in_place = in_place;
		initialise();
	}
	
	/**
	 * Creates, adds equations to and decomposes the blocks of BTF linear system.
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	private void initialise() throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		//Create and initialise the component blocks for the final class
		x_block  = new XBlock (qnm, basis);
		x_block.initialise();
				
		y_block  = new YBlock (qnm, basis);
		y_block.initialise();
				
		b1_block = new B1Block(qnm, basis);
		b1_block.initialise();
				
		b2_block = new B2Block(qnm, basis);
		b2_block.initialise();
				
		c_block  = new CBlock (qnm, basis);	
		c_block.initialise();
				
		//Add PCs and CEs to the matrices 
		generate();
				
		//Create rhs vector
		rhs = new BigRational[basis.getSize()];
		
		if (Main.verbose) {
			printFullMatrices();
		}
	}
	
	/**
	 * Inserts the equations into the BTF linear system and LUP decomposed the XMicroBlocks
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 */
	private void generate() throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {
		PopulationChangeVector n;
		int row_added;
	   	for (int i = 0; i < MiscFunctions.binomialCoefficient(qnm.M + qnm.R - 1 , qnm.M); i++)  { //loop over all possible population changes n
	   		n  = basis.getPopulationChangeVector(i).copy(); // To improve bug safety
	   		
	   		if (n.sumHead(qnm.R - 1) < qnm.M) {
	   			for (int k = 1; k <= qnm.M; k++) {
	   				row_added = x_block.addCE(basis.indexOf(n,k), n, k);   				
	   				y_block.addCE(row_added, n, k);
	   				b1_block.addCE(row_added, n, k);	   				
	  			}
	   			for (int s = 1; s < qnm.R; s++) {
	   				n.plusOne(s);
	   				row_added = x_block.addPC(basis.indexOf(n, 1), n, s);	   				
	   				y_block.addPC(row_added, n, s);
	   				b1_block.addPC(row_added, n, s);
	   				n.restore();
	   			}   			    			    			
	   		}
	   	}
	   	x_block.LUPDecompose(in_place);
	}
	
	@Override
	public void initialiseMatricesForClass(PopulationVector current_N, int current_class) throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException {		
		
		//create sub-blocks for the current class
		x  = (XBlock)   x_block.subBlock(current_class);
		y  = (YBlock)   y_block.subBlock(current_class);
		b1 = (B1Block) b1_block.subBlock(current_class);
		b2 = (B2Block) b2_block.subBlock(current_class);
		c  = (CBlock)   c_block.subBlock(current_class);
		
		System.out.print("Matrices for class " + current_class + "\n\n\n");
		
		if (Main.verbose) {
			printWorkingMatrices();
		}		
	}
	
	@Override
	public void solve() throws OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException, BTFMatrixErrorException {
		
		basis.startBasisComputation();
		//System.out.println("BEFORE: ");
		//basis.print_values();
		
		//Order of solving is important:
		//B First
		b1.solve(rhs);
		b2.solve(rhs);
		c.solve(rhs);
		
		//Then A; Y then X
		y.solve(rhs);
		//System.out.println("AFTER Y: ");
		//basis.print_values();
		x.solve(rhs);		
		
		//basis.print_values();
	}
	
	@Override
	public void update(int current_class_population ) {
		b2.update(current_class_population);
	}
	
	/**
	 * Multiplies the matrices by the values in the previous and current bases.
	 * @param lhs_result The Left-Hand-Side of the linear system
	 * @param rhs_result The Right-Hand-Side of the linear system
	 * @throws BTFMatrixErrorException
	 */
	public void multiply(BigRational[] lhs_result,  BigRational[] rhs_result) throws BTFMatrixErrorException {
		
		//Multiply each component
		x.multiply(lhs_result);
		y.multiply(lhs_result);
		b1.multiply(rhs_result);
		b2.multiply(rhs_result);
		c.multiply(lhs_result);			
		
		//achieves identity rows in A
		for (int i = MiscFunctions.binomialCoefficient(qnm.M + qnm.R - 1 , qnm.M) * qnm.M; i < basis.getSize(); i++) {
			lhs_result[i] = basis.getNewValue(i).copy();
		}
	}
	
	/**
	 * Prints the complete, final class matrices
	 */
	private void printFullMatrices() {
		print(x_block, y_block, b1_block, b2_block, c_block);
	} 
	
	/**
	 * Prints the matrices for the current class being considered.
	 */
	private void printWorkingMatrices() {
		print(x, y, b1, b2,c);
	}
	
	/**
	 * Prints the provided matrices to the screen
	 * @param x_block
	 * @param y_block
	 * @param b1_block
	 * @param b2_block
	 * @param c_block
	 */
	private void print(XBlock x_block, YBlock y_block, B1Block b1_block, B2Block b2_block, CBlock c_block) {
		
		//print A
		//calculate the number of rows in the top blocks of the matrices
		int top_half_rows = MiscFunctions.binomialCoefficient(qnm.M + qnm.R - 1 , qnm.M) * qnm.M;
		
		for (int row = 0; row < top_half_rows; row++) {
			x_block.printRow2(row);			
			y_block.printRow2(row);		
			ComponentBlock.newLine();
		}
					
		//print B1
		System.out.print("B1: \n\n");
		for (int row = 0; row < top_half_rows; row++) {
			b1_block.printRow2(row);
			ComponentBlock.newLine();
		}
				
		//print B2
		System.out.print("B2: \n\n");
		for (int row = top_half_rows; row < basis.getSize(); row++) {
			b2_block.printRow2(row);					
			ComponentBlock.newLine();
		}
	}	
}
