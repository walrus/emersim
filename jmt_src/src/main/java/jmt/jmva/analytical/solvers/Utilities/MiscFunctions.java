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

package jmt.jmva.analytical.solvers.Utilities;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import jmt.jmva.analytical.solvers.DataStructures.BigRational;

/**
 * This class implements several basic math functions, low-level fast memory
 * copies, matrix type changes, memory usage calculations and printing operations.
 * Its methods are used throughout the program, therefore it was best to gather
 * them all here.
 *
 * @author Michail Makaronidis, 2010
 */
public class MiscFunctions {

	/**
	 * Checks if specified number is power of 2 using bitwise operators.
	 * @param n The number to check.
	 * @return Boolean indicating if n is a power of 2.
	 */
	public static boolean isPowerOf2(int n) {
		return (n > 0) && ((n & (n - 1)) == 0);
	}
	
    /**
     * This method calculates the binomial coefficient C(n, k) ("n choose k")
     * @param n The first number
     * @param k The second number
     * @return The binomial coefficient
     */
    public static int binomialCoefficient(int n, int k) {
        int[] b = new int[n + 1];
        b[0] = 1;

        for (int i = 1; i <= n; ++i) {
            b[i] = 1;
            for (int j = i - 1; j > 0; --j) {
                b[j] += b[j - 1];
            }
        }
        return b[k];
    }

    /**
     * Computes the factorials up to a given number and returns them as a
     * Map<Integer,BigRational>.
     *
     * @param n The number up to which the factorials are computed
     * @return The Map<Integer,BigRational> containing the computed values
     */
    public static Map<Integer, BigRational> computeFactorials(int n) {
        Map<Integer, BigRational> toReturn = new HashMap<Integer, BigRational>();
        BigRational val = BigRational.ONE;
        for (Integer i = 0; i <= n; i++) {
            toReturn.put(i, val);
            val = val.multiply(new BigRational(i + 1));
        }
        return toReturn;
    }
    
    /**
     * Computes the factorials up to a given number and returns them as a
     * Map<Integer,BigDecimal>.
     *
     * @param n The number up to which the factorials are computed
     * @return The Map<Integer,BigDecimal> containing the computed values
     */
    public static Map<Integer, BigDecimal> computeFactorialsAsBigDecimal(int n) {
    	 Map<Integer, BigDecimal> toReturn = new HashMap<Integer, BigDecimal>();
    	 BigDecimal val = BigDecimal.ONE;
         for (Integer i = 0; i <= n; i++) {
             toReturn.put(i, val);
             val = val.multiply(new BigDecimal(i + 1));
         }
         return toReturn;
    }
    
    /**
     * Computes the factorial for a given number and returns the result as a
     * BigRational.
     *
     * @param n The number to calculate the factorial for.
     * @return The result as a BigRational.
     */
    public static BigRational computeFactorial(int n) {
        BigRational val = BigRational.ONE;
        for (Integer i = 0; i < n; i++) {
            val = val.multiply(new BigRational(i + 1));
        }
        return val;
    }
    
    /**
     * Computes the factorial for a given number and returns the result as a
     * BigDecimal.
     *
     * @param n The number to calculate the factorial for.
     * @return The result as a BigDecimal.
     */
    public static BigDecimal computeFactorialAsBigDecimal(int n) {
    	BigDecimal val = BigDecimal.ONE;
        for (Integer i = 0; i < n; i++) {
            val = val.multiply(new BigDecimal(i + 1));
        }
        return val;
    }


    /**
     * Prints a 2-dimensional matrix
     * @param A A 2-dimensional matrix of ints
     */
    public static void printMatrix(int[][] A) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                System.out.format("%3d ", A[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Prints a 2-dimensional matrix
     * @param A A 2-dimensional matrix of BigRationals
     */
    public static void printMatrix(BigRational[][] A) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                if (A[i][j].isUndefined()) {
                    System.out.print("*");
                }
                System.out.format("%2s ", A[i][j].toString());
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Prints a 2-dimensional matrix of BigRationals as doubles.
     * @param A A 2-dimensional matrix of BigRationals
     */
    public static void printPrettyMatrix(BigRational[][] A) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                System.out.format("%3s ", A[i][j].approximateAsDouble());
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Prints a 1-dimensional matrix.
     * @param A A 1-dimensional matrix of ints
     */
    public static void printMatrix(int[] A) {
        int matrixSize = A.length;
        for (int i = 0; i < matrixSize; i++) {
            System.out.format("%3d ", A[i]);
        }
        System.out.println();
    }

    /**
     * Prints a 1-dimensional matrix.
     * @param A A 1-dimensional matrix of BigRationals
     */
    public static void printMatrix(BigRational[] A) {
        int matrixSize = A.length;
        for (int i = 0; i < matrixSize; i++) {
            if (A[i].isUndefined()) {
                System.out.print("*\n");
            } else { //added by Jack            
            	System.out.format("%3s \n", A[i].toString());
            }
        }
        System.out.println();
    }

    /**
     * Prints a 1-dimensional matrix of BigRationals as doubles.
     * @param A A 1-dimensional matrix of BigRationals
     */
    public static void printPrettyMatrix(BigRational[] A) {
        int matrixSize = A.length;
        for (int i = 0; i < matrixSize; i++) {
            System.out.format("%3s ", A[i].approximateAsDouble());
        }
        System.out.println();
    }

    /**
     * Transforms an array of ints to an array of BigRationals.
     * @param s The source array of ints
     * @return The resulting array of BigRationals
     */
    public static BigRational[] createBigRationalArray(int[] s) {
        BigRational[] A = new BigRational[s.length];

        for (int i = 0; i < s.length; i++) {
            A[i] = new BigRational(s[i]);
        }
        return A;
    }

    /**
     * Transforms an array of ints to an array of BigRationals.
     * @param d The source array of ints
     * @return The resulting array of BigRationals
     */
    public static BigRational[][] createBigRationalArray(int[][] d) {
        BigRational[][] A = new BigRational[d.length][d[0].length];


        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                A[i][j] = new BigRational(d[i][j]);
            }
        }

        return A;
    }

    /**
     * Multiplies a two-dimensional matrix of BigRationals with a one-dimensional one.
     * It does not perform a generic multiplication, but it rather checks for
     * linear system inconsistencies as well.
     * @param A The two-dimensional matrix
     * @param v The one-dimensional matrix
     * @return The multiplication result
     */
    public static BigRational[] matrixVectorMultiply(BigRational[][] A, BigRational[] v) {
        //TODO: Matrix multiplication must be parallelised
        int rowsA = A.length;
        int columnsA = A[0].length;
        int rowsV = v.length;

        if (columnsA == rowsV) {
            BigRational[] c = new BigRational[rowsA];
            for (int i = 0; i < rowsA; i++) {
                c[i] = BigRational.ZERO;
                for (int j = 0; j < columnsA; j++) {
                    if (!A[i][j].isZero()) {
                        if (v[j].isPositive()) {
                            c[i] = c[i].add(A[i][j].multiply(v[j]));
                        } else if (v[j].isUndefined()) {
                            c[i] = new BigRational(-1);
                            c[i].makeUndefined();
                            break;
                        }
                    }
                }
            }
            return c;
        } else {
            throw new ArithmeticException("Cannot multiply matrices with wrong sizes! (" + rowsA + "x" + columnsA + ")x(" + rowsV + "x1)");
        }
    }
    
    public static BigRational[] matrixVectorMultiplyJ(BigRational[][] A, BigRational[] v) {
        //TODO: Matrix multiplication must be parallelised
        int rowsA = A.length;
        int columnsA = A[0].length;
        int rowsV = v.length;

        if (columnsA == rowsV) {
            BigRational[] c = new BigRational[rowsA];
            for (int i = 0; i < rowsA; i++) {
                c[i] = BigRational.ZERO;
                for (int j = 0; j < columnsA; j++) {
                    if (!A[i][j].isZero()) {
                        if (!v[j].isZero()) {
                            c[i] = c[i].add(A[i][j].multiply(v[j]));
                        } else if (v[j].isUndefined()) {
                            c[i] = new BigRational(-1);
                            c[i].makeUndefined();
                            break;
                        }
                    }
                }
            }
            return c;
        } else {
            throw new ArithmeticException("Cannot multiply matrices with wrong sizes! (" + rowsA + "x" + columnsA + ")x(" + rowsV + "x1)");
        }
    }

    /**
     * Copy a source array to a destination one. Arrays must be of same size and
     * initialised.
     * @param source Source array
     * @param destination Destination array
     */
    public static void arrayCopy(Object[][] source, Object[][] destination) {
        for (int a = 0; a < source.length; a++) {
            System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
        }
    }

    /**
     * Copy a source array to a destination one. Arrays must be of same size and
     * initialised.
     * @param source Source array
     * @param destination Destination array
     */
    public static void arrayCopy(Object[] source, Object[] destination) {
        System.arraycopy(source, 0, destination, 0, source.length);
    }

    private MiscFunctions() {
        super();
    }

    /**
     * Calculates and returns as a String the actual memory usage inside the JVM.
     * To do so means multiple invocations of the garbage collector.
     * @return A String containing the memory usage
     */
    public static String memoryUsage() {
        long mem;
        // Find memory usage:
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String res;
        if (mem < 1024) {
            res = mem + " B";
        } else if (mem < 1024 * 1024) {
            res = mem / 1024 + " kB";
        } else if (mem < 1024 * 1024 * 1024) {
            res = mem / 1024 / 1024 + " MB";
        } else {
            res = mem / 1024 / 1024 / 1024 + " GB";
        }
        return res;
    }
    
    /**
     * @author Jack Bradshaw
     * Prints a 2-dimensional matrix with zeros as dots.
     * @param A A 2-dimensional matrix of BigRationals
     */
    public static void dotprintMatrix(BigRational[][] A) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                if (A[i][j].isUndefined()) {
                    System.out.print("*");
                }
                if (A[i][j].equals(BigRational.ZERO)) {
                	 System.out.format("%2s ",".");
                } else {
                System.out.format("%2s ", A[i][j].toString());
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void printJVMMemoryStats() {
    	int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        System.out.println(" --- JVM Heap statistics (MB) ---");
        System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        System.out.println("Free Memory:" + runtime.freeMemory() / mb);
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }
    
    public static double max(double[] arr) {
        double max = arr[0];
        for (int j = 1; j < arr.length; j++) {
            if (arr[j] > max) {
                max = arr[j];
            }
        }
    
        return max;
    }
    
    public static double sum(double[] arr) {
        double sum = 0;
        for (int j = 0; j < arr.length; j++) {
           sum += arr[j];
        }
    
        return sum;
    }
}

