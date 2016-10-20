package jmt.jmva;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import jmt.jmva.analytical.CommandLineSolver;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class JmvaClosedModelTest {
	
	@Test
	public void randomModel_closed_1() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_1", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_1-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_1-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_2() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_2", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_2-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_2-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_3() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_3", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_3-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_3-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_4() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_4", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_4-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_4-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_5() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_5", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_5-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_5-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_6() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_6", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_6-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_6-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_7() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_7", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_7-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_7-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_8() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_8", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_8-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_8-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_9() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_9", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_9-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_9-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_10() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_10", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_10-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_10-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_11() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_11", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_11-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_11-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_12() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_12", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_12-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_12-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_recal() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_recal", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_recal-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_recal-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_mom() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_mom", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_mom-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_mom-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}
	
	@Test
	public void randomModel_closed_comom() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();
		
		File input = File.createTempFile("randomModel_closed_comom", ".xml");
		File expected = new File(getClass().getResource("randomModel_closed_comom-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_closed_comom-input.xml").toURI()), input);
		
		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}

}
