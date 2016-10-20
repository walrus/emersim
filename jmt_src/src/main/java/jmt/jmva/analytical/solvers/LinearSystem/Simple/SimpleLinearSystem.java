package jmt.jmva.analytical.solvers.LinearSystem.Simple;

import java.math.BigInteger;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Basis.BTFCoMoMBasis;
import jmt.jmva.analytical.solvers.Basis.CoMoMBasis;
import jmt.jmva.analytical.solvers.Control.Main;
import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.LinearSystem.LinearSystem;
import jmt.jmva.analytical.solvers.LinearSystem.Simple.Matrix.StandardMatrix;
import jmt.jmva.analytical.solvers.LinearSystemSolver.ModularSolver;
import jmt.jmva.analytical.solvers.LinearSystemSolver.SimpleSolver;
import jmt.jmva.analytical.solvers.LinearSystemSolver.Solver;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class extends the LinearSystem class to implement the linear system for 
 * the simple CoMoMimplementation.
 * 
 * @author Jack Bradshaw
 */
public class SimpleLinearSystem extends LinearSystem {

private static StandardMatrix A, B;
	
	private int M,R;	
	
	private  Solver solver;
	
	private int num_threads;
	
	public SimpleLinearSystem(QNModel qnm, CoMoMBasis basis, int num_threads)
			throws InternalErrorException {
		super(qnm, basis);
		
		//basis = new CoMoMReorderingBasis(qnm);
		//basis = new BTFCoMoMBasis(qnm);
	
		this.num_threads = num_threads;
		
		M = qnm.M;
		R = qnm.R;
		
		A = new StandardMatrix(basis, basis.getSize());
		B = new StandardMatrix(basis, basis.getSize());
		
		if (num_threads == 1) {
            solver = new SimpleSolver();
        } else {
            solver = new ModularSolver(num_threads);
        }       
	}

	@Override
	public void update(int current_class_population) {
		solver.goToULevel(current_class_population-1);		
	}

	@Override
	public void solve() throws OperationNotSupportedException,
			InconsistentLinearSystemException, InternalErrorException,
			BTFMatrixErrorException {
		
		basis.startBasisComputation();
		
		System.out.println("Solving System...\n");
		BigRational[] sysB  = B.multiply();
		
		BigRational[] result = solver.solve(sysB);
		
		for (int i = 0; i < basis.getSize(); i++) {
			basis.setValue(result[i], i);
		}
	}

	@Override
	public void initialiseMatricesForClass(PopulationVector current_N, int current_class)
			throws InternalErrorException, BTFMatrixErrorException, InconsistentLinearSystemException, OperationNotSupportedException {
		System.out.println("current_class: " + current_class);
		generateAB(current_N, current_class);
		
		System.out.println("Intialising A and B:");
		if (Main.verbose) { 
			System.out.println("A:");
			A.print();
			System.out.println("B:");
			B.print();
		}
		
		 Integer maxA = (int)getMaxAElement();
	     Integer val = basis.getSize();
	     BigInteger maxB = qnm.getMaxG().multiply(new BigInteger(maxA.toString())).multiply(new BigInteger(val.toString()));
	     solver.initialise(A.getArray(), A.getUpdateList(), basis.getUncomputables(), maxA, maxB, new BigRational(qnm.getMaxG()));
	}
	
	 private void generateAB(PopulationVector N, int current_class) throws InternalErrorException {
		 	A.reset();
		 	B.reset();
		 	basis.reset_uncomputables();
		 			
		   	int row = -1;
		   	int col = 0;
		   	PopulationChangeVector n;
		   	for (int i = 0; i < MiscFunctions.binomialCoefficient(M + R - 1 , M); i++)  { //loop over all possible population changes n
		   		n  = basis.getPopulationChangeVector(i).copy(); // To improve bug safety
		   		if (n.sumTail(current_class-1) > 0) {  //potential negative population
		   			for (int k = 0; k <= M; k++) {
		   				row++;
		   				col = basis.indexOf(n,k);
		   				A.write(row, col, BigRational.ONE);
		   				basis.computatble(col);
		   				//System.out.println("Fisrt n: " + n);
		   				//System.out.println("row: " + row);
		   				if (n.sumTail(current_class) > 0) {  //negative population
		   					A.write(row, col, BigRational.ONE);
		   					//System.out.println("1");
		   					col = basis.indexOf(n, k);
		   					//System.out.println("col: " + col);
		   					B.write(row, col, BigRational.ONE);
		   				} else {
		   					//System.out.println("2");
		   					A.write(row, col, BigRational.ONE);
		   					n.minusOne(current_class);    					
		   					col = basis.indexOf(n, k);
		   					//System.out.println("col: " + col);
		   					B.write(row, col,BigRational.ONE);
		   					n.restore();
		   				}    				
		   			}
		   		} else {
		   			if (n.sumHead(current_class - 1) < M) {
		   				for (int k = 1; k <= M; k++) {
		   					//add CE corresponding to G(0, N - n) for each queue k
		   					row++;
		   					col = basis.indexOf(n, k);
		   					A.write(row, col, BigRational.ONE);
		   					basis.computatble(col);
		   					col = basis.indexOf(n, 0);
		   					A.write(row, col, BigRational.ONE.negate());
		   					basis.computatble(col);
		   					for (int s = 1; s <= current_class - 1; s++) {
		   						n.plusOne(s);	
		   						//System.out.println("n: " + n);
		   						col = basis.indexOf(n, k);
		   						n.restore();
		   						A.write(row, col, qnm.getDemandAsBigRational(k-1, s-1).negate());
		   						basis.computatble(col);
		   						//System.out.println("k-1,s-1: " + qnm.getDemandAsBigRational(k-1, s-1));
		   					}
		   					col = basis.indexOf(n, k);
		  					B.write(row, col, qnm.getDemandAsBigRational(k-1, current_class-1));
		  				}
		   				for (int s = 1; s < current_class; s++) {
		   					//add PC  corresponding to G(0, N - n) for each class s less than the current class
		   					row++;
		    				col = basis.indexOf(n, 0);
		   					A.write(row, col, N.getAsBigRational(s-1).subtract(n.getAsBigRational(s-1))); 
		   					basis.computatble(col);
		   					n.plusOne(s);
		   					col = basis.indexOf(n, 0);
		   					A.write(row, col, qnm.getDelayAsBigRational(s-1).negate());
		   					basis.computatble(col);
		   					for (int k = 1; k <= M; k++) { //loop over all queues k (= sum)
		   						col = basis.indexOf(n, k);
		   						A.write(row, col, qnm.getDemandAsBigRational(k-1, s-1).negate());
		   						basis.computatble(col);
		   					}
		   					n.restore();
		   				}
		   			}    			    			
		   		}
		   	}
		   	for (int i = 0; i < MiscFunctions.binomialCoefficient(M + R - 1 , M); i++)  { //loop over all possible population changes n
		   		//add PC of class 'current_class'
		   		n  = basis.getPopulationChangeVector(i).copy(); // To improve bug safety
		   		if (n.sumTail(current_class-1) <= 0) {  // TODO remove <
		   			row++;
		   			col = basis.indexOf(n, 0);
					A.write(row, col, N.getAsBigRational(current_class-1));
					basis.computatble(col);
					A.toBeUpdated(row, col);					
					B.write(row, col, qnm.getDelayAsBigRational(current_class -1 ));
					for ( int k = 1; k <= M; k++) {
						col = basis.indexOf(n, k);
						B.write(row, col, qnm.getDemandAsBigRational( k - 1, current_class -1 ));					
					}
		   		}
		   	}
		}
	 
	 /**
	  * Multiply methods for testing purposes
	  */
	 public BigRational[] multiplyrhs() throws BTFMatrixErrorException {
		 return B.multiply();		 
	 }
	 
	 public BigRational[] multiplylhs() throws BTFMatrixErrorException {
		 return A.multiply();
		 
	 }
	 
	 private double getMaxAElement() throws OperationNotSupportedException {
		 double max = qnm.getMaxModelValue();
	     max *= qnm.multiplicities.max() + qnm.R;
	     return max;
	 }
}
