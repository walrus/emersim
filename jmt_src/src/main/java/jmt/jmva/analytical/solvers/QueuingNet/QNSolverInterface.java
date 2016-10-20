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

import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

/**
 * Interface which every solver must implement, as it defines a common way of
 * performing the same operations, such as initialisation, evaluation and
 * returning the results.
 *
 * @author Michail Makaronidis, 2010
 */
public interface QNSolverInterface {

    /**
     * Computes and stores the normalising constant.
     *
     * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     * @throws BTFMatrixErrorException 
     * @throws InconsistentLinearSystemException 
     * @throws OperationNotSupportedException 
     */
    public void computeNormalisingConstant() throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException;

    /**
     * Returns a string represnting the normalising constant.
     *
     * @return String representing the normalising constant
     */
    public String getNormalisingConstant();

    /**
     * Prints a short welcome message that says which solver is used.
     */
    public void printWelcome();

    /**
     * Computes and stores the performance measures (mean throughputs and mean
     * queue lengths for the current model.
     *
     * @throws InternalErrorException Thrown when any computation fails
     */
    public void computePerformanceMeasures() throws InternalErrorException;

    /**
     * Prints the time needed for the solver object to evaluate the network.
     * May print a more detailed time break-down.
     */
    public void printTimeStatistics();

    /**
     * Prints the highest actual memory usage during the network's evaluation.
     */
    public void printMemUsage();
}
