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

package jmt.gui.common.definitions;

import java.util.HashMap;
import java.util.Vector;

import jmt.engine.QueueNet.SimConstants;
import jmt.framework.gui.graph.MeasureValue;

/**
 * <p>Title: Result's Model data structure</p>
 * <p>Description: This class will store all values of measured loaded from a file.
 * it is used by <code>XMLResultsReader</code>.</p>
 * 
 * @author Bertoli Marco
 *         Date: 3-ott-2005
 *         Time: 14.10.50
 *         
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for the capturing the 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class.
 */
public class StoredResultsModel implements MeasureDefinition {

	protected Vector<Measure> measures = new Vector<Measure>();
	protected HashMap<String, Measure> names = new HashMap<String, Measure>();
	protected double pollingInterval = 1;
	private Vector<Integer> queueLength = new Vector<Integer>();
	private Vector<Integer> queueTime = new Vector<Integer>();
	private Vector<Integer> residenceTime = new Vector<Integer>();
	private Vector<Integer> responseTime = new Vector<Integer>();
	private Vector<Integer> utilization = new Vector<Integer>();
	private Vector<Integer> throughput = new Vector<Integer>();
	private Vector<Integer> dropRate = new Vector<Integer>();
	private Vector<Integer> systemResponseTime = new Vector<Integer>();
	private Vector<Integer> systemThroughput = new Vector<Integer>();
	private Vector<Integer> customerNumber = new Vector<Integer>();
	private Vector<Integer> systemDropRate = new Vector<Integer>();
	private Vector<Integer> meanWeight = new Vector<Integer>();
	private Vector<Integer> meanMemoryOccupation = new Vector<Integer>();
	private Vector<Integer> systemPower = new Vector<Integer>();
	private Vector<Integer> responseTimePerSink = new Vector<Integer>();
	private Vector<Integer> throughputTimePerSink = new Vector<Integer>();
	private Vector<Integer> FJResponseTime = new Vector<Integer>();
	private Vector<Integer> FJCustomerNumber = new Vector<Integer>();

	public Vector<Measure> getMeasures() {
		return measures;
	}

	private String logDecimalSeparator;
	private String logCsvDelimiter;

	/**
	 * Returns total number of measures
	 * 
	 * @return number of measures
	 */
	public int getMeasureNumber() {
		return measures.size();
	}

	/**
	 * Returns the station name of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return station name
	 */
	public String getStationName(int measureIndex) {
		return measures.get(measureIndex).stationName;
	}

	/**
	 * Returns the class name of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return class name
	 */
	public String getClassName(int measureIndex) {
		return measures.get(measureIndex).className;
	}

	/**
	 * Returns the alpha of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return alpha
	 */
	public double getAlpha(int measureIndex) {
		return measures.get(measureIndex).alpha;
	}

	/**
	 * Returns the precision of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return precision
	 */
	public double getPrecision(int measureIndex) {
		return measures.get(measureIndex).precision;
	}

	/**
	 * Returns filename corresponding to a measure
	 * @param measureIndex index of the measure
	 * @return null
	 */
	public String getLogFileName(int measureIndex) {
		return measures.get(measureIndex).fileName;
	}

	/**
	 * Returns number of analyzed samples for a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return number of analyzed samples
	 */
	public int getAnalyzedSamples(int measureIndex) {
		return measures.get(measureIndex).analyzedSamples;
	}

	/**
	 * Returns the number of discarded samples for a given measure
	 * @param measureIndex index of the measure
	 * @return number of discarded samples
	 */
	public int getDiscardedSamples(int measureIndex) {
		return measures.get(measureIndex).discardedSamples;
	}

	/**
	 * Returns the name of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return name of the measure
	 */
	public String getName(int measureIndex) {
		return measures.get(measureIndex).name;
	}

	/**
	 * Returns the node type of a given measure
	 * 
	 * @param measureIndex index of the measure
	 * @return name of the measure
	 */
	public String getNodeType(int measureIndex) {
		return measures.get(measureIndex).nodeType;
	}

	/**
	 * Returns the vector of Temporary values of a given measure. Each element of the vector
	 * is an instance of <code>Value</code> interface.
	 * 
	 * @param measureIndex index of the measure
	 * @return vector of temporary values until now
	 */
	public Vector<MeasureValue> getValues(int measureIndex) {
		return measures.get(measureIndex).values;
	}

	/**
	 * Returns the state of a measure, that can be MEASURE_IN_PROGRESS, MEASURE_NO_SAMPLES,
	 * MEASURE_FAILED, MEASURE_SUCCESS
	 * 
	 * @param measureIndex index of the measure
	 * @return measure state
	 */
	public int getMeasureState(int measureIndex) {
		return measures.get(measureIndex).state;
	}

	/**
	 * Returns the type of a measure
	 * 
	 * @param measureIndex index of the measure
	 * @return measure type
	 */
	public int getMeasureType(int measureIndex) {
		return measures.get(measureIndex).type;
	}

	//ADDED by ANEISH START.
	/**
	 * Returns true if the measure is a Time Or Number
	 * @param measureIndex index of the measure
	 * @return true if the measure is a rate or number quantity
	 */
	public boolean isTimeOrNumber(int measureIndex) {
		int  type = getMeasureType(measureIndex) ;
		if (type ==0 || type == 4 || type == 5 || type == 6 || type == 8 || type ==10 || type ==14) {
			return true;
		} else {
			return false;
		}
	}
	//ADDED by ANEISH STOP.

	/**
	 * Returns an array with the measureIndex of every queue length measure
	 * @return an array with measures' index
	 */
	public int[] getQueueLengthMeasures() {
		int[] tmp = new int[queueLength.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = queueLength.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every throughput measure
	 * @return an array with measures' index
	 */
	public int[] getThroughputMeasures() {
		int[] tmp = new int[throughput.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = throughput.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every queue time measure
	 * @return an array with measures' index
	 */
	public int[] getQueueTimeMeasures() {
		int[] tmp = new int[queueTime.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = queueTime.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every residence time measure
	 * @return an array with measures' index
	 */
	public int[] getResidenceTimeMeasures() {
		int[] tmp = new int[residenceTime.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = residenceTime.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every response time measure
	 * @return an array with measures' index
	 */
	public int[] getResponseTimeMeasures() {
		int[] tmp = new int[responseTime.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = responseTime.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every drop rate measure
	 * @return an array with measures' index
	 */
	public int[] getDropRateMeasures() {
		int[] tmp = new int[dropRate.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = dropRate.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every system drop rate measure
	 * @return an array with measures' index
	 */
	public int[] getSystemDropRateMeasures() {
		int[] tmp = new int[systemDropRate.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = systemDropRate.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every utilization measure
	 * @return an array with measures' index
	 */
	public int[] getUtilizationMeasures() {
		int[] tmp = new int[utilization.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = utilization.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every system response time measure
	 *
	 * @return an array with measures' index
	 */
	public int[] getSystemResponseTimeMeasures() {
		int[] tmp = new int[systemResponseTime.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = systemResponseTime.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every system throughput measure
	 *
	 * @return an array with measures' index
	 */
	public int[] getSystemThroughputMeasures() {
		int[] tmp = new int[systemThroughput.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = systemThroughput.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns an array with the measureIndex of every customer number measure
	 *
	 * @return an array with measures' index
	 */
	public int[] getCustomerNumberMeasures() {
		int[] tmp = new int[customerNumber.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = customerNumber.get(i).intValue();
		}
		return tmp;
	}

	//Added by ASHANKA START
	//Returns each indices of the System Power as a part of the process of adding the System Power as a new performance index to the 
	//JSIM
	/**
	 * Returns an array with the measureIndex of every customer number measure
	 *
	 * @return an array with measures' index
	 */
	public int[] getSystemPowerMeasures() {
		int[] tmp = new int[systemPower.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = systemPower.get(i).intValue();
		}
		return tmp;
	}
	//Added by ASHANKA STOP

	public int[] getResponsetimePerSinkMeasures() {
		int[] tmp = new int[responseTimePerSink.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = responseTimePerSink.get(i).intValue();
		}
		return tmp;
	}

	public int[] getThroughputPerSinkMeasures() {
		int[] tmp = new int[throughputTimePerSink.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = throughputTimePerSink.get(i).intValue();
		}
		return tmp;
	}

	public int[] getMeanWeightMeasures() {
		int[] tmp = new int[meanWeight.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = meanWeight.get(i).intValue();
		}
		return tmp;
	}

	public int[] getMeanMemoryOccupationMeasures() {
		int[] tmp = new int[meanMemoryOccupation.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = meanMemoryOccupation.get(i).intValue();
		}
		return tmp;
	}

	public int[] getFJResponseTimeMeasures() {
		int[] tmp = new int[FJResponseTime.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = FJResponseTime.get(i).intValue();
		}
		return tmp;
	}

	public int[] getFJCustomerNumberMeasures() {
		int[] tmp = new int[FJCustomerNumber.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = FJCustomerNumber.get(i).intValue();
		}
		return tmp;
	}

	/**
	 * Returns if simulation has finished, so results are fixed
	 * 
	 * @return true iff simulation has finished
	 */
	public boolean isSimulationFinished() {
		return true;
	}

	/**
	 * Returns simulation polling interval. This is the time elapsed between two temp values.
	 * 
	 * @return simulation polling interval in seconds
	 */
	public double getPollingInterval() {
		return pollingInterval;
	}

	/**
	 * Returns current simulation progress time
	 * @return current progress time
	 */
	public double getProgressTime() {
		return 1.0;
	}

	// --- Methods to populate data structure ----------------------------------------------------------------
	/**
	 * Adds a new measure into this data structure.
	 * @param name measure name
	 * @param stationName reference station name
	 * @param className reference class name
	 * @param alpha measure alpha
	 * @param precision measure precision
	 * @param analyzedSamples number of analyzed samples
	 * @param discardedSamples number of discarded samples
	 * @param state state of the measure
	 * @param type type of the measure
	 * @param fileName the output file name for the verbose measure
	 */
	public void addMeasure(String name, String stationName, String className, double alpha, double precision, int analyzedSamples, int discardedSamples, int state,
			int type, String nodeType,String fileName) {
		Measure tmp = new Measure(name, stationName, className, alpha, precision, analyzedSamples, discardedSamples, state, type, nodeType, fileName);
		measures.add(tmp);
		names.put(name, tmp);
		// Adds measure index to the right Vector
		switch (type) {
		case SimConstants.QUEUE_TIME:
			queueTime.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.RESIDENCE_TIME:
			residenceTime.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.RESPONSE_TIME:
			responseTime.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.UTILIZATION:
			utilization.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.THROUGHPUT:
			throughput.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.QUEUE_LENGTH:
			queueLength.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.SYSTEM_RESPONSE_TIME:
			systemResponseTime.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.SYSTEM_JOB_NUMBER:
			customerNumber.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.SYSTEM_THROUGHPUT:
			systemThroughput.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.DROP_RATE:
			dropRate.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.SYSTEM_DROP_RATE:
			systemDropRate.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.SYSTEM_POWER:
			systemPower.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.RESPONSE_TIME_PER_SINK:
			responseTimePerSink.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.THROUGHPUT_PER_SINK:
			throughputTimePerSink.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.FCR_TOTAL_WEIGHT:
			meanWeight.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.FCR_MEMORY_OCCUPATION:
			meanMemoryOccupation.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.FORK_JOIN_RESPONSE_TIME:
			FJResponseTime.add(new Integer(measures.size() - 1));
			break;
		case SimConstants.FORK_JOIN_NUMBER_OF_JOBS:
			FJCustomerNumber.add(new Integer(measures.size() - 1));
			break;
		}
	}

	/**
	 * Adds a new sample to specified measure
	 * @param measureName name of the measure
	 * @param meanValue mean value of the sample
	 * @param upperBound upper bound of the sample
	 * @param lowerBound lower bound of the sample
	 */
	public void addMeasureSample(String measureName, double lastIntervalAvgValue, double simulationTime, double meanValue, double upperBound, double lowerBound) {
		Measure tmp = names.get(measureName);
		tmp.addSample(meanValue, lastIntervalAvgValue, simulationTime, upperBound, lowerBound);
	}

	/**
	 * Sets measure polling interval
	 * @param interval polling interval
	 */
	public void setPollingInterval(double interval) {
		this.pollingInterval = interval;
	}

	// -------------------------------------------------------------------------------------------------------

	// --- Inner Classes--------------------------------------------------------------------------------------
	/**
	 * Inner class to store parameters of each measure
	 */
	protected class Measure {

		public String name;
		public String stationName;
		public String className;
		public String nodeType;
		public String fileName;
		public Vector<MeasureValue> values;
		public double alpha;
		public double precision;
		public int analyzedSamples;
		public int state;
		public int type;
		public int discardedSamples;

		/**
		 * Construct a new Measure object
		 * @param name measure name
		 * @param stationName reference station name
		 * @param className reference class name
		 * @param alpha measure alpha
		 * @param precision measure precision
		 * @param analyzedSamples number of analyzed samples
		 * @param discardedSamples number of discarded samples
		 * @param state state of the measure
		 * @param type type of the measure
		 * @param fileName the output file name for the verbose measure
		 */
		public Measure(String name, String stationName, String className, double alpha, double precision, int analyzedSamples, int discardedSamples, int state, int type,
				String nodeType, String fileName) {
			this.name = name;
			this.stationName = stationName;
			this.className = className;
			this.alpha = alpha;
			this.precision = precision;
			this.analyzedSamples = analyzedSamples;
			this.discardedSamples = discardedSamples;
			this.state = state;
			this.type = type;
			values = new Vector<MeasureValue>();
			this.nodeType = nodeType;
			this.fileName=fileName ;
		}

		/**
		 * Adds a new sample to current measure
		 * @param meanValue mean value of the sample
		 * @param upperBound upper bound of the sample
		 * @param lowerBound lower bound of the sample
		 * @param lastIntervalAvgValue 
		 * @param simulationTime
		 */
		public void addSample(double meanValue, double lastIntervalAvgValue, double simulationTime, double upperBound, double lowerBound) {
			MeasureValueImpl val = new MeasureValueImpl(meanValue, lastIntervalAvgValue, simulationTime, upperBound, lowerBound);
			values.add(val);
		}

	}

	/**
	 * Inner class that implements Value interface
	 */
	public class MeasureValueImpl implements MeasureValue {

		private double mean;
		private double upper;
		private double lower;
		private double lastS;
		private double simulationT;

		/**
		 * Creates a new MeasureValue object
		 * @param meanValue mean value of the sample
		 * @param upperBound sample upper bound
		 * @param lowerBound sample lower bound
		 */
		public MeasureValueImpl(double meanValue, double lastIntervalAvgValue, double simulationTime, double upperBound, double lowerBound) {
			mean = meanValue;
			upper = upperBound;
			lower = lowerBound;
			lastS =lastIntervalAvgValue;
			simulationT= simulationTime;
		}

		public double getUpperBound() {
			return upper;
		}

		public double getLowerBound() {
			return lower;
		}

		public double getMeanValue() {
			return mean;
		}

		@Override
		public double getLastIntervalAvgValue() {
			return lastS;
		}

		@Override
		public double getSimTime() {
			return simulationT;
		}

	}

	/* (non-Javadoc)
	 * @see jmt.gui.common.definitions.MeasureDefinition#getLogDecimalSeparator()
	 */
	@Override
	public String getLogDecimalSeparator() {
		return logDecimalSeparator;
	}

	/* (non-Javadoc)
	 * @see jmt.gui.common.definitions.MeasureDefinition#getLogCsvDelimiter()
	 */
	@Override
	public String getLogCsvDelimiter() {
		return logCsvDelimiter;
	}

	/**
	 * @param logDecimalSeparator the logDecimalSeparator to set
	 */
	public void setLogDecimalSeparator(String logDecimalSeparator) {
		this.logDecimalSeparator = logDecimalSeparator;
	}

	/**
	 * @param logCsvDelimiter the logCsvDelimiter to set
	 */
	public void setLogCsvDelimiter(String logCsvDelimiter) {
		this.logCsvDelimiter = logCsvDelimiter;
	}

	// -------------------------------------------------------------------------------------------------------

	// --- Useless methods -----------------------------------------------------------------------------------
	/**
	 * This feature is not required as loaded measures are static
	 * @param listener listener to be set or null to reset previous one
	 */
	public void setProgressTimeListener(MeasureDefinition.ProgressTimeListener listener) {
	}

	/**
	 * Sets a ProgressTimeListener to listen to detected malformed replayer file. This is unique.
	 * @param listener listener to be set or null to reset previous one
	 */
	public void setMalformedReplayerFileListener(MalformedReplayerFileListener listener) {
	}

	public void detectedMalformedReplayerFile(String msg) {
	}

	/**
	 * This feature is not required as loaded measures are static
	 * @param measureIndex index of the measure that this listener should listen
	 * @param listener     listener to add or null to remove old one.
	 */
	public void addMeasureListener(int measureIndex, MeasureDefinition.MeasureListener listener) {
	}
	// -------------------------------------------------------------------------------------------------------

}
