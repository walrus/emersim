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

package jmt.jmva.analytical.solvers.Utilities;

import jmt.jmva.analytical.solvers.DataStructures.MultiplicitiesVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * This class implements a calculator of the canonical order of
 * MultiplicitiesVector objects. By the canonical ordering, vectors are first
 * compared by the sum of their elements (greater sum means greater vector);
 * if the sums are equal, the vector with the greater leftmost element is greater.
 *
 * @author Michail Makaronidis, 2010
 */
public class CanonicalMultiplicitiesVectorCalculator {

    /**
     * Stores the queing model on which we are working.
     */
    private QNModel qnm;
    /**
     * The level is equal to the sum of the elements of each MultiplicitiesVector.
     */
    private int lastLevel;
    /**
     * Stores the MultiplicitiesVectors corresponding to the existingLevel.
     */
    private List<MultiplicitiesVector> storedVectors;
    /**
     * Stores the position in storedVectors of the last vector of a specific level.
     */
    private Map<Integer, Integer> vecsOfLevelEndAt;

    /**
     * Creates and initialises a CanonicalMultiplicitiesVectorCalculator object.
     * @param qnm The QNModel we are working on
     */
    public CanonicalMultiplicitiesVectorCalculator(QNModel qnm) {
        super();
        this.qnm = qnm;
        initialise();
    }

    /**
     * This initialises the variables. Can be used to "reset" the object to its
     * initial condition.
     */
    private void initialise() {
        lastLevel = -1;
        storedVectors = new ArrayList<MultiplicitiesVector>();
        vecsOfLevelEndAt = new HashMap<Integer, Integer>();
    }


    /**
     * This returns an ordered set containing the MultiplicitiesVectors the
     * elements of which have sum equal to a specific number (level).
     * @param level The level is equal to the sum of the elements of each MultiplicitiesVector
     * @return The set of the corresponding vectors
     */
    public List<MultiplicitiesVector> findMulVectorsSummingExactlyTo(int level) {
        if (vecsOfLevelEndAt.containsKey(level)) {
            int startFrom;
            if (level > 0) {
                startFrom = vecsOfLevelEndAt.get(level - 1);
            } else {
                startFrom = 0;
            }
            return storedVectors.subList(startFrom, vecsOfLevelEndAt.get(level));
        } else {
            findMulVectorsSummingUpTo(level);
            List<MultiplicitiesVector> toRet = findMulVectorsSummingExactlyTo(level);
            return toRet;
        }
    }

    /**
     * This returns an ordered set containing the MultiplicitiesVectors the
     * elements of which have sum less or equal to a specific number (level).
     * @param level The level is equal to the sum of the elements of each MultiplicitiesVector
     * @return The set of the corresponding vectors
     */
    public List<MultiplicitiesVector> findMulVectorsSummingUpTo(int level) {
        if (vecsOfLevelEndAt.containsKey(level)) {
            return storedVectors.subList(0, vecsOfLevelEndAt.get(level));
        } else if (level < 0) {
            throw new UnsupportedOperationException("Cannot evaluate negative level "+level);
        } else {
            for (int l = lastLevel + 1; l <= level; l++) {
                if (l == 0) {
                    MultiplicitiesVector zeroVector = new MultiplicitiesVector(0, qnm.M);
                    storedVectors.add(zeroVector);
                    vecsOfLevelEndAt.put(0, 1);
                } else {
                    List<MultiplicitiesVector> toProcess;
                    if (l >= 2) {
                        toProcess = new ArrayList<MultiplicitiesVector>(storedVectors.subList(vecsOfLevelEndAt.get(l - 2), vecsOfLevelEndAt.get(l - 1)));
                    } else {
                        toProcess = new ArrayList<MultiplicitiesVector>(storedVectors.subList(0, 1));
                    }
                    TreeSet<MultiplicitiesVector> toAdd = new TreeSet<MultiplicitiesVector>();
                    for (MultiplicitiesVector m : toProcess) {
                        for (int k = 0; k < m.size(); k++) {
                            m.plusOne(k + 1);
                            toAdd.add(m.copy());
                            m.restore();
                        }
                    }
                    storedVectors.addAll(toAdd);
                    vecsOfLevelEndAt.put(l, storedVectors.size());
                }
            }

            lastLevel = level;
            List<MultiplicitiesVector> toRet = storedVectors.subList(0, vecsOfLevelEndAt.get(level));
            return toRet;
        }
    }

    /**
     * Returns the index'th vector of the canonical ordering.
     * @param index The canonical position of the vector
     * @return The corresponding vector
     * @exception InternalErrorException Thrown when the index given does not correspond to vectors with sum greater than QNModel.R
     */
    public MultiplicitiesVector inttovec(int index) throws InternalErrorException {
        List<MultiplicitiesVector> s = findMulVectorsSummingUpTo(qnm.R);
        if (index < s.size()) {
            return s.get(index);
        } else {
            throw new InternalErrorException("Bad usage of inttovec(..).");
        }
    }

    /**
     * Returns the index of a vector in the canonical ordering.
     * @param v The vector
     * @return The corresponding canonical index
     */
    public int vectoint(MultiplicitiesVector v) {
        int sum = v.sum();
        List<MultiplicitiesVector> s = findMulVectorsSummingUpTo(sum);
        int padding = 0;
        if (sum != 0) {
            List<MultiplicitiesVector> initialElements = findMulVectorsSummingUpTo(sum - 1);
            padding = initialElements.size();
            s = s.subList(initialElements.size(), s.size());
        }
        return padding + s.indexOf(v);
    }
}
