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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.Exceptions.IllegalValueInInputFileException;
import jmt.jmva.analytical.solvers.Exceptions.InputFileParserException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

/**
 * This class contains an implementation of the input file reader and parser,
 * as well as a storage data structure for the model and performance evaluation 
 * values. It also implements some useful operations using the model, such as
 * various estimations.
 * 
 * @author Michail Makaronidis, 2010
 */
public class QNModel {
	
	/**
	 * The possible queue types.
	 * DELAY stations are a special form of LD (load dependent) 
	 * stations where the service rate function u_m(i) = i.
	 * N.B. Maintain this order to match JMT implementation.
	 * @author Ben Homer
	 */
	public enum QueueType { LD, LI, DELAY };

	/**
     * Number of classes R
     */
    public int R;
    /**
     * Number of queues M
     */
    public int M;
    /**
     * Contains total number of jobs for each class
     */
    public PopulationVector N;
    /**
     * Contains the mean delays (think time) for each class
     */
    public double Z[];
    /**
     * Contains the multiplicities for each queue
     */
    public MultiplicitiesVector multiplicities;
    /**
     * Contains the service demands for each class at each queue
     */
    private double[][] D;
    /**
     * Contains the types of each station.
     * Added by Ben Homer for Tree Convolution.
     */
    private QueueType[] T;
    private BigRational[] X;
    private BigRational[][] Q;
    private BigRational G;
    private boolean isNormalisingCOnstantComputed = false;
    private boolean arePerformanceMeasuresComputed = false;
    
    public static final String TempFileName = "tempModel.qn";

    /**
     * Creates a new QNModel object and initialises it according to an input file.
     *
     * @param filename The path to the input file
     * @throws InputFileParserException An exception is thrown if any error is encountered during input file parsing
     *
     */
    public QNModel(String filename) throws InputFileParserException {
        super();
        R = M = 0;
        N = new PopulationVector();
//        Z = new EnhancedVector();
        multiplicities = new MultiplicitiesVector();
        readModel(filename);
        
        T = new QueueType[M];
        for (int i = 0; i < M; i++) {
        	T[i] = QueueType.LI;
        }
    }

    /**
     * Creates a new QNModel object and initialises in according to the given
     * arguments.
     * @param R The number of classes
     * @param M The number of queues
     * @param T The types of each queue
     * @param N The array containing the population per class
     * @param Z The array containing the total delay per class
     * @param multiplicities The array containing the queue multiplicities
     * @param D The array containing the service demands
     * @throws Exception Thrown if the arguments' sizes do not match.
     */
    public QNModel(int R, int M, int[] t, Integer[] N, double[] Z, Integer[] multiplicities, double[][] D) throws Exception {
        this.R = R;
        this.M = M;
        if ((N.length != R) || (Z.length != R) || (multiplicities.length != M) || (D.length != M) || (D[0].length != R)) {
            throw new Exception("Model sizes do not match");
        }

        this.N = new PopulationVector(N);
        this.Z = Z;
        this.multiplicities = new MultiplicitiesVector(multiplicities);
        this.D = D;
        this.T = new QueueType[t.length];
        QueueType[] qtVals = QueueType.values();
        for (int i = 0; i < T.length; i++) {
        	this.T[i] = qtVals[t[i]];
        }
    }
    
    /**
     * Copy constructor which creates a new QNModel object and initialises it according to an existing QNModel.
     *
     * @param qnm The QNModel to copy from.
     *
     */
    public QNModel(QNModel qnm) {
    	this.R = qnm.R;
    	this.M = qnm.M;
    	this.T = qnm.T;
    	this.N = qnm.N.copy();
    	this.Z = qnm.Z;
    	this.multiplicities = qnm.multiplicities.copy();
    	this.D = qnm.D;
    }

    /**
     * Returns the service demand of a class at a queue. Indices start from 0.
     * @param k Queue index
     * @param r Class index
     * @return The corresponding service demand
     */
    public double getDemand(int k, int r) {
        return D[k][r];
    }

    /**
     * Returns the service demand of a class at a queue as a BigDecimal object.
     * Indices start from 0.
     *
     * @param k Queue index
     * @param r Class index
     * @return The corresponding service demand
     */
    public BigDecimal getDemandAsBigDecimal(int k, int r) {
        return new BigDecimal(getDemand(k, r));
    }

    /**
     * Returns the service demand of a class at a queue as a BigInteger object.
     * Indices start from 0.
     *
     * @param k Queue index
     * @param r Class index
     * @return The corresponding service demand
     */
    public BigInteger getDemandAsBigInteger(int k, int r) {
        return new BigInteger(Integer.toString((int)getDemand(k, r)));
    }

    /**
     * Returns the service demand of a class at a queue as a BigRational object.
     * Indices start from 0.
     *
     * @param k Queue index
     * @param r Class index
     * @return The corresponding service demand
     */
    public BigRational getDemandAsBigRational(int k, int r) {
        return new BigRational(getDemand(k, r));
    }

    /**
     * Returns the delay (think time) of a class. Indices start from 0.
     *
     * @param r Class index
     * @return The corresponding delay (think time)
     */
    public double getDelay(int r) {
        return Z[r];
    }

    /**
     * Returns the delay (think time) of a class as a BigDecimal object .
     * Indices start from 0.
     *
     * @param r Class index
     * @return The corresponding delay (think time)
     */
    public BigDecimal getDelayAsBigDecimal(int r) {
        return new BigDecimal(getDelay(r));
    }

    /**
     * Returns the delay (think time) of a class as a BigInteger object .
     * Indices start from 0.
     *
     * @param r Class index
     * @return The corresponding delay (think time)
     */
    public BigInteger getDelayAsBigInteger(int r) {
        return new BigInteger(Integer.toString((int)getDelay(r)));
    }

    /**
     * Returns the delay (think time) of a class as a BigRational object .
     * Indices start from 0.
     *
     * @param r Class index
     * @return The corresponding delay (think time)
     */
    public BigRational getDelayAsBigRational(int r) {
        return new BigRational(getDelay(r));
    }
    
    /**
     * Returns the type of the station/queue indexed by m.
     * 
     * @param m The index of the station/queue to get the type for.
     * @return The type of the station/queue m.
     */
    public QueueType getQueueType(int m) {
    	return T[m];
    }

    /**
     * Initialises the current QNModel object according to an input file.
     * Decimal numbers are approximated as integers.
     *
     * @param filename The path to the input file
     */
    private void readModel(String filename) throws InputFileParserException {
        FileReader fr = null;
        try {
            fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            StreamTokenizer stok = new StreamTokenizer(br);
            stok.parseNumbers();
            // Begin reading file
            // The first line stores the number of classes (R)
            stok.nextToken();
            if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                Double readR = stok.nval;
                if (hasNoFractionalPart(readR)) {
                    this.R = readR.intValue();
                } else {
                    throw new IllegalValueInInputFileException("Cannot have a decimal value as number of classes (R).");
                }
            } else {
                throw new InputFileParserException("Bad format of input file.");
            }
            
            this.Z = new double[R];
            
            // The second line stores the number of jobs of each class circulating (N)
            for (int i = 0; i < this.R; i++) {
                stok.nextToken();
                if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                    Double readN = stok.nval;
                    if (hasNoFractionalPart(readN)) {
                        N.add(readN.intValue());
                    } else {
                        throw new IllegalValueInInputFileException("Cannot have a decimal value as number of jobs for a class (N).");
                    }
                } else {
                    throw new InputFileParserException("Bad format of input file.");
                }
            }
            // The third line stores the delay (think time) for each class (Z)
            for (int i = 0; i < this.R; i++) {
                stok.nextToken();
                if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                    Double readZ = stok.nval;
                    if (hasNoFractionalPart(readZ)) {
                        Z[i] = readZ.intValue();
                    } else {
                        throw new IllegalValueInInputFileException("Cannot have a decimal value as delay time (Z).");
                    }
                } else {
                    throw new InputFileParserException("Bad format of input file.");
                }
            }
            // The fourth line contains the number of queues (M)
            stok.nextToken();
            if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                Double readM = stok.nval;
                if (readM == 0) {
                    throw new IllegalValueInInputFileException("Cannot have zero as number of queues (M).");
                } else if (hasNoFractionalPart(readM)) {
                    this.M = readM.intValue();
                } else {
                    throw new IllegalValueInInputFileException("Cannot have a decimal value as number of queues (M).");
                }
            } else {
                throw new InputFileParserException("Bad format of input file.");
            }
            D = new double[M][R];
            // And lastly there is a (noOfQueues)x(1+noOfClasses) matrix containing the "multiplicities"
            // on the first column and the service demands (D) on the next ones
            for (int i = 0; i < this.M; i++) {
                stok.nextToken();
                if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                    Double readMultiplicity = stok.nval;
                    if (hasNoFractionalPart(readMultiplicity)) {
                        multiplicities.add(readMultiplicity.intValue());
                    } else {
                        throw new IllegalValueInInputFileException("Cannot have a decimal value as multiplicity.");
                    }
                } else {
                    throw new InputFileParserException("Bad format of input file.");
                }
                for (int j = 0; j < this.R; j++) {
                    stok.nextToken();
                    if ((stok.ttype != StreamTokenizer.TT_EOF) && (stok.ttype == StreamTokenizer.TT_NUMBER)) {
                        Double readD = stok.nval;
                        //if (hasNoFractionalPart(readD)) {
                            D[i][j] = readD.intValue();
                        //} else {
                           // throw new IllegalValueInInputFileException("Cannot have a decimal value as demand.");
                        //}
                    } else {
                        throw new InputFileParserException("Bad format of input file.");
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw new InputFileParserException("File not found.");
        } catch (IOException ex) {
            throw new InputFileParserException("I/O exception");
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                throw new InputFileParserException("I/O exception");
            }
        }
    }

    /**
     * Writes the current QNModel to a file so it can be reused in the future.
     */
    public void writeToFile() {
    	PrintWriter writer;
		try {
//			String path = System.getProperty("user.dir");
			writer = new PrintWriter(TempFileName, "UTF-8");
			writer.println(R);
			StringBuilder sb = new StringBuilder();
			for (int n : N) {
				sb.append(n + " ");
			}
			writer.println(sb.toString());
			
			sb.setLength(0);
			for (double z : Z) {
				sb.append(z + " ");
			}
			writer.println(sb.toString());
			writer.println(M);
			
			for (int m = 0; m < M; m++) {
				sb.setLength(0);
				sb.append(this.multiplicities.get(m) + " ");
				for (int r = 0; r < R; r++) {
					sb.append(D[m][r] + " ");
				}
				
				writer.println(sb.toString());
			}
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Prints the parsed data of the current QNModel object.
     */
    public void printModel() {
        System.out.println(this.R);
        System.out.println(this.N.toString());
        System.out.println(this.Z.toString());
        System.out.println(this.M);
        for (int i = 0; i < this.M; i++) {
            System.out.print(this.multiplicities.get(i) + "\t");
            for (int j = 0; j < this.R; j++) {
                System.out.print(this.getDemand(i, j) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Prints out model and model solutions in easily readable format.
     */
    public void prettyPrint() {
    	System.out.println("\n --- Model ---");
    	System.out.println("Stations = " + M + " Classes = " + R);
    	System.out.print("Class populations: "); N.print();
    	System.out.print("Class delays     : " + Arrays.toString(Z));

    	System.out.println("Demands:");
    	for (int m = 0; m < M; m++) {
    		for (int r = 0; r < R; r++) {
    			System.out.print(D[m][r] + " ");
    		}
    		System.out.println();
    	}

    	if (arePerformanceMeasuresComputed) {
    		System.out.println("\n --- Computed Performance Measures ---");
    		
    		System.out.println("Normalization Constant G:");
    		System.out.println(G);
    		System.out.println();
    		
    		System.out.println("Throughputs:");
    		for (BigRational x : X) {
    			System.out.print(x.approximateAsDouble()*1000000 + " ");
    		}
    		System.out.println("\n");
    		
    		System.out.println("Mean queue lengths:");
    		for (int m = 0; m < M; m++) {
        		for (int r = 0; r < R; r++) {
        			System.out.print(Q[m][r].approximateAsDouble() + " ");
        		}
        		System.out.println("");
        	}
    	}
    }

    /**
     * Stores the performance measures (mean throughputs X and mean queue
     * lengths Q) in the model description object (QNModel). The function can
     * only be used once.
     * @param Q Mean queue lengths matrix
     * @param X Mean throughputs matrix
     * @throws InternalErrorException Thrown on attempt to re-assign performance measures
     */
    public void setPerformanceMeasures(BigRational[][] Q, BigRational[] X) throws InternalErrorException {
        if (!arePerformanceMeasuresComputed) {
            this.X = X;
            this.Q = Q;
            arePerformanceMeasuresComputed = true;
        } else {
            throw new InternalErrorException("Performance Measures have already been computed.");
        }
    }

    /**
     * Stores the normalising constant (G) in the model description object
     * (QNModel). The function can only be used once.
     * @param G The normalising constant
     * @throws InternalErrorException Thrown on attempt to re-assign performance measures
     */
    public void setNormalisingConstant(BigRational G) throws InternalErrorException {
        if (!isNormalisingCOnstantComputed) {
            this.G = G;
            isNormalisingCOnstantComputed = true;
        } else {
            throw new InternalErrorException("Normalising Constant has already been computed.");
        }
    }

    /**
     * Returns a string containing the computed normalising constant G.
     * @return A string containing the computed normalising constant G
     */
    public String getPrettyNormalisingConstant() {
        if (isNormalisingCOnstantComputed) {
            if (G.isBigDecimal()) {
                return G.asBigDecimal().toString();
            } else {
                return G.toString();
            }
        } else {
            throw new UnsupportedOperationException("Normalising Constant has not been computed yet.");
        }
    }

    /**
     * Returns the computed normalising constant G.
     * @return The computed normalising constant G
     */
    public BigRational getNormalisingConstant() {
        if (isNormalisingCOnstantComputed) {
            return G;
        } else {
            throw new UnsupportedOperationException("Normalising Constant has not been computed yet.");
        }
    }

    /**
     * Returns the computed mean throughputs X of the current model.
     * @return The computed mean throughputs X
     */
    public BigRational[] getMeanThroughputs() {
        if (arePerformanceMeasuresComputed) {
            return X;
        } else {
            throw new UnsupportedOperationException("Performance Measures have not been computed yet.");
        }
    }

    /**
     * Returns the computed mean queue lengths Q of the current model.
     * @return The computed mean queue lengths Q
     */
    public BigRational[][] getMeanQueueLengths() {
        if (arePerformanceMeasuresComputed) {
            return Q;
        } else {
            throw new UnsupportedOperationException("Performance Measures have not been computed yet.");
        }
    }

    private double getMaxDemand() {
        double max = 0;
        for (int k = 0; k < this.M; k++) {
            for (int r = 0; r < this.R; r++) {
                if (D[k][r] > max) {
                    max = D[k][r];
                }
            }
        }
        return max;
    }

    /**
     * Returns the maximum possible normalising constant of the queueing network.
     * @return The maximum value as a BigInteger object
     * @throws OperationNotSupportedException Thrown if the maximum normalising constant cannot be computed
     */
    public BigInteger getMaxG() throws OperationNotSupportedException {
        double Dmax = MiscFunctions.max(Z);
        for (int k = 0; k < this.M; k++) {
            for (int r = 0; r < this.R; r++) {
                if (D[k][r] > Dmax) {
                    Dmax = D[k][r];
                }
            }
        }
        int Ntot = N.sum();
        int nmax = (int) (Math.round(Math.ceil(Math.log10(Dmax * (Ntot + M + R)))) * Ntot);
        return BigInteger.TEN.pow(nmax + 1);
    }

    /**
     * Returns the maximum value of the queueing network model.
     * @return The maximum value
     * @throws OperationNotSupportedException Thrown if the maximum value cannot be computed
     */
    public double getMaxModelValue() throws OperationNotSupportedException {
        double max = N.max();
        double Zmax = MiscFunctions.max(Z);
        max = (max < Zmax) ? Zmax : max;
        max = (max < getMaxDemand()) ? getMaxDemand() : max;
        return max;
    }

    /**
     * Checks whether a double has a zero fractional part, i.e. it represents an
     * integer number.
     *
     * @param d The number to check
     * @return True if it is an integer, false otherwise
     */
    public boolean hasNoFractionalPart(double d) {
        if (d == (int) d) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method returns a copy of the current MultiplicitiesVector with the values
     * contained in this QNModel object (quening network model).
     *
     * @return The MultiplicitiesVector object
     */
    public MultiplicitiesVector getMultiplicitiesVector() {
        return this.multiplicities.copy();
    }

    /**
     * This method returns a copy of the current PopulationVector with the values
     * contained in this QNModel object (quening network model).
     *
     * @return The PopulationVector object
     */
    public PopulationVector getPopulationVector() {
    	PopulationVector to_return = new PopulationVector();
    	for (int i = 0; i < R; i++) {
    		to_return.add(this.N.get(i));
    	}
        return to_return;
    }

    /**
     * Returns the mean queue lengths as an MxR array of doubles.
     * @return The double[M][R] array.
     */
    public double[][] getMeanQueueLengthsAsDoubles() {
        double[][] toReturn = new double[M][R];
        for (int k = 0; k < M; k++) {
            for (int r = 0; r < R; r++) {
                toReturn[k][r] = Q[k][r].approximateAsDouble();
            }
        }
        return toReturn;
    }

    /**
     * Returns the mean throughputs as an array of R doubles.
     * @return The double[R] array.
     */
    public double[] getMeanThroughputsAsDoubles() {
        double[] toReturn = new double[R];
        for (int r = 0; r < R; r++) {
            toReturn[r] = X[r].approximateAsDouble();
        }
        return toReturn;
    }
    
    /**
     * Added by Jack Bradshaw
     * Prints the preformance measures computed for the model.
     */
    public void printPerformanceMeasures() {
    	System.out.println("\nMean Throughputs:");
    	MiscFunctions.printMatrix(X);
    	
    	System.out.println("Mean Queue Lengths:");
    	MiscFunctions.printMatrix(Q);
    }
    
    /*
     * The Following functions allow the mannual setting of model parameters for testing purposes
     * @author Jack Bradshaw
     */
    public void setM(int m) {
    	this.M = m;
    }
    
    public void setR(int r) {
    	this.R = r;
    }    

	public void setN(int n2) {
		N = new PopulationVector(n2/R,R);		
	}


}
