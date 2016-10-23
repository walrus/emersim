package jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Exceptions.UndefinedMultiplyException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the Micro Blocks of the X block of the A matrix.
 * 
 * It extends MatrixMicroBlock and implements the methods to insert the correct coefficients into
 * the block for given equations.
 * 
 * @author Jack Bradshaw
 */
public class YMicroBlock extends MatrixMicroBlock {

	public YMicroBlock(QNModel qnm, CoMoMBasis basis, Position position, int h) throws InternalErrorException {
		super(qnm, basis, position, h);
		computeDimensions();
		initialise();
	}

	public YMicroBlock(YMicroBlock micro_block, int current_class) {
		super(micro_block, current_class);
	}

	@Override
	protected MicroBlock subBlockCopy(int current_class) {
		return new YMicroBlock(this, current_class);
	}
	
	@Override
	protected void computeDimensions() {
		size.col = MiscFunctions.binomialCoefficient(qnm.M, h);
		size.row  = size.col * qnm.M;

	}	

	@Override
	public int addCE(int row, PopulationChangeVector n, int queue)
			throws BTFMatrixErrorException, InternalErrorException {
		
		int col = basis.indexOf(n, 0);	
		int insertion_row = row;
		col -= position.col;
		insertion_row -= position.row;
			
		if (col < 0 || col >= size.col) throw new BTFMatrixErrorException("Column " + col + " not in micro block, when considering n:" + n +" queue: 0");
		if (insertion_row < 0 || insertion_row >= size.row) throw new BTFMatrixErrorException("Row " + row + " not in micro block, when considering n:" + n +" queue: 0");
			
		array[insertion_row][col] = BigRational.MINUS_ONE;
		
		return row;			
	}

	@Override
	public int addPC(int row, PopulationChangeVector n, int _class)
			throws BTFMatrixErrorException, InternalErrorException {
		
		n.minusOne(_class);
		int col = basis.indexOf(n, 0);	
		
		int insertion_row = row;
		
		col -= position.col;
		insertion_row -= position.row;		
		
		if (col >= 0 && col < size.col) { //column is in this block
			array[insertion_row][col] = qnm.N.getAsBigRational(_class - 1).subtract(n.getAsBigRational(_class - 1)); 
		}		
		n.restore();
		
		col = basis.indexOf(n, 0);	
		insertion_row = row;
		col -= position.col;
		insertion_row -= position.row;
			
		
		if (col < 0 || col >= size.col) throw new BTFMatrixErrorException("Column " + col + " not in micro block, when considering n:" + n +" queue: 0");
		if (insertion_row < 0 || insertion_row >= size.row) throw new BTFMatrixErrorException("Row " + row + " not in micro block, when considering n:" + n +" queue: 0");
			
		array[insertion_row][col] = qnm.getDelayAsBigRational(_class - 1).negate();				 		
		
		return row;
	}

	@Override
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException {			
			
		if (position.col + size.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when multiplying");
		
		for (int i = 0; i < size.row; i++) { 			        
	        try {
	        	rhs[position.row + i] = rhs[position.row + i].add(multiplyRow(i).negate());
			} catch (UndefinedMultiplyException e) {
				rhs[position.row + i] = new BigRational(-1);
	            rhs[position.row + i].makeUndefined();				
			}
		}
	}
		

}
