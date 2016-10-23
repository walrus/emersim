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
import jmt.jmva.analytical.solvers.Utilities.CanonicalMultiplicitiesVectorCalculator;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the RECALSolver object, which computes the normalising
 * constant for a network using the RECAL algorithm. A non-recursive approach is
 * used.
 *
 * @author Michail Makaronidis, 2010
 */
public class RECALNonRecursiveSolver extends RecursiveSolver {

    private CanonicalMultiplicitiesVectorCalculator mvcalc = new CanonicalMultiplicitiesVectorCalculator(qnm);
    private boolean isGComputed = false;

    /**
     * Creates and initialises a RECALSolver object.
     *
     * @param qnm The QNModel object that we are working on
     * @throws InternalErrorException Thrown when the solver cannot be initialised
     */
    public RECALNonRecursiveSolver(QNModel qnm) throws InternalErrorException {
        super(qnm);
    }

    /**
     * Computes any normalising constant non-recursively and by taking
     * advantage of any already computed and stored value.
     *
     * @param finalm The MultiplicitiesVector
     * @param finalp The PopulationVector
     * @return The normalising constant as a BigRational object
     * @throws InternalErrorException An exception is thrown if any internal error is encountered during computations.
     */
    @Override
    protected BigRational compute(MultiplicitiesVector finalm, PopulationVector finalp) throws InternalErrorException {
        boolean firstVisit = true, secondVisit = false;
        BigRational curG;
        PopulationVector p = new PopulationVector(0, qnm.R);
        MultiplicitiesVector mBase = finalm.copy();
        int Ntot = finalp.sum();
        for (int r = 0; r < qnm.R; r++) {
            for (int nr = (r == 0) ? 0 : 1; nr <= finalp.get(r); nr++) {
                //System.out.println("r = "+r+" nr = "+nr+" #mvs = "+mvcalc.findMulVectorsSummingUpTo(Ntot).size());
                Map<PopulationVector, Map<MultiplicitiesVector, BigRational>> curMap = new HashMap<PopulationVector, Map<MultiplicitiesVector, BigRational>>();
                p.set(r, nr);
                boolean isPZeroVector = p.isZeroVector(), pContainsMinusOne = p.containsMinusOne();
                for (MultiplicitiesVector mDelta : mvcalc.findMulVectorsSummingUpTo(Ntot)) {
                    MultiplicitiesVector m = mBase.addVec(mDelta);
                    //curG = recallG(m, p);
                    //if (curG == null) {
                    if (!isPZeroVector) {
                        curG = BigRational.ZERO;
                        if (pContainsMinusOne) {
                            //curG = BigRational.ZERO;
                            continue;
                        } else if (m.isZeroVector()) {
                            //curG = initialConditionFor(p);
                            continue;
                        } else {
                            p.minusOne(r + 1);
                            BigRational delay = qnm.getDelayAsBigRational(r);
                            if (!delay.isZero()) {
                                curG = curG.add(recallG(m, p)).multiply(delay);
                            }

                            for (int k = 0; k < qnm.M; k++) {
                                m.plusOne(k + 1);
                                //BigRational toAdd = qnm.getDemandAsBigRational(k, r).multiply(new BigRational(1+mDelta.get(k)));
                                BigRational toAdd = qnm.getDemandAsBigRational(k, r).multiply(new BigRational(m.get(k) - 1));
                                toAdd = toAdd.multiply(recallG(m, p));
                                curG = curG.add(toAdd);
                                m.restore();
                            }
                            curG = curG.divide(new BigRational(nr));
                            p.restore();
                        }
                    } else {
                        continue;
                        //curG = BigRational.ONE;
                    }
                    storeG(m, p, curG, curMap);
                    //}
                }
                Ntot--;
                Gmap.clear();
                Gmap = curMap;
                //Gmap.putAll(curMap);
                if (firstVisit) {
                    secondVisit = true;
                    firstVisit = false;
                } else {
                    if (secondVisit) {
                        secondVisit = false;
                        totalTimer.pause();
                        memUsage = MiscFunctions.memoryUsage();
                        totalTimer.start();
                    }
                }
            }
        }
        return recallG(finalm, finalp);
    }

    /**
     * Prints a short welcome message that says which solver is used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using RECAL (non-recursive)");
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

        //G = compute(m, p);
        G = compute(m, p);
        totalTimer.pause();
        //memUsage = MiscFunctions.memoryUsage();
        qnm.setNormalisingConstant(G);
        isGComputed = true;
        //System.out.println(Gmap);
    }
}
