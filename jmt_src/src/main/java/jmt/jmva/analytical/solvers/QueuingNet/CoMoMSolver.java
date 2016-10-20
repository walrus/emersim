package jmt.jmva.analytical.solvers.QueuingNet;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.BTFCoMoMBasis;
import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.LinearSystem;
import jmt.jmva.analytical.solvers.LinearSystem.Simple.SimpleLinearSystem;

/**
 * This class provides the high-level implementation of the CoMoM algorithm, it is programmed 
 * to the interface provided by the LinearSystem class. Sub-classes must choose the linear system they 
 * are to use.
 * 
 * @author Jack Bradshaw *
 */
public class CoMoMSolver extends QNSolver {

	private  int M,R;	
	
	private PopulationVector current_N;
	private PopulationVector target_N;
	
	protected LinearSystem system; 
	
	protected CoMoMBasis basis;
	
	public CoMoMSolver(QNModel qnm) throws InternalErrorException {
		super(qnm);
		
		M = qnm.M;
		R = qnm.R;	
		target_N = qnm.N;
		
		System.out.println("Model under study:\n");
		qnm.printModel();
		System.out.println("\n");		
	}

    @Override
    public void computeNormalisingConstant() throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
		
		current_N = new PopulationVector(0,R);		
				
		for (int _class = 1; _class <= R; _class++) {
			System.out.println("Working on class " + _class);
			System.out.println("Current Population: " + current_N);
			
			current_N.plusOne(_class);
			system.initialiseForClass(current_N, _class);
		
			solveForClass(_class);			
		}						
		
		//Store the computed normalsing constant
		BigRational G = basis.getNormalisingConstant();
		qnm.setNormalisingConstant(G);			
						
	}
    
    private  void solveForClass(int _class) throws InternalErrorException, OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
		/*
		//If no jobs of current_class in target population, move onto next class
		if (target_N.get(current_class - 1) == 0) {
			return;
		}				
		*/
		for (int class_population = current_N.get(_class - 1); 
				class_population <= target_N.get(_class - 1); 
				class_population++ ) {
			
			
			System.out.println("Solving for population: " + current_N);
			System.out.println(class_population);
			
			system.update(class_population);
			
			system.solve();
						
			if (class_population < target_N.get(_class - 1)) {
				
				current_N.plusOne(_class);
			}
		}		
	}
    
    @Override
    public void computePerformanceMeasures() throws InternalErrorException {
    	system.computePerformanceMeasures();
    }
}
