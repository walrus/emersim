/**
 * Copyright (C) 2010, Michail Makaronidis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmt.jmva.analytical.solvers.DataStructures;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * This class is a representation of rational numbers. Essentially, it uses two 
 * BigIntegers to represent any rational number. It also contains the necessary
 * methods for the basic arithmetic operations.
 * 
 * @author Michail Makaronidis, 2010
 * 
 */
public class BigRational implements Comparable<BigRational> {

    /**
     * This is a variable equal to zero.
     */
    public final static BigRational ZERO = new BigRational(0);
    /**
     * This is a variable equal to one.
     */
    public final static BigRational ONE = new BigRational(1);
    /**
     * This is a variable equal to minus one.
     */
    public final static BigRational MINUS_ONE = new BigRational(-1);
    private BigInteger num;   // the numerator
    private BigInteger den;   // the denominator
    private boolean undefined = false;
    private BigDecimal asBigDecimal;

    /**
     * The constructor creates and initialises a new BigRational object
     * @param numerator The numerator of the rational number
     * @param denominator The denominator of the rational number
     */
    public BigRational(int numerator, int denominator) {
        this(new BigInteger("" + numerator), new BigInteger("" + denominator));
    }

    /**
     * The constructor creates and initialises a new BigRational object where the
     * rational is an integer (no denominator is needed).
     *
     * @param numerator The number
     */
    public BigRational(int numerator) {
        this(numerator, 1);
    }

    // 
    /**
     * The constructor creates and initialises a new BigRational object from a
     * string, e.g., "-343/1273".
     *
     * @param s The string representing the number
     */
    public BigRational(String s) {
        String[] tokens = s.split("/");
        if (tokens.length == 2) {
            init(new BigInteger(tokens[0]), new BigInteger(tokens[1]), false);
        } else if (tokens.length == 1) {
            init(new BigInteger(tokens[0]), BigInteger.ONE, true);
        } else {
            throw new RuntimeException("Parse error in BigRational");
        }
    }

    /**
     * The constructor creates and initialises a new BigRational object
     * @param numerator The numerator of the rational number
     * @param denominator The denominator of the rational number
     */
    public BigRational(BigInteger numerator, BigInteger denominator) {
        init(numerator, denominator, false); // we do not know if the fraction is
        // in reduced form.
    }

    /**
     * This private constructor creates and initialises a new BigRational object. It
     * is used by the other constructors, because we do not always need to
     * reduce the fraction. Control over this can speed the creation of such
     * objects.
     *
     * @param numerator The numerator of the rational number
     * @param denominator The denominator of the rational number
     * @param isNormal True if the fraction is already in reduced form, false otherwise.
     *
     */
    private BigRational(BigInteger numerator, BigInteger denominator, boolean isNormal) {
        init(numerator, denominator, isNormal);
    }

    /**
     * The constructor creates and initialises a new BigRational object
     * @param numerator The numerator of the rational number
     * @param denominator The denominator of the rational number
     */
    public BigRational(BigDecimal numerator, BigDecimal denominator) {
        int numeratorDecimalDigits = numerator.scale();
        int denominatorDecimalDigits = denominator.scale();
        int maxDecimalDigits = Math.max(numeratorDecimalDigits, denominatorDecimalDigits);
        if (maxDecimalDigits >= 0) {
            BigDecimal multiplicationFactor = BigDecimal.TEN.pow(maxDecimalDigits);
            BigInteger newNumerator = numerator.multiply(multiplicationFactor).toBigIntegerExact(); // no exception should be throwed
            BigInteger newDenominator = denominator.multiply(multiplicationFactor).toBigIntegerExact(); // no exception should be throwed
            init(newNumerator, newDenominator, false);
        } else {
            throw new RuntimeException("Not yet implemented!");
        }
    }

    /**
     * The constructor creates and initialises a new BigRational object and sets it
     * equal to a BigDecimal. No denominator is needed as input, it is added
     * automatically.
     *
     * @param a The number
     */
    public BigRational(BigDecimal a) {
        this(a, BigDecimal.ONE);
    }

    /**
     * The constructor creates and initialises a new BigRational object and sets it
     * equal to a BigInteger. No denominator is needed as input, it is added
     * automatically.
     *
     * @param a The number
     */
    public BigRational(BigInteger a) {
        this(a, BigInteger.ONE);
    }

    /**
     * The constructor creates and initialises a new BigRational object and sets it
     * equal to a double. No denominator is needed as input, it is added
     * automatically.
     *
     * @param d The number
     */
    public BigRational(double d) {
        this(new BigDecimal(d), BigDecimal.ONE);
    }

    /**
     * The constructor creates and initialises a new BigRational object with the
     * default value of zero.
     *
     */
    public BigRational() {
        this(BigDecimal.ZERO);
    }

    /**
     * Returns a copy of the current BigRational.
     * Amended by Jack, to copy undefined as well
     *
     * @return The BigRational object copy
     */
    public BigRational copy() {
    	BigRational b = new BigRational(num, den);
    	if (undefined) {
    		b.makeUndefined();
    	}
    	return b;
    }

    /**
     * This method is the core of all constructors. It takes the numerator and
     * the  denominator as BigIntegers and produces a BigRational, reducing the
     * fraction if needed.
     *
     * @param numerator The numerator of the rational number
     * @param denominator The denominator of the rational number
     * @param isNormal True if the fraction is already in reduced form, false otherwise.
     */
    private void init(BigInteger numerator, BigInteger denominator, boolean isNormal) {
        // If isNormal, then we do not need to reduce the fraction and compute the gcd
        // deal with x / 0
        if (denominator.equals(BigInteger.ZERO)) {
            throw new RuntimeException("Denominator is zero");
        }

        if (!isNormal) {
            // reduce fraction
            BigInteger g = numerator.gcd(denominator);

            num = numerator.divide(g);
            den = denominator.divide(g);
        } else {
            num = numerator;
            den = denominator;
        }
    }

    /**
     * This method ensures that the denominator of the fraction is positive. It
     * is used for easy comparison.
     */
    private void ensureDenoimatorPositive() {
        // to ensure invariant that denominator is positive
        if (den.signum() == -1) {
            den = den.negate();
            num = num.negate();
        }
    }

    /**
     * Return string representation of the BigRational
     * @return String representation of the BigRational
     */
    @Override
    public String toString() {
        if (den.equals(BigInteger.ONE)) {
            return num.toString();
        } else {
            return num.toString() + "/" + den.toString();
        }
    }

    /**
     * Compares the BigRational with another.
     * @param b The other number
     * @return Returns { -1, 0, + 1 } if a < b, a = b, or a > b
     */
    @Override
    public int compareTo(BigRational b) {
        if (this.num.signum() > b.num.signum()) {
            return +1;
        } else if (this.num.signum() < b.num.signum()) {
            return -1;
        } else {
            BigRational a = this;
            a.ensureDenoimatorPositive();
            return a.num.multiply(b.den).compareTo(a.den.multiply(b.num));
        }
    }

    /**
     * Checks whether this number is greater than another.
     * @param b The other number
     * @return True if this number is greater than the other, else false.
     */
    public boolean greaterThan(BigRational b) {
        if (compareTo(b) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether this number is smaller than another.
     * @param b The other number
     * @return True if this number is smaller than the other, else false.
     */
    public boolean smallerThan(BigRational b) {
        if (compareTo(b) < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether this number is greater than or equal to another.
     * @param b The other number
     * @return True if this number is greater than or equal to the other, else false.
     */
    public boolean greaterOrEqualThan(BigRational b) {
        if (greaterThan(b) || equals(b)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether this number is smaller than or equal to another.
     * @param b The other number
     * @return True if this number is smaller than or equal to the other, else false.
     */
    public boolean smallerOrEqualThan(BigRational b) {
        if (smallerThan(b) || equals(b)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Decides if this BigRational is equal to zero.
     *
     * @return True if equal to zero, false otherwise
     */
    public boolean isZero() {
        return (num == BigInteger.ZERO) || (num.signum() == 0);
    }
 /**
     * Decides if this BigRational is equal to one.
     *
     * @return True if equal to one, false otherwise
     */
    public boolean isOne() {
        return (this == ONE) || (this.equals(ONE));
    }

    /**
     * Decides if this BigRational is equal to minus one.
     *
     * @return True if equal to minus one, false otherwise
     */
    public boolean isMinusOne() {
        return (this == MINUS_ONE) || (this.equals(MINUS_ONE));
    }

    /**
     * Decides if this BigRational is positive.
     *
     * @return True if positive, false otherwise
     */
    public boolean isPositive() {
        this.ensureDenoimatorPositive();
        return num.signum() == 1;
    }

    /**
     * Decides if this BigRational is negative.
     *
     * @return True if negative, false otherwise
     */
    public boolean isNegative() {
        this.ensureDenoimatorPositive();
        return num.signum() == -1;
    }

    /**
     * Returns a BigRational object equal to the absolute value of this BigRational.
     * @return The absolute value BigRational
     */
    public BigRational abs() {
        int s = num.signum();
        if (s == -1) {
            return this.negate();
        } else {
            return this;
        }
    }

    /**
     * Decides if this BigRational is equal to another.
     *
     * @param y The other BigRational
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object y) {
        if (y == this) {
            return true;
        }
        if (y == null) {
            return false;
        }
        if (y.getClass() != this.getClass()) {
            return false;
        }
        BigRational b = (BigRational) y;
        b.ensureDenoimatorPositive();
        this.ensureDenoimatorPositive();
        return compareTo(b) == 0;
    }

    /**
     * Returns a hashCode consistent with equals() and compareTo().
     * @return The hashCode
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Multiplies this number (a) with another (b)
     * @param b The number b
     * @return The number a*b
     */
    public BigRational multiply(BigRational b) {
        BigRational a = this;
        if (a.isZero()) {
            return BigRational.ZERO;
        } else if (this.isOne()) {
            return b;
        } else if (b.isOne()) {
            return this;
        } else if (a.isInteger()) {
            return new BigRational(a.num.multiply(b.num), b.den);
        } else if (b.isInteger()) {
            return new BigRational(a.num.multiply(b.num), a.den);
        } else {
            return new BigRational(a.num.multiply(b.num), a.den.multiply(b.den));
        }
    }

    /**
     * Multiplies this number (a) with another (b)
     * @param b The number b
     * @return The number a*b
     */
    public BigRational multiply(BigInteger b) {
        BigRational a = this;
        if (a.isZero() || b.equals(BigInteger.ZERO)) {
            return BigRational.ZERO;
        } else if (a.isInteger()) {
            return new BigRational(a.num.multiply(b), BigInteger.ONE);
        } else {
            return new BigRational(a.num.multiply(b), a.den);
        }
    }

/**
 * Decides whether the BigRational represents an integer number.
 * @return True if it represents an integer, no otherwise
 */
    public boolean isInteger() {
        return (this.den.equals(BigInteger.ONE));
    }

    /**
     * Adds this number (a) to another (b)
     *
     * @param b The number b
     * @return The number a+b
     */
    public BigRational add(BigRational b) {
        BigRational a = this;
        BigInteger numerator = a.num.multiply(b.den).add(b.num.multiply(a.den));
        BigInteger denominator = a.den.multiply(b.den);
        return new BigRational(numerator, denominator);
    }

    /**
     * Negates this number (a)
     *
     * @return The number -a
     */
    public BigRational negate() {
        BigRational toReturn = new BigRational(num.negate(), den, true);
        toReturn.ensureDenoimatorPositive();
        return toReturn;
    }

    /**
     * Subtracts a number (b) from this one (a)
     *
     * @param b The number b
     * @return The number a-b
     */
    public BigRational subtract(BigRational b) {
        BigRational a = this;
        if (b.isZero()) {
            return this;
        } else if (this.isZero()) {
            return b.negate();
        } else if (this.equals(b)) {
            return BigRational.ZERO;
        } else if (this.den == b.den) {
            return new BigRational(this.num.subtract(b.num), this.den);
        } else if (this.den.equals(BigInteger.ONE)) {
            return new BigRational(this.num.multiply(b.den).subtract(b.num), b.den);
        } else if (b.den.equals(BigInteger.ONE)) {
            return new BigRational(this.num.subtract(this.den.multiply(b.num)), this.den);
        } else {
            BigInteger numerator = a.num.multiply(b.den).subtract(b.num.multiply(a.den));
            BigInteger denominator = a.den.multiply(b.den);
            return new BigRational(numerator, denominator);
        }
    }

    /**
     * Return the reciprocal (1/a) of this number (a)
     *
     * @return The number 1/a
     */
    public BigRational reciprocal() {
        return new BigRational(den, num, true);
    }

    /**
     * Divides this number (a) by another (b)
     * @param b The number b
     * @return The number a/b
     */
    public BigRational divide(BigRational b) {
        if (!b.isZero()) {
            /*if (this.isZero()) {
            return ZERO;
            }/* else if (b.isOne()) {
            return this;
            } else if (this.isOne()) {
            return b.reciprocal();
            }/* else if (b.isMinusOne()) {
            return this.negate();
            } else if (this.isMinusOne()) {
            return b.reciprocal().negate();
            } *///else {
            BigRational toRet = this.multiply(b.reciprocal());
            toRet.ensureDenoimatorPositive();
            return toRet;
            //}
        } else {
            throw new ArithmeticException("Division by zero!");
        }
    }

    private BigRational divide(BigInteger b) {
        if (b.compareTo(BigInteger.ZERO) != 0) {
            BigInteger newDen = den.multiply(b);
            BigRational toRet = new BigRational(num, newDen);
            toRet.ensureDenoimatorPositive();
            return toRet;
        } else {
            throw new ArithmeticException("Division by zero!");
        }
    }

    /**
     * Computes this number raised to the power of another
     * @param n The exponent
     * @return The result of the exponentiation
     */
    public BigRational pow(int n) {
        BigInteger newNumerator = num.pow(n);
        BigInteger newDenominator = den.pow(n);
        return new BigRational(newNumerator, newDenominator);
    }

    /**
     * Returns the number as a BigDecimal. This can only work if the division of
     * the numerator by the denominator does not have an infinite decimal
     * expansion. This can be ensured by checking isBigDecimal() first.
     *
     * @return The division result
     */
    public BigDecimal asBigDecimal() {
        if (asBigDecimal != null) {
            return asBigDecimal;
        } else {
            BigDecimal result = new BigDecimal(num);
            try {
                result = result.divide(new BigDecimal(den));
            } catch (ArithmeticException ex) {
                result = result.divide(new BigDecimal(den), 60, RoundingMode.HALF_EVEN);
            }
            return result;
        }
    }

    /**
     * Returns whether the number can be expressed as a BigDecimal. This can
     * only happen if the division of the numerator by the denominator does not
     * have an infinite decimal expansion.
     *
     * @return True if it can be expressed as a BigDecimal, false otherwise.
     */
    public boolean isBigDecimal() {
        try {
            BigDecimal result = new BigDecimal(num);
            asBigDecimal = result.divide(new BigDecimal(den));
        } catch (ArithmeticException ex) {
            return false;
        }
        return true;
    }

    /**
     * Approximates the current rational number as a double. Leads to loss of
     * precision both in decimal digits and (possibly) in magnitude (if
     * this > MAX_DOUBLE), therefore it is used only for printing purposes and
     * not for calculations.
     *
     * @return The double approximating this rational number.
     */
    public double approximateAsDouble() {
        if (isBigDecimal()) {
            return asBigDecimal().doubleValue();
        } else {
            BigDecimal toReturn = new BigDecimal(num);
            toReturn = toReturn.setScale(11);
            toReturn = toReturn.divide(new BigDecimal(den), RoundingMode.HALF_EVEN);
            return toReturn.doubleValue();
        }
    }

    /**
     * Flags the current BigRational as indeterminable.
     */
    public void makeUndefined() {
        undefined = true;
    }

    /**
     * Flags the current BigRational as determinable (default).
     */
    public void makeDefined() {
        undefined = false;
    }

    /**
     * Checks whether the current BigRational is flagged as indeterminable.
     *
     * @return True if it is indeterminable, false otherwise.
     */
    public boolean isUndefined() {
        return undefined;
    }

    /**
     * Returns (this mod m), allowing for negative numbers or zeros as results.
     * @param m The divisor
     * @return The result of the modulo operation
     */
    public BigRational mod(BigInteger m) {
        if (undefined) {
            return this; // We cannot compute the mod of an "uncomputable" number.
        } else if (this.isNegative()) {
            return this.negate().mod(m).negate();
        } else {
            BigRational toReturn;
            BigInteger a = this.num;
            BigInteger b = this.den.multiply(m);
            toReturn = new BigRational(a.mod(b), den);
            return toReturn;
        }
    }

    /**
     * Returns (this mod m), allowing for positive numbers or zeros as results.
     * @param m The divisor
     * @return The result of the modulo operation
     */
    public BigRational modNormal(BigInteger m) {
        if (undefined) {
            return this; // We cannot compute the mod of an "uncomputable" number.
        } else {
            BigRational toReturn;
            BigInteger a = this.num;
            BigInteger b = this.den.multiply(m);
            toReturn = new BigRational(a.mod(b), den);
            return toReturn;
        }
    }

}
