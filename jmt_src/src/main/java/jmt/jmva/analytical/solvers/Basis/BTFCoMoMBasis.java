package jmt.jmva.analytical.solvers.Basis;

import jmt.jmva.analytical.solvers.Basis.Comparators.SortVectorByhThenRmnz;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 *A concrete implementation of the CoMoMBasis that orders the basis in the way required for the BTF linear systems
 * @author Jack Bradshaw
 */
public class BTFCoMoMBasis extends CoMoMBasis {

	public BTFCoMoMBasis(QNModel m) {
		super(m);
		setComparator(new SortVectorByhThenRmnz());
	}	
	
	@Override
	public int indexOf(PopulationChangeVector n, int m) throws InternalErrorException {	
		 
		//Find the position of the n vector in the ordering
		int population_position = order.indexOf(n);		
		int queue_added = m;	
		
		if (population_position == -1) throw new InternalErrorException("Invalid PopulationChangeVector:" + n);
		
		//No queues added, constant is in Lambda_Y
		if (queue_added == 0) {
			return MiscFunctions.binomialCoefficient(M + R - 1 , M)*M + population_position;
		} 
		
		//Queue added, constant is in Lambda_X
		return population_position * M + queue_added - 1;	
	}
}
