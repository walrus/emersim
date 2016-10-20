package jmt.jmva.analytical.solvers.Basis.Comparators;

import java.util.Comparator;

import jmt.jmva.analytical.solvers.DataStructures.EnhancedVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;

/**
 * A comparator to sort an array of Enhanced Vectors, to be used by Basis objects.
 * @author Jack Bradshaw
 */
public class SortVectorByhThenRmnz implements Comparator<EnhancedVector>{

	/**
     * Compares two PopulationChangeVector objects.* for the fine grain Block Triangular Form.
     * First vectors are compared at the number of non zero elements (h)
     * Then the position of those elements based on right most nonzero
     * Then the value at those positions, left to right
     * leftmost non-zero is the smaller.
     *
     * @param o The other EnhancedVector object
     * @return -1 if this < o, 0 if this = o, 1 if this > o
     */
    @Override
    public int compare(EnhancedVector v1,EnhancedVector v2) {    	

    	if (v1.size() < v2.size()) {
            return -1;
        } else if (v1.size() > v2.size()) {
            return 1;
        } else { 
        	//Vectors have same length
        	//Check 1: Compare number of non zero elements
        	if (v1.countNonZeroElements() < v2.countNonZeroElements()) {
        		return -1;
        	} else if (v1.countNonZeroElements() > v2.countNonZeroElements()) {
        		return 1;
        	} else {
        		// Vectors have the same number of non zero elements
        		// Check 2: vector with rightmost non-zero value is greater
        		for (int j = v1.size() - 1 ; j >= 0; j--) {
        	         if (v1.get(j) == 0 && v2.get(j) > 0 ) {
        	             return -1; // v2 has the right-most non zero
        	         }
        	         if (v1.get(j) > 0 && v2.get(j) == 0) {
        	             return 1;
        		     }
        		}        		
        		// Vectors have non zero elements in same position
        		// Check 3: Compare values of non zero elements, left to right
        		for ( int j = 0; j < v1.size() - 1; j++) { //sum over first R - 1 positions
            		if (v1.get(j) < v2.get(j) ) return -1;
            		if (v1.get(j) > v2.get(j) ) return 1;
            	}   			
        		return 0;	   		
        	}
        }
    }
}
