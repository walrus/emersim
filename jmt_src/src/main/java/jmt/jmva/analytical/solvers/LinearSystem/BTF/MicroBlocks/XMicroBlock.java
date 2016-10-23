package jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks;

import java.util.HashSet;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.Tuple;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.LinearSystemSolver.SimpleSolver;
import jmt.jmva.analytical.solvers.LinearSystemSolver.Solver;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the Micro Blocks of the X block of the A matrix.
 * 
 * It extends MatrixMicroBlock and implements the methods to insert the correct coefficients into
 * the block for given equations.
 * 
 * The block is decomposed in the LUP_decomposition object.
 * This is used to solve the block, with the resulting values stored in the Basis.
 * 
 * @author Jack Bradshaw
 */
public class XMicroBlock extends MatrixMicroBlock {

	private Solver solver;
	
	private LUP_Decomposition LUP;
	
	public XMicroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) throws InconsistentLinearSystemException, InternalErrorException {
		super(qnm, basis, position, h);
		computeDimensions();
		initialise();				
	}

	public XMicroBlock(XMicroBlock micro_block, int current_class) {
		super(micro_block, current_class);
		this.LUP = micro_block.LUP;
	}
	
	@Override
	protected MicroBlock subBlockCopy(int current_class) {
		return new XMicroBlock(this, current_class);
	}
	
	public void LUPDecompose(boolean in_place) throws InconsistentLinearSystemException {
		if (in_place) {
			LUP = new LUP_Decomposition(basis, position, array, true);
		} else {
			LUP = new LUP_Decomposition(basis, position, array, false);
		}
	}

	@Override
	protected void computeDimensions() {
		size.row = MiscFunctions.binomialCoefficient(qnm.M, h) * qnm.M;
		size.col = size.row;
	}	

	@Override
	public int addCE(int vector_index, PopulationChangeVector n, int queue)
			throws BTFMatrixErrorException, InternalErrorException {
		
		int col = basis.indexOf(n, queue);	
		
		col -= position.col;		
			
		if (col < 0 || col >= array.length) throw new BTFMatrixErrorException("Column not in micro block, when considering n:" + n +" queue: " + queue);
			
		array[row][col] = BigRational.ONE;			
			
		for (int s = 1; s <= qnm.R - 1; s++) {
			
			n.plusOne(s);				
			
			col = basis.indexOf(n, queue);				
			n.restore();
				
			col -= position.col;			
			
			//Column is in this block
			if (col >= 0 && col < array.length) {							
				array[row][col] =  qnm.getDemandAsBigRational(queue - 1, s - 1).negate();
			}		
			
		}
			
		//Increment row, ready for next equation
		int inserted_at_row = row;
		row++;
		return inserted_at_row + position.row;
	}

	@Override
	public int addPC(int vector_index, PopulationChangeVector n, int _class)
			throws BTFMatrixErrorException, InternalErrorException {		
		
		int col = basis.indexOf(n, 1);	
		
		col -= position.col;		
		
		if (col < 0 || col >= array.length) throw new BTFMatrixErrorException("Column not in micro block, when considering n:" + n +" class: " + _class);
		if ( col + qnm.M - 1 >= array.length) throw new BTFMatrixErrorException("PC will not fit, when considering:" + n +" class: " + _class);
		
		for (int k = 1; k <= qnm.M; k++) {			
			
			array[row][col] = qnm.getDemandAsBigRational( k - 1, _class -1).negate();  
			col++;  //NOTE: Specific to this order
		}
		
		//Increment row, ready for next equation
		int inserted_at_row = row;
		row++;
		return inserted_at_row + position.row;
	}

	/**
	 * Alternative solve that uses the simple linear system solver
	 * @param rhs
	 * @throws BTFMatrixErrorException
	 * @throws OperationNotSupportedException
	 * @throws InconsistentLinearSystemException
	 * @throws InternalErrorException
	 */
	public void solve2(BigRational[] rhs) throws BTFMatrixErrorException, OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {
		System.out.print("Solving XMicroBlock...\n\n");
		
		BigRational[] sysB = new BigRational[size.row];
		BigRational[] result = new BigRational[size.row];
		
		//copy portion of rhs to sysB
		for (int i = 0; i < size.row; i++) {
			sysB[i] = rhs[position.row + i];
		}
		
		System.out.println("SysB");
		MiscFunctions.printMatrix(sysB);
		
		//Solve...
		solver = new SimpleSolver();
		solver.initialise(array,  new LinkedList<Tuple<Integer, Integer>>()	, new HashSet<Integer>());
		
		result = solver.solve(sysB);
		
		System.out.println("result");
		MiscFunctions.printMatrix(result);
		//copy result to basis
		for (int i = 0; i < size.row; i++) {
			basis.setValue(result[i], position.row + i);
		}
		
	}
	
	@Override
	public void solve(BigRational[] rhs) {
		LUP.solve(rhs);
	}


}
