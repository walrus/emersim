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

import java.util.ArrayList;
import java.util.Stack;
import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

/**
 * This class extends the ArrayList<Integer> object class by adding several useful
 * methods for operations encountered when dealing with the algorithms that
 * compute the normalising constants.
 *
 * @author Michail Makaronidis, 2010, 
 * @author amended by Jack Bradshaw 2012.
 * 
 * Amendments: No longer implements comparable, to allow subclasses to do so
 */
public class EnhancedVector extends ArrayList<Integer> implements Comparable<EnhancedVector> {

    //TODO: More efficient implementation of several functions here. Special properties (i.e. zeroVector, containsMinusOne, etc) can be produced during every access, and not be the result of element-by-element search.

    // This stack holds the "positions" of the elenents that have been altered.
    private Stack<Integer> positions = new Stack<Integer>();
    // This stack holds the "deltas" of alterations corresponding to each position.
    private Stack<Integer> deltas = new Stack<Integer>();

    /**
     * Creates an empty EnhancedVector object.
     */
    public EnhancedVector() {
        super();
    }

    /**
     * Creates an EnhancedVector with content equal to the given matrix.
     *
     * @param A The matrix containing the vector elements
     */
    public EnhancedVector(Integer[] A) {
        super();
        for (Integer el : A) {
            this.add(el);
        }
    }
    
    /**
     * Creates an EnhancedVector with content equal to the given matrix.
     *
     * @param A The matrix containing the vector elements
     */
    public EnhancedVector(int[] A) {
        super();
        for (Integer el : A) {
            this.add(el);
        }
    }

    /**
     * Creates a new EnhancedVector of specific lenth, where
     * all elements are equal to a specific value.
     *
     * @param k The value of all elements
     * @param length The length of the EnhancedVector
     */
    public EnhancedVector(int k, int length) {
        this.clear();
        for (int i = 0; i < length; i++) {
            this.add(k);
        }
    }

    /**
     * This method decides whether all elements of the EnhancedVector are equal
     * to zero (hence, a zero vector).
     * @return True if it is a zero vector, false otherwise
     */
    public boolean isZeroVector() {
        for (Integer el : this) {
            if (el != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a value of -1 is contained in the EnhancedVector.
     * @return True if -1 is contained, else false.
     */
    public boolean containsMinusOne() {
        return this.contains(-1);
    }

    /**
     * This method returns the index of the first non-zero EnhancedVector 
     * element. Throws exception when used on a zero vector.
     *
     * @return The index of the first non-zero EnhancedVector element
     * @throws InternalErrorException Thrown when used on a zero vector
     */
    public int findFirstNonZeroElement() throws InternalErrorException {
        int i = 0;
        for (Integer k : this) {
            if (k > 0) {
                return i;
            } else if (k < 0) {
                return i;
            }
            i++;
        }
        throw new InternalErrorException("Internal Error: Called findFirstNonZeroElement() on a zero vector!");
    }

    /**
     * Returns the number in a specific index of the EnhancedVector as a BigRational object.
     * @param index The position's index
     * @return The number in that position as a BigRational
     */
    public BigRational getAsBigRational(int index) {
        return new BigRational(this.get(index));
    }

    /**
     * This method returns the sum of all EnhancedVector elements.
     *
     * @return The sum
     */
    public int sum() {
        int sum = 0;
        for (Integer el : this) {
            sum += el;
        }
        return sum;
    }

    /**
     * This method alters the element at a position by adding a specific delta.
     * @param i The index of the position (starts from 0)
     * @param delta The delta added
     */
    private void alterElementAt(int i, Integer delta) {
        int prevValue = this.get(i);
        int newValue = prevValue + delta;
        this.set(i, newValue);
    }

    /**
     * This method dereases the element at a position by 1.
     * @param s The index of the position (starts from 1)
     */
    public void minusOne(int s) {
        if (s != 0) {
            this.alterElementAt(s - 1, -1);
        }
        positions.push(s);
        deltas.push(-1);
    }

    /**
     * This method increases the element at a position by 1.
     * @param k The index of the position (starts from 1)
     */
    public void plusOne(int k) {
        if (k != 0) {
            this.alterElementAt(k - 1, 1);
        }
        positions.push(k);
        deltas.push(+1);
    }

    /**
     * This method restores the last alteration made to the EnhancedVector. If
     * no alteration has been made, the method terminates silently.
     */
    public void restore() {
        if (!positions.empty()) {
            int lastPos = positions.pop();
            int lastDelta = deltas.pop();
            if (lastPos != 0) {
                this.alterElementAt(lastPos - 1, -lastDelta);
            }
        }
    }

    /**
     * This method is used to count the elements of the EnhancedVvector that are 
     * equal to zero.
     *
     * @return The number of zero elements
     */
    public int countZeroElements() {
        int count = 0;
        for (Integer el : this) {
            if (el == 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * This method copies the current EnhancedVector to a new one. Position and 
     * delta stacks are disregarded.
     *
     * @param destination The EnhancedVector to which the current one is copied
     */
    protected void copyTo(EnhancedVector destination) {
        destination.clear();
        for (Integer el : this) {
            destination.add(el);
        }

        //TODO: Copy stack as well?
    }

    /**
     * This method copies a source EnhancedVector to the current one. Position
     * and delta stacks are disregarded.
     *
     * @param source The EnhancedVector from which the elements are copied
     */
    protected void fillFromMyVector(EnhancedVector source) {
        this.clear();
        for (Integer el : source) {
            this.add(el);
        }
    }

    /**
     * This method returns the maximum element of an EnhancedVector
     *
     * @return The maximum element
     * @throws OperationNotSupportedException Thrown when evaluation of the maximum element is requested on an empty vector
     */
    public int max() throws OperationNotSupportedException {
        int max;
        if (this.size() > 0) {
            max = this.get(0);
            for (int el : this) {
                if (el > max) {
                    max = el;
                }
            }
        } else {
            throw new OperationNotSupportedException("Cannot define max for an empty vector!");
        }
        return max;
    }

    /**
     * This method returns the minimum element of an EnhancedVector
     *
     * @return The minimum element
     * @throws OperationNotSupportedException Thrown when evaluation of the minimum element is requested on an empty vector
     */
    public int min() throws OperationNotSupportedException {
        int min;
        if (this.size() > 0) {
            min = this.get(0);
            for (int el : this) {
                if (el < min) {
                    min = el;
                }
            }
        } else {
            throw new OperationNotSupportedException("Cannot define min for an empty vector!");
        }
        return min;
    }

    /**
     * This method is like the equals() method, with the only diferrence that it
     * only check the elements of the EnhancedVectors and does not consider the
     * previous alterations held in the two stacks.
     *
     * @param o The object to which
     * @return
     */
    public synchronized boolean equalsIgnoreHistory(Object o) {
        return this.toString().equals(o.toString());
    }

    /**
     * Compares two EnhancedVector objects according to the sum of their elements.
     * elements. If they have the same sum, then the one with the
     * leftmost non-zero is the smaller.
     *
     * @param o The other EnhancedVector object
     * @return -1 if this < o, 0 if this = o, 1 if this > o
     */
    @Override
    public int compareTo(EnhancedVector o) {
        EnhancedVector v1 = this;
        EnhancedVector v2 = o;

        if (v1.size() < v2.size()) {
            return 1;
        } else if (v1.size() > v2.size()) {
            return -1;
        } else {
            // Vectors have the same length
            // Check 1: Lower sum ==> Greater vector
            if (v1.sum() < v2.sum()) {
                return -1;
            } else if (v1.sum() > v2.sum()) {
                return 1;
            } else {
                // Vectors have same length and same sum
                // Check 2: Greater leftmost element ==> Greater vector
                for (int i = 0; i < v1.size(); i++) {
                    if (v1.get(i) > v2.get(i)) {
                        return -1;
                    } else if  (v1.get(i)< v2.get(i)) {
                        return 1;
                    }
                }
                // If control reaches this point, then the vector are the same
                return 0;
            }
        }
    }

    /**
     * This method returns a copy of the current EnhancedVector object. Position
     * and delta stacks are disregarded.
     *
     * @return Copy of the initial EnhancedVector object.
     */
    public EnhancedVector copy() {
        EnhancedVector c = new EnhancedVector();
        this.copyTo(c);
        return c;
    }

    /**
     * Adds two vectors. The vectors must be of the same size.
     * @param b Vector to add
     * @return The result of the addition
     */
    public EnhancedVector addVec(EnhancedVector b) {
        EnhancedVector a = this.copy();
        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) {
                a.set(i, a.get(i) + b.get(i));
            }
            return a;
        } else {
            throw new UnsupportedOperationException("Cannot add vectors of different size.");
        }
    }

    /**
     * @author Jack Bradshaw
     * Subtracts two vectors. The vectors must be of the same size.
     * @param b Vector to add
     * @return The result of the subtraction
     */
    public EnhancedVector subVec(EnhancedVector b) {
        EnhancedVector a = this.copy();
        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) {
                a.set(i, a.get(i) - b.get(i));
            }
            return a;
        } else {
            throw new UnsupportedOperationException("Cannot add vectors of different size.");
        }
    }
    /**
     * @author Jack Bradshaw
     * Prints the vector.
     */
    public void print() {
    	String newLine = System.getProperty("line.separator");
    	System.out.print("[");
    	for (int i = 0; i < this.size(); i++) {
    		System.out.print(this.get(i) + " ");    	 	
    	}
    	System.out.print("]" + newLine);
    }

	 /**
	 * @author Jack Bradshaw
     * This method is used to count the elements of the EnhancedVvector that are 
     * greater than zero.
     *
     * @return The number of non-zero elements
     */
    public int countNonZeroElements() {
        int count = 0;
        for (Integer el : this) {
            if (el > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * @author Haixiao Su
     * This method is used to decide whether the vector contains all ones.
     *
     * @return whether the vector contains all ones.
     */
    public boolean isOneVector() {
        for (Integer i : this) {
            if (i != 1) {
                return false;
            }
        }
        return true;
    }

}
