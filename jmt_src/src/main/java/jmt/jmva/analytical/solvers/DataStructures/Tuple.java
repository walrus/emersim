/**
 * Copyright (C) 2009, 2010, Michail Makaronidis
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
 * The Tuple class provides objects which function just like a 2-tuple.
 *
 * @param <T> The class of the first element of the Tuple
 * @param <U> The class of the second element of the Tuple
 *
 * @author Michail Makaronidis, 2009, 2010
 */
public class Tuple<T, U> {

    /**
     * Tuple elements.
     */
    private T objX;
    private U objY;

    /**
     * Creates a new Tuple object gives its 2 elements.
     *
     * @param argX    The first element
     * @param argY    The second element
     */
    public Tuple(T argX, U argY) {
        this.objX = argX;
        this.objY = argY;
    }

    /**
     * Returns the first element of a Tuple
     *
     * @return The first element
     */
    public T getX() {
        return objX;
    }

    /**
     * Returns the second element of a Tuple
     *
     * @return The second element
     */
    public U getY() {
        return objY;
    }

    /**
     * Sets the first element of a Tuple
     *
     * @param t The first element
     */
    public void setX(T t) {
        objX = t;
    }

    /**
     * Sets the second element of a Tuple
     *
     * @param u The second element
     */
    public void setY(U u) {
        objY = u;
    }

    /**
     * Returns a String object representing the Tuple.
     *
     * @return The representing String object
     */
    @Override
    public String toString() {
        return ("(" + objX.toString() + ", " + objY.toString() + ")");
    }
}
