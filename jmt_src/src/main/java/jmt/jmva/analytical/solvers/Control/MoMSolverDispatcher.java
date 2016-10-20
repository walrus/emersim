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

package jmt.jmva.analytical.solvers.Control;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.MoMSolver;
import jmt.jmva.analytical.solvers.QueuingNet.QNSolver;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the API to allow use by other applications. One can
 * define the queueing network model details, call the solvers transparently and,
 * afterwards, read the results.
 *
 * @author Michail Makaronidis, 2010
 */
public class MoMSolverDispatcher {

    /**
     * number of classes or jobs in the system
     */
    protected int classes = 0;
    /**
     * Array containing population for each class
     */
    protected int[] population;
    /**
     * Contains the queuing network model
     */
    protected QNModel qnm;
    /**
     * Number of threads that the MoMSolver should use
     */
    protected int nThreads;
    /**--------------CONSTANTS DEFINITION------------------------*/
    /** constant for Load Dependent service center  */
    public final static int LD = 0;
    /** constant for Load Independent service center  */
    public final static int LI = 1;
    /** constant for delay center*/
    public final static int DELAY = 2;
    /**---------------MODEL DEFINITION------------------------*/
    /**number of resources*/
    protected int stations = 0;
    /**---------------MODEL SOLUTION------------------------*/
    /** array containing the throughput of each service center  */
    protected double[] throughput;
    /** array containing the queue length of each service center  */
    protected double[][] queueLen;

    /** Creates new MoMSolverDispatcher
     *  @param  stations    number of service centers
     *  @param  classes     number o classes of classes
     *  @param  population     array of population classes
     */
    public MoMSolverDispatcher(int classes, int stations, int[] population) {
        this.classes = classes;
        this.stations = stations;
        this.population = population;
    }

    /** Initializes the solver with the system parameters.
     * It must be called before trying to solve the model.
     *  @param  t   array of the types (LD or LI) of service centers
     *  @param  s   matrix of service times of the service centers
     *  @param  v   array of visits to the service centers
     *  @param  nThreads The number of threads the solver should use. If nThreads > 2 then the parallel algorithm is used.
     *  @return True if the operation is completed with success
     */
    public boolean input(int[] t, double[][] s, double[][] v, int nThreads) {
        if ((t.length > stations) || (s.length > stations) || (v.length > stations)) {
            // wrong input.
            return false;
        }
        try {
            int M = 0, R = this.classes;
            double[] Z = new double[classes];
            for (int r = 0; r < R; r++) {
                Z[r] = 0;
            }

            // Discover delay times (think times)
            for (int i = 0; i < stations; i++) {
                if (t[i] == LI) {
                    M++;
                } else if (t[i] == LD) {
                    for (int r = 0; r < classes; r++) {
                        Z[r] += (int) (s[i][r] * v[i][r]);
                    }
                } else {
                    return false;
                }
            }
            // Now Z contains the delay times

            // Discover service demands
            double[][] D = new double[M][R];
            int mIndex = 0; // current queue
            for (int i = 0; i < stations; i++) {
                if (t[i] == LI) {
                    for (int r = 0; r < classes; r++) {
                        D[mIndex][r] = (int) (s[i][r] * v[i][r]);
                    }
                    mIndex++;
                }
            }
            // Now D contains service demands

            // Create queue multiplicities array
            // All multiplicities are set to 1, as JMT does not seem to use queue multiplicities
            // If this array is instantiated properly, the rest of the MoMSolver can support them
            Integer[] multiplicities = new Integer[M];
            for (int m = 0; m < M; m++) {
                multiplicities[m] = 1;
            }

            // Transform population from int[] to Integer[]
            Integer[] N = new Integer[R];
            for (int r = 0; r < R; r++) {
                N[r] = population[r];
            }

            // Instantiate queuing network model
            qnm = new QNModel(R, M, t, N, Z, multiplicities, D);
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return false if initialisation fails for any reason.
            return false;
        }
        return true;
    }

    /**
     * Solves the system throught the MoM algorithm.
     * input(...) must have been called first.
     *
     * @throws InternalErrorException Thrown when any error is encountered during computations, i.e. due to linear system singularities
     * @throws BTFMatrixErrorException 
     * @throws InconsistentLinearSystemException 
     * @throws OperationNotSupportedException 
     */
    public void solve() throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
        QNSolver c = new MoMSolver(qnm, nThreads);
        c.printWelcome();
        System.out.println("Will solve the following model: ");
        qnm.printModel();
        c.computeNormalisingConstant();
        System.out.println("G = " + qnm.getPrettyNormalisingConstant());
        c.printTimeStatistics();
        c.printMemUsage();
        c.computePerformanceMeasures();
        System.out.println("\nX = ");
        MiscFunctions.printPrettyMatrix(qnm.getMeanThroughputs());
        System.out.println("\nQ = ");
        MiscFunctions.printPrettyMatrix(qnm.getMeanQueueLengths());
        System.out.println();
        c.printTimeStatistics();

        throughput = getMeanThroughputs();
        queueLen = getMeanQueueLengths();
    }

    /**
     * Returns the value of the normalising constant as a string. May contain fractional part.
     * @return The string representing the normalising constant
     */
    public String getPrettyNormalisingConstant() {
        return qnm.getPrettyNormalisingConstant();
    }

    /**
     * Returns the value of the normalising constant as a BigRational.
     * @return The BigRational representing the normalising constant
     */
    public BigRational getNormalisingConstant() {
        return qnm.getNormalisingConstant();
    }

    /**
     * Returns the mean queue lengths as an MxR array of doubles.
     * @return The double[M][R] array.
     */
    public double[][] getMeanQueueLengths() {
        return qnm.getMeanQueueLengthsAsDoubles();
    }

    /**
     * Returns the mean throughputs as an array of R doubles.
     * @return The double[R] array.
     */
    public double[] getMeanThroughputs() {
        return qnm.getMeanThroughputsAsDoubles();
    }
}
