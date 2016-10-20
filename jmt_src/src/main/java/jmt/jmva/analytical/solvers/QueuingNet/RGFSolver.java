package jmt.jmva.analytical.solvers.QueuingNet;

import jmt.jmva.analytical.solvers.DataStructures.*;
import jmt.jmva.analytical.solvers.Exceptions.InternalErrorException;
import jmt.jmva.analytical.solvers.Utilities.MiscFunctions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RGFSolver extends QNSolver {

    // N distinct node type.
    private int nodeType;

    // counters for storage management.
    private int[] counters;

    // Set Combination for length n, sum up to k, not counting index i.
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Set<Integer[]>>>> setCombinations = new HashMap<>();

    //

    // Storing the values of each normalising constant based on the Base.
    private HashMap<Base, BigRational[][]> loadResult = new HashMap<Base, BigRational[][]>();

    private HashMap<SingleClassBase, BigRational> singleClassResult = new HashMap<SingleClassBase, BigRational>();

    public RGFSolver(QNModel qnm) throws InternalErrorException {
        super(qnm);
        nodeType = qnm.M;
        counters = new int[qnm.R];
        for (int i = 0; i < qnm.R; i++) {
            counters[i] = 1;
        }
    }

    @Override
    public void computeNormalisingConstant() throws InternalErrorException {
        totalTimer.start();
        BigRational normalisingConstant
                = computeNotSetNormalisingConstant(qnm.getMultiplicitiesVector(), qnm.getPopulationVector());
        System.out.println(normalisingConstant.approximateAsDouble());
        qnm.setNormalisingConstant(normalisingConstant);
    }

    private BigRational computeNotSetNormalisingConstant(MultiplicitiesVector m, PopulationVector p) throws InternalErrorException {
        BigRational[][] load = new BigRational[nodeType][qnm.R];
        for (int i = 0; i < nodeType; i++) {
            for (int j = 0; j < qnm.R; j++) {
                boolean sameDemand = false;
                for (int k = 0; k < nodeType; k++) {
                    if (qnm.getDemand(i, j) == qnm.getDemand(i, k)) {
                        sameDemand = true;
                    }
                }
                if (sameDemand) {
                    load[i][j] = qnm.getDemandAsBigRational(i, j).add(BigRational.ONE.pow(-10));
                }
            }
        }



        BigRational norm = null;
        if (qnm.R == 1) {
            norm = computeSingleClass(m, p, load);
        } else {
            norm = BigRational.ZERO;
            while (!terminate()) {
                try {
                    norm = norm.add(computeForEach(m, p, qnm.R, load));
                } catch (ArithmeticException e) {
                    System.out.println("The result is NaN");
                    return BigRational.ZERO;
                }
                incrementCounters();
            }
            norm = norm.add(computeForEach(m, p, qnm.R, load));
        }
        return norm;
    }

    private void reset() {
        for (int i = 0; i < qnm.R; i++) {
            counters[i] = 1;
        }
    }

    private void incrementCounters() {
        counters[1]++;
        for (int i = 2; i < counters.length; i++) {
            if (counters[i - 1] > nodeType) {
                if (i == 2) {
                    loadResult.clear();
                    singleClassResult.clear();
                }
                counters[i]++;
                counters[i - 1] = 1;
            }
        }
    }

    private boolean terminate() {
        for (int i = 1; i < counters.length; i++) {
            if (counters[i] < nodeType) {
                return false;
            }
        }
        return true;
    }

    private BigRational f(int i, int j, MultiplicitiesVector m, BigRational[][] load) {
        BigRational coeff = new BigRational(MiscFunctions.binomialCoefficient(j
                + m.get(i) - 1, j));
        BigRational rho = load[i][0].pow(j);
        return coeff.multiply(rho);
    }

    private BigRational computeBuzenG(MultiplicitiesVector m,
                                      PopulationVector p, BigRational[][] load) {
        int k = p.get(0);
        BigRational[][] G = new BigRational[nodeType][k + 1];
        for (int i = 0; i < nodeType; i++) {
            for (int j = 0; j <= k; j++) {
                G[i][j] = BigRational.ZERO;
            }
        }
        for (int i = 0; i < nodeType; i++) {
            G[i][0] = BigRational.ONE;
        }
        for (int i = 0; i <= k; i++) {
            G[0][i] = f(0, i, m, load);
        }
        for (int i = 1; i < nodeType; i++) {
            for (int k1 = k; k1 >= 0; k1--) {
                BigRational sum = BigRational.ZERO;
                for (int j = 0; j <= k1; j++) {
                    sum = sum.add(f(i, j, m, load).multiply(G[i - 1][k1 - j]));
                }
                G[i][k1] = sum;
            }
        }

        return G[nodeType - 1][k];
    }

    private BigRational reiserAndKobayashi(MultiplicitiesVector m,
                                           PopulationVector p, BigRational[][] load) {
        int k = p.get(0);
        BigRational[][] G = new BigRational[nodeType][k + 1];
        for (int i = 0; i < nodeType; i++) {
            G[i][0] = BigRational.ONE;
        }
        for (int i = 0; i <= k; i++) {
            G[0][i] = f(0, i, m, load);
        }
        for (int i = 1; i < nodeType; i++) {
            for (int j = 1; j <= k; j++) {
                G[i][j] = G[i - 1][j].add(load[i][0].multiply(G[i][j - 1]));
            }
        }

        return G[nodeType - 1][k];
    }

    private BigRational computeSingleClass(MultiplicitiesVector m,
                                           PopulationVector p, BigRational[][] load) {
        SingleClassBase base = new SingleClassBase(load, m, p);
        BigRational result = singleClassResult.get(base);
        if (result != null) {
            return result;
        }
        int index = m.isNearlyAllOneElements();
        if (index != -1) {
            MultiplicitiesVector newM = m.copy();
            BigRational[][] newLoad = new BigRational[load.length][load[0].length];
            for (int i = 0; i < load.length; i++) {
                for (int j = 0; j < load[0].length; j++) {
                    newLoad[i][j] = load[i][j];
                }
            }
            if (index != 0) {
                int value = m.get(index);
                newM.set(index, 1);
                newM.set(0, value);
                System.arraycopy(load[index], 0, newLoad[0], 0,
                        load[index].length);
                System.arraycopy(load[0], 0, newLoad[index], 0,
                        load[0].length);

            }
            result = reiserAndKobayashi(newM, p, newLoad);
        //    System.out.println("single " + reiserAndKobayashi(newM, p, newLoad));

        } else {
  //          System.out.println("single " + computeBuzenG(m, p, load));
            result = computeBuzenG(m, p, load);
        }
        singleClassResult.put(base, result);
        return result;
    }

    private BigRational[][] computeNewLoad(BigRational[][] load, int i, int q) {
        Base base = new Base(load, i, q);
        BigRational[][] newLoad = loadResult.get(base);
        if (newLoad != null)  {
            return newLoad;
        }
        newLoad = new BigRational[nodeType][q - 1];
        BigRational piq = load[i][q - 1];
        for (int a = 0; a < nodeType; a++) {
            for (int pop = 0; pop < q - 1; pop++) {
                if (a != i) {
                    BigRational top1 = piq.multiply(load[a][pop]);
                    BigRational top2 = load[a][q - 1].multiply(load[i][pop]);
                    BigRational top = top1.subtract(top2);
                    BigRational bottom = piq.subtract(load[a][q - 1]);
                    newLoad[a][pop] = top.divide(bottom);
                } else {
                    newLoad[a][pop] = load[i][pop];
                }
            }

        }
        loadResult.put(base, newLoad);
        return newLoad;
    }

    // Generic method to calculate the normalising constant.
    private BigRational computeForEach(MultiplicitiesVector m,
                                       PopulationVector p, int q, BigRational[][] load) throws InternalErrorException {
        if (q == 1) {
            BigRational singleResult = computeSingleClass(m, p, load);
            return singleResult;
        } else if (m.isOneVector()) {
            return compute(m, p, q, load);
        } else {
            int i = counters[q - 1] - 1;
            int mi = m.get(i);
            int kq = p.get(q - 1);
            BigRational sum = BigRational.ZERO;
            BigRational piq = load[i][q - 1];
            for (int j = 1; j <= mi; j++) {
                BigRational coeff = new BigRational(
                        MiscFunctions.binomialCoefficient(mi + kq - j, kq));
                BigRational rhoPow = piq.pow(kq);
                BigRational mult = new BigRational(-1).pow(j + 1).multiply(coeff).multiply(rhoPow);
                //System.out.println("mult " + mult);
                for (int l = 0; l < nodeType; l++) {
                    int ml = m.get(l);
                    if (l != i) {
                        BigRational top = piq;
                        BigRational bottom = piq.subtract(load[l][q - 1]);
                        BigRational divisionResult = top.divide(bottom);
                        mult = mult.multiply(divisionResult.pow(ml));
                    }
                }
            //    System.out.println("mult " + mult);
                Set<Integer[]> jSet = getSetCombinations(nodeType, j - 1, i);
                BigRational jSum = BigRational.ZERO;
                for (Integer[] v : jSet) {
                    BigRational setMult = BigRational.ONE;
                    for (int l = 0; l < nodeType; l++) {
                        if (l != i) {
                            int jl = v[l];
                            int ml = m.get(l);
                            BigRational setCoeff = new BigRational(
                                    MiscFunctions.binomialCoefficient(ml + jl - 1, jl));
                            BigRational top = load[l][q - 1].pow(jl);
                            BigRational bottom = piq.subtract(load[l][q - 1]).pow(jl);
                            BigRational dResult = top.divide(bottom);
                            setMult = setMult.multiply(setCoeff).multiply(dResult);
                        }
                    }

                    PopulationVector newP = p.copy();
                    newP.remove(newP.size() - 1);
                    Integer[] newV = v.clone();
                    newV[i] = kq + 1 - j;
                    MultiplicitiesVector newM = m.addVec(new EnhancedVector(newV));
                    BigRational nextResult = computeForEach(newM, newP, q - 1, computeNewLoad(load, i, q));
                    setMult = setMult.multiply(nextResult);
                    jSum = jSum.add(setMult);
                }
                mult = mult.multiply(jSum);
                sum = sum.add(mult);
            }

            return sum;
        }
    }

    // Method to calculate when M = 1.
    protected BigRational compute(MultiplicitiesVector m, PopulationVector p, int q,
                                  BigRational[][] load) throws InternalErrorException {
        int i = counters[q - 1] - 1;
        BigRational piq = load[i][q - 1];
        BigRational multResult = piq.pow(p.get(q - 1));
        for (int j = 0; j < nodeType; j++) {
            if (j != i) {
                BigRational denominator = piq.subtract(load[j][q - 1]);
                multResult = multResult.multiply(piq.divide(denominator));
            }
        }

        Integer[] newM = new Integer[m.size()];
        for (int count = 0; count < newM.length; count++) {
            newM[count] = 1;
        }
        newM[i] += p.get(q - 1);
        MultiplicitiesVector newMVec = new MultiplicitiesVector(newM);
        PopulationVector newP = p.copy();
        newP.remove(q - 1);
        BigRational result = computeForEach(newMVec, newP, q - 1, computeNewLoad(load, i, q));
        multResult = multResult.multiply(result);
        return multResult;
    }

    // Get the vector of size length, sums up to sum without adding the element at index.
    private Set<Integer[]> getSetCombinations(int length, int sum,
                                              int index) {
        HashMap<Integer, HashMap<Integer, Set<Integer[]>>> combinations = setCombinations
                .get(length);
        if (combinations == null) {
            generateSetCombinations(length, sum, index);
        } else {
            HashMap<Integer, Set<Integer[]>> set = combinations.get(sum);
            if (set == null) {
                generateSetCombinations(length, sum, index);
            } else {
                Set<Integer[]> indexSet = set.get(index);
                if (indexSet == null) {
                    generateSetCombinations(length, sum, index);
                }
            }
        }
        return setCombinations.get(length).get(sum).get(index);
    }

    private void addArray(int length, int sum, int index, Integer[] array) {
        HashMap<Integer, HashMap<Integer, Set<Integer[]>>> combinations = setCombinations
                .get(length);
        if (combinations == null) {
            combinations = new HashMap<Integer, HashMap<Integer, Set<Integer[]>>>();
        }
        HashMap<Integer, Set<Integer[]>> newSet = combinations.get(sum);
        if (newSet == null) {
            newSet = new HashMap<Integer, Set<Integer[]>>();
        }

        Set<Integer[]> indexSet = newSet.get(index);
        if (indexSet == null) {
            indexSet = new HashSet<Integer[]>();
        }
        indexSet.add(array);
        newSet.put(index, indexSet);
        combinations.put(sum, newSet);
        setCombinations.put(length, combinations);
    }

    private void generateSetCombinations(int length, int sum, int index) {
        Integer[] array = new Integer[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
        if (length == 1) {
            addArray(length, sum, index, array);
            return;
        }

        if (length == 2) {
            array[index] = 0;
            array[1 - index] = sum;
            addArray(length, sum, index, array);
            return;
        }

        int startIndex = index == 0 ? 1 : 0;
        for (int i = 0; i <= sum; i++) {
            array[startIndex] = i;
            generateSubSetCombinations(length, sum, i, index, startIndex + 1,
                    array);
            array[startIndex] = 0;
        }
    }

    private void generateSubSetCombinations(int length, int sum, int tempSum,
                                            int index, int startIndex, Integer[] array) {
        if (startIndex == index) {
            startIndex++;
        }
        if (startIndex == length - 1) {
            array[startIndex] = (sum - tempSum);
            addArray(length, sum, index, array);
            return;
        } else if (startIndex == length - 2 && index == length - 1) {
            array[startIndex] = sum - tempSum;
            addArray(length, sum, index, array);
            return;
        }

        for (int i = 0; i <= sum - tempSum; i++) {
            array[startIndex] = i;
            generateSubSetCombinations(length, sum, tempSum + i, index,
                    startIndex + 1, array);
            array[startIndex] = 0;
        }
    }

    @Override
    public void computePerformanceMeasures() throws InternalErrorException {
        BigRational normalisingConstant
                = qnm.getNormalisingConstant();


       // qnm.setNormalisingConstant(normalisingConstant);
        //Array of Mean Throughputs per class
        BigRational[] X = new BigRational[qnm.R];

        //Array of Mean Queue Lengths
        BigRational[][] Q = new BigRational[qnm.M][qnm.R];

        PopulationVector p = qnm.getPopulationVector();
        MultiplicitiesVector m = qnm.getMultiplicitiesVector();
        for (int i = 0; i < qnm.R; i++) {
            reset();
            p.minusOne(i + 1);
            BigRational tempNormalisingCons = computeNotSetNormalisingConstant(m,p);
            p.restore();
            X[i] = tempNormalisingCons.divide(normalisingConstant);
        }

        for (int i = 0; i < qnm.M; i++) {
            m.plusOne(i + 1);
            for (int j = 0; j < qnm.R; j++) {
                reset();
                p.minusOne(j + 1);
                BigRational tempNormalisingCons = computeNotSetNormalisingConstant(m,p);
                Q[i][j] = qnm.getDemandAsBigRational(i, j).multiply(tempNormalisingCons).divide(normalisingConstant);
                p.restore();
            }
            m.restore();
        }

        qnm.setPerformanceMeasures(Q, X);
        memUsage = MiscFunctions.memoryUsage();
        totalTimer.pause();
    }

    @Override
    public void printWelcome() {
        System.out.println("Using RGF solver...");
    }

    private class SingleClassBase {
        private final BigRational[][] load;
        private final MultiplicitiesVector m;
        private final PopulationVector p;

        public SingleClassBase(BigRational[][] load, MultiplicitiesVector m, PopulationVector p) {
            this.load = load;
            this.m = m;
            this.p = p;
        }

        @Override
        public int hashCode() {
            int sum = 0;
            for (int i = 0; i < m.size(); i++) {
                sum += m.get(i);
            }
            for (int j = 0; j < p.size(); j++) {
                sum += p.get(j);
            }
            for (int i = 0; i < load.length; i++) {
                for (int j = 0; j < load[0].length; j++) {
                    sum += load[i][j].approximateAsDouble();
                }
            }
            return sum;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SingleClassBase) {
                SingleClassBase that = (SingleClassBase) obj;
                if (load.length != that.load.length
                        || load[0].length != that.load[0].length
                        || m.size() != that.m.size()
                        || p.size() != that.p.size()) {
                    return false;
                }
                for (int i = 0; i < load.length; i++) {
                    for (int j = 0; j < load[0].length; j++) {
                        if (!load[i][j].equals(that.load[i][j])) {
                            return false;
                        }
                    }
                }
                for (int i = 0; i < m.size(); i++) {
                    if (m.get(i) != that.m.get(i)) {
                        return false;
                    }
                }
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i) != that.p.get(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }


    }

    private class Base {
        private final BigRational[][] load;
        private final int i;
        private final int q;

        public Base(BigRational[][] load, int i, int q) {
            this.i = i;
            this.q = q;
            this.load = load;
        }

        @Override
        public int hashCode() {
            int sum = i + q;
            for (int i = 0; i < load.length; i++) {
                for (int j = 0; j < load[0].length; j++) {
                    sum += load[i][j].approximateAsDouble();
                }
            }
            return sum;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Base) {
                Base that = (Base) obj;
                if (load.length != that.load.length
                        || load[0].length != that.load[0].length) {
                    return false;
                }
                for (int i = 0; i < load.length; i++) {
                    for (int j = 0; j < load[0].length; j++) {
                        if (!load[i][j].equals(that.load[i][j])) {
                            return false;
                        }
                    }
                }
                return that.q == this.q && that.i == this.i;
            }
            return false;
        }
    }
}
