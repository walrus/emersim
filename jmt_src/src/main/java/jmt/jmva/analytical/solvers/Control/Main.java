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

package jmt.jmva.analytical.solvers.Control;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.naming.OperationNotSupportedException;

import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.Exceptions.BTFMatrixErrorException;
import jmt.jmva.analytical.solvers.Exceptions.InconsistentLinearSystemException;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.CoMoMBTFSolver;
import jmt.jmva.analytical.solvers.QueuingNet.CoMoMSimpleSolver;
import jmt.jmva.analytical.solvers.QueuingNet.ConvolutionSolver;
import jmt.jmva.analytical.solvers.QueuingNet.MoMSolver;
import jmt.jmva.analytical.solvers.QueuingNet.RECALNonRecursiveSolver;
import jmt.jmva.analytical.solvers.QueuingNet.RECALSolver;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TMVA.TreeMVASolver;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.TreeConvolution.TreeConvolutionSolver;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;
import jmt.jmva.analytical.solvers.Utilities.RandomNetworkGenerator;
import jmt.jmva.analytical.solvers.Utilities.Timer;

/**
 * This is the main class of the program. It reads the input arguments and
 * calls the respective solvers. If the MoM method using the auto-select argument,
 * the prefferable between the serial and the parallel MoM is used.
 *
 * @author Michail Makaronidis, 2010
 * @author Jack Bradshaw, 2012
 * @author Ben Homer, 2014 (added Tree-Convolution and Tree-MVA algorithms).
 */
public class Main {
	/**
	 * Global field to indicate whether or no to print matrices
	 */
	public static boolean verbose = false;
	
    /**
     * Used to test the provided API.
     * @throws BTFMatrixErrorException 
     * @throws InconsistentLinearSystemException 
     * @throws OperationNotSupportedException 
     */
    private static void interfaceTest() throws OperationNotSupportedException, InconsistentLinearSystemException, BTFMatrixErrorException {
        int R = 2, M = 4;

        int[] type = {MoMSolverDispatcher.LI, MoMSolverDispatcher.LI, MoMSolverDispatcher.LD, MoMSolverDispatcher.LD, MoMSolverDispatcher.LD};
        int E = type.length;
        double[][] servt = new double[E][R];

        int[] pop = new int[R];
        pop[0] = 10;
        pop[1] = 10;

        MoMSolverDispatcher mClosed = new MoMSolverDispatcher(R, E, pop);

        //station 1
        servt[0][0] = 10;
        servt[1][0] = 5;
        servt[2][0] = 91;
        servt[3][0] = 4;
        servt[4][0] = 10;

        //station 2
        servt[0][1] = 5;
        servt[1][1] = 9;
        servt[2][1] = 0;
        servt[3][1] = 8;
        servt[4][1] = 10;

        double[][] visits = new double[E][R];

        //station 1
        visits[0][0] = 1;
        visits[1][0] = 1;
        visits[2][0] = 0;
        visits[3][0] = 0;
        visits[4][0] = 0;

        //station 2
        visits[0][1] = 1;
        visits[1][1] = 1;
        visits[2][1] = 0;
        visits[3][1] = 0;
        visits[4][1] = 0;

        if (mClosed.input(type, servt, visits, Runtime.getRuntime().availableProcessors())) {
            try {
                mClosed.solve();
            } catch (InternalErrorException ex) {
                System.err.println("Evaluation Failed!");
                ex.printStackTrace();
            }
        } else {
            System.out.println("Wrong input!");
        }
    }

    // This method prints a help message regarding the command line arguments.
    private static void printHelp() {
        System.out.println("Usage: java -jar MoM.jar <Algorithm> <Output Performance Indices> <Input File> [<Number of Threads>]");
        System.out.println("Output Performance Indices: 0 for no, 1 for yes");
        System.out.println("To generate a random network, type 'Random' for the Input File argument.");
        System.out.println("Available algorithms:");
        System.out.println("0: Convolution");
        System.out.println("1: RECAL (recursive)");
        System.out.println("2: RECAL (non-recursive)");
        System.out.println("3: MoM (parallel)");
        System.out.println("4: MoM (serial)");
        System.out.println("5: MoM (auto-select)");
        System.out.println("6: CoMoM (serial)");
        System.out.println("7: CoMoM (BTF)");
        System.out.println("8: Tree-Convolution");
        System.out.println("9: Tree-MVA");
        
        //System.out.println("999: Interface test mode");
    }

    /**
     * The main function parses the command line arguments and initiates the
     * respective procedures.
     * @param args The command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	/*
    	System.setOut(new java.io.PrintStream(
    		    new java.io.OutputStream() {
    		       public void write(int b) {}
    		    }
    		 ));
    	*/
        if (args.length < 3) {
            printHelp();
            return;
        } else {
        	PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out)); 
        	Timer total_time = new Timer();
        	total_time.start();
        	
        	boolean failed = false;
            try {
                String filename = args[2];
                int algorithm = Integer.parseInt(args[0]);
                if (algorithm == 999) {
                    interfaceTest();
                    return;
                }
                int performanceMeasures = Integer.parseInt(args[1]);
                if (performanceMeasures == 2) {
                	verbose = true;
                }
                if (performanceMeasures != 0 && performanceMeasures != 1 &&  performanceMeasures != 2) {
                    printHelp();
                    return;
                }
                int nThreads;
                if (args.length >= 4) {
                    nThreads = Integer.parseInt(args[3]);
                } else {
                    nThreads = Runtime.getRuntime().availableProcessors();
                }

                /** Added by Ben Homer */
                QNModel qnm;
                if (filename.toLowerCase().equals("random")) {
                	RandomNetworkGenerator rng = new RandomNetworkGenerator(true, 3);
                	int M = 4;
                	int R = 4;
                	double sparcity = 0.5;
		        	if (args.length >= 5) {
		              	//Manual setting M and R
		              	M = Integer.parseInt(args[4]);
		              	R = Integer.parseInt(args[5]);
		              	if (args.length >= 7) {  
		              		sparcity = Double.parseDouble(args[6]);
		              	}
		        	}
                	qnm = rng.generateSparseNetwork(M, R, sparcity, false, false); 
                } else {
                	qnm = new QNModel(filename);
                }
                /* End */
                
                if (args.length >= 5) {
                	//Manual setting M and R
                	int M = Integer.parseInt(args[4]);
                	int R = Integer.parseInt(args[5]);
                	qnm.setM(M);
                	qnm.setR(R);
                	if (args.length >= 7) {   
                		int N = Integer.parseInt(args[6]);
                		qnm.setN(N);
                	}
                }
                
                jmt.jmva.analytical.solvers.QueuingNet.QNSolver c;
                if (algorithm == 5) {
                    int matrixSize = MoMMatrixSize(qnm);
                    if (matrixSize >= 120) {
                        algorithm = 3;
                    } else {
                        algorithm = 4;
                    }
                }
                switch (algorithm) {
                    case 0:
                        c = new ConvolutionSolver(qnm);
                        break;
                    case 1:
                        c = new RECALSolver(qnm);
                        break;
                    case 2:
                        c = new RECALNonRecursiveSolver(qnm);
                        break;
                    case 3:
                        if (nThreads > 1) {
                            c = new MoMSolver(qnm, nThreads);
                            break;
                        } else {
                            printHelp();
                            return;
                        }
                    case 4:
                        c = new MoMSolver(qnm, 1);
                        break;
                    case 6:
                    	c= new CoMoMSimpleSolver(qnm, 1);
                    	break;
                    case 7:
                    	c= new CoMoMBTFSolver(qnm);
                    	break;
                    case 8:
                    	c= new TreeConvolutionSolver(qnm, nThreads);
                    	break;
                    case 9:
                    	c= new TreeMVASolver(qnm, nThreads, false);
                    	break;
                    default:
                        printHelp();
                        return;
                }
                System.out.println("Will read " + filename);
                qnm.printModel();
                c.printWelcome();
                if (!(c instanceof TreeMVASolver)) {
	                c.computeNormalisingConstant();
	                System.out.println("G = " + qnm.getNormalisingConstant());
	                System.out.println("G = " + qnm.getPrettyNormalisingConstant());
	                c.printTimeStatistics();
	                c.printMemUsage();
                }
                if (performanceMeasures == 1) {
                    c.computePerformanceMeasures();
                    System.out.println("\nX = ");
                    MiscFunctions.printPrettyMatrix(qnm.getMeanThroughputs());
                    System.out.println("\nQ = ");
                    MiscFunctions.printPrettyMatrix(qnm.getMeanQueueLengths());
                    System.out.println();
                    c.printTimeStatistics();
                }
            } catch (NumberFormatException ex) {
                System.err.println("\nIllegal argument " + ex.getMessage());
                failed = true;
                /*System.out.println("\n======= DEBUGGING INFO =======");
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);*/
            } catch (Throwable ex) {
                System.err.println("\n" + ex.getMessage());
                ex.printStackTrace();
                failed = true;
                /*
                System.out.println("\n======= DEBUGGING INFO =======");
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);*/
            } finally {
            	total_time.pause();
            	ps.print(total_time.getInterval());
            	
            	if (failed) {
            		System.exit(1);
            	}
            	//System.err.println("exited");
                System.exit(0);
            }
        }
    }

    private static int MoMMatrixSize(QNModel qnm) {
        return MiscFunctions.binomialCoefficient(qnm.M + qnm.R, qnm.R) * qnm.R;
    }

    private Main() {
    }
}
