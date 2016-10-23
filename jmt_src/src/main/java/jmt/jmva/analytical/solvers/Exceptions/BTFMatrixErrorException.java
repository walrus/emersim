package jmt.jmva.analytical.solvers.Exceptions;

/**
 * This class represents an exception that can be thrown if an internal error is
 * encountered where solving the BTF linear system
 *
 * @author Jack Bradshaw, 2012
 */
public class BTFMatrixErrorException extends Exception {

    /**
     * Creates an BTFMatrixErrorException with an exception message.
     *
     * @param string The exception message
     */
    public BTFMatrixErrorException(String string) {
        super(string);
    }

}