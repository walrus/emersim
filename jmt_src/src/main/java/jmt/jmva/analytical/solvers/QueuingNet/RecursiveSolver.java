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
import jmt.jmva.analytical.solvers.Utilities.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;

/**
 * This class implements the RecursiveSolver object, which contains common methods
 * for the Convolution and RECAL algorithms.
 *
 * @author Michail Makaronidis, 2010
 */
public class RecursiveSolver extends QNSolver {

    private int curlevel = -1;

    /**
     * Here is stored every normalising constant computed. This speeds up the
     * computation.
     */
    protected Map<PopulationVector, Map<MultiplicitiesVector, BigRational>> Gmap = new HashMap<PopulationVector, Map<MultiplicitiesVector, BigRational>>();
    /**
     * The factorials for some numbers are pre-computed for efficiency reasons.
     */
    protected Map<Integer, BigRational> factorial;

    /**
     * Creates and initialises a RecursiveSolver object.
     *
     * @param qnm The QNModel object that we are working on
     * @throws InternalErrorException Thrown when the solver cannot be initialised
     */
    public RecursiveSolver(QNModel qnm) throws InternalErrorException {
        super(qnm);
        try {
            initialise();
        } catch (OperationNotSupportedException ex) {
            Logger.getLogger(RecursiveSolver.class.getName()).log(Level.SEVERE, null, ex);
            throw new InternalErrorException("Initialisation failed.");
        }
    }

    /**
     * Initialises the RecursiveSolver according to the specified QNModel.
     */
    private void initialise() throws OperationNotSupportedException {
        totalTimer = new Timer();
        Gmap.clear();
        // Necessary for faster computation of initial conditions
        factorial = MiscFunctions.computeFactorials(qnm.N.max());
    }

    /**
     * Returns the initial condition corresponding to a particular
     * PopulationVector object.
     *
     * @param n The PopulationVector object
     * @return The corresponding initial condition
     */
    protected BigRational initialConditionFor(PopulationVector n) {
        // Consider think time
        BigRational toAssign = BigRational.ONE;
        for (int r = 0; r < qnm.R; r++) {
            if (qnm.getDelay(r) == 0) {
                return BigRational.ZERO;
            }
            int nr = n.get(r);
            if (nr > 0) {
                BigRational curVal = qnm.getDelayAsBigRational(r).pow(nr);
                curVal = curVal.divide(factorial.get(nr));
                toAssign = toAssign.multiply(curVal);

            } else if (nr < 0) {
                return BigRational.ZERO;
            }
        }
        return toAssign;
    }

    /**
     * Stores a computed normalising constant value for future re-use.
     *
     * @param m The MultiplicitiesVector
     * @param p The PopulationVector
     * @param Gval The corresponding normalising constant that has been computed
     */
    protected void storeG(MultiplicitiesVector m, PopulationVector p, BigRational Gval) {
        storeG(m, p, Gval, Gmap);
    }

    /**
     * Stores at a particular Map<PopulationVector, Map<MultiplicitiesVector, BigRational>>
     * datastructure a computed normalising constant value for future re-use.
     *
     * @param m The MultiplicitiesVector
     * @param p The PopulationVector
     * @param Gval The corresponding normalising constant that has been computed
     * @param map The datastructure at which the constant will be stored
     */
    protected void storeG(MultiplicitiesVector m, PopulationVector p, BigRational Gval, Map<PopulationVector, Map<MultiplicitiesVector, BigRational>> map) {
        Map<MultiplicitiesVector, BigRational> step1 = map.get(p);
        if (step1 == null) {
            Map<MultiplicitiesVector, BigRational> step2 = new HashMap<MultiplicitiesVector, BigRational>();
            step2.put(m.copy(), Gval);
            map.put(p.copy(), step2);
        } else {
            //TODO: Remove this when adequately tested
            /*if (step1.containsKey(m)) {
                throw new InternalErrorException("Attempt to re-enter data into Gmap");
            }*/
            // step2 must be null, or else we would have found the value of G.
            step1.put(m.copy(), Gval);
        }
    }

    /**
     * Searches for a normalising constant in the memory. If it's not found, it
     * returns null.
     *
     * @param m The MultiplicitiesVector
     * @param p The PopulationVector
     * @return The normalising constant as a BigRational object. Null if it has not been precomputed in the past.
     */
    protected BigRational recallG(MultiplicitiesVector m, PopulationVector p) {
        if (p.isZeroVector()) {
            return BigRational.ONE;
        } else if (p.containsMinusOne()) {
            return BigRational.ZERO;
        } else // Added when attempting serialisation
        if (m.isZeroVector()) {
            return initialConditionFor(p);
        } else {
            Map<MultiplicitiesVector, BigRational> step1 = Gmap.get(p);
            if (step1 != null) {
                BigRational toReturn = step1.get(m); // We do not care if a null is returned
                return toReturn;
            } else {
                return null;
            }
        }
    }

    /**
     * Searches for a normalising constant in the memory. If it's not found, it
     * computes and returns it.
     * @param m The MultiplicitiesVector
     * @param p The PopulationVector
     * @return The normalising constant as a BigRational object
     * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     */
    protected BigRational findG(MultiplicitiesVector m, PopulationVector p) throws InternalErrorException {
        BigRational toReturn = recallG(m, p);
        if (toReturn == null) {
            toReturn = compute(m, p);
        }
        return toReturn;
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
    protected BigRational compute(MultiplicitiesVector m, PopulationVector p) throws InternalErrorException {
        throw new InternalErrorException("Operation not supported.");
    }

    @Override
    public void computePerformanceMeasures() throws InternalErrorException {
        totalTimer.start();
        BigRational[] X = new BigRational[qnm.R];
        BigRational[][] Q = new BigRational[qnm.M][qnm.R];
        try {
            MultiplicitiesVector m = qnm.multiplicities.copy();
            PopulationVector p = qnm.N.copy();
            for (int s = 0; s < qnm.R; s++) {
                p.minusOne(s + 1);
                X[s] = findG(m, p).divide(G);
                p.restore();
            }

            for (int k = 0; k < qnm.M; k++) {
                m.plusOne(k + 1);
                for (int s = 0; s < qnm.R; s++) {
                    p.minusOne(s + 1);
                    Q[k][s] = qnm.getDemandAsBigRational(k, s).multiply(findG(m, p)).divide(G);
                    p.restore();
                }
                m.restore();
            }
        } catch (ArithmeticException ex) {
            throw new InternalErrorException("Cannot compute performance measures. (G = 0)");
        }
        totalTimer.pause();
        qnm.setPerformanceMeasures(Q, X);
    }
}
