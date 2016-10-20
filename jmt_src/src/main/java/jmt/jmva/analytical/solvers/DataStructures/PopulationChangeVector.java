package jmt.jmva.analytical.solvers.DataStructures;

/**
 * This class implements the PopulationChangeVector object, which are used in 
 * in the definition of the CoMoM basis. It extends an EnhancedVector object.
 *
 * @author Jack Bradshaw, 2012
 */
@SuppressWarnings("serial")
public class PopulationChangeVector extends EnhancedVector   {

	/**
     * Creates an empty PopulationChangeVector object.
     */
    public PopulationChangeVector() {
        super();
    }

    /**
     * Creates an PopulationChangeVector with content equal to the given matrix.
     *
     * @param P The matrix containing the vector elements
     */
    public PopulationChangeVector(Integer[] P) {
        super(P);
    }

    /**
     * Creates a new PopulationChangeVector of specific length, where
     * all elements are equal to a specific value.
     *
     * @param k The value of all elements
     * @param length The length of the PopulationChangeVector
     */
    public PopulationChangeVector(int k, int length) {
        super(k, length);
    }
    
    /**
     * This method returns a copy of the current PopulationChangeVector object. Position
     * and delta stacks are disregarded.
     *
     * @return Copy of the initial PopulationChangeVector object.
     */
    @Override
    public PopulationChangeVector copy() {
       PopulationChangeVector c = new PopulationChangeVector();
        this.copyTo(c);
        return c;
    }    
       
    public int sum(int from, int to) {
    	int total = 0;
    	for (;from <to; from++) {
    		total += get(from);
    	}
    	return total;
    }
    
    //index inclusive
    public int sumTail(int index) {
    	return sum(index, size());    
    }
    
    //index inclusive
    public int sumHead(int index) {
    	return sum(0, index +1);    
    }
       
    /**
     * returns index of the right-most non-zero element
     * indices start from 0
     */
    public int RightMostNonZero() {
    	for (int i = size() - 1; i >= 0; i-- ) {
    		if (get(i) != 0) return i;
    	}
    	return -1;
    }
}
