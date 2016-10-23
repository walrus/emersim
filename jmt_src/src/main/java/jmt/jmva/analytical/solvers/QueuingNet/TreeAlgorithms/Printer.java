package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.io.PrintWriter;

/** Wrapper class for printing to console.
 * @author Ben Homer, 2014.
 */
public abstract class Printer {

	/** The static Printer instance to be used for printing to console. */
	public static Printer out;
	
	/** Creates a static Printer instance depending on the VERBOSE flag.
	 * @return The static Printer instance.
	 */
	public static Printer create() {
		if (Config.VERBOSE) {
			if (Config.LOG) {
				out = new FilePrinter();
			} else {
				out = new StandardPrinter();
			}
		} else {
			out = new SilentPrinter();
		}
		return out;
	}
	
	/**
	 * Prints the specified string.
	 * @param s The string to print.
	 */
	public abstract void print(String s);
	
	/**
	 * Prints the specified string and newline.
	 * @param s The string to print.
	 */
	public abstract void println(String s);
	
	/** Dummy printer which does nothing. */
	private static class SilentPrinter extends Printer {
		public void print(String s) {}

		@Override
		public void println(String s) {}
	}
	
	/** Standard printer which prints normally to console. */
	private static class StandardPrinter extends Printer {
		@Override
		public void println(String s) {
			System.out.println(s);
		}
		
		@Override
		public void print(String s) {
			System.out.print(s);
		}
	}
	
	/** File printer which prints normally to console. */
	private static class FilePrinter extends Printer {
		
		private PrintWriter writer;
		
		private FilePrinter() {
			try {
				writer = new PrintWriter("log.txt", "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void println(String s) {
			System.out.println(s);
			writer.println(s);
		}
		
		@Override
		public void print(String s) {
			System.out.print(s);
			writer.print(s);
		}
	}
}
