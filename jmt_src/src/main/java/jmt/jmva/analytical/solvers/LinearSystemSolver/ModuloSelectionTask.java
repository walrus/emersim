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
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * The ModuloSelectionTask performs the selection of a prime modulo of a given
 * size. It is used to parallelise the time-consuming selection process, as several
 * ModuloSelectionTasks can be invoked simultaneously.
 *
 * @author Michail Makaronidis, 2010
 */
public class ModuloSelectionTask implements Callable<BigInteger>{

    private int bitLength;

    /**
     * Initialises a ModuloSelectionTask object by setting the desired modulo bitlength.
     * @param bitLength
     */
    public ModuloSelectionTask(int bitLength) {
        this.bitLength = bitLength;
    }

    /**
     * This method performs the actual selection and return the modulo.
     * @return The prime modulo as a BigInteger
     * @throws Exception Thrown when selection failed.
     */
    @Override
    public BigInteger call() throws Exception {
        Random rnd = new Random();
        return BigInteger.probablePrime(bitLength, rnd);
    }

}
