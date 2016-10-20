package jmt.jmva.analytical.solvers.LinearSystem.Simple.Matrix;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class provides a concrete implementation of the Matrix class that
 * is based in a two dimensional BigRational array.
 * 
 *  The Gaussian Elimination Solver is sued to solve this system.
 * 
 * @author Jack Bradshaw
 */
public class StandardMatrix extends Matrix {

	private BigRational[][] array;
	
	public StandardMatrix( CoMoMBasis basis, int size) {
		super(basis, size);
		array = new BigRational[size][size];		
	}
	
	@Override
	 public BigRational[][] getArray() {
		return array;
	 }
	
	@Override
	public void write(int row, int col, BigRational v) {
		array[row][col] = v.copy();
		//TODO do you need to copy?
	}

	@Override
	public BigRational get(int row, int col) {
		return array[row][col];
	}	
	
	@Override
	public BigRational[] multiply() {
	
		BigRational[][] A = array;
        int rowsA = A.length;
        int columnsA = A[0].length;
        int rowsV = basis.getSize();

        if (columnsA == rowsV) {
            BigRational[] c = new BigRational[rowsA];
            for (int i = 0; i < rowsA; i++) {
                c[i] = BigRational.ZERO;
                for (int j = 0; j < columnsA; j++) {
                    if (!A[i][j].isZero()) {
                        if (basis.getOldValue(j).isPositive()) {
                            c[i] = c[i].add(A[i][j].multiply(basis.getOldValue(j)));
                        } else if (basis.getOldValue(j).isUndefined()) {
                            c[i] = new BigRational(-1);
                            c[i].makeUndefined();
                            break;
                        }
                    }
                }
            }
            return c;
        } else {
            throw new ArithmeticException("Cannot multiply matrices with wrong sizes! (" + rowsA + "x" + columnsA + ")x(" + rowsV + "x1)");
        }
    }
	 /**
	  * Makes every entry in the array 0
	  */
	 protected void fillWithZeros() {
		 for (int i = 0; i < size; i++) {
	    	 for (int j = 0; j < size; j++) {
	    		 array[i][j] = BigRational.ZERO; 	    		
	    	 }	    		   
	     }
	 }
	
	@Override
	public void print() {
		MiscFunctions.dotprintMatrix(array);
	}

	
	
	/**
	 * Adapted from code found at http://professorjava.weebly.com/matrix-determinant.html
	 * For testing purposes.
	 * @param matrix
	 * @return the determinant of the matrix
	 */
	private BigRational determinant(BigRational[][] matrix) { //method sig. takes a matrix (two dimensional array), returns determinant.
	    BigRational sum = BigRational.ZERO; 
	    BigRational s;
	    if (matrix.length==1) {  //bottom case of recursion. size 1 matrix determinant is itself.
	        return(matrix[0][0]);
	    }
	    for (int i=0;i<matrix.length;i++) { //finds determinant using row-by-row expansion
	    BigRational[][]smaller= new BigRational[matrix.length-1][matrix.length-1]; //creates smaller matrix- values not in same row, column
	    	for (int a=1;a<matrix.length;a++) {
	    		for (int b=0;b<matrix.length;b++) {
	    			if (b<i) {
	    				smaller[a-1][b]=matrix[a][b];
	    			}
	    			else if (b>i) {
	    				smaller[a-1][b-1]=matrix[a][b];
	    			}
	    		}
	    	}
	    	if (i%2==0) { //sign changes based on i
	    		s = BigRational.ONE;
	    	}
	    	else {
	    		s= BigRational.ONE.negate();
	    	}
	    	sum = sum.add( s.multiply(matrix[0][i].multiply((determinant(smaller))))); // recursive step: determinant of larger determined by smaller.
	    	}
	    return(sum); //returns determinant value. once stack is finished, returns final determinant.
	}
	
	public  BigRational getDeterminatnt() {
		return determinant(array);
	}

}
