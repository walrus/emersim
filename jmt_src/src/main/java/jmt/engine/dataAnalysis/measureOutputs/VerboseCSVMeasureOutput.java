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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jmt.engine.NodeSections.LogTunnel;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.dataAnalysis.MeasureOutput;
import jmt.engine.dataAnalysis.SimParameters;
import jmt.engine.log.CSVLogger;
import jmt.engine.log.JSimLoggerFactory;

/**
 * This class implements a csv output for measure class. <br>
 * File output: <br>
 * <table border="1"> <tr>
 * <td>Timestamp (simulation time)</td>
 * <td>Sample</td>
 * <td>Weight</td>
 * </tr></table>
 */
public class VerboseCSVMeasureOutput extends MeasureOutput {

	public static final String COLUMN_TS = LogTunnel.COLUMN_TIMESTAMP;
	public static final String COLUMN_SAMPLEVALUE = "SAMPLE";
	public static final String COLUMN_WEIGHTVALUE = "WEIGHT";

	public static final String[] COLUMNS = {COLUMN_TS, COLUMN_SAMPLEVALUE, COLUMN_WEIGHTVALUE};

	private CSVLogger logger;
	private File file;

	private Map<String, Object> defaults = Collections.emptyMap();

	/**
	 * Constructor of a NewCSVMeasureOutput object, using a file.
	 * @param Measure the measure to be sent in output
	 * @param Append true to write at the end of the file (of course
	 * it is useful only if a file is used)
	 * @throws java.io.IOException
	 */
	public VerboseCSVMeasureOutput(jmt.engine.dataAnalysis.Measure Measure, SimParameters simParameters) throws IOException {
		super(Measure);
		file = new File(simParameters.getLogPath(), measure.getName() + ".csv");

		char decimalSeparator;

		String ds = simParameters.getLogDecimalSeparator();
		if (ds != null && ds.length() > 0) {
			decimalSeparator = ds.charAt(0);
		} else {
			decimalSeparator = '.';
		}

		logger = JSimLoggerFactory.getCSVLogger(file, COLUMNS, false, simParameters.getLogDelimiter(), decimalSeparator);
	}

	@Override
	public void write(double sample, double weight) {
		try {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put(COLUMN_TS, NetSystem.getTime());
			values.put(COLUMN_SAMPLEVALUE, sample);
			values.put(COLUMN_WEIGHTVALUE, weight);
			logger.log(values, defaults);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public void close() {
		if (logger != null) {
			try {
				logger.close();
			} 
			catch (IOException e) {
			}
			logger = null;
		}
	}

	@Override
	public void finalizeMeasure() {
		close();
//FIXME fix this...
//		try {
//			if (measure.getSuccess() == true) {
//				writer.write("\nMeasure name; " + measure.getName() + ";\n" + "Mean; " + statistics.getMean() + ";\n" + "2nd Moment; "
//						+ statistics.getMoment2() + ";\n" + "3rd Moment; " + statistics.getMoment3() + ";\n" + "4th Moment; "
//						+ statistics.getMoment4() + ";\n" + "Variance; " + statistics.getVar() + ";\n" + "Standard Deviation; " 
//						+ statistics.getSd() + ";\n" + "Coefficient of Variation; " + statistics.getCoefVar() + ";\n" + "Skewness; "
//						+ statistics.getSkew() + ";\n" + "Max Value; " + statistics.getMax() + ";\n" + "Min Value; " +
//						+ statistics.getMin() + ";\n" + "Total Samples; " + statistics.getSamples() + ";\n" + "Measure was successful; "
//						+ measure.getSuccess() + ";");
//			} 
//			if (!StdOutput) {
//				writer.close();
//			}
//		} catch (IOException exc) {
//		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.dataAnalysis.MeasureOutput#getOutputFile()
	 */
	@Override
	public File getOutputFile() {
		return file;
	}

}
