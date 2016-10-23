package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.UndefinedMultiplyException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * This class implements the underlying data structure of the Secondary Macro Blocks.
 * It is a block matrix, composed of SparseBlocks.
 * 
 * @author Jack Bradshaw
 */
public class SparseBlockMatrix {

	protected CoMoMBasis basis;
	
	protected SparseBlock[][] matrix;
	
	protected Position position;
	
	protected int rows;

	public int cols;
	protected int block_rows, block_cols;
		
	protected int rows_in_blocks;
	protected int cols_in_blocks;

	public SparseBlockMatrix(CoMoMBasis basis, Position position, Position size, Position divisions) throws BTFMatrixErrorException {
		this.basis = basis;
		this.position = position;
		this.rows = size.row;
		this.cols = size.col;
		block_rows = divisions.row;
		block_cols = divisions.col;		
		
		initialise();
	}

	public SparseBlockMatrix(SparseBlockMatrix block_matrix, Position divisions) {
		this.basis = block_matrix.basis;
		this.position = block_matrix.position;
		
		this.rows_in_blocks = block_matrix.rows_in_blocks;
		this.cols_in_blocks = block_matrix.cols_in_blocks;
		
		this.block_rows = divisions.row;
		this.block_cols = divisions.col;
		
		matrix = new SparseBlock[block_rows][block_cols];
		
		for (int x = 0; x < block_rows; x++) {
			for (int y = 0; y < block_cols; y++) {
				this.matrix[x][y] = block_matrix.matrix[x][y];
			}
		}
		this.rows = matrix.length * rows_in_blocks;
		this.cols = matrix[0].length * cols_in_blocks;
	}

	private void initialise() throws BTFMatrixErrorException {
		
		if (rows % block_rows != 0 || cols %  block_cols != 0) {			
			throw new BTFMatrixErrorException("Block matrix of size " + rows + " x " + cols + " cannot be divided into " +  block_rows + "rows and " + block_cols + "columns");
		}
		
		rows_in_blocks = rows /  block_rows;
		cols_in_blocks = cols /  block_cols;
		
		int block_starting_col = 0;
		int block_starting_row = 0;
		
		matrix = new SparseBlock[block_rows][block_cols];
		
		for (int i = 0; i <  block_rows; i++) {
			block_starting_row = i * rows_in_blocks;
			for (int j = 0; j <  block_cols; j++) {
				block_starting_col = j * cols_in_blocks;
				matrix[i][j] = new SparseBlock(basis, new Position(position.row + block_starting_row, position.col + block_starting_col), rows_in_blocks, cols_in_blocks);
			}
		}
		
		//Fill with zeros
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				 write(x, y, BigRational.ZERO);
			}
		}
	}
	
	public void write(int row, int col, BigRational value) {
		int block_row    = row / rows_in_blocks;
		int row_in_block = row %  rows_in_blocks;
		
		int block_col    = col / cols_in_blocks;
		int col_in_block = col %  cols_in_blocks;
		
		matrix[block_row][block_col].write(row_in_block, col_in_block, value); //TODO copy?
	}
	
	/**	 * 
	 * @param row
	 * @param col
	 * @return returns the value at (row, col) of the block matrix (not a copy)
	 */
	
	public BigRational get(int row, int col) {
		int block_row    = row / rows_in_blocks;
		int row_in_block = row %  rows_in_blocks;
		
		int block_col    = col / cols_in_blocks;
		int col_in_block = col %  cols_in_blocks;
		
		return matrix[block_row][block_col].get(row_in_block, col_in_block); 
	}
	
	public void printRow(int row) {
		int row_to_write = row - position.row;
		if (row_to_write >= 0 && row_to_write < rows) {
			for (int y = 0; y < cols; y++) {
				 System.out.format("%2s ", get(row_to_write, y).toString());
			}			
		}
	}
	
	public Position getSize() {
		return new Position(rows, cols);
	}
	
	public void multiply(BigRational[] result) throws BTFMatrixErrorException {
		
		if (cols + position.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when multiplying");;		
		
		for (int i = 0; i < block_rows; i++) {  		
            for (int j = 0; j < block_cols; j++) {
                matrix[i][j].multiply(result);                
            }
		}		
	}		
	
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException {
		if (cols + position.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when solving");		
		
		for (int i = 0; i < block_rows; i++) {  		
            for (int j = 0; j < block_cols; j++) {
            	matrix[i][j].solve(rhs);                   
            }
		}			
	}
	
}