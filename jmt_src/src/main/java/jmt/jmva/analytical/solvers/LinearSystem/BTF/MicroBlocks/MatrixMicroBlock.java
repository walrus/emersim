package jmt.jmva.analytical.solvers.LinearSystem.BTF.MicroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.UndefinedMultiplyException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class extends to MicroBlock class to provided extra functionality common to 
 * the XMicroBlocks and YMicroBlocks that are both based on matrix data structrues.
 * 
 * An array is used as the under;lying data structure and the print and multiply methods are implemented at this level,
 * 
 * @author Jack Bradshaw
 */
public abstract class MatrixMicroBlock extends MicroBlock {

	/**
	 * The row at which to insert next equation
	 */	
	protected int row;	
			
	/**
	 * The underlying array
	 */
	protected BigRational[][] array;
		
	protected MatrixMicroBlock(QNModel qnm, CoMoMBasis basis,
			Position position, int h) {
		super(qnm, basis, position, h);
	}

	public MatrixMicroBlock(MatrixMicroBlock micro_block, int current_class) {
		super(micro_block, current_class);
		this.array = micro_block.array;
	}

	@Override
	protected void initialiseDataStructures() {
		row = 0;
		array = new BigRational[size.row][size.col];
		
		//fill with zeros
		for (int x = 0; x < size.row; x++) {
			for (int y = 0; y < size.col; y++) {
				array[x][y] = BigRational.ZERO;
			}
		}
		
	}
	protected BigRational multiplyRow(int index) throws UndefinedMultiplyException {
		
		BigRational result = BigRational.ZERO;
		
		for (int j = 0; j < size.col; j++) {
			if (!array[index][j].isZero()) {
				if (basis.getNewValue(j + position.col).isPositive()) {
					result = result.add((array[index][j].multiply(basis.getNewValue(j + position.col))));						
				} else if (basis.getNewValue(j + position.col).isUndefined()) { 
					throw new UndefinedMultiplyException();               	                 
                }
            }
		}
		return result;
	}
	
	@Override
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {		
		
		if (position.col + size.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when multiplying");
		
		for (int i = 0; i < size.row; i++) {           
            try {
            	result[position.row + i] = result[position.row + i].add(multiplyRow(i));
			} catch (UndefinedMultiplyException e) {
				result[position.row + i] = new BigRational(-1);
                result[position.row + i].makeUndefined();
			}
		}
	}
	
	@Override
	public void printRow2(int row) {
		int row_to_print = row - position.row;		
		if (row_to_print >= 0 && row_to_print < size.row) {
			//print white space offset
			for (int i = cols_printed; i < position.col; i++) {
				System.out.format("%2s ", " ");	
				cols_printed++;
			}
			for (int col = 0; col < size.col; col++) {				
				System.out.format("%2s ", array[row_to_print][col].toString());
				cols_printed++;
			}
		}
	}

}
