package jmt.jmva.analytical.solvers.LinearSystem.Simple.Matrix;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.Tuple;

/**
 * An abstract class that provides functionality required of the matrices of the simple linear system.
 * This includes:
 * - the ability to write to positions in the matrix
 * - The ability to multiply by the matrix
 * - A list of positions in the matrix that are to be updated.
 * 
 * @author Jack Bradshaw
 */
public abstract class Matrix {	

		
	/**
	 * List of positions to update
	 */
	 protected List<Tuple<Integer, Integer>> update_list;
	 
	 /**
	  * Size of Square Matrix
	  */
	 protected int size;	
	 
	 /**
	  * The basis of the model under consideration.
	  */
	 protected CoMoMBasis basis;
		
	 /**
	  * Constructor
	  * @param qnm The Queuing Network Model under study
	  * @param size The size of the basis (will make a matrix of size:  size x size)
	  */
	 public Matrix(CoMoMBasis basis, int size) {	
		 this.size = size;
		 this.basis = basis;
		 update_list = new LinkedList<Tuple<Integer, Integer>>();		 
	 }
	 
	 /**
	  * Returns the matrix as a BigRational[][] array
	  * @return underlying matrix
	  */
	 public abstract BigRational[][] getArray();
	 
	 /**
	  * Writes value v at position (row,col) in matrix
	  * @param v value to be written
	  * @param row row to be written at
	  * @param col column two be written at
	  */
	 public abstract void write( int row, int col, BigRational v);
	 
	 /**
	  * Returns value a (row, col)
	  * @param row row
	  * @param col column
	  * @return array[row][column]
	  */
	 public abstract BigRational get( int row, int col);
	 

	 /**
	  * Multiplies the previous basis by the matrix 
	  * @return rhs of linear system
	  */
	 public abstract BigRational[] multiply();
	 
	 /**
	  * Adds (row, col) to the list of of positions to be updated
	  * @param row row 
	  * @param col column
	  */	 
	 public void toBeUpdated(int row, int col) {
		 update_list.add(new Tuple<Integer, Integer>(row,col));
	 }
	 
	 /**
	  * @return the list of positions to be updated
	  */
	 public List<Tuple<Integer,Integer>> getUpdateList() {
		 return update_list;
	 }
	 
	 /**
	  * Reset the data structures for the generation of the linear system for a new class.
	  */
	 public void reset() {
		 fillWithZeros();
		 update_list = new LinkedList<Tuple<Integer, Integer>>();			 
	 }
	 	 
	 /**
	  * Makes every entry in the array 0
	  */
	 protected abstract void fillWithZeros();
	 
	 /**
	  * Prints the matrix to screen
	  */
	 public abstract void print();
}
