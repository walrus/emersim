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

package jmt.jmva.analytical.solvers.QueuingNet;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.MultiplicitiesVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.DataStructures.Tuple;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystemSolver.ModularSolver;
import jmt.jmva.analytical.solvers.LinearSystemSolver.SimpleSolver;
import jmt.jmva.analytical.solvers.LinearSystemSolver.Solver;
import jmt.jmva.analytical.solvers.Utilities.CanonicalMultiplicitiesVectorCalculator;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;
import jmt.jmva.analytical.solvers.Utilities.Timer;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.naming.OperationNotSupportedException;

/**
 * This class implements the MoMSolver object, which computes the
 * normalising constant for a network using the MoM algorithm. This algorithm is
 * linear system solver agnostic.
 *
 * @author Michail Makaronidis, 2010
 */
public class MoMSolver extends QNSolver {

    /**
     * The matrix A of the algorithm.
     */
    /**
     * The matrix B of the algorithm.
     */
    private BigRational[][] A, B;
    /**
     * Contains the positions of A that need to be updated in each iteration.
     */
    private List<Tuple<Integer, Integer>> UList;
    private Set<Integer> uncomputables;
    private int sz, matrixSize;
    private CanonicalMultiplicitiesVectorCalculator canonicalMVCalc;
    private BigRational[] lastG, prevG;
    private int nThreads;
    private Timer selfTimer, mulTimer, solverTimer;

    /**
     * Creates and initialises a RECALSolver object.
     * @param qnm The QNModel object that we are working on
     * @param nThreads The number of threads the MoMSolver must use for solution. If nThreads >= 2, then LinearSystem.ModularSolver is used.
     */
    public MoMSolver(QNModel qnm, int nThreads) {
        super(qnm);
        initialise(nThreads);
    }

    /**
     * Prints a short welcome message that says which solver is used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using MoM (matrixSize = " + matrixSize+")");
    }

    /**
     * This method can allow in the future easy use of a sparse matrix
     * implementation of A. A similar method can be written for B.
     *
     * @param i
     * @param j
     * @param val
     */
    private void writeToA(int i, int j, BigRational val) {
        A[i][j] = val;//.copy();
        uncomputables.remove(j);
        /*if (val.abs().greaterThan(maxOfAColumns[j])) {
        maxOfAColumns[j] = val.abs();
        }*/
    }

    private void generateABU(PopulationVector N, int s) throws InternalErrorException {
        MultiplicitiesVector dM;
        int M = qnm.M, R = qnm.R, row = -1;
        A = new BigRational[matrixSize][matrixSize];
        //maxOfAColumns = new BigRational[matrixSize];
        B = new BigRational[matrixSize][matrixSize];
        UList = new LinkedList<Tuple<Integer, Integer>>();
        uncomputables = new HashSet<Integer>();

        /*for (int j = 0; j < matrixSize; j++) {
        maxOfAColumns[j]= BigRational.ZERO;
        }*/
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                A[i][j] = BigRational.ZERO; // We do not use writeToA() as this would aldo remove j from the uncomputables set and we are in the initialisation phase.
                B[i][j] = BigRational.ZERO;
            }
            uncomputables.add(i);
        }
        int limit = (int) ((float) R / M * MiscFunctions.binomialCoefficient(M + R - 1, R) - 1);
        for (int i = 0; i <= limit; i++) { // (* Add to the linear system all convolution equations *)
            dM = canonicalMVCalc.inttovec(i);
            if (dM.sum() == R - 1) {
                MultiplicitiesVector dMPlusk = dM.copy();
                for (int k = 1; k <= M; k++) {
                    dMPlusk.plusOne(k);
                    row++;
                    int pos = canonicalMVCalc.vectoint(dMPlusk);
                    writeToA(row, pos, BigRational.ONE);//A[row][pos] = BigRational.ONE;

                    writeToA(row, i, new BigRational(-1));//A[row][i] = BigRational.ONE.negate();
                    for (int r = 1; r <= s - 1; r++) {
                        int pos2 = sz * r + canonicalMVCalc.vectoint(dMPlusk);
                        writeToA(row, pos2, qnm.getDemandAsBigRational(k - 1, r - 1).negate());//A[row][pos2] = qnm.getDemandAsBigRational(k - 1, r - 1).negate();
                    }
                    B[row][pos] = qnm.getDemandAsBigRational(k - 1, s - 1);
                    dMPlusk.restore();
                }
            }
        }
        for (int i = 0; i <= limit; i++) { // (* Add to the linear system all population constraints *)
            dM = canonicalMVCalc.inttovec(i);
            if (dM.sum() == R - 1) {
                for (int r = 1; r <= s - 1; r++) {
                    row++;
                    writeToA(row, i, N.getAsBigRational(r - 1));//A[row][i] = N.getAsBigRational(r - 1);
                    int pos = sz * r + i;
                    writeToA(row, pos, qnm.getDelayAsBigRational(r-1).negate());//A[row][pos] = qnm.Z.getAsBigRational(r - 1).negate();
                    MultiplicitiesVector dMPlusk = dM.copy();
                    for (int k = 1; k <= M; k++) {
                        dMPlusk.plusOne(k);
                        int f = dM.get(k - 1) + qnm.multiplicities.get(k - 1);
                        BigRational bf = new BigRational(f);
                        pos = sz * r + canonicalMVCalc.vectoint(dMPlusk);
                        writeToA(row, pos, bf.negate().multiply(qnm.getDemandAsBigRational(k - 1, r - 1)));//A[row][pos] = bf.negate().multiply(qnm.getDemandAsBigRational(k - 1, r - 1));
                        dMPlusk.restore();
                    }
                }
            }
            for (int r = 0; r <= s - 1; r++) {
                row++;
                int pos = sz * r + i;
                writeToA(row, pos, N.getAsBigRational(s - 1));//A[row][pos] = N.getAsBigRational(s - 1);
                UList.add(new Tuple<Integer, Integer>(row, pos));
                B[row][pos] = qnm.getDelayAsBigRational(s-1);//Z.getAsBigRational(s - 1);
                MultiplicitiesVector dMPlusk = dM.copy();
                for (int k = 1; k <= M; k++) {
                    dMPlusk.plusOne(k);
                    int f = dM.get(k - 1) + qnm.multiplicities.get(k - 1);
                    BigRational bf = new BigRational(f);
                    B[row][sz * r + canonicalMVCalc.vectoint(dMPlusk)] = bf.multiply(qnm.getDemandAsBigRational(k - 1, s - 1));
                    dMPlusk.restore();
                }
            }
        }
        for (int i = row + 1; i < sz * R; i++) {
            writeToA(i, i, BigRational.ONE);//A[i][i] = BigRational.ONE;
            B[i][i] = BigRational.ONE;
        }
    }

    /**
     * Initialises the MoMSolver according to the specified QNModel.
     */
    private void initialise(int nThreads) {
        selfTimer = new Timer();
        mulTimer = new Timer();
        totalTimer = new Timer();

        this.nThreads = nThreads;
        int R = qnm.R, M = qnm.M;
        sz = MiscFunctions.binomialCoefficient(M + R, R);
        matrixSize = sz * R;
        canonicalMVCalc = new CanonicalMultiplicitiesVectorCalculator(qnm);
    }

    @Override
    public void computeNormalisingConstant() throws InternalErrorException {
        totalTimer.start();
        PopulationVector N = qnm.N.copy();
        int r = 0, Nr;
        int R = qnm.R, M = qnm.M;
        BigRational[] curG = new BigRational[matrixSize], G_1 = null;
        for (int i = 0; i < matrixSize; i++) {
            curG[i] = BigRational.ONE;
        }
        PopulationVector N0 = new PopulationVector(0, R);
        N0.set(0, 1);
        System.out.println("Initialising data structures of class " + (r + 1));
        generateABU(N0, 1);
        try {
            Solver s;
            if (nThreads == 1) {
                s = new SimpleSolver();
            } else {
                s = new ModularSolver(nThreads);
            }
            Integer maxA = (int)getMaxAElement();
            Integer val = matrixSize;
            BigInteger maxB = qnm.getMaxG().multiply(new BigInteger(maxA.toString())).multiply(new BigInteger(val.toString()));
            s.initialise(A, UList, uncomputables, maxA, maxB, new BigRational(qnm.getMaxG()));
            N0.set(0, 0);
            int N0r = 1;
            int Ntot = 0;

            for (r = 1; r <= R; r++) {
                System.out.println("Processing class " + r);
                for (Nr = N0r; Nr <= N.get(r - 1) - 1; Nr++) {
                    mulTimer.start();
                    BigRational[] sysB = MiscFunctions.matrixVectorMultiply(B, curG);
                    mulTimer.pause();
                    s.goToULevel(Nr - 1);
                    curG = solve(s, sysB);
                    Ntot++;
                    System.out.println("Population " + Nr + " completed (total population " + Ntot + " jobs)");
                }
                G_1 = curG;
                mulTimer.start();
                BigRational[] sysB = MiscFunctions.matrixVectorMultiply(B, curG);
                mulTimer.pause();
                s.goToULevel(Nr - 1);
                curG = solve(s, sysB);
                Ntot++;
                System.out.println("Population " + Nr + " completed (total population " + Ntot + " jobs)");
                N0.set(r - 1, N.get(r - 1));
                System.out.println("Class " + r + " completed");
                if (r < R) {
                    N0.set(r, 1);
                    N0r = 1;
                    System.out.println("Initialising data structures of class " + (r + 1));
                    generateABU(N0, r + 1);
                    s.initialise(A, UList, uncomputables, maxA, maxB, new BigRational(qnm.getMaxG()));
                }
            }
            if (!curG[0].isUndefined()) {
                this.G = curG[0];
            } else {
                throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
            }
            totalTimer.pause();
            memUsage = MiscFunctions.memoryUsage();
            solverTimer = s.getTimer();
            s.shutdown();
        } catch (OperationNotSupportedException ex) {
            //ex.printStackTrace();
            throw new InternalErrorException("Error in linear system solver.");
        } catch (InconsistentLinearSystemException ex) {
            //ex.printStackTrace();
            throw new InternalErrorException(ex.getMessage());
        }
        qnm.setNormalisingConstant(G);
        this.lastG = curG;
        this.prevG = G_1;
    }

    @Override
    public void computePerformanceMeasures() throws InternalErrorException {
        totalTimer.start();
        BigRational[] X = new BigRational[qnm.R];
        BigRational[][] Q = new BigRational[qnm.M][qnm.R];

        if (lastG[0].isUndefined()) {
            throw new InternalErrorException(("Singular system. Cannot compute performance indices."));
        }
        for (int i = 0; i < qnm.M; i++) {
            for (int r = 0; r < qnm.R - 1; r++) {
                if (!lastG[sz * (r + 1) + i + 1].isUndefined()) {
                    Q[i][r] = qnm.getDemandAsBigRational(i, r).multiply(lastG[sz * (r + 1) + i + 1]).divide(lastG[0]);
                } else {
                    throw new InternalErrorException(("Singular system. Cannot compute performance indices."));
                }
            }
            if (!prevG[i + 1].isUndefined()) {
                Q[i][qnm.R - 1] = qnm.getDemandAsBigRational(i, qnm.R - 1).multiply(prevG[i + 1]).divide(lastG[0]);
            } else {
                throw new InternalErrorException(("Singular system. Cannot compute performance indices."));
            }
        }
        for (int r = 0; r < qnm.R - 1; r++) {
            if (!lastG[sz * (r + 1)].isUndefined()) {
                X[r] = lastG[sz * (r + 1)].divide(lastG[0]);
            } else {
                throw new InternalErrorException(("Singular system. Cannot compute performance indices."));
            }
        }
        if (!prevG[0].isUndefined()) {
            X[qnm.R - 1] = prevG[0].divide(lastG[0]);
        } else {
            throw new InternalErrorException(("Singular system. Cannot compute performance indices."));
        }
        totalTimer.pause();
        qnm.setPerformanceMeasures(Q, X);
    }

    @Override
    public void printTimeStatistics() {
        long selfTime = totalTimer.getInterval() - mulTimer.getInterval() - solverTimer.getInterval();
        selfTimer = new Timer(selfTime);

        System.out.println("MoM time: " + selfTimer.getPrettyInterval());
        System.out.println("LS Solver time: " + solverTimer.getPrettyInterval());
        System.out.println("Multiplier time: " + mulTimer.getPrettyInterval());
        System.out.println("Total time: " + totalTimer.getPrettyInterval());
    }

    private BigRational[] solve(Solver s, BigRational[] b) throws InconsistentLinearSystemException, OperationNotSupportedException, InternalErrorException {
        BigRational[] sol = s.solve(b);
        // If the first element (current normalising constant) has been corrupted
        // then there is no recovery
        if (sol[0].isUndefined()) {
            throw new InconsistentLinearSystemException("Singular system. Cannot proceed.");
        }
        return sol;
    }

    private double getMaxAElement() throws OperationNotSupportedException {
        double max = qnm.getMaxModelValue();
        max *= qnm.multiplicities.max() + qnm.R;
        return max;
    }
    /*
    private int getMaxAElement(int R) throws OperationNotSupportedException {
    int max = qnm.getMaxModelValue();
    max *= qnm.multiplicities.max() + R;
    return max;
    }*/
}
