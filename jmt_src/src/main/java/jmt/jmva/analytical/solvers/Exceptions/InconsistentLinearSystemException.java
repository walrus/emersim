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

package jmt.jmva.analytical.solvers.Exceptions;

/**
 * This InconsistentLinearSystemException object is thrown when undeterminable
 * values that have been returned from a previous run of the linear system
 * solver need to be used for determining a subsequent run. This means that the
 * MoMSolver has entered a state in which it cannot recover.
 *
 * @author Michail Makaronidis, 2010
 */
public class InconsistentLinearSystemException extends Exception {

    /**
     * Creates an InconsistentLinearSystemException with an exception message.
     *
     * @param string The exception message
     */
    public InconsistentLinearSystemException(String string) {
        super(string);
    }
}
