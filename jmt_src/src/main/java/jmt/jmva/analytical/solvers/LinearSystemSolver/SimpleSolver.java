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
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

import javax.naming.OperationNotSupportedException;

/**
 * Class which defines a SimpleSolver object, which solves the
 * system using Gaussian elimination with partial pivoting.
 *
 * @author Michail Makaronidis, 2010
 */
public class SimpleSolver extends Solver {

    /**
     * Constructs a SimpleSolver object.
     */
    public SimpleSolver() {
        super();
        System.out.println("Using simple solver");
    }

    /**
     * Solves the linear system using Gaussian elimination with partial pivoting
     * @param b The vector b of the linear system Ax = b
     * @return A vector containing the solutions of the linear system
     * @throws OperationNotSupportedException Thrown when the system cannot be solved due to bad vector b size
     */
    @Override
    public BigRational[] solve(BigRational[] b) throws OperationNotSupportedException, InconsistentLinearSystemException {
        t.start();
        boolean existsDefinedOrNonZero = false;
        /*System.out.println("Matrix A:");
        MiscFunctions.printMatrix(A);
        System.out.println("Matrix b:");
        MiscFunctions.printMatrix(b);*/

        if (N != b.length) {
            throw new OperationNotSupportedException("Wrong size of vector b.");
        }
        if (N > 100) {
            System.out.print("Solving LinSys.");
        }

        for (int i = 0; i < N; i++) {
            if (!b[i].isUndefined() && !b[i].isZero()) {
                existsDefinedOrNonZero = true;
                break;
            }
        }

        if (!existsDefinedOrNonZero) {
            throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
        }
        int swapCount = 0;
        for (int p = 0; p < N; p++) {
            // find pivot row and swap
            int max = p;
            for (int i = p + 1; i < N; i++) {
                //if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                if (A[i][p].abs().greaterThan(A[max][p].abs())) {
                    max = i;
                }
            }
            if (max != p) {
                swapCount++;
            }
            BigRational[] temp = A[p];
            A[p] = A[max];
            A[max] = temp;
            BigRational tmp = b[p];
            b[p] = b[max];
            b[max] = tmp;

            // singular or nearly singular
            //if (Math.abs(A[p][p]) <= EPSILON) {
            BigRational element;
            if (A[p][p].abs().isZero()) {
                //MiscFunctions.printMatrix(A);
                //throw new RuntimeException("Matrix is singular");
                element = BigRational.ONE;
            } else {
                element = A[p][p];
            }

            // pivot within A and b
            for (int i = p + 1; i < N; i++) {
                if (!b[i].isUndefined()) {
                    b[i] = b[i].subtract(A[i][p].divide(element).multiply(b[p]));
                }
                for (int j = p + 1; j < N; j++) {
                    A[i][j] = A[i][j].subtract(A[i][p].divide(element).multiply(A[p][j]));
                }
            }
        }

        // back substitution
        BigRational[] x = new BigRational[N];
        for (int i = N - 1; i >= 0; i--) {
            BigRational sum = BigRational.ZERO;
            for (int j = i + 1; j < N; j++) {
                if (!A[i][j].isZero()) {
                    if (!x[j].isUndefined()) {
                        sum = sum.add(A[i][j].multiply(x[j]));
                    } else {
                        // x[i] must become undefined in this case
                        x[i] = new BigRational(-1);
                        x[i].makeUndefined();
                        // We do not need to consider further A[i][j]s
                        break;
                    }
                }
            }
            // This is to skip current loop when we have already decided that x[i] should be undefined.
            if (x[i] != null) {
                continue;
            }
            if (!A[i][i].isZero() && (!b[i].isUndefined())) {
                x[i] = (b[i].subtract(sum)).divide(A[i][i]);
            } else {
                //x[i] = BigRational.ZERO;
                x[i] = new BigRational(-1);
                x[i].makeUndefined();
            }
        }

        BigRational det = BigRational.ONE;
        for (int i = 0; i < N; i++) {
            if (!x[i].isUndefined()) {
                det = det.multiply(A[i][i]);
            }
        }
        if (swapCount % 2 == 1) {
            det = det.negate();
        }

        if (N > 100) {
            System.out.print(".");
        }

        // Restore original matrix
        MiscFunctions.arrayCopy(cleanA, A);

        if (N > 100) {
            System.out.println(".OK!");
        }

        //System.out.println("Solution: (det=" + det + ")");
        //MiscFunctions.printMatrix(x);
        t.pause();
        return x;
    }
}
