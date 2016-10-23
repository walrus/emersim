package jmt.jmva.analytical.solvers.Basis;

import jmt.jmva.analytical.solvers.Basis.Comparators.SortVectorMatLab;
import jmt.jmva.analytical.solvers.DataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 *A concrete implementation of the CoMoMBasis that orders the basis in the same way as the MatLab implementation, for comparison.
 * @author Jack Bradshaw
 */
public class CoMoMBasisMLorder extends BTFCoMoMBasis{

	public CoMoMBasisMLorder(QNModel qnm) {
		super(qnm);
		setComparator(new SortVectorMatLab());
	}

	@Override
	public int indexOf(PopulationChangeVector n, int m) throws InternalErrorException {
		
		int population_position = order.indexOf(n);
		int queue_added = m;	
		
		if (population_position == -1) throw new InternalErrorException("Invalid PopulationChangeVector");
		
		if (queue_added == 0) {
			return MiscFunctions.binomialCoefficient(M + R - 1 , M)*M + population_position;
		} 		
		return population_position * M + queue_added - 1;	
	}
}
