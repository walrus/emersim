package jmt.jmva.analytical.solvers.Basis.Comparators;

import java.util.Comparator;

import jmt.jmva.analytical.solvers.DataStructures.EnhancedVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;

/**
 * A comparator to sort an array of Enhanced Vectors, to be used by Basis objects.
 * Sorts in the order used in the MatLab implementation.
 * @author Jack Bradshaw
 */
public class SortVectorMatLab implements Comparator<EnhancedVector> {
	
	/**
     * Compares two PopulationChangeVector objects according to the same order as in 
     * the MatLab implementation
     * First vectors are compared at the number of non zero elements (h)
     * Then the position of those elements
     * Then the value at those positions, left to right
     * leftmost non-zero is the smaller.
     *
     * @param o The other EnhancedVector object
     * @return -1 if this < o, 0 if this = o, 1 if this > o
     */
    @Override
    public int compare(EnhancedVector v1, EnhancedVector v2) {   

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
        		// Check 2: Compare non zero element positions by leftmost non zero
        		for (int j = 0; j < v1.size(); j++) {
        	         if (v1.get(j) == 0 && v2.get(j) > 0 ) {
        	             return 1; // v1 has the left-most zero
        	         }
        	         if (v1.get(j) > 0 && v2.get(j) == 0) {
        	             return -1;
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
