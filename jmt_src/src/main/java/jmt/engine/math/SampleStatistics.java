/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.engine.math;

/**
 * Collect statistics related to given simulation samples
 */
public class SampleStatistics {	
	public static enum DistributionType {
		FREQUENCY, CUMULATIVE;
	}
	
	private double moment1;//1st moment(mean) of all data
	private double moment2;//2nd moment of all data
	private double moment3;//3rd moment of all data
	private double moment4;//4th moment of all data
	private double var;//variance of all data
	private double sd;//standard deviation of all data
	private double coefvar;//coefficient of variation of all data
	private double skew;//skewness of all data
	private double kurtosis;//kurtosis of all data
	protected double n;//number of data
	protected double max;//the greatest sample of all data inserted
	protected double min;//the smallest sample of all data inserted
	
	protected double totalWeight;// sum of weights of all data
	protected double weigthedSum1;//sum of (sample)*weight
	protected double weigthedSum2;//sum of (sample^2)*weight
	protected double weigthedSum3;//sum of (sample^3)*weight
	protected double weigthedSum4;//sum of (sample^4)*weight
	
	protected double minSimTime;
	protected double maxSimTime;
	
	// Following variables are used to get intervals for density charts
	protected boolean canInitializeIntervals;
	protected double intervalWidth;
	protected double intervalMin;
	protected double intervalMax;
	protected double totalIntervalWeights;
	protected double[] intervalWeights;
	

	/** Creates a new instance of SampleStatistics */
	public SampleStatistics() {
		initialize();
	}
	
	/** 
	 * initializes a new SampleStatistics 	 
	 * @param invertedIndex true if performance index is inverted i.e. throughput.
	 */
	private void initialize() {
		totalWeight = 0;
		moment1 = 0;
		moment2 = 0;
		moment3 = 0;
		moment4 = 0;
		var = 0;
		sd = 0;
		coefvar = 0;
		skew = 0;
		kurtosis = 0;
		n = 0;
		max = 0;
		min = Double.MAX_VALUE;
		weigthedSum1 = 0;
		weigthedSum2 = 0;
		weigthedSum3 = 0;
		weigthedSum4 = 0;
		minSimTime = Double.MAX_VALUE;
		maxSimTime = 0;
		canInitializeIntervals = true;
	}
	
	/** updates the max, min, sum of weights with the new value.
	 *  @param  sample the value of the new sample
	 *  @param  weight the weight of the new sample
	 *  @param  simulationTime the simulation time for the new sample
	 */
	public void putNewSample(double sample, double weight, double simulationTime) {
		if (weight == 0) {
			return;
		}
		// Avoid initialization of intervals after samples were already loaded
		canInitializeIntervals = false;
		
		// updates the maximum and the minimum value of the sequence
		max= Math.max(max, sample);
		min = Math.min(min, sample);
		// updates the sum of weights of the values of the sequence
		totalWeight = totalWeight + weight;
		// updates the sum of weights multiplied by appropriate power of samples of the sequence
		weigthedSum1 = weigthedSum1 + weight*sample;
		weigthedSum2 = weigthedSum2 + weight*Math.pow(sample,2);
		weigthedSum3 = weigthedSum3 + weight*Math.pow(sample,3);
		weigthedSum4 = weigthedSum4 + weight*Math.pow(sample,4);
		//updates the number of samples
		n++;
		//updates the simulation time
		minSimTime = Math.min(minSimTime,simulationTime);
		maxSimTime = Math.max(maxSimTime, simulationTime);
		
		// Updates intervals if enabled
		if (intervalWeights != null && sample >= intervalMin && sample <= intervalMax) {
			int index = (int) Math.floor((sample - intervalMin)/intervalWidth);
			
			// The following avoids that due to numerical rounding, some samples exceed last bucket.
			if (index >= intervalWeights.length) {
				index = intervalWeights.length - 1;
			}
			intervalWeights[index] += weight;
			totalIntervalWeights += weight;
		}
		
		//calculates the statistical parameters until that point
		calcStatistics();
	}
	
	/**
	 * Tells that this sample statistic should also populate distributions. This method must
	 * be called before samples are collected
	 * @param intervalMin the minimum value of samples to collect in intervals
	 * @param intervalMax the maximum value of samples to collect in intervals
	 * @param intervals the number of intervals to build
	 * @throws IllegalStateException if this method is called after addSample
	 */
	public void initializeDistrubution(double intervalMin, double intervalMax, int intervals) {
		if (!canInitializeIntervals) {
			throw new IllegalStateException("Cannot initialize distribution after samples were already added.");
		}
		this.intervalMin = intervalMin;
		this.intervalMax = intervalMax;
		totalIntervalWeights = 0;
		//calculate the width of each interval
		intervalWidth = (intervalMax-intervalMin)/intervals;
		intervalWeights = new double[intervals];
	}
	
	/**
	 * Returns the distribution computed based on samples
	 * @param type the type of distribution
	 * @return the distribution if initialized, null otherwise.
	 */
	public double[] getDistribution(DistributionType type) {
		if (intervalWeights == null) {
			return null;
		}
		double[] ret = new double[intervalWeights.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = intervalWeights[i] / totalIntervalWeights;
			if (type == DistributionType.CUMULATIVE && i > 0) {
				ret[i] += ret[i-1];
			}
		}
		return ret;
	}
	
	/**
	 * @return the start point for intervals of the distribution
	 */
	public double[] getDistributionIntervalsStart() {
		double[] ret = new double[intervalWeights.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = intervalMin + i * intervalWidth;
		}
		return ret;
	}
	
	/**
	 * @return the end point for intervals of the distribution
	 */
	public double[] getDistriburionIntervalsEnd() {
		double[] ret = new double[intervalWeights.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = intervalMax - (ret.length - i - 1) * intervalWidth;
		}
		return ret;
		
	}
	
	/** computes all the statistical parameters, using their respective mathematical formulae
	  */
	protected void calcStatistics() {
		moment1 = weigthedSum1/totalWeight;
		moment2 = weigthedSum2/totalWeight;
		moment3 = weigthedSum3/totalWeight;
		moment4 = weigthedSum4/totalWeight;
		
		var = moment2 - Math.pow(moment1, 2);
		sd = Math.sqrt(var);
		coefvar = sd/moment1;
		skew = (moment3 - (3*moment1*Math.pow(sd, 2)) - Math.pow(moment1, 3))/Math.pow(sd, 3);
		kurtosis = (moment4 - (4*moment3*moment1) + (6*moment2*Math.pow(moment1,2)) - (3*Math.pow(moment1, 4)))/(Math.pow(sd, 4)) - 3;
	}
	
	/** gets the sample mean
	 *  @return the actual sample mean
	 */
	public double getMean() {
		return moment1;
	}
	
	/** gets the sample 2nd moment
	 *  @return the actual 2nd moment
	 */
	public double getMoment2() {
		return moment2;
	}
	
	/** gets the sample 3rd moment
	 *  @return the actual 3rd moment
	 */
	public double getMoment3() {
		return moment3;
	}
	
	/** gets the sample 4th moment
	 *  @return the actual 4th moment
	 */
	public double getMoment4() {
		return moment4;
	}
	
	/** gets the sample variance
	 *  @return the actual variance
	 */
	public double getVariance() {
		return var;
	}
	
	/** gets the sample standard deviation
	 *  @return the actual standard deviation
	 */
	public double getStandardDeviation() {
		return sd;
	}
	
	/** gets the sample coefficient of variation
	 *  @return the actual coefficient of variation
	 */
	public double getCoefficienfOfVariation() {
		return coefvar;
	}
	
	/** gets the sample skewness
	 *  @return the actual skewness
	 */
	public double getSkew() {
		return skew;
	}
	
	/** gets the sample kurtosis
	 *  @return the actual kurtosis
	 */
	public double getKurtosis() {
		return kurtosis;
	}

	/** gets the greatest value
	 *  @return the actual max value
	 */
	public double getMax() {
		return max;
	}

	/** gets the smallest value
	 *  @return the actual min value
	 */
	public double getMin() {
		if (Double.MAX_VALUE == min) {
			return 0.0;
		} else {
			return min;
		}
	}
	
	/** gets the number of samples
	 *  @return the actual number of samples
	 */
	public double getSamples() {
		return n;
	}
	
	/**
	 * @return the minimum simulation time
	 */
	public double getMinSimTime() {
		if (Double.MAX_VALUE == minSimTime) {
			return 0.0;
		} else {
			return minSimTime;
		}
	}
	
	/**
	 * @return the maximum simulation time
	 */
	public double getMaxSimTime() {
		return maxSimTime;
	}
	
	/** creates a string with the calculated parameters of the sequence monitored
	 *  by SampleStatistics
	 *  @return a string
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getName());
		buf.append("\n--------------------------");
		buf.append("\nNumber of samples : " + getSamples());
		buf.append("\nLargest Sample    : " + getMax());
		buf.append("\nSmallest Sample   : " + getMin());
		buf.append("\nMean of samples   : " + getMean());
		buf.append("\n2nd Moment of samples   : " + getMoment2());
		buf.append("\n3rd Moment of samples   : " + getMoment3());
		buf.append("\n4th Moment of samples   : " + getMoment4());
		buf.append("\nVariance of samples   : " + getVariance());
		buf.append("\nStandard Deviation of samples   : " + getStandardDeviation());
		buf.append("\nCoefficient of Variation of samples   : " + getCoefficienfOfVariation());
		buf.append("\nSkewness of samples   : " + getSkew());
		buf.append("\nKurtosis of samples   : " + getKurtosis());
		return buf.toString();
	}

	/**
	 * @return the width of each interval used by the distribution
	 */
	public double getIntervalWidth() {
		return intervalWidth;
	}

	/**
	 * @return the minimum value of the intervals considered for distribution (inclusive)
	 */
	public double getIntervalMin() {
		return intervalMin;
	}

	/**
	 * @return the maximum value of the intervals considered for distribution (inclusive)
	 */
	public double getIntervalMax() {
		return intervalMax;
	}
	
	/**
	 * @return true if distribution is available, false otherwise.
	 */
	public boolean isDistributionAvailable() {
		return intervalWeights != null;
	}
	
	/**
	 * This method is called at the end of the loading phase.
	 */
	public void finalizeLoading() {
		// Subclasses will do something here.
	}
	
	
}