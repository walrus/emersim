package jmt.jmva.analytical.solvers.QueuingNet;


import jmt.jmva.analytical.solvers.DataStructures.*;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.QueuingNet.amva.Linearizer;
import jmt.jmva.analytical.solvers.QueuingNet.amva.Solver;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MonteCarloSolver extends QNSolver {

    private int maxSamples;

    private double tolerance;

    private int N; // Number of queues

    private PopulationVector pop; //Population

    private int M; // Number of classes

    private ArrayList<Double> samplingParam;

    private double[][] load;

    // Values to analyse the accuracy
    private static int I = 1000;
    private static double CRITICAL_VALUE = 2.575;

    // Number of threads for sampling
    private static int NUMBER_OF_TASKS = 10;

    public MonteCarloSolver(QNModel qnm, double tolerance, int maxSamples) throws InternalErrorException {
        super(qnm);
        this.tolerance = tolerance;
        this.maxSamples = maxSamples;
        setUp(qnm.getMultiplicitiesVector(), qnm.getPopulationVector());
    }

    private void setUp(MultiplicitiesVector m, PopulationVector p) {
        this.pop = p;
        for (int i = 0; i < m.size(); i++) {
            N += m.get(i);
        }
        M = qnm.R;
        load = new double[N][M];

        int currIndex = 0;
        for (int i = 0; i < qnm.M; i++) {
            for (int k = 0; k < m.get(i); k++) {
                for (int j = 0; j < qnm.R; j++) {
                    load[currIndex][j] = qnm.getDemand(i, j);
                }
                currIndex++;
            }
        }

        int stationNo = N;
        samplingParam = new ArrayList<Double>(stationNo);
        Integer[] population = new Integer[p.size()];
        population = p.toArray(population);
        int[] pop = new int[population.length];
        for (int i = 0; i < population.length; i++) {
            pop[i] = population[i];
        }

        Linearizer linearizer = new Linearizer(M, stationNo, pop, false);
        String[] stationName = new String[stationNo];
        int[] stationType = new int[stationNo];
        double[][][] s = new double[stationNo][M][1];
        double[][] v = new double[stationNo][M];

        for (int i = 0; i < stationNo; i++) {
            stationType[i] = Solver.LI;
            for (int j = 0; j < M; j++) {
                v[i][j] = 1;
                s[i][j][0] = load[i][j];
            }
        }

        linearizer.input(stationName, stationType, s, v);
        linearizer.solveLI();
        for (int i = 0; i < stationNo; i++) {
            double util = linearizer.getAggrUtilization(i);
            if (util < 0.9) {
                samplingParam.add(1 - util);
            } else {
                samplingParam.add(1 / Math.sqrt(Collections.max(Arrays.asList((population)))));
            }
        }
    }

    private BigRational computeNotSetNormalisingConstant() {
        ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_TASKS);
        List<MonteCarloSamplingTask> tasks = new ArrayList<MonteCarloSamplingTask>(NUMBER_OF_TASKS);
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            tasks.add(new MonteCarloSamplingTask(I / NUMBER_OF_TASKS));
        }
        double currentSum = 0;
        double currentSumSquared = 0;
        int currentSamples = I;
        //	BigRational currentSum = BigRational.ZERO;
        //	BigRational currentSumSquared = BigRational.ZERO;
        double mean = currentSum / currentSamples;
        double variance = (currentSumSquared - currentSum * currentSum/currentSamples)/currentSamples;
        try {
            do {
                List<Future<Tuple<Double, Double>>> results = pool.invokeAll(tasks);

                for (Future<Tuple<Double, Double>> result : results) {
                    Tuple<Double, Double> t = result.get();
                    currentSum = currentSum + (t.getX());
                    currentSumSquared = currentSumSquared + (t.getY());
                }
                currentSamples += I;
                mean = currentSum / currentSamples;
                variance = (currentSumSquared - currentSum * currentSum/currentSamples)/currentSamples;
            } while (getConfidenceLevel(variance, currentSamples) > tolerance * mean
                    && currentSamples < maxSamples);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Calcuate variance
/*
        double currentVariance = (currentSumSquared - currentSum * currentSum / I) / I;
        double currentMean = currentSum / I;

        // Calculate number of iterations needed
        int n = (int) Math.pow((1.96 * Math.sqrt(currentVariance) / (0.001 * currentMean)), 2);
        System.out.println("Sampling " + n + " samples....");
        double newSum = 0;
        double newSumSquared = 0;
        List<MonteCarloSamplingTask> newTasks = new ArrayList<MonteCarloSamplingTask>(NUMBER_OF_TASKS);
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            newTasks.add(new MonteCarloSamplingTask(n / NUMBER_OF_TASKS));
        }
        try {
            List<Future<Tuple<Double, Double>>> results = pool.invokeAll(newTasks);
            for (Future<Tuple<Double, Double>> result : results) {
                Tuple<Double, Double> t = result.get();
                newSum += (t.getX());
                newSumSquared = newSumSquared + (t.getY());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentVariance = (newSumSquared - newSum * newSum / n) / n;
        currentMean = newSum / n;*/

        System.out.println("Monte Carlo : " + mean + " with confidence level: " + getConfidenceLevel(variance, currentSamples));
        pool.shutdown();

        return new BigRational(mean);
    }

    @Override
    public void computeNormalisingConstant() throws InternalErrorException {
        totalTimer.start();
        qnm.setNormalisingConstant(computeNotSetNormalisingConstant());
        System.out.println("MonteCarlo");
        System.out.println(qnm.getNormalisingConstant().approximateAsDouble());
        totalTimer.pause();
        memUsage = MiscFunctions.memoryUsage();
        printTimeStatistics();
        printMemUsage();
    }

    private double getConfidenceLevel(double variance, int i) {
        double sd = Math.sqrt(variance);
        return CRITICAL_VALUE * sd / (Math.sqrt(i));
    }


    @Override
    public void computePerformanceMeasures() throws InternalErrorException {

        //Array of Mean Throughputs per class
        BigRational[] X = new BigRational[qnm.R];

        //Array of Mean Queue Lengths
        BigRational[][] Q = new BigRational[qnm.M][qnm.R];

        PopulationVector p = qnm.getPopulationVector();
        MultiplicitiesVector m = qnm.getMultiplicitiesVector();
        for (int i = 0; i < qnm.R; i++) {
            p.minusOne(i + 1);
            setUp(m, p);
            BigRational tempNormalisingCons = computeNotSetNormalisingConstant();
            p.restore();
            X[i] = tempNormalisingCons.divide(qnm.getNormalisingConstant());
        }

        for (int i = 0; i < qnm.M; i++) {
            m.plusOne(i + 1);
            for (int j = 0; j < qnm.R; j++) {
                p.minusOne(j + 1);
                setUp(m, p);
                BigRational tempNormalisingCons = computeNotSetNormalisingConstant();
                Q[i][j] = qnm.getDemandAsBigRational(i, j).multiply(tempNormalisingCons).divide(qnm.getNormalisingConstant());
                p.restore();
            }
            m.restore();
        }
        qnm.setPerformanceMeasures(Q, X);
        qnm.printPerformanceMeasures();
    }

    private class MonteCarloSamplingTask implements Callable<Tuple<Double, Double>> {

        private int sampleNo;
        private HashMap<Integer, Double[][]> gResults = new HashMap<Integer, Double[][]>();

        public MonteCarloSamplingTask(int sampleNo) {
            this.sampleNo = sampleNo;
        }

        @Override
        public Tuple<Double, Double> call() {
            double currentSample;
            //	    double currentSum = BigRational.ZERO;
            //	    BigRational currentSumSquared = BigRational.ZERO;
            double currentSum = 0;
            double currentSumSquared = 0;
            for (int i = 0; i < sampleNo; i++) {
                currentSample = getSampleZ();
                // Update sum and sum Squared
                currentSum = currentSum + (currentSample);
                currentSumSquared = currentSumSquared + (currentSample * currentSample);
            }
            return new Tuple(currentSum, currentSumSquared);
        }

        // Generate Zi based on (1)
        private double getSampleZ() {
            ArrayList<Double> sample = getSample(samplingParam.size());
            double left = 1;
            double sum = 0;
            for (int i = 0; i < sample.size(); i++) {
                left *= samplingParam.get(i);
                sum -= (1 - samplingParam.get(i)) * sample.get(i);
            }
            double middle = Math.exp(sum);

            // topRight
            double topRight = 1;
            //BigRational topRight = BigRational.ONE;
            for (int i = 0; i < M; i++) {
                int nj = pop.get(i);
                double subSum = 0;
                for (int j = 0; j < samplingParam.size(); j++) {
                    double rightFirst = load[j][i];
                    double right = rightFirst * sample.get(j);
                    subSum = subSum + right;
                }
                topRight = topRight * Math.pow(qnm.getDelay(i) + subSum, nj);
                Map<Integer, BigRational> map = MiscFunctions.computeFactorials(nj);
                topRight = topRight / map.get(nj).approximateAsDouble();
            }
            return middle * topRight / left;
        }

        // Generate elements based on exponential distribution
        private ArrayList<Double> getSample(int length) {
            ArrayList<Double> returnList = new ArrayList<Double>(length);
            for (int i = 0; i < length; i++) {
                Random random = new Random();
                Double randomNumber = random.nextDouble();
                double x = Math.log(1 - randomNumber) / (-samplingParam.get(i));
                returnList.add(x);
            }
            return returnList;
        }
    }
}
