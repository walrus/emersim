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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.SwingWorker;

import jmt.engine.dataAnalysis.Measure;
import jmt.engine.dataAnalysis.measureOutputs.VerboseCSVMeasureOutputReader;
import jmt.engine.math.SampleStatistics;

/**
 *<p>Title: StatisticalOutputsParameters</p>
 * <p>Description: Consists of the controlling elements of the StatisticalOutputsEditor,
 *  and the various parameters needed to display statistical outputs and generate required graphs.</p>
 * 
 * @author Samarth Shukla, Aneish Goel
 *         Month: June 2013
 */     
public class StatisticalOutputsLoader {

	// Percentage upgrade time. Since it is a costly operation it is performed based on time.
	private static final int UPDATE_PCT_MILLIS = 500;
	private SampleStatistics statistics;
	//private MeasureDefinition measureDefinition;
	private String filename;
	private double progress = 0.0 ;
	private ProgressTimeListener ptlistener = null ;
	private int start = -1;
	private int end = -1;
	private int totalLines = 0;
	private double maxSimTime = Double.NaN;
	private double initialSimTime = Double.NaN;
	private double finalSimTime = Double.NaN;
	private double distMin = Double.NaN;
	private double distMax = Double.NaN;
	private int distIntervals = -1;
	private String decimalSeparator;
	private String logCsvDelimiter;

	private CSVLoader currentLoader;

	public StatisticalOutputsLoader(MeasureDefinition measureDefinition, int measureIndex) throws FileNotFoundException{
		this.decimalSeparator = measureDefinition.getLogDecimalSeparator();
		this.logCsvDelimiter = measureDefinition.getLogCsvDelimiter();
		this.filename = measureDefinition.getLogFileName(measureIndex);
		this.start = measureDefinition.getDiscardedSamples(measureIndex);

		// Starts loading of CSV file in a separate thread.
		reloadData();
	}

	public StatisticalOutputsLoader(Measure measure, String decimalSeparator, String logCsvDelimiter) throws FileNotFoundException, IOException, ParseException {
		this.filename = measure.getOutput().getOutputFile().getAbsolutePath();
		this.start = measure.getDiscardedSamples();
		this.decimalSeparator = decimalSeparator;
		this.logCsvDelimiter = logCsvDelimiter;

		// Starts loading of CSV file in a separate thread.
		loadCSV();
	}

	public SampleStatistics getStatistics() {
		return statistics ;
	}

	public void setDistribution(double min, double max, int intervals) {
		this.distMin = min;
		this.distMax = max;
		this.distIntervals = intervals;
	}

	public double getPercentage(double linesRead, double totalLines) {
		double percent = linesRead / totalLines ;
		return percent;
	}

	//reads file and generates graph. Code linked with code of CSVOutputReader.
	private void loadCSV() throws IOException, ParseException {
		File measureFile = new File(filename);
		VerboseCSVMeasureOutputReader reader = new VerboseCSVMeasureOutputReader(measureFile, decimalSeparator, logCsvDelimiter);
		statistics = new SampleStatistics();

		// Fill distribution if requested.
		if (distIntervals > 0) {
			statistics.initializeDistrubution(distMin, distMax, distIntervals);
		}

		// Skips header.
		updateProgress(0.0);
		reader.openFile(start > 1 ? start : 1);
		try {
			long lastUpdateTime = System.currentTimeMillis();
			while (reader.hasMoreLines()) {
				reader.readLine();

				// Check bounds on simulation time
				if (!Double.isNaN(initialSimTime) && reader.getParsedSimulationTime() < initialSimTime) {
					continue;
				}
				if ((!Double.isNaN(finalSimTime) && reader.getParsedSimulationTime() > finalSimTime)) {
					break;
				}

				// Process sample
				statistics.putNewSample(reader.getParsedSample(), reader.getParsedWeight(), reader.getParsedSimulationTime());

				if (System.currentTimeMillis() - lastUpdateTime > UPDATE_PCT_MILLIS) {
					updateProgress(reader.getParsedPercentage());
					lastUpdateTime = System.currentTimeMillis();
				}
				if (end > 0 && reader.getLineNumber() >= end) {
					break;
				}

			}
			// If totalLines was not set, count them.
			if (totalLines <= 0) {
				while (reader.hasMoreLines()) {
					reader.readLine();
				}
				totalLines = reader.getLineNumber();
				maxSimTime = reader.getParsedSimulationTime();
			}
		} finally {
			reader.closeFile();
			updateProgress(100.0);
			statistics.finalizeLoading();
		}
	}

	public double getProgress() {
		return progress;
	}

	/**
	 * Update progress time loading the file
	 * @param percentage the progress percentage
	 */
	private void updateProgress(double percentage) {
		progress = percentage;

		if (ptlistener != null) {
			ptlistener.timeChanged(percentage);
		}
	}

	public synchronized void setProgressTimeListener(ProgressTimeListener listener) {
		ptlistener = listener;
	}

	public void setStart(int str) {
		start = str;
	}

	public void setEnd(int en) {
		end = en;
	}

	/**
	 * Set bounds on initial simulation time
	 * @param initialSimTime initial simulation time or NaN to skip
	 */
	public void setInitialSimTime(double initialSimTime) {
		this.initialSimTime = initialSimTime;
	}

	/**
	 * Set bounds on final simulation time
	 * @param finalSimTime final simulation time or NaN to skip
	 */
	public void setFinalSimTime(double finalSimTime) {
		this.finalSimTime = finalSimTime;
	}

	public int getTotalLines() {
		return totalLines;
	}

	/**
	 * Returns maximum simulation file in the entire file
	 * @return the maximum simulation time
	 */
	public double getMaxSimulationTime() {
		return maxSimTime;
	}

	/** listener for ProgressBar in StatisticalOutputsEditor. */
	public interface ProgressTimeListener {
		public void timeChanged(double progressPct);
	}

	/**
	 * Reloads the data structure
	 */
	public void reloadData() {
		if (currentLoader != null) {
			// Avoid concurrent reloads.
			return;
		}
		currentLoader = new CSVLoader();
		currentLoader.execute();
	}

	/**
	 * Loads verbose measure from CSV file in the background
	 * @author Marco Bertoli
	 */
	private class CSVLoader extends SwingWorker<Object, Object> {

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			loadCSV();
			currentLoader = null;
			return null;
		}

	}

}
