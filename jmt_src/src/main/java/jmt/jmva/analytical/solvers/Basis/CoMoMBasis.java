package jmt.jmva.analytical.solvers.Basis;

import java.util.ArrayList;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;
import jmt.jmva.analytical.solvers.DataStructures.MultiplicitiesVector;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * A class to encapsulate the Basis of CoMoM
 * 
 * The basis is intialised according to the initial conditions of CoMoM. 
 * 
 * An ordered list of PopulationChangeVectors is generated to provide the indexOf() functionality
 * i.e. to return the index of a normalising constant in the basis.
 * Subclasses can specify a Comparator to sort this list as they choose.
 * 
 * Subclasses can specify a Comparator to sort this list as they choose.
 * 
 * @author Jack Bradshaw 
 */
public class CoMoMBasis extends Basis{		
	
	
	/**
	 * Constructor
	 * @param m The Queueing Network Model under study
	 */
	public CoMoMBasis(QNModel m) {
		super(m);		
		initialise();
	}	
	
	private void initialise() {
		order = new ArrayList<PopulationChangeVector>();
		
		//Generate all the possible population change vectors
		generate();		
		
	}
	
	/**
	 * Fills the list 'order' with all positive vectors of the form (n_1, n_2, ..., n_(R-1), 0 ),
	 * where n_1 + n_2 + ... + n_(R-1) <= M
	 */
	private void generate() {
		
		PopulationChangeVector v = new PopulationChangeVector(0,R);		
		order.add(v);
		int current_list_end= 1;
		for (int i = 1; i <= M; i++) {
			for (int j = 0; j < R - 1; j++) {
				for (int k = current_list_end - MiscFunctions.binomialCoefficient(R - 1 - j + i - 2, i - 1); 
					k < current_list_end; k++) {
						v = order.get(k);						
						v.plusOne(j+1);
						order.add(v.copy());						
						v.restore();
				}				
			}
			current_list_end += MiscFunctions.binomialCoefficient(R - 1 + i - 1 , i );			
		}
	}
	
	/**
	 * Finds the index of Normalising Constant G(N-n, m) as determined by n and m
	 * @param n Change in population vector
	 * @param m Multiplicity vector
	 * @return Index of G(N-n, m) in basis
	 * @throws InternalErrorException
	 */
	public int indexOf(PopulationChangeVector n, MultiplicitiesVector m) throws InternalErrorException {		
		int queue_added = m.whichSingleQueueAdded();
		return indexOf(n,queue_added);		
	}
	
	/**
	 * Finds the index of Normalising Constant G(N-n, m) as determined by n and m
	 * @param n Change in population vector
	 * @param m index of queue added (0 for no queue added)
	 * @return Index of G(N-n, m) in basis
	 * @throws InternalErrorException
	 */
	public int indexOf(PopulationChangeVector n, int m) throws InternalErrorException {
		int population_position = order.indexOf(n);
		if (population_position == -1) throw new InternalErrorException("Invalid PopulationChangeVector");
		
		//order multiplicities 0,1,2,3,...M						
		return population_position*(qnm.M + 1) + m;
	}
	
	/**
	 * @param index index in order
	 * @return Vector store in 'order' at 'index'
	 */
	public PopulationChangeVector getPopulationChangeVector(int index) {
		return order.get(index);
	}
	
	
	/**
	 * Prints the vectors in the 'order' vector
	 */
	public void print() {
		int total = 0;
		 for (PopulationChangeVector m : order) {
			 m.print();			 
			 total++;
		 }
		 System.out.println("total: " + total);
	}

	/**
	 * Initialises the basis for population (0,...0)
	 * @throws InternalErrorException 
	 */	
	@Override
	public void initialiseBasis() throws InternalErrorException {
		
		System.out.println("Intialising Basis");
			
		// Negative populations have normalising constant equal to ZERO
		for ( int i = 0 ; i < size; i++) {
			basis[i] = BigRational.ZERO;
			previous_basis[i] = BigRational.ZERO;
		}
		
		// Zero populations have normalising constant equal to ONE
		PopulationChangeVector zero_population = new PopulationChangeVector(0,R);
		for (int k = 0; k <= M; k++) {
			int index = indexOf(zero_population,k);
			basis[index] = BigRational.ONE;	
		}	
	}
	
	/**
	 * Calculates the size of the basis to be store in variable size
	 */
	@Override
	public void setSize() {
		size  = (M + 1)*MiscFunctions.binomialCoefficient(M + R - 1 , M);		
	}	
	
	/**
	 * Computes Mean Throughput and Mean Queue Length performance indices
	 * and stores them in the queueing network model object, qnm
	 * @throws InternalErrorException
	 */
	public void computePerformanceMeasures() throws InternalErrorException {
		
		//Array of Mean Throughputs per class
		BigRational[] X = new BigRational[qnm.R];
		
		//Array of Mean Queue Lengths
        BigRational[][] Q = new BigRational[qnm.M][qnm.R];
        
        PopulationChangeVector n = new PopulationChangeVector(0,R);
        
        //Computing Throughput
        for (int job_class = 1; job_class < qnm.R; job_class++) {
        	n.plusOne(job_class);
        	System.out.println(n);
        	System.out.println(indexOf(n, 0));
        	X[job_class-1] = (basis[indexOf(n, 0)]).copy().divide(qnm.getNormalisingConstant());
        	n.restore();
        }
        X[qnm.R-1] = (previous_basis[indexOf(n, 0)]).copy().divide(qnm.getNormalisingConstant());        
        
        //Computing Queue Lengths
        for (int queue = 1; queue <= qnm.M; queue++) {
        	for (int job_class = 1; job_class < qnm.R; job_class++) {
        		Q[queue-1][job_class-1] = qnm.getDemandAsBigRational(queue - 1, job_class - 1).copy();
        		n.plusOne(job_class);
        		Q[queue-1][job_class-1] = Q[queue-1][job_class-1].multiply(basis[indexOf(n, queue)]);
        		n.restore();
        		Q[queue-1][job_class-1] = Q[queue-1][job_class-1].divide(qnm.getNormalisingConstant());
        	}    
        	Q[queue-1][qnm.R-1] = qnm.getDemandAsBigRational(queue - 1, qnm.R - 1).copy();    		
        	Q[queue-1][qnm.R-1] = Q[queue-1][qnm.R-1].multiply(previous_basis[indexOf(n, queue)]);    		
        	Q[queue-1][qnm.R-1] = Q[queue-1][qnm.R-1].divide(qnm.getNormalisingConstant());        	
        }
        
        //Store outcome in queueing network model
        qnm.setPerformanceMeasures(Q, X);		
	}
	
	/**
	 * Returns the Normalising Constant for the current computed population  
	 * @throws InternalErrorException 
	 */
	public BigRational getNormalisingConstant() throws InternalErrorException {
		PopulationChangeVector zeros = new PopulationChangeVector(0,R);
		BigRational G = basis[indexOf(zeros, 0)];		
		return G;	
	}

	//TODO currently just for debugging purposes
	public void print_values() {
		for (int col = 0; col < size*2; col++ ) {
			System.out.print("-");
		}
		System.out.println("\nBasis Values: \n");		
		for (int col = 0; col < size; col++ ) {
			if (basis[col].isUndefined()) {
				System.out.print("*" + "\n");
			} else {
				System.out.print(basis[col] + "\n");
			}
		}
		System.out.print("\n");
		for (int col = 0; col < size*2; col++ ) {
			System.out.print("-");
		}
		System.out.print("\n\n");				
	}
	
	//For testing purposes
	public ArrayList<PopulationChangeVector> getOrder() {
		return order;
	}
}
