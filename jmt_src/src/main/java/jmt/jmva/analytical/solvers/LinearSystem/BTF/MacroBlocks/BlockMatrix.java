package jmt.jmva.analytical.solvers.LinearSystem.BTF.MacroBlocks;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.UndefinedMultiplyException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.Position;

/**
 * @author Jack Bradshaw
 */
public class BlockMatrix {

	protected CoMoMBasis basis;
	
	protected BigRational[][][][] matrix;
	
	protected Position position;
	
	protected int rows;

	public int cols;
	protected int block_rows, block_cols;
		
	protected int rows_in_blocks;
	protected int cols_in_blocks;

	public BlockMatrix(CoMoMBasis basis, Position position, Position size, Position divisions) throws BTFMatrixErrorException {
		this.basis = basis;
		this.position = position;
		this.rows = size.row;
		this.cols = size.col;
		block_rows = divisions.row;
		block_cols = divisions.col;		
		
		initialise();
	}

	public BlockMatrix(BlockMatrix block_matrix, Position divisions) {
		this.basis = block_matrix.basis;
		this.position = block_matrix.position;
		
		this.rows_in_blocks = block_matrix.rows_in_blocks;
		this.cols_in_blocks = block_matrix.cols_in_blocks;
		
		this.block_rows = divisions.row;
		this.block_cols = divisions.col;
		
		matrix = new BigRational[block_rows][block_cols][][];
		
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
		
		matrix = new BigRational[block_rows][block_cols][][];
		
		for (int i = 0; i <  block_rows; i++) {
			for (int j = 0; j <  block_cols; j++) {
				matrix[i][j] = new BigRational[rows_in_blocks][cols_in_blocks];
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
		
		matrix[block_row][block_col][row_in_block][col_in_block] = value.copy(); //TODO copy?
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
		
		return matrix[block_row][block_col][row_in_block][col_in_block]; 
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
		
		int block_starting_col = 0;
		int block_starting_row = 0;
		
		for (int i = 0; i < block_rows; i++) {  		
			block_starting_row = i * rows_in_blocks;
            for (int j = 0; j < block_cols; j++) {
            	block_starting_col = j * cols_in_blocks;
                block_multiply(matrix[i][j], result, position.row + block_starting_row, position.col + block_starting_col);                
            }
		}		
	}		
	
	private void block_multiply(BigRational[][] array, BigRational[] result, int starting_row, int starting_col) throws BTFMatrixErrorException {
		
		int rows = array.length;
		int cols = array[0].length;
		
		if (cols + position.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when multiplying");		

		for (int i = 0; i < rows; i++) {           
           	try {
                result[starting_row + i] = result[starting_row + i].add(multiplyBlockRow(array, i, starting_col));
    		} catch (UndefinedMultiplyException e) {
    			result[starting_row + i] = new BigRational(-1);
            	result[starting_row + i].makeUndefined();    		
            }
		}		
	}

	private BigRational multiplyBlockRow(BigRational[][] array, int index, int starting_col) throws UndefinedMultiplyException {
		
		BigRational result = BigRational.ZERO;
		int cols = array[0].length;
		
		for (int j = 0; j < cols; j++) {
			if (!array[index][j].isZero()) {
				if (basis.getNewValue(j + starting_col).isPositive()) {
					result = result.add((array[index][j].multiply(basis.getNewValue(j + starting_col))));						
				} else if (basis.getNewValue(j + starting_col).isUndefined()) { 
					throw new UndefinedMultiplyException();               	                 
				}   
			}
		}
		return result;
    }
	
	public void solve(BigRational[] rhs) throws BTFMatrixErrorException {
		if (cols + position.col > basis.getSize()) throw new BTFMatrixErrorException("Matrix exceeds end of vector when solving");		
		
		int block_starting_col = 0;
		int block_starting_row = 0;
		
		for (int i = 0; i < block_rows; i++) {  		
			block_starting_row = i * rows_in_blocks;
            for (int j = 0; j < block_cols; j++) {
            	block_starting_col = j * cols_in_blocks;
                block_solve(matrix[i][j], rhs, position.row + block_starting_row, position.col + block_starting_col);                
            }
		}			
	}
	
	private void block_solve(BigRational[][] array, BigRational[] rhs, int starting_row, int starting_col) throws BTFMatrixErrorException {
		
		int rows = array.length;
		int cols = array[0].length;
		
		if (starting_col + cols > basis.getSize()) throw new BTFMatrixErrorException("Incompatible matrix and vector size when multiplying block");			

		for (int i = 0; i < rows; i++) {               
			try {
	        	rhs[starting_row + i] = rhs[starting_row + i].add(multiplyBlockRow(array, i, starting_col).negate());
			} catch (UndefinedMultiplyException e) {
				rhs[starting_row + i] = new BigRational(-1);
	            rhs[starting_row + i].makeUndefined();				
			}
		}		
	}
}
