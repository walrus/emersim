package jmt.jmva.analytical.solvers.LinearSystem;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;

/**
 * This abstract class provides the interface for the Linear System which is used by the CoMoM algorithm.
 * @author Jack Bradshaw
 */
public abstract class LinearSystem {

	/**
	 * Basis for the model
	 */
	protected  CoMoMBasis basis;
	
	/**
	 * The model under consideration.
	 */
	protected QNModel qnm;
	
	/**
	 * Constructor
	 * @param qnm The model under consideration.
	 * @param basis Basis for the model
	 * @throws InternalErrorException
	 */
	public LinearSystem(QNModel qnm, CoMoMBasis basis) throws InternalErrorException {
		this.qnm = qnm;
		this.basis = basis;
		basis.initialiseBasis();	
	}

	public void computePerformanceMeasures() throws InternalErrorException {
		basis.computePerformanceMeasures();		
	}
	
	/**
	 * Updates the N_l values in the linear system
	 * @param current_class_population The population in the current class under consideration
	 */
	public abstract void update(int current_class_population ); 
	
	/**
	 * Solve the linear system in its current configuration
	 * @throws OperationNotSupportedException
	 * @throws InconsistentLinearSystemException
	 * @throws InternalErrorException
	 * @throws BTFMatrixErrorException
	 */
	public abstract void solve() throws OperationNotSupportedException, InconsistentLinearSystemException, InternalErrorException, BTFMatrixErrorException;
	
	/**
	 * Initialises the linear system ready for iteration on current_class at the specified population
	 * @param current_N The current population vector
	 * @param current_class The current class under consideration
	 * @throws InternalErrorException
	 * @throws OperationNotSupportedException
	 * @throws BTFMatrixErrorException
	 * @throws InconsistentLinearSystemException
	 */
	public final void initialiseForClass(PopulationVector current_N, int current_class) 
			throws InternalErrorException, OperationNotSupportedException, BTFMatrixErrorException, InconsistentLinearSystemException {
		basis.initialiseForClass(current_class);
		initialiseMatricesForClass(current_N, current_class);
	}
	
	/**
	 * Initialises the matrices of the linear system ready for iteration on current_class at the specified population
	 * @param current_N The current population vector
	 * @param current_class The current class under consideration
	 * @throws BTFMatrixErrorException
	 * @throws InternalErrorException
	 * @throws InconsistentLinearSystemException
	 * @throws OperationNotSupportedException
	 */
	protected abstract void initialiseMatricesForClass(PopulationVector current_N, int current_class) 
			throws BTFMatrixErrorException, InternalErrorException, InconsistentLinearSystemException, OperationNotSupportedException;		
}
