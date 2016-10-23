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

package jmt.engine.dataAnalysis.measureOutputs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Reads a Verbose CSV measure output file, providing all the samples and weights.
 *
 * @author Bertoli Marco 2014-09-14
 */
public class VerboseCSVMeasureOutputReader {

	private String logCsvDelimiter;
	private File measureCsvFile;
	private RandomAccessFile file;
	private BufferedReader reader;
	private int lineNumber;
	private DecimalFormat numberParser;
	private double parsedSimTime;
	private double parsedSample;
	private double parsedWeight;
	private boolean hasMoreLines;
	private long fileSize;

	/**
	 * Builds a new CSV measure reader
	 * @param measureCsvFile the measure csv file. Muse be a valid file
	 * @param logDecimalSeparator the decimal separator
	 * @param logCsvDelimiter the CSV delimiter
	 */
	public VerboseCSVMeasureOutputReader(File measureCsvFile, String logDecimalSeparator, String logCsvDelimiter) {
		this.measureCsvFile = measureCsvFile;
		this.logCsvDelimiter = logCsvDelimiter;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
		dfs.setDecimalSeparator(logDecimalSeparator.charAt(0));
		numberParser = new DecimalFormat("#.#", dfs);
		fileSize = measureCsvFile.length();
	}

	/**
	 * Opens the measure file
	 * @param initialLine the initial line to read.
	 * @throws IOException
	 */
	public void openFile(int initialLine) throws IOException {
		if (file != null) {
			file.seek(0L);
		}
		file = new RandomAccessFile(measureCsvFile, "r");
		lineNumber = 0;
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getFD())));	

		// Skips initial lines.
		for (int j=0; j<initialLine; j++) {
			reader.readLine();
			lineNumber++;
		}
		hasMoreLines = file.getFilePointer() < fileSize;
	}

	/**
	 * Closes the reader if it is open
	 * @throws IOException if there is an error closing the file.
	 */
	public void closeFile() throws IOException {
		if (file != null) {
			reader.close();
			file.close();
			file = null;
			reader = null;
			hasMoreLines = false;
		}
	}

	/**
	 * Tells if there are more lines in this file.
	 * @return true if there are more lines, false otherwise
	 * @throws IOException
	 */
	public boolean hasMoreLines() throws IOException {
		return hasMoreLines;
	}

	/**
	 * Reads a line from the verbose CSV
	 * @return true if more lines are available, false otherwise
	 * @throws IOException when a data access error happens
	 * @throws ParseException when data is not well formed
	 */
	public boolean readLine() throws IOException, ParseException {
		String str = reader.readLine();
		// Close file when read is end
		if (str == null) {
			closeFile();
			return false;
		}
		this.lineNumber++;
		parseLine(str);
		return true;
	}

	/**
	 * Parses a CSV file line
	 * @param line the output data structure
	 */
	private void parseLine(String str) throws ParseException {
		int index = str.indexOf(logCsvDelimiter);
		int index2 = str.indexOf(logCsvDelimiter, index+logCsvDelimiter.length());
		parsedSimTime = numberParser.parse(str).doubleValue();
		parsedSample = numberParser.parse(str, new ParsePosition(index + logCsvDelimiter.length())).doubleValue();
		parsedWeight = numberParser.parse(str, new ParsePosition(index2 + logCsvDelimiter.length())).doubleValue();
	}

	/**
	 * @return the last line number read
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @return the parsed sample
	 */
	public double getParsedSample() {
		return parsedSample;
	}

	/**
	 * @return the parsed weight
	 */
	public double getParsedWeight() {
		return parsedWeight;
	}

	/**
	 * @return the parsed simulation time
	 */
	public double getParsedSimulationTime() {
		return parsedSimTime;
	}

	/**
	 * Returns the percentage of file parsed
	 * @return the percentage of file parsed
	 * @throws IOException
	 */
	public double getParsedPercentage() throws IOException {
		if (file != null) {
			return (double) file.getFilePointer() / fileSize;
		} else {
			return 1.0;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		closeFile();
	}

}
