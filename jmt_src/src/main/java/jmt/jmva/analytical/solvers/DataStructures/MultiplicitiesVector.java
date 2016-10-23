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

import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

/**
 * This class implements the MultiplicitiesVector object, which is used to
 * store a multiplicities vector. It extends an EnhancedVector object.
 *
 * @author Michail Makaronidis, 2010
 */
public class MultiplicitiesVector extends EnhancedVector {

    /**
     * Creates an empty MultiplicitiesVector object.
     */
    public MultiplicitiesVector() {
        super();
    }

    /**
     * Creates an MultiplicitiesVector with content equal to the given matrix.
     *
     * @param M The matrix containing the vector elements
     */
    public MultiplicitiesVector(Integer[] M) {
        super(M);
    }

    /**
     * Creates a new MultiplicitiesVector of specific lenth, where
     * all elements are equal to a specific value.
     *
     * @param k The value of all elements
     * @param length The length of the MultiplicitiesVector
     */
    public MultiplicitiesVector(int k, int length) {
        super(k, length);
    }

    /**
     * This method returns a copy of the current MultiplicitiesVector object. Position
     * and delta stacks are disregarded.
     *
     * @return Copy of the initial MultiplicitiesVector object.
     */
    @Override
    public MultiplicitiesVector copy() {
        MultiplicitiesVector c = new MultiplicitiesVector();
        this.copyTo(c);
        return c;
    }

    @Override
    public MultiplicitiesVector addVec(EnhancedVector b) {
        return (MultiplicitiesVector) super.addVec(b);
    }
    
    
    /**
     * Added by Jack Bradshaw.
     * 
     * @return The position of the entry that contains at 1
     * @throws InternalErrorException if more or less than one queue added. 
     */
    public int whichSingleQueueAdded() throws InternalErrorException {
    	int queue_added = 0;
    	for (int i = 0; i < size(); i++) {
    		if (get(i)  != 0 ) {
    			if (queue_added > 0 || get(i) != 1) {
    				throw new InternalErrorException("Internal Error: Called whichSingleQueueAdded() on a vector with more than one non-zero element");
    			} else {
    				queue_added = i + 1;
    			}
    		}
        }        
    	return queue_added;
    }

    /**
     * Added by Haixiao Su
     *
     * @return The position of the element that is not 1 if that's the only element that is not 1, otherwise return -1.
     */
    public int isNearlyAllOneElements() {
        int count = 0;
        int index = -1;
        for (int i = 0; i < size(); i++) {
            if (get(i) == 1) {
                count++;
            } else {
                index = i;
            }
        }
        if (count == size() -1) {
            return index;
        }
        return -1;
    }
}
