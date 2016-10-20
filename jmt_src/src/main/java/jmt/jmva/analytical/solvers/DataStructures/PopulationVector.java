/**
 * Copyright (C) 2010, Michail Makaronidis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmt.jmva.analytical.solvers.DataStructures;

/**
 * This class implements the PopulationVector object, which is used to
 * store a populations vector. It extends an EnhancedVector object.
 *
 * @author Michail Makaronidis, 2010
 */
public class PopulationVector extends EnhancedVector {

    /**
     * Creates an empty PopulationVector object.
     */
    public PopulationVector() {
        super();
    }

    /**
     * Creates an PopulationVector with content equal to the given matrix.
     *
     * @param P The matrix containing the vector elements
     */
    public PopulationVector(Integer[] P) {
        super(P);
    }
    
    /**
     * Creates an PopulationVector with content equal to the given matrix.
     *
     * @param P The matrix containing the vector elements
     */
    public PopulationVector(int[] P) {
        super(P);
    }

    /**
     * Creates a new PopulationVector of specific lenth, where
     * all elements are equal to a specific value.
     *
     * @param k The value of all elements
     * @param length The length of the PopulationVector
     */
    public PopulationVector(int k, int length) {
        super(k, length);
    }

    /**
     * This method returns a copy of the current PopulationVector object. Position
     * and delta stacks are disregarded.
     *
     * @return Copy of the initial PopulationVector object.
     */
    @Override
    public PopulationVector copy() {
        PopulationVector c = new PopulationVector();
        this.copyTo(c);
        return c;
    }
    @Override
    public PopulationVector addVec(EnhancedVector b) {
        return (PopulationVector) super.addVec(b);
    }
    
    @Override
    public PopulationVector subVec(EnhancedVector b) {
        return (PopulationVector) super.subVec(b);
    }
    
    
    /**
     * Changes the population by subtracting the supplied Population Change Vector
     *@author Jack Bradshaw
     * @param n The PopulationChangeVector
     * @return
     */
    public PopulationVector changePopulation(PopulationChangeVector n) {
    	return (PopulationVector) this.subVec(n);
    }
    
    public boolean anyGreaterThan(PopulationVector a) {
    	for (int i = 0; i < this.size(); i++) {
    		if (this.get(i) > a.get(i)) {
    			return true;
    		}
    	}
    	
    	return false;
    }

}
