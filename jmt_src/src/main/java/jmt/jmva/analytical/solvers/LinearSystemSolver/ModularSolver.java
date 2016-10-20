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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.Tuple;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;
import jmt.jmva.analytical.solvers.Utilities.Timer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;

/**
 * Class which defines a ModularSolver object, which solves the
 * linear system using residual systems solved by Gaussian elimination.
 *
 * @author Michail Makaronidis, 2010
 */
public class ModularSolver extends ParallelSolver {

    private int invocation = 0;
    private List<BigInteger> moduli;
    private BigInteger M = BigInteger.ONE, maxA, minModulo;
    private BigRational maxG;
    //private boolean previousInvocationSwapped;
    private Set<Integer> rowsToSkip, columnsToSkip;
    private List<ModularSolverParallelTask> taskList;
    private int ra, rb;
    private Timer moduliSelectionTimer = new Timer();

    /**
     * Constructs a ModularSolver object.
     *
     * @param nThreads Specifies the number of threads that the ModularSolver must use
     */
    public ModularSolver(int nThreads) {
        super(nThreads);
        System.out.println("Using parallel modular solver (" + this.nThreads + " threads)");
    }

    @Override
    public void initialise(BigRational[][] A, List<Tuple<Integer, Integer>> UList, Set<Integer> uncomputables, int maxA, BigInteger maxb, BigRational maxG) throws OperationNotSupportedException, InternalErrorException {
        t.start();
        super.initialise(A, UList, uncomputables);
        //this.previousInvocationSwapped = false;
        this.maxA = new BigInteger((new Integer(maxA).toString()));
        //this.maxOfAColumns = maxOfAColumns;
        this.maxG = maxG;
        int bitlength = lowerLimitForMMorhac(this.maxA, maxb);
        //int bitlength = lowerLimitForMG(this.maxG.asBigDecimal().toBigIntegerExact());
        if (bitlength > M.bitLength()) {
            System.out.println("Selecting moduli. Total bitlength: " + bitlength);
            moduliSelectionTimer.start();
            moduli = new ArrayList<BigInteger>(nThreads);
            M = BigInteger.ONE;
            for (BigInteger m : moduli) {
                M = M.multiply(m);
            }

            //selectModuli(bitlength);
            // Report linear system only
            /*BigInteger m = new BigInteger("775239");
            moduli.add(m);
            M = M.multiply(moduli.get(moduli.size()-1));
            minModulo = m;
            moduli.add(new BigInteger("1942111"));
            M = M.multiply(moduli.get(moduli.size()-1));*/
            selectModuliInParallel(bitlength);
            if (M.bitLength() < bitlength || minModulo.compareTo(this.maxA) <= 0) {
                throw new InternalErrorException("Cannot select moduli");
            }
            moduliSelectionTimer.pause();
            System.out.println("Moduli selection took " + moduliSelectionTimer.getPrettyInterval() + ".\nWill now solve " + moduli.size() + " residue systems.");
        }
        taskList = new ArrayList<ModularSolverParallelTask>(moduli.size());
        for (int i = 0; i < moduli.size(); i++) {
            taskList.add(new ModularSolverParallelTask(moduli.get(i), M));
        }
        t.pause();
    }

    private void selectModuliInParallel(int totalBitLength) throws InternalErrorException {
        try {
            // SUPER SOS EVERY MODULO > MaxA
            int numberOfModuli = nThreads;
            int size = totalBitLength / numberOfModuli + 1;
            while (size > 2500) {
                size = (int) Math.round((double) size / 2);
                numberOfModuli *= 2;
            }
            size = (size < this.maxA.bitLength()) ? (this.maxA.bitLength() + 1) : size;
            size = (size < 2) ? 2 : size;
            List<ModuloSelectionTask> tasks = new ArrayList<ModuloSelectionTask>(numberOfModuli);
            int attempt = 0;
            while (moduli.size() < numberOfModuli && attempt < 5) {
                for (int i = 0; i < numberOfModuli - moduli.size(); i++) {
                    tasks.add(new ModuloSelectionTask((i % 2 == 0) ? size : size + 1));
                }
                List<Future<BigInteger>> results = pool.invokeAll(tasks);
                for (Future<BigInteger> f : results) {
                    BigInteger nextModulo = f.get();
                    moduli.add(nextModulo);
                    M = M.multiply(nextModulo);
                    if (minModulo == null) {
                        minModulo = nextModulo;
                    } else {
                        minModulo = (minModulo.compareTo(nextModulo) < 0) ? minModulo : nextModulo;
                    }
                    //System.out.println("Modulo #" + moduli.size() + ": " /*+ nextModulo*/ + " bitSize = " + nextModulo.bitLength());
                }
                attempt++;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ModularSolver.class.getName()).log(Level.SEVERE, null, ex);
            throw new InternalErrorException("Cannot select moduli");
        } catch (ExecutionException ex) {
            Logger.getLogger(ModularSolver.class.getName()).log(Level.SEVERE, null, ex);
            throw new InternalErrorException("Cannot select moduli");
        }
    }

    private void selectModuli(int totalBitLength) throws InternalErrorException {
        // SUPER SOS EVERY MODULO > MaxA
        do {
            int optimalModuloSize = 200;
            optimalModuloSize = (optimalModuloSize < this.maxA.bitLength()) ? (this.maxA.bitLength() + 1) : optimalModuloSize;
            int size = (totalBitLength - M.bitLength() >= optimalModuloSize) ? optimalModuloSize : totalBitLength - M.bitLength();
            //int size = (moduli.size() < nThreads) ? (totalBitLength - M.bitLength()) / (nThreads - moduli.size()) : (totalBitLength - M.bitLength());

            size = (size < this.maxA.bitLength()) ? (this.maxA.bitLength() + 1) : size;
            size = (size < 2) ? 2 : size;
            //size = (moduli.size() % 2 == 1) ? size + 1 : size;
            boolean repetitionNecessary;
            BigInteger nextModulo;
            do {
                repetitionNecessary = false;
                nextModulo = getPrimeLessThan(size, moduli);

                //check if we must increase the current prime bitlength to produce exactly as many moduli as the number of threads.
                /*if ((totalBitLength - M.bitLength() <= optimalModuloSize) && (M.multiply(nextModulo).bitLength() < totalBitLength)) {
                size++;
                repetitionNecessary = true;
                }*/
            } while (repetitionNecessary);
            M = M.multiply(nextModulo);
            moduli.add(nextModulo);
            if (minModulo == null) {
                minModulo = nextModulo;
            } else {
                minModulo = (minModulo.compareTo(nextModulo) < 0) ? minModulo : nextModulo;
            }
            //System.out.println("Modulo #" + moduli.size() + ": "/* + nextModulo*/ + " bitSize = " + nextModulo.bitLength());
        } while (M.bitLength() < totalBitLength);
    }

    /**
     * Solves the linear system using residual systems solved by Gaussian elimination
     * @param bInput The vector b of the linear system Ax = b
     * @return A vector containing the solutions of the linear system
     * @throws OperationNotSupportedException Thrown when the system cannot be solved due to bad vector b size
     */
    @Override
    public BigRational[] solve(BigRational[] bInput) throws OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException {
        try {
            t.start();
            invocation++;
            b = bInput;

            boolean existsDefinedOrNonZero = false;/*, swappedSomeTwoRows = false, check = false;*/

            // Check if matrix sizes match
            if (N != b.length) {
                throw new OperationNotSupportedException("Wrong size of vector b.");
            }
            // Check if all b vector is "undefined". In such a case the system cannot be solved.
            //rowsToDiscard = new HashSet<Integer>();
            for (int i = 0; i < N; i++) {
                if (!b[i].isUndefined()) {
                    if (!b[i].isZero()) {
                        existsDefinedOrNonZero = true;
                        break;
                    }
                }
            }
            /*System.out.println("Matrix A:");
            MiscFunctions.printMatrix(A);
            System.out.println("Matrix b:");
            MiscFunctions.printMatrix(b);*/

            if (!existsDefinedOrNonZero) {
                throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
            }
            if (N > 100) {
                System.out.print("Solving LinSys.");
            }

            SystemSanitisation();
            //System.out.println("Skipping rows: " + rowsToSkip + "\nSKipping columns: " + columnsToSkip);
            // Compute rows that can be swapped
            for (int i = 0; i < N; i++) {
                if (!rowsToSkip.contains(i)) {
                    ra = i;
                    for (int j = i + 1; j < N; j++) {
                        if (!rowsToSkip.contains(j)) {
                            rb = j;
                            break;
                        }
                    }
                    break;
                }
            }

            BigRational det = BigRational.ZERO;
            BigRational[] x = new BigRational[N];

            // ****** SOLVER STARTS HERE ******
            BigInteger mod = moduli.get(0); // We can use any modulo when computing the common between
            BigRational[][] Amod = mod(A, mod); //all residue systems Amod, as every modulo is bigger than Amax.
            for (int modIndex = 0; modIndex < moduli.size(); modIndex++) {
                taskList.get(modIndex).prepare(Amod, b, rowsToSkip, columnsToSkip);
            }
            pool.invokeAll(taskList);
            for (int modIndex = 0; modIndex < moduli.size(); modIndex++) {
                det = det.add(taskList.get(modIndex).getDetRes());
            }
            det = det.mod(M);

            int indexAtResidueSol = 0;
            for (int i = 0; i < N; i++) {
                x[i] = BigRational.ZERO;
                if (!columnsToSkip.contains(i)) {
                    for (ModularSolverParallelTask task : taskList) {
                        x[i] = x[i].add(task.getyRes()[indexAtResidueSol]);
                    }
                    indexAtResidueSol++;
                } else {
                    x[i] = new BigRational(-1);
                    x[i].makeUndefined();
                }
            }

            for (int i = 0; i < N; i++) {
                if ((!columnsToSkip.contains(i)) && (!x[i].isUndefined())) {
                    x[i] = x[i].modNormal(M);
                    if (!det.isZero()) {
                        x[i] = x[i].divide(det);
                    }
                } else {
                    x[i] = new BigRational(-1);
                    x[i].makeUndefined();
                }
            }
            // ****** SOLVER ENDS HERE ******


            if (N > 100) {
                System.out.print(".");
            }

            // Restore original matrices
            //MiscFunctions.arrayCopy(cleanA, A);

            if (N > 100) {
                System.out.println(".OK!");
            }

            /*System.out.println("Solution: (det = " + det.toString() + ")");*/
            /*MiscFunctions.printMatrix(x);
            System.out.println();*/

            //System.out.println("x[0] = " + x[0]);
            //checkIfAllSolutionsLegal(x);

            t.pause();
            return x;
        } catch (NullPointerException ex) {
            throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
        } catch (InterruptedException ex) {
            throw new InternalErrorException(ex.getMessage());
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new InternalErrorException(ex.getMessage());
        }
    }

    private void checkIfAllSolutionsLegal(BigRational[] x) throws InternalErrorException {
        for (int i = 0; i < N; i++) {
            if ((!x[i].isUndefined())) {
                if (x[i].greaterThan(maxG)) {
                    System.out.println("Solution:");
                    MiscFunctions.printMatrix(x);
                    System.out.println();
                    throw new InternalErrorException("Arrived at a solution greater than maxG!");
                }
            }
        }
    }

    private int lowerLimitForMMorhac(BigInteger maxA, BigInteger maxb) {
        // Per Morhac
        int logN = (int) Math.ceil(Math.log(N) / Math.log(2));
        int bitlength = 1 + Math.max(N / 2 * logN + maxA.bitLength() * N, logN + ((N - 1) / 2 + 1) * logN + maxA.bitLength() + maxb.bitLength());
        return bitlength;
    }
    /*
    private int lowerLimitForMKoc(BigInteger maxA, BigInteger maxb) {
    // Per Koc
    int bitlength = 1 + Math.max(N * maxA.bitLength(), 0);
    return bitlength;
    }
    private int lowerLimitForMG(BigInteger maxG) {
    // Per Koc
    int bitlength = 1 + maxG.bitLength();
    return bitlength;
    }*/

    private BigInteger max(BigInteger a, BigInteger b) {
        if (a.compareTo(b) == 1) {
            return a;
        } else {
            return b;
        }
    }

    private BigInteger getPrimeLessThan(int b, List<BigInteger> banned) throws InternalErrorException {
        int bitLength = b;
        Random rnd = new Random();
        BigInteger probPrime;
        int attempt = 0;
        do {
            probPrime = BigInteger.probablePrime(bitLength, rnd);
            attempt++;
        } while ((banned.contains(probPrime)) && (attempt < 10));

        if (attempt >= 10) {
            throw new InternalErrorException("Cannot select moduli.");
        }
        return probPrime;
    }
    /*
    private BigRational hadamardLimit() {
    BigRational maxDet = BigRational.ONE;
    for (int j = 0; j < N; j++) {
    if ((!columnsToSkip.contains(j)) && !maxOfAColumns[j].isZero()) {
    maxDet = maxDet.multiply(maxOfAColumns[j]);
    }
    }
    return maxDet;
    }*/

    private void SystemSanitisation() throws InconsistentLinearSystemException {
        rowsToSkip = new HashSet<Integer>();
        columnsToSkip = new HashSet<Integer>();

        for (int i = 0; i < N; i++) {
            if (b[i].isUndefined()) {
                rowsToSkip.add(i);
            }
        }
        for (Integer i : rowsToSkip) {
            for (int j = 0; j < N; j++) {
                if (!columnsToSkip.contains(j)) {
                    if (!A[i][j].isZero()) {
                        BigRational sum = BigRational.ZERO;
                        for (int k = 0; k < N; k++) {
                            if (!rowsToSkip.contains(k) && k != j) {
                                sum = sum.add(A[k][j].abs());
                            }
                        }
                        if (sum.isZero()) {
                            columnsToSkip.add(j);
                        }
                    }
                }
            }
        }

        columnsToSkip.addAll(uncomputables);
        if (rowsToSkip.size() > columnsToSkip.size()) {
            throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
        }
    }

    private BigRational[][] mod(BigRational[][] A, BigInteger mod) {
        BigRational[][] newA = new BigRational[N - rowsToSkip.size()][N - columnsToSkip.size()];
        int indexI = 0, indexJ = 0;
        for (int i = 0; i < N; i++) {
            if (!rowsToSkip.contains(i)) {
                if (this.ra == i) {
                    this.ra = indexI;
                }
                if (this.rb == i) {
                    this.rb = indexI;
                }
                indexJ = 0;
                for (int j = 0; j < N; j++) {
                    if (!columnsToSkip.contains(j)) {
                        if (A[i][j].isZero()) {
                            newA[indexI][indexJ] = BigRational.ZERO;
                        } else {
                            newA[indexI][indexJ] = A[i][j].mod(mod);
                        }
                        indexJ++;
                    }
                }
                indexI++;
            }
        }
        return newA;
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }
}
