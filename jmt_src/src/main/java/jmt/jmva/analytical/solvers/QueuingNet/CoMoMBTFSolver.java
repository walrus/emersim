package jmt.jmva.analytical.solvers.QueuingNet;

import jmt.jmva.analytical.solvers.Basis.BTFCoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.BTF.BTFLinearSystem;

/**
 * A concrete implementation of the CoMoMSolver class that choose to use the BTF Linear System.
 * @author Jack Bradshaw
 */
public class CoMoMBTFSolver extends CoMoMSolver {

	public CoMoMBTFSolver(QNModel qnm) throws InternalErrorException, BTFMatrixErrorException, InconsistentLinearSystemException {
		super(qnm);
		
		basis =  new BTFCoMoMBasis(qnm);		
		system = new BTFLinearSystem(qnm, basis);
	}

	 /**
     * Prints a short welcome message that says which solver is being used.
     */
    @Override
    public void printWelcome() {
        System.out.println("Using BTF CoMoM");
    }
}
