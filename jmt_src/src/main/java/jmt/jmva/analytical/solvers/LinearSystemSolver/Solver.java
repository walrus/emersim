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
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;
import jmt.jmva.analytical.solvers.Utilities.Timer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.OperationNotSupportedException;

/**
 * Class which defines several methods every Solver object must
 * support.
 *
 * @author Michail Makaronidis, 2010
 */
public class Solver implements SolverInterface {

    /**
     * The Timer object corresponding to this Solver is responsible for storing
     * and providing the total runtime.
     */
    protected Timer t = new Timer();
    /**
     * The matrix A of the linear system Ax = b.
     */
    /**
     * Holds the initial version of A, as calling solve(..) destroys
     * A's contents (the calculations are performed in-place).
     */
    protected BigRational[][] A, cleanA;
    /**
     * The vector b of the linear system Ax = b.
     */
    protected BigRational[] b;
    /**
     * Represents how many times the elements in the positions list UList have
     * been increased by 1.
     */
    protected int curULevel;
    /**
     * The positions of elements of A that may need to be increased between
     * successive invocations of solve(..). This is a requirement of the MoM
     * algorithm, which was embedded in the Solver for performance reasons.
     */
    protected List<Tuple<Integer, Integer>> UList;
    /**
     * The set of indices of possible zero-columns of A.
     */
    protected Set<Integer> uncomputables;
    /**
     * The number of unknowns.
     */
    protected int N;
    /**
     * It is used to determine wheter the matrix A is singular.
     */
    protected static final BigRational EPSILON = new BigRational(1e10).reciprocal();

    /**
     * Constructs a Solver object.
     */
    public Solver() {
        super();
    }

    /**
     * Initialises the solver object.
     * @param A The matrix A of the linear system Ax = b
     * @throws OperationNotSupportedException Thrown when the matrix A is not square
     */
    @Override
    public void initialise(BigRational[][] A) throws OperationNotSupportedException {
        t.start();
        List<Tuple<Integer, Integer>> l = new ArrayList<Tuple<Integer, Integer>>();
        Set<Integer> emptyUncomputables = new HashSet<Integer>();
        t.pause();
        this.initialise(A, l, emptyUncomputables);
    }

    /**
     * Initialises the solver object, taking into account a list of positions of
     * A that may need to be updated when requested. This is needed by the MoM 
     * algorithm. 
     * @param A The matrix A of the linear system Ax = b
     * @param UList The list containing these positions as Tuple objects
     * @throws OperationNotSupportedException Thrown when the matrix A is not square
     */
    @Override
    public void initialise(BigRational[][] A, List<Tuple<Integer, Integer>> UList, Set<Integer> uncomputables) throws OperationNotSupportedException {
        t.start();
        this.A = A;
        N = A.length;
        if (A[0].length != N) {
            throw new OperationNotSupportedException("Matrix A of linear system is not square.");
        }
        this.UList = UList;
        this.uncomputables = uncomputables;
        curULevel = 0;

        cleanA = new BigRational[N][N];
        MiscFunctions.arrayCopy(A, cleanA);
        t.pause();
    }

    /**
     * Initialises the solver object, taking into account a list of positions of
     * A that may need to be updated when requested. This is needed by the MoM
     * algorithm. Furthermore, some bounds of the maximum elements contained
     * in the linear systems (max A, max B, max Solution = max G) are given.
     * @param A The matrix A of the linear system Ax = b
     * @param UList The list containing these positions as Tuple objects
     * @param uncomputables The set of indices of possible zero-columns of A
     * @param maxA The maximum possible element in matrix A
     * @param maxb The maximum possible element of vector b
     * @param maxG The maximum possible legal solution value
     * @throws OperationNotSupportedException Thrown when the matrix A is not square
     * @throws InternalErrorException Thrown when any unrecoverable internal error is encountered during initialisation
     */
    public void initialise(BigRational[][] A, List<Tuple<Integer, Integer>> UList, Set<Integer> uncomputables, int maxA, BigInteger maxb, BigRational maxG) throws OperationNotSupportedException, InternalErrorException {
        initialise(A, UList, uncomputables);
    }

    /**
     * Sets matrices A and cleanA equal to the original A used during
     * initialisation plus "level" increments in the positions needed. See how
     * the MoM algorithm updates A during the iterations.
     * @param level The number of applications with regard to the original A
     */
    @Override
    public void goToULevel(int level) {
        int i, j;
        BigRational delta = new BigRational(level - curULevel);

        if (!delta.isZero()) {
            for (Tuple<Integer, Integer> tuple : UList) {
                i = tuple.getX();
                j = tuple.getY();
                A[i][j] = A[i][j].add(delta);
                cleanA[i][j] = A[i][j];//.copy(); // Copy seems to be unnecessary
            }
            curULevel = level;
        }
    }

    /**
     * Solves the linear system.
     * @param b The vector b of the linear system Ax = b
     * @return A vector containing the solutions of the linear system
     * @throws OperationNotSupportedException Thrown always, as this Solver does not support actual solution of the system
     * @throws InconsistentLinearSystemException Thrown when the algorithm should return an undeterminable, from the initial linear system, value.
     * @throws InternalErrorException Thrown when an unrecoverable internal error has been detected by the solver.
     */
    @Override
    public BigRational[] solve(BigRational[] b) throws OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {
        throw new OperationNotSupportedException();
    }

    /**
     * Returns the Timer object corresponding to this particular Solver.
     * @return The Timer object
     */
    @Override
    public Timer getTimer() {
        return t;
    }

    /**
     * Shutsdown the solver, terminating all pools of threads and child processes
     * if any exist.
     */
    public void shutdown() {
    }
}
