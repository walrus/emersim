package jmt.engine;

import static org.junit.Assert.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jmt.commandline.Jmt;

public class FiniteCapacityRegion {

	@Test
	public void fcr01() throws Exception {
		File input = File.createTempFile("fcr-01", ".xml");
		File result = File.createTempFile("fcr-01-result", ".xml");
		File expected = new File(getClass().getResource("fcr-01-output.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("fcr-01-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String, String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

		/* This part of the code removes the first row from the result file */
		Scanner scanner = new Scanner(result);
		ArrayList<String> coll = new ArrayList<String>();
		scanner.nextLine();
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			coll.add(line);
		}
		scanner.close();

		FileWriter writer = new FileWriter(result);
		for (String line : coll) {
			writer.write(line);
		}
		writer.close();
		/* The row has now been removed */

		String strExp = FileUtils.readFileToString(expected, "utf-8");
		String strRes = FileUtils.readFileToString(result, "utf-8");
		String strDiff = StringUtils.difference(strExp, strRes);

		assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
	}

	@Test
	public void fcrgroup01() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-01-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 900, class member = Low Class
		 * Group B: Strategy = HPS_LCFC, maxJob = 100, class member = High Class
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 9.0 job/sec
		 * systemThroughput of High Class = 1.0 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClass);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 9.0) / 9.0) < 0.03);

		assertTrue("systemThroughput of High Class is incorrect", (Math.abs(systemThroughputHighClass - 1.0) / 1.0) < 0.03);
	}

	@Test
	public void fcrgroup02() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-02-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 500, class member = Low Class
		 * Group B: Strategy = FCFC, maxJob = 400, class member = High Class
		 * Group C: Strategy = HPS_LCFC, maxJob = 100, class member = High Class
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 5.0 job/sec
		 * systemThroughput of High Class = 5.0 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClass);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 5.0) / 5.0) < 0.03);

		assertTrue("systemThroughput of High Class is incorrect", (Math.abs(systemThroughputHighClass - 5.0) / 5.0) < 0.03);
	}

	@Test
	public void fcrgroup03() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-03-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 900, class member = Low Class
		 * Group B: Strategy = HPS_FCFC, maxJob = 100, class member = High Class
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 9.0 job/sec
		 * systemThroughput of High Class = 1.0 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClass);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 9.0) / 9.0) < 0.03);

		assertTrue("systemThroughput of High Class is incorrect", (Math.abs(systemThroughputHighClass - 1.0) / 1.0) < 0.03);
	}

	@Test
	public void fcrgroup04() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-04-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 500, class member = Low Class
		 * Group B: Strategy = FCFC, maxJob = 400, class member = High Class
		 * Group C: Strategy = HPS_FCFC, maxJob = 100, class member = High Class
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 5.0 job/sec
		 * systemThroughput of High Class = 5.0 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClass);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 5.0) / 5.0) < 0.03);

		assertTrue("systemThroughput of High Class is incorrect", (Math.abs(systemThroughputHighClass - 5.0) / 5.0) < 0.03);
	}

	@Test
	public void fcrgroup05() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-05-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class A
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClassA = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class B
		nNode = nList.item(3);
		eElement = (Element) nNode;
		double systemThroughputHighClassB = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class A: Arrival rate = 12.0 job/sec, priority = 1
		 * High Class B: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 600, class member = Low Class
		 * Group B: Strategy = FCFC, maxJob = 200, class member = High Class A
		 * Group C: Strategy = FCFC, maxJob = 100, class member = High Class B
		 * Group D: Strategy = HPS_FCFC, maxJob = 100, class member = Low Class, High Classes A and B
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 6.0 job/sec
		 * systemThroughput of High Class A = 2.0 job/sec
		 * systemThroughput of High Class B = 2.0 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClassA);
		//System.out.println(systemThroughputHighClassB);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 6.0) / 6.0) < 0.03);

		assertTrue("systemThroughput of High Class A is incorrect", (Math.abs(systemThroughputHighClassA - 2.0) / 2.0) < 0.03);

		assertTrue("systemThroughput of High Class B is incorrect", (Math.abs(systemThroughputHighClassB - 2.0) / 2.0) < 0.03);
	}

	@Test
	public void fcrgroup06() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-06-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput Low Class
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputLowClass = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class A
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputHighClassA = Double.parseDouble(eElement.getAttribute("meanValue"));

		//System Throughput High Class B
		nNode = nList.item(3);
		eElement = (Element) nNode;
		double systemThroughputHighClassB = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3%
		/*----------------configuration----------------
		 * Low Class: Arrival rate = 12.0 job/sec, priority = 0
		 * High Class A: Arrival rate = 12.0 job/sec, priority = 1
		 * High Class B: Arrival rate = 12.0 job/sec, priority = 1
		 * Group A: Strategy = FCFC, maxJob = 600, class member = Low Class
		 * Group B: Strategy = FCFC, maxJob = 200, class member = High Class A
		 * Group C: Strategy = FCFC, maxJob = 100, class member = High Class B
		 * Group D: Strategy = HPS_LCFC, maxJob = 100, class member = Low Class, High Classes A and B
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Low Class = 6.0 job/sec
		 * systemThroughput of High Class A = 2.5 job/sec
		 * systemThroughput of High Class B = 1.5 job/sec
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputLowClass);
		//System.out.println(systemThroughputHighClassA);
		//System.out.println(systemThroughputHighClassB);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Low Class is incorrect", (Math.abs(systemThroughputLowClass - 6.0) / 6.0) < 0.03);

		assertTrue("systemThroughput of High Class A is incorrect", (Math.abs(systemThroughputHighClassA - 2.5) / 2.5) < 0.03);

		assertTrue("systemThroughput of High Class B is incorrect", (Math.abs(systemThroughputHighClassB - 1.5) / 1.5) < 0.03);
	}

	@Test
	public void fcrgroup07() throws Exception {
		File input = File.createTempFile("fcr-group", ".xml");
		File result = File.createTempFile("fcr-group-result", ".xml");
		FileUtils.copyFile(new File(getClass().getResource("fcr-group-07-input.xml").toURI()), input);

		String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
		Map<String,String> options = Jmt.parseParameters(args, 2);
		assertTrue("Solver was unable to solve file!", Jmt.sim(input, result, options));

		//read result
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(result);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("measure");
		Node nNode;
		Element eElement;

		//System Throughput All Classes
		nNode = nList.item(0);
		eElement = (Element) nNode;
		double systemThroughput = Double.parseDouble(eElement.getAttribute("meanValue"));

		//systemThroughput of Class A1
		nNode = nList.item(1);
		eElement = (Element) nNode;
		double systemThroughputClassA1 = Double.parseDouble(eElement.getAttribute("meanValue"));

		//systemThroughput of Class A2
		nNode = nList.item(2);
		eElement = (Element) nNode;
		double systemThroughputClassA2 = Double.parseDouble(eElement.getAttribute("meanValue"));

		//systemThroughput of Class B1
		nNode = nList.item(3);
		eElement = (Element) nNode;
		double systemThroughputClassB1 = Double.parseDouble(eElement.getAttribute("meanValue"));

		//systemThroughput of Class B2
		nNode = nList.item(4);
		eElement = (Element) nNode;
		double systemThroughputClassB2 = Double.parseDouble(eElement.getAttribute("meanValue"));

		//accepted error is less than 3% 
		/*----------------configuration----------------
		 * Class A1: Arrival rate = 12.0 job/sec, priority = 0
		 * Class A2: Arrival rate = 12.0 job/sec, priority = 0
		 * Class B1: Arrival rate = 12.0 job/sec, priority = 0
		 * Class B2: Arrival rate = 12.0 job/sec, priority = 0
		 * Group A: Strategy = FCFC, maxJob = 600, class member = Classes A1 and A2
		 * Group B: Strategy = FCFC, maxJob = 400, class member = Classes B1 and B2
		 * Station: Service rate = 10.0 job/sec
		 * 
		 * expectation
		 * systemThroughput = 10.0 job/sec
		 * systemThroughput of Group A = 6.0 job/sec
		 * systemThroughput of Group B = 4.0 job/sec
		 * 
		 */
		//System.out.println(systemThroughput);
		//System.out.println(systemThroughputClassA1);
		//System.out.println(systemThroughputClassA2);
		//System.out.println(systemThroughputClassB1);
		//System.out.println(systemThroughputClassB2);
		assertTrue("systemThroughput is incorrect", (Math.abs(systemThroughput - 10.0) / 10.0) < 0.03);

		assertTrue("systemThroughput of Group A is incorrect", (Math.abs(systemThroughputClassA1 + systemThroughputClassA2 - 6.0) / 6.0) < 0.03);

		assertTrue("systemThroughput of Group B is incorrect", (Math.abs(systemThroughputClassB1 + systemThroughputClassB2 - 4.0) / 4.0) < 0.03);
	}

}
