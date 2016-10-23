package jmt.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * A class which creates and writes to a temporary text file.
 * Used in tests to collect output and compare it to a model 
 * file, with the expected output.
 * 
 * @author Piotr Tokaj
 *
 */
public class TempTextFile {
	
	private File file;
	private PrintWriter writer;
	
	public TempTextFile(String name) throws IOException {
		file = File.createTempFile(name, ".txt");
		writer = new PrintWriter(file.getAbsoluteFile());
	}
	
	public void println(Object line) {
		writer.println(line);
	}
	
	public void print(Object line) {
		writer.print(line);
	}
	
	public File getFile() {
		writer.close();
		return file;
	}

}
