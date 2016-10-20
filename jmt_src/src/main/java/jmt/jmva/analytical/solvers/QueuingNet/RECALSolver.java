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
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the RECALSolver object, which computes the normalising
 * constant for a network using the RECAL algorithm. A recursive approach is
 * used.
 *
 * @author Michail Makaronidis, 2010
 */
public class RECALSolver extends RecursiveSolver {

    /**
     * Creates and initialises a RECALSolver object.
     *
     * @param qnm The QNModel object that we are working on
     * @throws InternalErrorException Thrown when the solver cannot be initialised
     */
    public RECALSolver(QNModel qnm) throws InternalErrorException {
        super(qnm);
    }

    /**
     * Computes any normalising constant using a simple recursion and taking
     * advantage of any already computed and stored value.
     *
     * @param m The MultiplicitiesVector
     * @param p The PopulationVector
     * @return The normalising constant as a BigRational object
     * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     */
    @Override
    protected BigRational compute(MultiplicitiesVector m, PopulationVector p) throws InternalErrorException {
        // First we try to find if this value of G has been already computed and stored previously
        BigRational toRet = recallG(m, p);
        if (toRet == null) {
            // This is the first time we computeNormalisingConstant this value
            if (m.isZeroVector()) {
                toRet = initialConditionFor(p);
            } else {
                // computeNormalisingConstant this G
                int r = p.findFirstNonZeroElement();

                //Create N-Ir
                p.minusOne(r + 1);
                if (qnm.getDelay(r) != 0) {
                    toRet = compute(m, p);
                    toRet = toRet.multiply(qnm.getDelayAsBigRational(r));
                } else {
                    toRet = BigRational.ZERO;
                }

                for (int k = 0; k < qnm.M; k++) {
                    if (m.get(k) > 0) {
                        // Create Δm+1k
                        m.plusOne(k + 1);
                        BigRational toAdd = compute(m, p);
                        // restore Δm
                        m.restore();
                        BigRational mulFactor = qnm.getDemandAsBigRational(k, r);
                        mulFactor = mulFactor.multiply(new BigRational(m.get(k) /*+ qnm.multiplicities.get(k)-1*/));
                        toAdd = toAdd.multiply(mulFactor);
                        toRet = toRet.add(toAdd);
                    }
                }
                // restore N
                p.restore();
                // divide by Nr
                toRet = toRet.divide(new BigRational(p.get(r)));
            }
            // Store for future use (add to Gmap)
            storeG(m, p, toRet);
        }
        return toRet;
    }

    /**
     * Prints a short welcome message that says which solver is used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using RECAL (recursive)");
    }

    /**
     * Computes the normalising constant for the specified QNModel.
     *
     * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     */
    @Override
    public void computeNormalisingConstant() throws InternalErrorException {
        totalTimer.start();
        MultiplicitiesVector m = qnm.getMultiplicitiesVector();
        PopulationVector p = qnm.getPopulationVector();

        G = compute(m, p);
        totalTimer.pause();
        memUsage = MiscFunctions.memoryUsage();
        qnm.setNormalisingConstant(G);
        //System.out.println(Gmap);
    }
}
