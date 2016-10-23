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

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.Timer;

/**
 * Class which defines several methods every QNSolver object must support. It
 * provides generic implementation of common operations, such as initialisation,
 * evaluation and returning the results.
 *
 * @author Michail Makaronidis, 2010
 */
public class QNSolver implements QNSolverInterface {

    /**
     * The Timer object responsible for keeping the total time needed by this
     * QNSolver to evaluate the network.
     */
    public Timer totalTimer = new Timer();
    /**
     * The QNModel object that we are working on.
     */
    protected QNModel qnm;
    /**
     * The normalising constant is stored here after it has been computed.
     */
    protected BigRational G;
    /**
     * Contains a human-readable string representation of the QNSolver's memory usage.
     */
    protected String memUsage;

    /**
     * Creates and initialises a QNSolver object.
     *
     * @param qnm The QNModel object that we are working on
     */
    public QNSolver(QNModel qnm) {
        super();
        this.qnm = qnm;
    }

    /**
     * Computes the normalising constant for the specified QNModel.
     *
     * @throws InternalErrorException * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     * @throws BTFMatrixErrorException 
     * @throws InconsistentLinearSystemException 
     * @throws OperationNotSupportedException 
     */
    @Override
    public void computeNormalisingConstant() throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Returns a string represnting the normalising constant.
     *
     * @return String representing the normalising constant
     */
    @Override
    public String getNormalisingConstant() {
        if (G.isBigDecimal()) {
            return G.asBigDecimal().toString();
        } else {
            return G.toString();
        }
    }

    /**
     * Prints a short welcome message that says which solver is used.
     */
    @Override
    public void printWelcome() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Computes and stores the performance measures (mean throughputs and mean
     * queue lengths for the current model.
     *
     * @throws InternalErrorException Thrown when any computation fails
     */
    @Override
    public void computePerformanceMeasures() throws InternalErrorException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Prints the time needed for the solver object to evaluate the network.
     *
     */
    @Override
    public void printTimeStatistics() {
        System.out.println("Total time: " + totalTimer.getPrettyInterval());
    }

    /**
     * Prints the highest actual memory usage during the network's evaluation.
     */
    @Override
    public void printMemUsage() {
        System.out.println("Memory Usage: " + memUsage);
    }
}
