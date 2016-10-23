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

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.MultiplicitiesVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class implements the ConvolutionSolver object, which computes the
 * normalising constant for a network using the Convolution algorithm.
 *
 * @author Michail Makaronidis, 2010
 */
public class ConvolutionSolver extends RecursiveSolver {

    /**
     * Creates and initialises a RECALSolver object.
     *
     * @param qnm The QNModel object that we are working on
     * @throws InternalErrorException Thrown when the solver cannot be initialised
     */
    public ConvolutionSolver(QNModel qnm) throws InternalErrorException {
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
	public BigRational compute(MultiplicitiesVector m, PopulationVector p) throws InternalErrorException { //TODO change back to private
        // First we try to find if this value of G has been already computed and stored previously
        BigRational toRet = recallG(m, p);
        if (toRet == null) { 
            // This is the first time we computeNormalisingConstant this value
            if (m.isZeroVector()) {
                toRet = initialConditionFor(p);
            } else {
                // computeNormalisingConstant this G

                int k = m.findFirstNonZeroElement();

                //Create
                m.minusOne(k + 1);
                toRet = compute(m, p);
                // restore 
                m.restore();

                for (int r = 0; r < qnm.R; r++) {
                    if (p.get(r) > 0) { // This is the point that differs when a MoM instantiation is performed.
                        // Create
                        p.minusOne(r + 1);
                        BigRational toAdd = compute(m, p);
                        // restore
                        p.restore();
                        toAdd = toAdd.multiply(qnm.getDemandAsBigRational(k, r));
                        toRet = toRet.add(toAdd);
                    }
                }
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
        System.out.println("Using Convolution.");
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
    }
    
    /*
    /**
     * Added by Jack Bradshaw
     * @param basis The basis to initialise
     * @throws InternalErrorException
     
    public void initialiseFirstClass(CoMoMBasis basis) throws InternalErrorException {
    	
    	PopulationVector N = qnm.getPopulationVector();
    	
    	//set populations to zero for all further classes
    	for (int i = 0; i < N.size(); i++ ) {
    		N.set(i, 0);
    	}    	
    	
    	MultiplicitiesVector model_m = qnm.getMultiplicitiesVector();
    	 
    	compute(model_m, N);
    	
    	MultiplicitiesVector m = new MultiplicitiesVector(0, model_m.size()); 
    	
    	BigRational G;
    	
    	BigRational[] basis_values = new BigRational[basis.getSize()];
    	
    	for (int i = 0; i < basis.getOrder().size(); i++) {
    		PopulationChangeVector n = basis.getPopulationChangeVector(i);
    		for (int k = 0; k <= qnm.M; k++) {
    			
    			m.plusOne(k);
    			G = recallG(m, N.changePopulation(n));
    			m.restore();
    			
    			basis_values[basis.indexOf(n, m)] = G;
    			    			
    		} 
    	}
    	
    //	basis.setBasis(basis_values);
    }
    */
}
