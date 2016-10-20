package jmt.jmva.analytical.solvers.QueuingNet;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.BTFCoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.Simple.SimpleLinearSystem;

/**
 * A concrete implementation of the CoMoMSolver class that choose to use the Simple Linear System.
 * @author Jack Bradshaw
 */
public class CoMoMSimpleSolver extends CoMoMSolver {

	public CoMoMSimpleSolver(QNModel qnm, int num_threads) throws InternalErrorException {
		super(qnm);
		
		basis =  new BTFCoMoMBasis(qnm);		
		system = new SimpleLinearSystem(qnm, basis, num_threads);
	}

	 /**
     * Prints a short welcome message that says which solver is being used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using Simple CoMoM");
    }
}
