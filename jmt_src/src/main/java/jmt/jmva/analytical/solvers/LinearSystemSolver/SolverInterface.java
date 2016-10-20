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

package jmt.jmva.analytical.solvers.LinearSystemSolver;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.Tuple;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.Timer;

import java.util.List;
import java.util.Set;
import javax.naming.OperationNotSupportedException;

/**
 * Interface for classes which define several methods every LinearSystemSolver
 * object must support.
 *
 * @author Michail Makaronidis, 2010
 */
public interface SolverInterface {

    /**
     * Initialises the solver object.
     * @param A The matrix A of the linear system Ax = b
     * @throws OperationNotSupportedException Thrown when the matrix A is not square
     */
    public void initialise(BigRational[][] A) throws OperationNotSupportedException;

    /**
     * Initialises the solver object, taking into account a list of positions of
     * A that may need to be updated when requested. This is needed by the MoM
     * algorithm.
     * @param A The matrix A of the linear system Ax = b
     * @param UList The list containing these positions as Tuple objects
     * @param uncomputables Set of the indices of possible zero-columns of A
     * @throws OperationNotSupportedException Thrown when the matrix A is not square
     */
    public abstract void initialise(BigRational[][] A, List<Tuple<Integer, Integer>> UList, Set<Integer> uncomputables) throws OperationNotSupportedException;

    /**
     * Sets matrices A and cleanA equal to the original A used during
     * initialisation plus "level" increments in the positions needed. See how
     * the MoM algorithm updates A during the iterations.
     * @param level The number of applications with regard to the original A
     */
    public void goToULevel(int level);

    /**
     * Gaussian elimination linear system solver with partial pivoting.
     * @param b The vector b of the linear system Ax = b
     * @return A vector containing the solutions of the linear system
     * @throws OperationNotSupportedException Thrown when the system cannot be solved due to bad vector b size
     * @throws InconsistentLinearSystemException Thrown when the system cannot failed due to singularity.
     * @throws InternalErrorException Thrown when an unrecoverable internal error has been detected by the solver.
     */
    public BigRational[] solve(BigRational[] b) throws OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException;

    /**
     * Returns the Timer object corresponding to this particular Solver.
     * @return The Timer object
     */
    public Timer getTimer();
}
