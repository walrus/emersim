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
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The ModularSolverParallelTask performs the solution of a residual linear system
 * and part of the result recombination process. It is used to acieve
 * parallelisation of the ModularSolver.
 *
 * @author Michail Makaronidis, 2010
 */
public class ModularSolverParallelTask implements Callable<Object> {

    private BigInteger mod, M, factor;
    private BigRational detRes;
    private BigRational[] b, bmod, yRes;
    private BigRational[][] /*A,*/ Amod;
    private Set<Integer> rowsToSkip, columnsToSkip;
    private int Nrows, Ncols;

    /**
     * Creates a ModularSolverParallelTask for solving a residual linear system.
     * @param mod The prime modulo corresponding to this system
     * @param M The product of all prime moduli
     */
    public ModularSolverParallelTask(BigInteger mod, BigInteger M) {
        this.mod = mod;
        this.M = M;
        BigInteger Mi = M.divide(mod);
        factor = Mi.modInverse(mod).multiply(Mi); //<Mi(-1)>Ni (modular multiplicative inverse)
    }

    /**
     * Initialises a ModularSolverParallelTask object for solving a residual linear system Ax=b.
     * @param Amod The pre-computed residual matrix A
     * @param b The (initial, non-residual) vector b
     * @param rowsToSkip The rows of the initial matrix A and the vector b that must be skipped
     * @param columnsToSkip The columns of the initial matrix A and the vector b that must be skipped
     */
    public void prepare(BigRational[][] Amod, BigRational[] b, Set<Integer> rowsToSkip, Set<Integer> columnsToSkip) throws InconsistentLinearSystemException {
        //this.N = Amod.length;
        this.Nrows = Amod.length;
        this.Ncols = Amod[0].length;
        this.Amod = new BigRational[Nrows][Ncols];
        MiscFunctions.arrayCopy(Amod, this.Amod);
        //this.Amod = mod(A, mod);
        this.b = b;
        //this.bmod = mod(b, mod);
        if (rowsToSkip.size() > columnsToSkip.size()) {
            //System.out.println("Must skip less number of rows than colums!");
            throw new InconsistentLinearSystemException("Must skip less number of rows than colums!");
            //System.exit(0);
        }
        this.rowsToSkip = new HashSet<Integer>(rowsToSkip);
        this.columnsToSkip = new HashSet<Integer>(columnsToSkip);
        //precomputedModArrays = false;
    }

    /**
     * Solves the residual system using Extended Gaussian Elimination.
     * @throws InternalErrorException Thrown when an unrecoverable internal error is made.
     * @throws InconsistentLinearSystemException Thrown when the solution cannot proceed due to linear system singularities.
     */
    public void run() throws InternalErrorException, InconsistentLinearSystemException {
        //try {
            // Amod has been already computed
            this.bmod = mod(b, mod);
            detRes = BigRational.ONE;
            yRes = new BigRational[Ncols];

            // Gaussian Elimination
            for (int k = 0; k < Ncols; k++) {
                // Find the largest value row
                int max = k;
                for (int j = k + 1; j < Nrows; j++) {
                    if ((Amod[j][k].abs().greaterThan(Amod[max][k].abs()))) {
                        max = j;
                    }
                }
                if (max != k) {
                    // Swap the largest row with the ith row
                    BigRational temp = bmod[k];
                    bmod[k] = bmod[max];
                    bmod[max] = temp;
                    BigRational[] temp2 = Amod[k];
                    Amod[k] = Amod[max];
                    Amod[max] = temp2;
                }

                //if (Nrows - k < 15) {
                    // do operations serially here
                    for (int i = k + 1; i < Nrows; i++) {
                        // Make row operation
                        BigRational f = /*(Amod[k][k].isZero()) ? Amod[i][k] :*/ Amod[i][k].divide(Amod[k][k]);
                        bmod[i] = bmod[i].subtract(f.multiply(bmod[k]));
                        for (int j = k; j < Ncols; j++) {
                            if (!Amod[k][j].isZero()) {
                                Amod[i][j] = Amod[i][j].subtract(f.multiply(Amod[k][j]));
                            }
                        }
                    }
                /*} else {
                    // do operations in parallel

                    int startFrom = k + 1;
                    for (int r = 0; r < rowAppliers.size(); r++) {
                        int endAt = startFrom + (Nrows - k - 1) / rowAppliers.size() + 1;
                        endAt = (endAt >= Nrows) ? Nrows : endAt;
                        rowAppliers.get(r).prepare(Amod, bmod, k, startFrom, endAt, Ncols);
                        startFrom = endAt;
                    }
                    es.invokeAll(rowAppliers);
                }*/
                detRes = detRes.multiply(Amod[k][k]);
            }
            //detRes = detRes.multiply(Amod[Ncols - 1][Ncols - 1]); // Because the loop above omits the last element
            if (detRes.isZero()) {
                throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
            }
            if (detRes.isNegative()) {
                detRes = detRes.negate();
            }
            // Now the detRes=detRes has been computed
            backSubstitution:
            for (int i = Ncols - 1; i >= 0; i--) {
                BigRational sum = BigRational.ZERO;
                for (int j = i + 1; j < Ncols; j++) {
                    if (!Amod[i][j].isZero()) {
                        sum = sum.add(yRes[j].multiply(Amod[i][j]));
                    }
                }
                yRes[i] = bmod[i].subtract(sum).divide(Amod[i][i]);
            }

            for (int i = 0; i < Ncols; i++) {
                yRes[i] = yRes[i].multiply(detRes);
            }

            // Local Reconstruction
            detRes = detRes.multiply(factor);
            for (int i = 0; i < Ncols; i++) {
                yRes[i] = yRes[i].multiply(factor);
            }
        /*} catch (InterruptedException ex) {
            Logger.getLogger(ModularSolverParallelTask.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private BigRational[] mod(BigRational[] b, BigInteger mod) throws InternalErrorException {
        BigRational[] newb = new BigRational[Nrows];
        int indexI = 0;
        for (int i = 0; i < b.length; i++) {
            if (!rowsToSkip.contains(i)) {
                newb[indexI] = b[i].mod(mod);
                indexI++;

                if (b[i].isUndefined()) {
                    throw  new InternalErrorException("Uncomputable element " + i + " in b vector! This must never occur here: deal with it in ModularSolver.");
                }
            }
        }
        return newb;
    }

    /**
     * Returns the value of the residual determinant.
     * @return The value of the residual determinant as a BigRational
     */
    public BigRational getDetRes() {
        return detRes;
    }

    /**
     * The array of the semi-combined residual solutions.
     * @return An array of BigRational object containing such solutions
     */
    public BigRational[] getyRes() {
        return yRes;
    }

    /**
     * This method implements the call() method of the Callable object by invoking
     * the run() method.
     * @return An always null object
     * @throws Exception When any errors are encountered during the run() method.
     */
    @Override
    public Object call() throws Exception {
        this.run();
        return null;
    }
}
