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

package jmt.gui.common.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmt.common.xml.XSDSchemaLoader;
import jmt.engine.log.JSimLogger;
import jmt.engine.log.LoggerParameters;
import jmt.framework.data.MacroReplacer;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.definitions.CommonModel;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.distributions.Distribution;
import jmt.gui.common.forkStrategies.ClassSwitchFork;
import jmt.gui.common.forkStrategies.CombFork;
import jmt.gui.common.forkStrategies.ForkStrategy;
import jmt.gui.common.forkStrategies.MultiBranchClassSwitchFork;
import jmt.gui.common.forkStrategies.OutPath;
import jmt.gui.common.forkStrategies.ProbabilitiesFork;
import jmt.gui.common.joinStrategies.GuardJoin;
import jmt.gui.common.joinStrategies.JoinStrategy;
import jmt.gui.common.joinStrategies.PartialJoin;
import jmt.gui.common.routingStrategies.LoadDependentRouting;
import jmt.gui.common.routingStrategies.ProbabilityRouting;
import jmt.gui.common.routingStrategies.RoutingStrategy;
import jmt.gui.common.semaphoreStrategies.NormalSemaphore;
import jmt.gui.common.semaphoreStrategies.SemaphoreStrategy;
import jmt.gui.common.serviceStrategies.LDStrategy;
import jmt.gui.common.serviceStrategies.ZeroStrategy;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Title: XML Reader</p>
 * <p>Description: Reads model information from an XML file. This
 * class provide methods for model load. it is designed to be used by both JModel and JSim.</p>
 *
 * @author Bertoli Marco
 *         Date: 27-lug-2005
 *         Time: 13.59.48
 *         
 * Modified by Ashanka (Aug 09):
 * Desc: The code to include the changes for label changes from 
 *       1. Queue Length to Customer Number 
 *       2. Number of Customers to System Customer Number 
 * 
 * Modified by Ashanka (Sep 09):
 * Desc: The code to include the changes for label changes from 
 *       1. Customer Number to Number of Customers
 *       2. System Customer Number to System Number of Customers.
 */
public class XMLReader implements XMLConstantNames, CommonConstants {
	protected static TreeMap<String, Object> classes; // Data structure used to map between class name and its key
	protected static TreeMap<String, Object> stations; // Data structure used to map between station name and its key
	protected static TreeMap<String, Object> regions; // Data structure used to map between region name and its key
	protected static HashMap<Object, String> refStations; // Data structure used to hold classes' reference stations
	protected static HashMap<Object[], Map<String, Double>> empiricalRouting; // Data structure to save malformed empirical routing tuples
	protected static HashMap<Object[], Map<String, OutPath>> empiricalForking;
	protected static HashMap<Object[], Map<String, Double>> combForking;
	protected static HashMap<Object[], Map<String, Integer>> enablingConditionMap;
	protected static HashMap<Object[], Map<String, Integer>> inhibitingConditionMap;
	protected static HashMap<Object[], Map<String, Integer>> firingOutcomeMap;

	/*defines the default logger (used to report errors and information for debugging purposes)*/
	private static final jmt.engine.log.JSimLogger debugLog = jmt.engine.log.JSimLogger.getLogger(JSimLogger.STD_LOGGER);

	/**defines matching between engine representation and gui names for drop
	rules.*/
	protected static final Map<String,String> DROP_RULES_MAPPING;

	static {
		HashMap<String, String> d = new HashMap<String, String>();
		d.put("drop", FINITE_DROP);
		d.put("BAS blocking", FINITE_BLOCK);
		d.put("waiting queue", FINITE_WAITING);
		DROP_RULES_MAPPING = Collections.unmodifiableMap(d);
	}

	// Variables used with caching purpose to improve reading speed
	protected static Map<String, Distribution> engineToGuiDistr = null;
	protected static Map<String, RoutingStrategy> engineToGuiRouting = null;

	protected static Map<String, ForkStrategy> engineToGuiForking = null;
	protected static Map<String, JoinStrategy> engineToGuiJoin = null;
	protected static Map<String, SemaphoreStrategy> engineToGuiSemaphore = null;

	protected static final String queueGetFCFS = "jmt.engine.NetStrategies.QueueGetStrategies.FCFSstrategy";
	protected static final String queueGetLCFS = "jmt.engine.NetStrategies.QueueGetStrategies.LCFSstrategy";
	protected static final String queueGetPS = "jmt.engine.NetStrategies.QueueGetStrategies.PSSstrategy";
	protected static final String queueGetSRPT = "jmt.engine.NetStrategies.QueueGetStrategies.SRPT";
	protected static final String queuePut = "jmt.engine.NetStrategies.QueuePutStrategy";
	protected static final String serviceStrategy = "jmt.engine.NetStrategies.ServiceStrategy";
	protected static final String distributionContainer = "jmt.engine.random.DistributionContainer";

	/**
	 * Restore a model saved in an XML file, given the name of the file. If specified file
	 * is a jmodel archive, extracts model informations from it and uses them to reconstruct
	 * the model. This method is provided to be used with JSIM
	 * @param fileName name of the file to be opened
	 * @param model data structure where model should be created (a new data structure
	 * is the best choice)
	 * @return true iff model was recognized and loaded, false otherwise
	 */
	public static boolean loadModel(String fileName, CommonModel model) {
		Document doc = loadXML(fileName, XSDSchemaLoader.loadSchema(XSDSchemaLoader.JSIM_MODEL_DEFINITION));
		if (doc.getElementsByTagName(XML_DOCUMENT_ROOT).getLength() != 0) {
			// Document is a simulation model
			parseXML(doc, model);
			return true;
		} else if (doc.getElementsByTagName(GuiXMLConstants.XML_ARCHIVE_DOCUMENT_ROOT).getLength() != 0) {
			// Document is an archive
			parseXML(XMLArchiver.getSimFromArchiveDocument(doc), model);
			return true;
		}
		return false;
	}

	/**
	 * Restore a model saved in an XML file, given the handler to the file. If specified file
	 * is a jmodel archive, extracts model informations from it and uses them to reconstruct
	 * the model. This method is provided to be used with JSIM
	 * @param xmlFile handler to the file to be opened
	 * @param model data structure where model should be created (a new data structure
	 * is the best choice)
	 * @return true iff model was recognized and loaded, false otherwise
	 */
	public static boolean loadModel(File xmlFile, CommonModel model) {
		return loadModel(xmlFile.getAbsolutePath(), model);
	}

	/**
	 * Parses given Gui XML Document to reconstruct simulation model.
	 * @param root root of document to be parsed
	 * @param model data model to be elaborated
	 */
	public static void parseXML(Element root, CommonModel model) {
		// Gets optional parameter simulation seed
		String seed = root.getAttribute(XML_A_ROOT_SEED);
		if (seed != null && seed != "") {
			model.setUseRandomSeed(false);
			model.setSimulationSeed(new Long(seed));
		} else {
			model.setUseRandomSeed(true);
		}
		// Gets optional parameter maximum time
		String maxTime = root.getAttribute(XML_A_ROOT_DURATION);
		if (maxTime != null && maxTime != "") {
			model.setMaximumDuration(new Double(maxTime));
		} else {
			model.setMaximumDuration(new Double(-1));
		}

		// Gets optional parameter maximum simulated time
		String maxSimulated = root.getAttribute(XML_A_ROOT_SIMULATED);
		if (maxSimulated != null && maxSimulated != "") {
			model.setMaxSimulatedTime(new Double(maxSimulated));
		} else {
			model.setMaxSimulatedTime(new Double(-1));
		}

		// Gets optional parameter polling interval
		String polling = root.getAttribute(XML_A_ROOT_POLLING);
		if (polling != null && polling != "") {
			model.setPollingInterval(Double.parseDouble(polling));
		}

		// Gets optional parameter maximum samples
		String maxSamples = root.getAttribute(XML_A_ROOT_MAXSAMPLES);
		if (maxSamples != null && maxSamples != "") {
			model.setMaxSimulationSamples(Integer.decode(maxSamples));
		}

		// Gets optional parameter disable statistic
		String disableStatistic = root.getAttribute(XML_A_ROOT_DISABLESTATISTIC);
		if (disableStatistic != null && disableStatistic != "") {
			model.setDisableStatistic(Boolean.valueOf(disableStatistic));
		}

		/* Gets optional parameters log path, replace policy, and delimiter
		 * Values here should correspond to SimLoader values (Ctrl+F for them) */
		String logPath = root.getAttribute(XML_A_ROOT_LOGPATH);
		if (logPath != null && logPath != "") {
			File dir = new File(logPath);
			if (dir.isDirectory()) {
				model.setLoggingGlbParameter("path", logPath);
			} else {
				model.setLoggingGlbParameter("path", MacroReplacer.replace(MacroReplacer.MACRO_WORKDIR));
			}
		} else {
			model.setLoggingGlbParameter("path", "");
		}
		String logReplaceMode = root.getAttribute(XML_A_ROOT_LOGREPLACE);
		if (logReplaceMode != null && logReplaceMode != "") {
			model.setLoggingGlbParameter("autoAppend", logReplaceMode);
		} else {
			model.setLoggingGlbParameter("autoAppend", Defaults.get("loggerAutoAppend"));
		}
		String logDelimiter = root.getAttribute(XML_A_ROOT_LOGDELIM);
		if (logDelimiter != null && logDelimiter != "") {
			model.setLoggingGlbParameter("delim", logDelimiter);
		} else {
			model.setLoggingGlbParameter("delim", Defaults.get("loggerDelimiter"));
		}
		String logDecimalSeparator = root.getAttribute(XML_A_ROOT_LOGDECIMALSEPARATOR);
		if (logDecimalSeparator != null && logDecimalSeparator != "") {
			model.setLoggingGlbParameter("decimalSeparator", logDecimalSeparator);
		} else {
			model.setLoggingGlbParameter("decimalSeparator", ".");
		}

		parseClasses(root, model);
		empiricalRouting = new HashMap<Object[], Map<String, Double>>();
		empiricalForking = new HashMap<Object[], Map<String, OutPath>>();
		combForking = new HashMap<Object[], Map<String, Double>>();
		enablingConditionMap = new HashMap<Object[], Map<String, Integer>>();
		inhibitingConditionMap = new HashMap<Object[], Map<String, Integer>>();
		firingOutcomeMap = new HashMap<Object[], Map<String, Integer>>();
		parseStations(root, model);
		parseConnections(root, model);
		parseBlockingRegions(root, model);
		parseMeasures(root, model);
		parsePreloading(root, model);
		// Set reference station for each class
		Object[] keys = refStations.keySet().toArray();
		for (Object key : keys) {
			if (CommonConstants.STATION_TYPE_CLASSSWITCH.equals(refStations.get(key)))
				model.setClassRefStation(key, CommonConstants.STATION_TYPE_CLASSSWITCH);
			else if (CommonConstants.STATION_TYPE_FORK.equals(refStations.get(key)))
				model.setClassRefStation(key, CommonConstants.STATION_TYPE_FORK);
			else if (CommonConstants.STATION_TYPE_SCALER.equals(refStations.get(key)))
				model.setClassRefStation(key, CommonConstants.STATION_TYPE_SCALER);
			else
				model.setClassRefStation(key, stations.get(refStations.get(key)));
		}
		// Sets correct station key into every empiricalRouting element
		// Now each key is an Object[] where (0) is station key and (1) class key
		keys = empiricalRouting.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			RoutingStrategy rs = (RoutingStrategy) model.getRoutingStrategy(dualkey[0], dualkey[1]);
			Map<Object, Double> routing = rs.getValues();
			Map<String, Double> values = empiricalRouting.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				routing.put(stations.get(name), values.get(name));
			}
		}
		keys = empiricalForking.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			ForkStrategy fs = (ForkStrategy) model.getForkingStrategy(dualkey[0], dualkey[1]);
			Map<Object, OutPath> outPaths = (Map<Object, OutPath>) fs.getOutDetails();
			Map<String, OutPath> values = empiricalForking.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				outPaths.put(stations.get(name), values.get(name));
			}
		}
		keys = combForking.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			ForkStrategy fs = (ForkStrategy) model.getForkingStrategy(dualkey[0], dualkey[1]);
			Map<Object, Double> forking = (Map<Object, Double>) fs.getOutDetails();
			Map<String, Double> values = combForking.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				forking.put(name, values.get(name));
			}
		}
		keys = enablingConditionMap.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			Map<String, Integer> values = enablingConditionMap.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				model.setEnablingCondition(dualkey[0], dualkey[1], stations.get(name), values.get(name));
			}
		}
		keys = inhibitingConditionMap.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			Map<String, Integer> values = inhibitingConditionMap.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				model.setInhibitingCondition(dualkey[0], dualkey[1], stations.get(name), values.get(name));
			}
		}
		keys = firingOutcomeMap.keySet().toArray();
		for (Object key : keys) {
			Object[] dualkey = (Object[]) key;
			Map<String, Integer> values = firingOutcomeMap.get(key);
			Object[] names = values.keySet().toArray();
			for (Object name : names) {
				model.setFiringOutcome(dualkey[0], dualkey[1], stations.get(name), values.get(name));
			}
		}
	}

	/**
	 * Parses given Gui XML Document to reconstruct simulation model.
	 * @param xml Document to be parsed
	 * @param model data model to be elaborated
	 */
	public static void parseXML(Document xml, CommonModel model) {
		parseXML(xml.getDocumentElement(), model);
	}

	// --- Helper methods ----------------------------------------------------------------------------
	/**
	 * Helper method that searches for first text node, between all children of current node
	 * and returns its value. (This is needed to garbage out all comments)
	 * @param elem root node to begin search
	 * @return parsed text if found, otherwise null
	 */
	protected static String findText(Node elem) {
		NodeList tmp = elem.getChildNodes();
		for (int j = 0; j < tmp.getLength(); j++) {
			if (tmp.item(j).getNodeType() == Node.TEXT_NODE) {
				return tmp.item(j).getNodeValue();
			}
		}
		return null;
	}

	// -----------------------------------------------------------------------------------------------

	// --- Class section -----------------------------------------------------------------------------
	/**
	 * Parses userclasses information. Note that distributions for open class will be set lately
	 * and reference station information is stored into refStations data structure as will
	 * be used later
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parseClasses(Element root, CommonModel model) {
		// Initialize classes and refStations data structure
		classes = new TreeMap<String, Object>();
		refStations = new HashMap<Object, String>();
		NodeList nodeclasses = root.getElementsByTagName(XML_E_CLASS);
		// Now scans all elements
		Element currclass;
		int type, priority;
		Integer customers;
		String name;
		Distribution defaultDistr = (Distribution) Defaults.getAsNewInstance("classDistribution");
		Object key;
		for (int i = 0; i < nodeclasses.getLength(); i++) {
			currclass = (Element) nodeclasses.item(i);
			name = currclass.getAttribute(XML_A_CLASS_NAME);
			type = currclass.getAttribute(XML_A_CLASS_TYPE).equals("closed") ? CLASS_TYPE_CLOSED : CLASS_TYPE_OPEN;
			customers = new Integer(0);
			priority = 0;
			// As these elements are not mandatory, sets them to 0, then tries to parses them
			String tmp = currclass.getAttribute(XML_A_CLASS_CUSTOMERS);
			if (tmp != null && tmp != "") {
				customers = Integer.valueOf(tmp);
			}

			tmp = currclass.getAttribute(XML_A_CLASS_PRIORITY);
			if (tmp != null && tmp != "") {
				priority = Integer.parseInt(tmp);
			}

			// Now adds user class. Note that distribution will be set lately.
			key = model.addClass(name, type, priority, customers, defaultDistr);
			// Stores reference station as will be set lately (when we will have stations key)
			refStations.put(key, currclass.getAttribute(XML_A_CLASS_REFSOURCE));
			// Creates mapping class-name -> key into stations data structure
			classes.put(name, key);
		}
	}

	// -----------------------------------------------------------------------------------------------

	// --- Station section ---------------------------------------------------------------------------
	/**
	 * Parses all station related informations and puts them into data structure
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parseStations(Element root, CommonModel model) {
		// Initialize stations data structure
		stations = new TreeMap<String, Object>();
		NodeList nodestations = root.getElementsByTagName(XML_E_STATION);
		Object key;
		Element station;
		String type, name;
		NodeList sections;
		// For every station, identifies its type and parses its parameters
		for (int i = 0; i < nodestations.getLength(); i++) {
			station = (Element) nodestations.item(i);
			sections = station.getElementsByTagName(XML_E_STATION_SECTION);
			type = getStationType(station);
			name = station.getAttribute(XML_A_STATION_NAME);
			// Puts station into data structure
			key = model.addStation(name, type);
			// Creates mapping station-name -> key into stations data structure
			stations.put(name, key);
			// Handles source (set distribution)
			if (type.equals(STATION_TYPE_SOURCE)) {
				parseSourceSection((Element) sections.item(0), model, key, name);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_TERMINAL)) {
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_ROUTER)) {
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_DELAY)) {
				parseDelaySection((Element) sections.item(1), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_SERVER)) {
				parseQueueSection((Element) sections.item(0), model, key);
				parseServerSection((Element) sections.item(1), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_FORK)) {
				parseQueueSection((Element) sections.item(0), model, key);
				parseForkSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_JOIN)) {
				parseJoinSection((Element) sections.item(0), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_LOGGER)) {
				parseLoggerSection((Element) sections.item(1), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_CLASSSWITCH)) {
				parseQueueSection((Element) sections.item(0), model, key);
				parseClassSwitchSection((Element) sections.item(1), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_SEMAPHORE)) {
				parseSemaphoreSection((Element) sections.item(0), model, key);
				parseRouterSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_SCALER)) {
				parseJoinSection((Element) sections.item(0), model, key);
				parseForkSection((Element) sections.item(2), model, key);
			} else if (type.equals(STATION_TYPE_PLACE)) {
				parseStoreSection((Element) sections.item(0), model, key);
			} else if (type.equals(STATION_TYPE_TRANSITION)) {
				parseEnablingSection((Element) sections.item(0), model, key);
				parseTimingSection((Element) sections.item(1), model, key);
				parseFiringSection((Element) sections.item(2), model, key);
			}
		}
	}

	/**
	 * Extract all informations regarding Source section. If this source is reference class
	 * for any kind of open class, uses service time informations stored here to set distribution
	 * for this class.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 * @param stationName Name of current station. This is used to correctly set reference station
	 * distribution. That cannot be derived from model.getStationName(key) as JSim can change
	 * source name upon opening a model stored with JModel.
	 */
	protected static void parseSourceSection(Element section, CommonModel model, Object key, String stationName) {
		Element parameter = (Element) section.getElementsByTagName(XML_E_PARAMETER).item(0);
		// Now parses Service Distribution
		Map<String, Node> distributions = parseParameterRefclassArray(parameter);
		// Assign distribution for a class only if current source is its reference station
		Object[] classNames = distributions.keySet().toArray();
		Object classkey;
		for (Object className : classNames) {
			// If current class has this station as reference source and is open...
			if (refStations.get(classes.get(className)) != null && refStations.get(classes.get(className)).equals(stationName)
					&& model.getClassType(classes.get(className)) == CLASS_TYPE_OPEN) {
				classkey = classes.get(className);
				model.setClassDistribution(parseServiceStrategy((Element) distributions.get(className)), classkey);
				model.setClassRefStation(classkey, key);
				// Removes this class from refStations as it was already handled
				refStations.remove(classkey);
			}
		}
	}

	/**
	 * Extract all informations regarding Queue section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseQueueSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Element curr;
		String name, classpath;
		boolean fcfs = true;
		boolean ps = false;
		boolean srpt = false;
		Map<String, Node> putStrategy = null;
		Map<String, Node> dropRules = null;
		for (int i = 0; i < parameters.getLength(); i++) {
			curr = (Element) parameters.item(i);
			name = curr.getAttribute(XML_A_PARAMETER_NAME);
			classpath = curr.getAttribute(XML_A_PARAMETER_CLASSPATH);
			if (classpath.equals(queueGetFCFS)) {
				fcfs = true;
			} else if (classpath.equals(queueGetLCFS)) {
				fcfs = false;
			} else if (classpath.equals(queueGetPS)) {
				fcfs = false;
				ps = true;
			} else if (classpath.equals(queueGetSRPT)) {
				fcfs = false;
				srpt = true;
			} else if (classpath.equals(queuePut)) {
				putStrategy = parseParameterRefclassArray(curr);
			} else if (name.equals("size")) {
				Integer size = Integer.valueOf(findText(curr.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				model.setStationQueueCapacity(size, key);
			} else if (name.equals("dropStrategies")) {
				dropRules = parseParameterRefclassArray(curr);
			}
		}
		if (putStrategy != null) {
			Object[] classNames = putStrategy.keySet().toArray();
			String strategy;
			for (Object className : classNames) {
				strategy = ((Element) putStrategy.get(className)).getAttribute(XML_A_SUBPARAMETER_CLASSPATH);
				// Takes away classpath from put strategy name
				strategy = strategy.substring(strategy.lastIndexOf(".") + 1, strategy.length());
				// Now sets correct queue strategy, given combination of queueget and queueput policies
				if (ps) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_PS);
					model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
				} else if (srpt) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_SRPT);
					model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
				} else if (strategy.equals("TailStrategy")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE);
					if (fcfs) {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
					} else {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_LCFS);
					}
				} else if (strategy.equals("TailStrategyPriority")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY);
					if (fcfs) {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
					} else {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_LCFS);
					}
				} else if (strategy.equals("HeadStrategy")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE);
					if (fcfs) {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_LCFS);
					} else {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
					}
				} else if (strategy.equals("HeadStrategyPriority")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY);
					if (fcfs) {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_LCFS);
					} else {
						model.setQueueStrategy(key, classes.get(className), QUEUE_STRATEGY_FCFS);
					}
				} else if (strategy.equals("RandStrategy")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_RAND);
				} else if (strategy.equals("RandStrategyPriority")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_RAND);
				} else if (strategy.equals("SJFStrategy")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_SJF);
				} else if (strategy.equals("SJFStrategyPriority")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_SJF);
				} else if (strategy.equals("LJFStrategy")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_LJF);
				} else if (strategy.equals("LJFStrategyPriority")) {
					model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_QUEUE_PRIORITY);
					model.setQueueStrategy(key,classes.get(className), QUEUE_STRATEGY_LJF);
				}
			}
		}
		// Decodes drop rules
		if (dropRules != null) {
			Object[] classNames = dropRules.keySet().toArray();
			String strategy;
			for (Object className : classNames) {
				strategy = findText(((Element) dropRules.get(className)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setDropRule(key, classes.get(className), DROP_RULES_MAPPING.get(strategy));
			}
		}
	}

	/**
	 * Extract all informations regarding Delay section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseDelaySection(Element section, CommonModel model, Object key) {
		Element parameter = (Element) section.getElementsByTagName(XML_E_PARAMETER).item(0);
		// Retrieves all distributions subParameters
		Map<String, Node> distributions = parseParameterRefclassArray(parameter);
		Object[] classNames = distributions.keySet().toArray();
		// Sets service time distributions
		for (Object className : classNames) {
			model.setServiceTimeDistribution(key, classes.get(className), parseServiceStrategy((Element) distributions.get(className)));
		}
	}

	/**
	 * Extract all informations regarding Delay section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseServerSection(Element section, CommonModel model, Object key) {
		String className = section.getAttribute(XML_A_STATION_SECTION_CLASSNAME);
		if (CLASSNAME_PSSERVER.equals(className)) {
			model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_PS);
		}
		if (CLASSNAME_SRPT.equals(className)) {
			model.setStationQueueStrategy(key, QUEUE_STRATEGY_STATION_SRPT);
		}
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Element curr;
		String name, classpath;
		for (int i = 0; i < parameters.getLength(); i++) {
			curr = (Element) parameters.item(i);
			name = curr.getAttribute(XML_A_PARAMETER_NAME);
			classpath = curr.getAttribute(XML_A_PARAMETER_CLASSPATH);
			if (classpath.equals(serviceStrategy)) {
				// Retrieves all distributions subParameters
				Map<String, Node> distributions = parseParameterRefclassArray((Element) parameters.item(i));
				Object[] classNames = distributions.keySet().toArray();
				// Sets service time distributions
				for (Object className2 : classNames) {
					model.setServiceTimeDistribution(key, classes.get(className2), parseServiceStrategy((Element) distributions.get(className2)));
				}
			} else if (name.equals("maxJobs")) {
				// Sets number of servers
				Integer jobs = Integer.valueOf(findText(curr.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				model.setStationNumberOfServers(jobs, key);
			}
		}
	}

	/**
	 * Parses A Parameter Array node, returning a Map of ClassName -> subParameter
	 * @param parameterNode
	 * @return a Map of ClassName -> subParameter
	 */
	protected static Map<String, Node> parseParameterRefclassArray(Element parameterNode) {
		// For some reasons getElementsByTagName returns only first service time strategy.
		// So we need to look every children of parameterNode node.
		TreeMap<String, Node> res = new TreeMap<String, Node>();
		Node child = parameterNode.getFirstChild();
		String refClass;
		// This manual parsing is a bit unclean but works well and it is really fast.
		// I was forced to do in this way for the problem said before.
		while (child != null) {
			while (child != null && (child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals(XML_E_PARAMETER_REFCLASS))) {
				child = child.getNextSibling();
			}

			if (child == null) {
				break;
			}
			refClass = findText(child);
			// Now finds first subParameter element
			while (child != null && (child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals(XML_E_SUBPARAMETER))) {
				child = child.getNextSibling();
			}

			if (child == null) {
				break;
			}

			// Puts className and subParameter into destination Map
			res.put(refClass, child);
			child = child.getNextSibling();
		}

		return res;
	}

	/**
	 * Parses a parameter array and returns Vector of found subParameters
	 * @param parameterNode
	 * @return Vector with found subParameters
	 */
	protected static Vector<Node> parseParameterArray(Element parameterNode) {
		Vector<Node> ret = new Vector<Node>();
		Node child = parameterNode.getFirstChild();

		while (child != null) {
			while (child != null && (child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals(XML_E_SUBPARAMETER))) {
				child = child.getNextSibling();
			}

			if (child == null) {
				break;
			}
			// Puts found subParameter into destination Vector
			ret.add(child);
			child = child.getNextSibling();
		}
		return ret;
	}

	/**
	 * Parse router section
	 * @param section router section
	 * @param model data structure
	 * @param key station's key
	 */
	protected static void parseRouterSection(Element section, CommonModel model, Object key) {
		Element parameter = (Element) section.getElementsByTagName(XML_E_PARAMETER).item(0);
		Map<String, Node> routing = parseParameterRefclassArray(parameter);
		Object[] classNames = routing.keySet().toArray();
		String className;

		// Creates a Map of Name --> Routing Strategy if needed
		if (engineToGuiRouting == null) {
			engineToGuiRouting = new TreeMap<String, RoutingStrategy>();
			RoutingStrategy[] allRS = RoutingStrategy.findAll();
			for (RoutingStrategy element : allRS) {
				engineToGuiRouting.put(element.getClass().getName(), element);
			}
		}

		Object[] routStratKeys = engineToGuiRouting.keySet().toArray();
		for (Object className2 : classNames) {
			className = ((Element) routing.get(className2)).getAttribute(XML_A_SUBPARAMETER_CLASSPATH);
			// Searches all available routing strategy to find the one saved
			for (Object routStratKey : routStratKeys) {
				if (className.equals(engineToGuiRouting.get(routStratKey).getClassPath())) {
					model.setRoutingStrategy(key, classes.get(className2), engineToGuiRouting.get(routStratKey).clone());
				}
			}

			// Treat particular case of Empirical (Probabilities) Routing
			RoutingStrategy rs = (RoutingStrategy) model.getRoutingStrategy(key, classes.get(className2));
			if (rs instanceof ProbabilityRouting) {
				// Creates a Vector of all empirical entris. Could not be done automaticly
				// for the above problem with array (see parseParameterRefclassArray)
				Vector<Node> entries = new Vector<Node>();
				// Finds EntryArray node
				Node entryArray = routing.get(className2).getFirstChild();
				while (entryArray.getNodeType() != Node.ELEMENT_NODE || !entryArray.getNodeName().equals(XML_E_SUBPARAMETER)) {
					entryArray = entryArray.getNextSibling();
				}
				// Now finds every empirical entry
				Node child = entryArray.getFirstChild();
				while (child != null) {
					// Find first subParameter element
					while (child != null && (child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals(XML_E_SUBPARAMETER))) {
						child = child.getNextSibling();
					}
					if (child != null) {
						entries.add(child);
						child = child.getNextSibling();
					}
				}
				// For each empirical entry get station name and probability
				Map<String, Double> tmp = new TreeMap<String, Double>();
				for (int j = 0; j < entries.size(); j++) {
					NodeList values = ((Element) entries.get(j)).getElementsByTagName(XML_E_SUBPARAMETER);
					String stationName = findText(((Element) values.item(0)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					Double probability = Double.valueOf(findText(((Element) values.item(1)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
					// Now puts the tuple stationName -> probability into a Map, then adds it
					// to empiricalRouting Map. This is needed as at this
					// point we do not have all station's key so will be adjusted lately
					tmp.put(stationName, probability);
				}
				// Put into empiricalRouting a pair of station key and class key and map with
				// station names instead of station key
				empiricalRouting.put(new Object[] { key, classes.get(className2) }, tmp);
			}
			if (rs instanceof LoadDependentRouting) {
				Node entryArray = routing.get(className2).getFirstChild();
				Vector<Node> entriesNew = new Vector<Node>();
				while (entryArray != null && (entryArray.getNodeType() != Node.ELEMENT_NODE || !entryArray.getNodeName().equals(XML_E_SUBPARAMETER))) {
					entryArray = entryArray.getNextSibling();//This is a SubParameter..So next time it wont enter.
					String name = null;
					try {
						name = ((Element) entryArray).getAttribute("name");//LDParameter Here
						name = ((Element) entryArray).getAttribute("classPath");//"jmt.engine.NetStrategies.RoutingStrategies.LDParameter"
						name = ((Element) entryArray).getAttribute("array");//true
					} catch (Exception e) {
					}
					if (entryArray != null) {
						entriesNew.add(entryArray.getFirstChild());
					}
				}
				Vector<Node> from = new Vector<Node>();
				while (entryArray.getNodeType() != Node.ELEMENT_NODE || !entryArray.getNodeName().equals(XML_E_SUBPARAMETER)) {
					entryArray = entryArray.getNextSibling();//Did not enter as I am in LDParameter Array
				}
				// Now finds every From entry
				Node child = entryArray.getFirstChild();
				while (child != null) {
					while (child != null && (child.getNodeType() != Node.ELEMENT_NODE || !child.getNodeName().equals(XML_E_SUBPARAMETER))) {
						child = child.getNextSibling();//This is a SubParameter
						String name = null;
						try {
							name = ((Element) child).getAttribute("name");//LDParameter Here
							name = ((Element) child).getAttribute("classPath");//"jmt.engine.NetStrategies.ServiceStrategies.LDParameter"
							name = ((Element) child).getAttribute("array");//false
						} catch (Exception e) {
						}
					}
					if (child != null) {
						from.add(child);
						child = child.getNextSibling();//TXT
					}
				}
				LoadDependentRouting ld = new LoadDependentRouting();
				for (int j = 0; j < from.size(); j++) {
					NodeList values = ((Element) from.get(j)).getElementsByTagName(XML_E_SUBPARAMETER);
					String fromValue = findText(((Element) values.item(0)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					int fromVal = new Integer(fromValue);
					values = ((Element) values.item(1)).getElementsByTagName(XML_E_SUBPARAMETER);

					for (int n=0; n<values.getLength(); n++) {
						try{//Empirical Entry..Station Name..probability Keeps repeating..
							String atrName = ((Element) values.item(n)).getAttribute("name");//Empirical Entry..Station Name..probability..for a from.
							if ("EmpiricalEntry".equalsIgnoreCase(atrName)) {
								Node empiricalEntry = values.item(n);
								Node station = empiricalEntry.getFirstChild().getNextSibling();
								String stationName = findText(((Element)station).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
								Node probability = empiricalEntry.getFirstChild().getNextSibling().getNextSibling().getNextSibling();
								String probabilityValue = findText(((Element)probability).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
								Double prob = Double.parseDouble(probabilityValue);
								//ld.getRange(fromVal).setProbabilities(stationName, probVal);
								ld.addEmricalEntryForFrom(new Integer(fromVal),stationName, prob);
							}
						}catch(Exception e) {
						}
					}
				}
				rs = ld;
				model.setRoutingStrategy(key, classes.get(className2), rs);
			}
		}
	}

	/**
	 * Parse class switch section
	 * @param section router section
	 * @param model data structure
	 * @param key station's key
	 */
	protected static void parseClassSwitchSection(Element section, CommonModel model, Object stationKey) {
		Element matrix = (Element) section.getElementsByTagName(XML_E_PARAMETER).item(0);
		Map<String, Node> rows = parseParameterRefclassArray(matrix);
		Iterator<String> i = rows.keySet().iterator();

		while (i.hasNext()) {
			String classIn = i.next();
			Object classInKey = model.getClassByName(classIn);
			Element row = (Element) rows.get(classIn);
			NodeList rowChild = row.getChildNodes(); 
			for (int j = 0; j < rowChild.getLength();j++) {
				if (rowChild.item(j).getNodeType() == Node.TEXT_NODE ) {
					continue;
				}
				Node refClass = rowChild.item(j);
				String classOut = refClass.getChildNodes().item(0).getNodeValue();
				j++;
				while (rowChild.item(j).getNodeType() == Node.TEXT_NODE) {
					j++;
				}
				Node subParam = rowChild.item(j);
				NodeList subParamChild = subParam.getChildNodes();
				int h = 0;
				while (subParamChild.item(h).getNodeType() == Node.TEXT_NODE) {
					h++;
				}
				String value = subParamChild.item(h).getChildNodes().item(0).getNodeValue();
				Object classOutKey = model.getClassByName(classOut);    			
				model.setClassSwitchMatrix(stationKey, classInKey, classOutKey, Float.parseFloat(value));
			}
		}
	}

	/**
	 * Extract all parameters for a Logger section from the XML document.
	 * The information from parseLogger is passed to LogTunnel.
	 * 
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 * @author Michael Fercu (Bertoli Marco)
	 *		   Date: 08-aug-2008
	 * @see jmt.engine.log.LoggerParameters LoggerParameters
	 * @see jmt.gui.common.XMLWriter#writeLoggerSection XMLWriter.writeLoggerSection()
	 * @see jmt.gui.common.definitions.CommonModel#getLoggingParameters CommonModel.getLoggingParameters()
	 * @see jmt.gui.common.definitions.CommonModel#setLoggingParameters CommonModel.setLoggingParameters()
	 * @see jmt.engine.NodeSections.LogTunnel LogTunnel
	 */
	protected static void parseLoggerSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		LoggerParameters logParams = new LoggerParameters();

		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String parameterName = parameter.getAttribute(XML_A_PARAMETER_NAME);
			try {
				// Get the parameters from the XML file
				if (parameterName.equals(XML_LOG_FILENAME)) {
					logParams.name = new String(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_FILEPATH)) {
					//logParams.path = new String(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
					// temporary fix
					logParams.path = MacroReplacer.replace(model.getLoggingGlbParameter("path"));
				} else if (parameterName.equals(XML_LOG_B_EXECTIMESTAMP)) {
					logParams.boolExecTimestamp = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_LOGGERNAME)) {
					logParams.boolLoggername = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_TIMESTAMP)) {
					logParams.boolTimeStamp = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_JOBID)) {
					logParams.boolJobID = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_JOBCLASS)) {
					logParams.boolJobClass = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_TIMESAMECLS)) {
					logParams.boolTimeSameClass = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals(XML_LOG_B_TIMEANYCLS)) {
					logParams.boolTimeAnyClass = new Boolean(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0)));
				} else if (parameterName.equals("numClasses")) {
					/* No parsing needed for these parameters:
					 * Only useful to (and has already been passed to) the simulator. */;
				} else {
					debugLog.error("XMLReader.parseLogger() - Unknown parameter \"" + parameterName + "\".");
				}
			} catch (Exception e) {
				debugLog.error("XMLreader.parseLogger: " + e.toString());
			}

			model.setLoggingParameters(key, logParams);
		}
	}

	/**
	 * Extract all informations regarding Fork section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseForkSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String parameterName = parameter.getAttribute(XML_A_PARAMETER_NAME);
			// Fork number of server is used as number of jobs per link
			if (parameterName.equals("jobsPerLink")) {
				model.setStationNumberOfServers(Integer.decode(findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0))), key);
			} else if (parameterName.equals("block")) {
				model.setForkBlock(key, Integer.valueOf(findText(parameter
						.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0))));
			} else if (parameterName.equals("isSimplifiedFork")) {
				model.setIsSimplifiedFork(key, Boolean
						.parseBoolean(findText(parameter.getElementsByTagName(
								XML_E_PARAMETER_VALUE).item(0))));
			} else if (parameterName.equals("ForkStrategy")) {
				Map<String, Node> forking = parseParameterRefclassArray(parameter);
				Object[] classNames = forking.keySet().toArray();
				String className;

				if (engineToGuiForking == null) {
					engineToGuiForking = new TreeMap<String, ForkStrategy>();
					ForkStrategy[] allFS = ForkStrategy.findAll();
					for (ForkStrategy element : allFS) {
						engineToGuiForking.put(element.getClass().getName(),
								element);
					}
				}

				Object[] forkStratKeys = engineToGuiForking.keySet().toArray();
				for (Object className2 : classNames) {
					Map<String, OutPath> map = new HashMap<String, OutPath>();
					className = ((Element) forking.get(className2))
							.getAttribute(XML_A_SUBPARAMETER_CLASSPATH);
					for (Object forkStratKey : forkStratKeys) {
						if (className.equals(engineToGuiForking.get(
								forkStratKey).getClassPath())) {
							model.setForkingStrategy(key, classes
									.get(className2),
									engineToGuiForking.get(forkStratKey)
									.clone());
						}
					}

					ForkStrategy fs = (ForkStrategy) model.getForkingStrategy(
							key, classes.get(className2));
					if (fs instanceof ProbabilitiesFork) {
						ArrayList<Node> entries = new ArrayList<Node>();
						// Finds EntryArray node
						Node entryArray = forking.get(className2)
								.getFirstChild();
						while (entryArray.getNodeType() != Node.ELEMENT_NODE
								|| !entryArray.getNodeName().equals(
										XML_E_SUBPARAMETER)) {
							entryArray = entryArray.getNextSibling();
						}
						// Now finds every outPaths
						Node child = entryArray.getFirstChild();
						while (child != null) {
							// Find first subParameter element
							while (child != null
									&& (child.getNodeType() != Node.ELEMENT_NODE || !child
									.getNodeName().equals(
											XML_E_SUBPARAMETER))) {
								child = child.getNextSibling();
							}
							if (child != null) {
								entries.add(child);
								child = child.getNextSibling();
							}
						}
						// For each empirical entry get station name and
						// probability
						for (int j = 0; j < entries.size(); j++) {

							OutPath op = new OutPath();
							op.putEntry(1, 1.0);

							Node temp = entries.get(j).getFirstChild();
							while (temp.getNodeType() != Node.ELEMENT_NODE
									|| !temp.getNodeName().equals(
											XML_E_SUBPARAMETER)) {
								temp = temp.getNextSibling();
							}
							NodeList values = ((Element) temp)
									.getElementsByTagName(XML_E_SUBPARAMETER);
							String stationName = findText(((Element) values
									.item(0)).getElementsByTagName(
											XML_E_PARAMETER_VALUE).item(0));
							Double probability = Double
									.valueOf(findText(((Element) values.item(1))
											.getElementsByTagName(
													XML_E_PARAMETER_VALUE)
											.item(0)));
							op.setOutName(stationName);
							op.setProb(probability);
							temp = temp.getNextSibling();
							while (temp.getNodeType() != Node.ELEMENT_NODE
									|| !temp.getNodeName().equals(
											XML_E_SUBPARAMETER)) {
								temp = temp.getNextSibling();
							}
							ArrayList<Node> probs = new ArrayList<Node>();
							Node temp2 = temp.getFirstChild();
							while (temp2 != null) {
								// Find first subParameter element
								while (temp2 != null
										&& (temp2.getNodeType() != Node.ELEMENT_NODE || !temp2
										.getNodeName().equals(
												XML_E_SUBPARAMETER))) {
									temp2 = temp2.getNextSibling();
								}
								if (temp2 != null) {
									probs.add(temp2);
									temp2 = temp2.getNextSibling();
								}
							}
							HashMap<Object, Object> tempMap = new HashMap<Object, Object>();
							for (int k = 0; k < probs.size(); k++) {
								Node temp3 = probs.get(k);
								NodeList results = ((Element) temp3)
										.getElementsByTagName(XML_E_SUBPARAMETER);
								String num = findText(((Element) results
										.item(0)).getElementsByTagName(
												XML_E_PARAMETER_VALUE).item(0));
								Double prob = Double
										.valueOf(findText(((Element) results
												.item(1)).getElementsByTagName(
														XML_E_PARAMETER_VALUE).item(0)));
								tempMap.put(num, prob);
							}
							op.setOutParameters(tempMap);
							map.put(stationName, op);
						}
						empiricalForking.put(
								new Object[] { key, classes.get(className2) },
								map);
					} else if (fs instanceof CombFork) {
						ArrayList<Node> entries = new ArrayList<Node>();
						// Finds EntryArray node
						Node entryArray = forking.get(className2)
								.getFirstChild();
						while (entryArray.getNodeType() != Node.ELEMENT_NODE
								|| !entryArray.getNodeName().equals(
										XML_E_SUBPARAMETER)) {
							entryArray = entryArray.getNextSibling();
						}
						Node child = entryArray.getFirstChild();
						while (child != null) {
							// Find first subParameter element
							while (child != null
									&& (child.getNodeType() != Node.ELEMENT_NODE || !child
									.getNodeName().equals(
											XML_E_SUBPARAMETER))) {
								child = child.getNextSibling();
							}
							if (child != null) {
								entries.add(child);
								child = child.getNextSibling();
							}
						}
						Map<String, Double> tmp = new TreeMap<String, Double>();
						for (int j = 0; j < entries.size(); j++) {
							NodeList values = ((Element) entries.get(j))
									.getElementsByTagName(XML_E_SUBPARAMETER);
							String num = findText(((Element) values.item(0))
									.getElementsByTagName(XML_E_PARAMETER_VALUE)
									.item(0));
							Double probability = Double
									.valueOf(findText(((Element) values.item(1))
											.getElementsByTagName(XML_E_PARAMETER_VALUE)
											.item(0)));
							tmp.put(num, probability);	
						}
						combForking.put(
								new Object[] { key, classes.get(className2) }, tmp);
					} else if (fs instanceof MultiBranchClassSwitchFork || fs instanceof ClassSwitchFork) {
						ArrayList<Node> entries = new ArrayList<Node>();
						// Finds EntryArray node
						Node entryArray = forking.get(className2)
								.getFirstChild();
						while (entryArray.getNodeType() != Node.ELEMENT_NODE
								|| !entryArray.getNodeName().equals(
										XML_E_SUBPARAMETER)) {
							entryArray = entryArray.getNextSibling();
						}
						// Now finds every outPaths
						Node child = entryArray.getFirstChild();
						while (child != null) {
							// Find first subParameter element
							while (child != null
									&& (child.getNodeType() != Node.ELEMENT_NODE || !child
									.getNodeName().equals(
											XML_E_SUBPARAMETER))) {
								child = child.getNextSibling();
							}
							if (child != null) {
								entries.add(child);
								child = child.getNextSibling();
							}
						}
						// For each empirical entry get station name and
						// probability
						for (int j = 0; j < entries.size(); j++) {

							OutPath op = new OutPath();
							op.putEntry(1, 1.0);

							Node temp = entries.get(j).getFirstChild();
							while (temp.getNodeType() != Node.ELEMENT_NODE
									|| !temp.getNodeName().equals(
											XML_E_SUBPARAMETER)) {
								temp = temp.getNextSibling();
							}
							//NodeList values = ((Element) entries.get(j))
							//		.getElementsByTagName(XML_E_SUBPARAMETER);
							String stationName = findText(((Element) temp)
									.getElementsByTagName(
											XML_E_PARAMETER_VALUE).item(0));
							op.setOutName(stationName);
							temp = temp.getNextSibling();
							while (temp.getNodeType() != Node.ELEMENT_NODE
									|| !temp.getNodeName().equals(
											XML_E_SUBPARAMETER)) {
								temp = temp.getNextSibling();
							}
							ArrayList<Node> probs = new ArrayList<Node>();
							Node temp2 = temp.getFirstChild();
							while (temp2 != null) {
								// Find first subParameter element
								while (temp2 != null
										&& (temp2.getNodeType() != Node.ELEMENT_NODE || !temp2
										.getNodeName().equals(
												XML_E_SUBPARAMETER))) {
									temp2 = temp2.getNextSibling();
								}
								if (temp2 != null) {
									probs.add(temp2);
									temp2 = temp2.getNextSibling();
								}
							}
							List<String> classes = new ArrayList<String>();
							for (int k = 0; k < probs.size(); k++) {
								String jobClass = findText(((Element) probs.get(k))
										.getElementsByTagName(
												XML_E_PARAMETER_VALUE).item(0));
								//findText(((Element) results
								//		.item(0)).getElementsByTagName(
								//				XML_E_PARAMETER_VALUE).item(0));
								classes.add(jobClass);
							}
							temp = temp.getNextSibling();
							while (temp.getNodeType() != Node.ELEMENT_NODE
									|| !temp.getNodeName().equals(
											XML_E_SUBPARAMETER)) {
								temp = temp.getNextSibling();
							}
							probs = new ArrayList<Node>();
							temp2 = temp.getFirstChild();
							while (temp2 != null) {
								// Find first subParameter element
								while (temp2 != null
										&& (temp2.getNodeType() != Node.ELEMENT_NODE || !temp2
										.getNodeName().equals(
												XML_E_SUBPARAMETER))) {
									temp2 = temp2.getNextSibling();
								}
								if (temp2 != null) {
									probs.add(temp2);
									temp2 = temp2.getNextSibling();
								}
							}
							List<Object> numbers = new ArrayList<Object>();
							for (int k = 0; k < probs.size(); k++) {
								Integer number = Integer.parseInt(findText(((Element) probs.get(k))
										.getElementsByTagName(
												XML_E_PARAMETER_VALUE).item(0)));
								numbers.add(number);
							}
							HashMap<Object, Object> tempMap = new HashMap<Object, Object>();
							for (int k = 0; k < classes.size(); k++) {
								tempMap.put(model.getClassByName(classes.get(k)), numbers.get(k));
							}
							op.setOutParameters(tempMap);
							map.put(stationName, op);
						}
						empiricalForking.put(
								new Object[] { key, classes.get(className2) },
								map);
					}

				}
			}
		}
	}

	protected static void parseJoinSection(Element section, CommonModel model,
			Object key) {
		Element parameter = (Element) section.getElementsByTagName(
				XML_E_PARAMETER).item(0);
		Map<String, Node> join = null;
		if (parameter != null) {
			join = parseParameterRefclassArray(parameter);
			Object[] classNames = join.keySet().toArray();
			String className;

			if (engineToGuiJoin == null) {
				engineToGuiJoin = new TreeMap<String, JoinStrategy>();
				JoinStrategy[] allJS = JoinStrategy.findAll();
				for (JoinStrategy element : allJS) {
					engineToGuiJoin.put(element.getClass().getName(), element);
				}
			}

			Object[] joinStratKeys = engineToGuiJoin.keySet().toArray();
			for (Object className2 : classNames) {
				className = ((Element) join.get(className2))
						.getAttribute(XML_A_SUBPARAMETER_CLASSPATH);

				for (Object joinStratKey : joinStratKeys) {
					if (className.equals(engineToGuiJoin.get(joinStratKey)
							.getClassPath())) {
						model.setJoinStrategy(key, classes.get(className2),
								engineToGuiJoin.get(joinStratKey).clone());
					}
				}

				JoinStrategy js = (JoinStrategy) model.getJoinStrategy(key,
						classes.get(className2));
				if (js instanceof PartialJoin) {
					Element temp = (Element) join.get(className2);
					NodeList temp2 = temp.getElementsByTagName(XML_E_SUBPARAMETER);
					for (int i = 0; i < temp2.getLength(); i++) {
						Element temp3 = (Element) temp2.item(i);
						if (temp3.getAttribute(XML_A_CLASS_NAME).equals(
								"numRequired")) {
							js.setRequiredNum(Integer.parseInt(temp3
									.getElementsByTagName("value").item(0)
									.getFirstChild().getTextContent()));
						}
					}
				} else if (js instanceof GuardJoin) {
					Element temp = (Element) join.get(className2);
					NodeList parList = temp.getElementsByTagName(XML_E_SUBPARAMETER);
					Map<Object, Integer> mix = new HashMap<>();
					List<String> classes = new ArrayList<>();
					List<Integer> numbers = new ArrayList<>();
					for (int i = 0; i < parList.getLength(); i++) {
						Element par = (Element) parList.item(i);
						if (par.getAttribute(XML_A_CLASS_NAME).equals(
								"Classes")) {
							NodeList classList = par.getElementsByTagName(XML_E_SUBPARAMETER);
							for (int j = 0; j < classList.getLength(); j++) {
								Element classPar = (Element) classList.item(j);
								classes.add(classPar.getElementsByTagName("value").item(0).getFirstChild().getTextContent());
							}
						} else if (par.getAttribute(XML_A_CLASS_NAME).equals(
								"Numbers")) {
							NodeList numList = par.getElementsByTagName(XML_E_SUBPARAMETER);
							for (int j = 0; j < numList.getLength(); j++) {
								Element numPar = (Element) numList.item(j);
								numbers.add(Integer.parseInt(numPar.getElementsByTagName("value").item(0).getFirstChild().getTextContent()));
							}
						}
					}
					for (int i = 0; i < classes.size(); i++) {
						mix.put(model.getClassByName(classes.get(i)), numbers.get(i));
					}
					((GuardJoin) js).setGuard(mix);
				}
			}
		} else {
			for (Object o: classes.values()) {
				model.setJoinStrategy(key, o, CommonConstants.JOINING_NORMAL.clone());
			}
		}
	}

	protected static void parseSemaphoreSection(Element section, CommonModel model,
			Object key) {
		Element parameter = (Element) section.getElementsByTagName(
				XML_E_PARAMETER).item(0);
		Map<String, Node> semaphore = null;
		if (parameter != null) {
			semaphore = parseParameterRefclassArray(parameter);
			Object[] classNames = semaphore.keySet().toArray();
			String className;

			if (engineToGuiSemaphore == null) {
				engineToGuiSemaphore = new TreeMap<String, SemaphoreStrategy>();
				SemaphoreStrategy[] allSS = SemaphoreStrategy.findAll();
				for (SemaphoreStrategy element : allSS) {
					engineToGuiSemaphore.put(element.getClass().getName(), element);
				}
			} 

			Object[] semaphoreStratKeys = engineToGuiSemaphore.keySet().toArray();
			for (Object className2 : classNames) {
				className = ((Element) semaphore.get(className2))
						.getAttribute(XML_A_SUBPARAMETER_CLASSPATH);

				for (Object semaphoreStratKey : semaphoreStratKeys) {
					if (className.equals(engineToGuiSemaphore.get(semaphoreStratKey)
							.getClassPath())) {
						model.setSemaphoreStrategy(key, classes.get(className2),
								engineToGuiSemaphore.get(semaphoreStratKey).clone());
					}
				}

				SemaphoreStrategy ss = (SemaphoreStrategy) model.getSemaphoreStrategy(key,
						classes.get(className2));
				if (ss instanceof NormalSemaphore) {
					Element temp = (Element) semaphore.get(className2);
					NodeList temp2 = temp.getElementsByTagName(XML_E_SUBPARAMETER);
					for (int i = 0; i < temp2.getLength(); i++) {
						Element temp3 = (Element) temp2.item(i);
						if (temp3.getAttribute(XML_A_CLASS_NAME).equals(
								"SemThres")) {
							ss.setSemaphoreThres(Integer.parseInt(temp3
									.getElementsByTagName("value").item(0)
									.getFirstChild().getTextContent()));
						}
					}
				}
			}
		} else {
			for (Object o: classes.values()) {
				model.setSemaphoreStrategy(key, o, CommonConstants.SEMAPHORE_NORMAL.clone());
			}
		}
	}

	/**
	 * Extract all informations regarding Store section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseStoreSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Map<String, Node> dropRules = null;
		Map<String, Node> putStrategies = null;

		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String name = parameter.getAttribute(XML_A_PARAMETER_NAME);
			if (name.equals("capacity")) {
				String capacity = findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setStationQueueCapacity(Integer.valueOf(capacity), key);
			} else if (name.equals("dropRules")) {
				dropRules = parseParameterRefclassArray(parameter);
			} else if (name.equals("putStrategies")) {
				putStrategies = parseParameterRefclassArray(parameter);
			}
		}

		if (dropRules != null) {
			for (Entry<String, Node> rule : dropRules.entrySet()) {
				String dropRule = findText(((Element) rule.getValue()).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setDropRule(key, classes.get(rule.getKey()), DROP_RULES_MAPPING.get(dropRule));
			}
		}

		if (putStrategies != null) {
			for (Entry<String, Node> strategy : putStrategies.entrySet()) {
				String putStrategy = ((Element) strategy.getValue()).getAttribute(XML_A_SUBPARAMETER_CLASSPATH);
				putStrategy = putStrategy.substring(putStrategy.lastIndexOf(".") + 1);
				if (putStrategy.equals("TailStrategy")) {
					model.setQueueStrategy(key, classes.get(strategy.getKey()), QUEUE_STRATEGY_FCFS);
				} else if (putStrategy.equals("HeadStrategy")) {
					model.setQueueStrategy(key, classes.get(strategy.getKey()), QUEUE_STRATEGY_LCFS);
				} else if (putStrategy.equals("RandStrategy")) {
					model.setQueueStrategy(key, classes.get(strategy.getKey()), QUEUE_STRATEGY_RAND);
				}
			}
		}
	}

	/**
	 * Extract all informations regarding Enabling section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseEnablingSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Map<String, Node> enablingConditions = null;
		Map<String, Node> inhibitingConditions = null;

		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String name = parameter.getAttribute(XML_A_PARAMETER_NAME);
			if (name.equals("enablingConditions")) {
				enablingConditions = parseParameterRefclassArray(parameter);
			} else if (name.equals("inhibitingConditions")) {
				inhibitingConditions = parseParameterRefclassArray(parameter);
			}
		}

		if (enablingConditions != null) {
			for (Entry<String, Node> condition : enablingConditions.entrySet()) {
				Vector<Node> childNodes = parseParameterArray((Element) condition.getValue());
				Vector<Node> enablingEntries = parseParameterArray((Element) childNodes.get(0));
				Map<String, Integer> enablingEntryMap = new HashMap<String, Integer>();
				for (Node entry : enablingEntries) {
					NodeList values = ((Element) entry).getElementsByTagName(XML_E_SUBPARAMETER);
					String stationName = findText(((Element) values.item(0)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					String numberOfJobs = findText(((Element) values.item(1)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					enablingEntryMap.put(stationName, Integer.valueOf(numberOfJobs));
				}
				enablingConditionMap.put(new Object[] {key, classes.get(condition.getKey())}, enablingEntryMap);
			}
		}

		if (inhibitingConditions != null) {
			for (Entry<String, Node> condition : inhibitingConditions.entrySet()) {
				Vector<Node> childNodes = parseParameterArray((Element) condition.getValue());
				Vector<Node> inhibitingEntries = parseParameterArray((Element) childNodes.get(0));
				Map<String, Integer> inhibitingEntryMap = new HashMap<String, Integer>();
				for (Node entry : inhibitingEntries) {
					NodeList values = ((Element) entry).getElementsByTagName(XML_E_SUBPARAMETER);
					String stationName = findText(((Element) values.item(0)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					String numberOfJobs = findText(((Element) values.item(1)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					inhibitingEntryMap.put(stationName, Integer.valueOf(numberOfJobs));
				}
				inhibitingConditionMap.put(new Object[] {key, classes.get(condition.getKey())}, inhibitingEntryMap);
			}
		}
	}

	/**
	 * Extract all informations regarding Timing section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseTimingSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Map<String, Node> timingStrategies = null;
		Map<String, Node> firingPriorities = null;
		Map<String, Node> firingWeights = null;

		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String name = parameter.getAttribute(XML_A_PARAMETER_NAME);
			if (name.equals("numberOfServers")) {
				String numberOfServers = findText(parameter.getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setStationNumberOfServers(Integer.valueOf(numberOfServers), key);
			} else if (name.equals("timingStrategies")) {
				timingStrategies = parseParameterRefclassArray(parameter);
			} else if (name.equals("firingPriorities")) {
				firingPriorities = parseParameterRefclassArray(parameter);
			} else if (name.equals("firingWeights")) {
				firingWeights = parseParameterRefclassArray(parameter);
			}
		}

		if (timingStrategies != null) {
			for (Entry<String, Node> strategy : timingStrategies.entrySet()) {
				Object timingStrategy = parseServiceStrategy((Element) strategy.getValue());
				model.setServiceTimeDistribution(key, classes.get(strategy.getKey()), timingStrategy);
			}
		}

		if (firingPriorities != null) {
			for (Entry<String, Node> priority : firingPriorities.entrySet()) {
				String firingPriority = findText(((Element) priority.getValue()).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setFiringPriority(key, classes.get(priority.getKey()), Integer.valueOf(firingPriority));
			}
		}

		if (firingWeights != null) {
			for (Entry<String, Node> weight : firingWeights.entrySet()) {
				String firingWeight = findText(((Element) weight.getValue()).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
				model.setFiringWeight(key, classes.get(weight.getKey()), Integer.valueOf(firingWeight));
			}
		}
	}

	/**
	 * Extract all informations regarding Firing section.
	 * @param section input section of source station
	 * @param model link to data structure
	 * @param key key of search for this source station into data structure
	 */
	protected static void parseFiringSection(Element section, CommonModel model, Object key) {
		NodeList parameters = section.getElementsByTagName(XML_E_PARAMETER);
		Map<String, Node> firingOutcomes = null;

		for (int i = 0; i < parameters.getLength(); i++) {
			Element parameter = (Element) parameters.item(i);
			String name = parameter.getAttribute(XML_A_PARAMETER_NAME);
			if (name.equals("firingOutcomes")) {
				firingOutcomes = parseParameterRefclassArray(parameter);
			}
		}

		if (firingOutcomes != null) {
			for (Entry<String, Node> outcome : firingOutcomes.entrySet()) {
				Vector<Node> childNodes = parseParameterArray((Element) outcome.getValue());
				Vector<Node> firingEntries = parseParameterArray((Element) childNodes.get(0));
				Map<String, Integer> firingEntryMap = new HashMap<String, Integer>();
				for (Node entry : firingEntries) {
					NodeList values = ((Element) entry).getElementsByTagName(XML_E_SUBPARAMETER);
					String stationName = findText(((Element) values.item(0)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					String numberOfJobs = findText(((Element) values.item(1)).getElementsByTagName(XML_E_PARAMETER_VALUE).item(0));
					firingEntryMap.put(stationName, Integer.valueOf(numberOfJobs));
				}
				firingOutcomeMap.put(new Object[] {key, classes.get(outcome.getKey())}, firingEntryMap);
			}
		}
	}

	/**
	 * Parses service section informations contained in serviceTimeStrategy element to create a
	 * correct Distribution or LDStrategy object
	 * @param serviceTimeStrategy Element that holds all distribution informations
	 * @return created Distribution or LDStrategy or null if this field is set to null
	 */
	protected static Object parseServiceStrategy(Element serviceTimeStrategy) {
		String serviceClassPath = serviceTimeStrategy.getAttribute(XML_A_SUBPARAMETER_CLASSPATH);

		if (serviceClassPath.equals(ZeroStrategy.getEngineClassPath())) {
			// Zero Service Time Strategy
			return new ZeroStrategy();
		} else if (serviceClassPath.equals(LDStrategy.getEngineClassPath())) {
			// Load Dependent Service Strategy
			Element LDParameterArray = (Element) serviceTimeStrategy.getElementsByTagName(XML_E_SUBPARAMETER).item(0);
			LDStrategy strategy = new LDStrategy();
			// Now parses LDStrategy ranges
			Vector<Node> ranges = parseParameterArray(LDParameterArray);
			for (int i = 0; i < ranges.size(); i++) {
				Vector<Node> parameters = parseParameterArray((Element) ranges.get(i));
				int from = Integer.parseInt(findText(((Element) parameters.get(0)).getElementsByTagName(XML_E_SUBPARAMETER_VALUE).item(0)));
				Distribution distr = parseDistribution((Element) parameters.get(1), (Element) parameters.get(2));
				String mean = findText(((Element) parameters.get(3)).getElementsByTagName(XML_E_SUBPARAMETER_VALUE).item(0));
				Object key;
				if (from == 1) {
					// If this is first range
					key = strategy.getAllRanges()[0];
				} else {
					// next ranges
					key = strategy.addRange();
					strategy.setRangeFrom(key, from);
					// This is needed as key will change
					key = strategy.getAllRanges()[strategy.getRangeNumber() - 1];
				}
				strategy.setRangeDistributionNoCheck(key, distr);
				strategy.setRangeDistributionMeanNoCheck(key, mean);
			}
			return strategy;
		} else {
			//use the parseParameterArray function to return only DIRECT subparameters
			Vector<Node> distribution = parseParameterArray(serviceTimeStrategy);
			if (distribution.size() == 0) {
				return null;
			}
			return parseDistribution((Element) distribution.get(0), (Element) distribution.get(1));
		}
	}

	/**
	 * Parses a distribution, given its distribution and distributionPar nodes
	 * @param distr distribution node
	 * @param distrPar distribution's parameter node
	 * @return parsed distribution
	 */
	protected static Distribution parseDistribution(Element distr, Element distrPar) {
		String classname = distr.getAttribute(XML_A_SUBPARAMETER_CLASSPATH);

		//get the subparameter which are directly passed to the distribution
		Vector<Node> distributionParameters = parseParameterArray(distr);
		//add the subparameters which are passed to the distribution parameter
		distributionParameters.addAll(parseParameterArray(distrPar));

		// Creates a map with distribution classpath --> Distribution if needed
		if (engineToGuiDistr == null) {
			Distribution[] allDistr = Distribution.findAll();
			engineToGuiDistr = new TreeMap<String, Distribution>();
			for (Distribution element : allDistr) {
				engineToGuiDistr.put(element.getClassPath(), element);
			}
		}

		// Gets correct instance of distribution
		Distribution dist = engineToGuiDistr.get(classname).clone();
		Element currpar;
		String param_name;
		for (int i = 0; i < distributionParameters.size(); i++) {
			currpar = (Element) distributionParameters.get(i);
			param_name = currpar.getAttribute(XML_A_SUBPARAMETER_NAME);
			//if current parameter is a nested Distribution
			if (currpar.getAttribute(XML_A_SUBPARAMETER_CLASSPATH).equals(distributionContainer)) {

				//parse the currentparameter to get DIRECT subparameters
				Vector<Node> nestedDistr = parseParameterArray(currpar);
				// If distribution is not set, returns null
				Object param_value = null;
				if (nestedDistr.size() == 0) {
					param_value = null;
				} else {
					//parse the nested distribution
					param_value = parseDistribution((Element) nestedDistr.get(0), (Element) nestedDistr.get(1));
					dist.getParameter(param_name).setValue(param_value);
				}

			} else {
				String param_value = findText(currpar.getElementsByTagName(XML_E_SUBPARAMETER_VALUE).item(0));
				dist.getParameter(param_name).setValue(param_value);
			}

			dist.updateCM(); // Updates values of c and mean
		}
		return dist;
	}

	/**
	 * Returns the type of a station, reconstructing it from section names. This method must be
	 * modified if a new station type is inserted.
	 * @param station element containing sections
	 * @return station type as expected by CommonModel / JMODELModel
	 */
	protected static String getStationType(Element station) {
		NodeList sections = station.getElementsByTagName(XML_E_STATION_SECTION);
		String[] sectionNames = new String[sections.getLength()];

		// Gets all section classnames
		for (int i = 0; i < sectionNames.length; i++) {
			sectionNames[i] = ((Element) sections.item(i)).getAttribute(XML_A_STATION_SECTION_CLASSNAME);
		}

		// Finds station type, basing on section names
		if (sectionNames[0].equals(CLASSNAME_SOURCE)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_SOURCE;
		} else if (sectionNames[0].equals(CLASSNAME_SINK)) {
			return STATION_TYPE_SINK;
		} else if (sectionNames[0].equals(CLASSNAME_TERMINAL)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_TERMINAL;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_ROUTER;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& sectionNames[1].equals(CLASSNAME_DELAY)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_DELAY;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& (sectionNames[1].equals(CLASSNAME_SERVER)
						|| sectionNames[1].equals(CLASSNAME_PSSERVER)
						|| sectionNames[1].equals(CLASSNAME_SRPT))
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_SERVER;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_FORK)) {
			return STATION_TYPE_FORK;
		} else if (sectionNames[0].equals(CLASSNAME_JOIN)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_JOIN;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& sectionNames[1].equals(CLASSNAME_LOGGER)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_LOGGER;
		} else if (sectionNames[0].equals(CLASSNAME_QUEUE)
				&& sectionNames[1].equals(CLASSNAME_CLASSSWITCH)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_CLASSSWITCH;
		} else if (sectionNames[0].equals(CLASSNAME_SEMAPHORE)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_ROUTER)) {
			return STATION_TYPE_SEMAPHORE;
		} else if (sectionNames[0].equals(CLASSNAME_JOIN)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_FORK)) {
			return STATION_TYPE_SCALER;
		} else if (sectionNames[0].equals(CLASSNAME_STORE)
				&& sectionNames[1].equals(CLASSNAME_TUNNEL)
				&& sectionNames[2].equals(CLASSNAME_LINK)) {
			return STATION_TYPE_PLACE;
		} else if (sectionNames[0].equals(CLASSNAME_ENABLING)
				&& sectionNames[1].equals(CLASSNAME_TIMING)
				&& sectionNames[2].equals(CLASSNAME_FIRING)) {
			return STATION_TYPE_TRANSITION;
		}
		return null;
	}

	// -----------------------------------------------------------------------------------------------

	// --- Measure section ---------------------------------------------------------------------------
	/**
	 * Parses all informations on measures to be taken during simulation
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parseMeasures(Element root, CommonModel model) {
		NodeList measures = root.getElementsByTagName(XML_E_MEASURE);
		Object stationKey, classKey;
		String type;
		Double alpha, precision;
		for (int i = 0; i < measures.getLength(); i++) {
			Element measure = (Element) measures.item(i);
			String stationName = measure.getAttribute(XML_A_MEASURE_STATION);
			String nodeType = measure.getAttribute(XML_A_MEASURE_NODETYPE);
			if (stationName != null && !stationName.equals("")) {
				if (nodeType.equalsIgnoreCase(NODETYPE_REGION)) {
					stationKey = regions.get(stationName);
				} else {
					stationKey = stations.get(stationName);
				}
			} else {
				stationKey = null;
			}
			String className = measure.getAttribute(XML_A_MEASURE_CLASS);
			if (className != null && !className.equals("")) {
				classKey = classes.get(className);
			} else {
				classKey = null;
			}
			type = measure.getAttribute(XML_A_MEASURE_TYPE);
			//Begins all backward compatibility conditions for Changes of Labels of Perf Index.
			//Supports old names
			if ("Customer Number".equalsIgnoreCase(type) && "".equalsIgnoreCase(stationName)) {
				type = SimulationDefinition.MEASURE_S_CN;
			}
			if ("System Customer Number".equalsIgnoreCase(type)) {
				type = SimulationDefinition.MEASURE_S_CN;
			}
			if ("Number of Customers".equalsIgnoreCase(type) && "".equalsIgnoreCase(stationName)) {
				type = SimulationDefinition.MEASURE_S_CN;
			}
			if ("Queue Length".equalsIgnoreCase(type)) {
				type = SimulationDefinition.MEASURE_QL;
			}
			//if ("Fairness".equalsIgnoreCase(type)) {
			//	type = SimulationDefinition.MEASURE_FAIRNESS;
			//}
			if ("Customer Number".equalsIgnoreCase(type) && !"".equalsIgnoreCase(stationName)) {
				type = SimulationDefinition.MEASURE_QL;
			}
			//Ends the backward compatibility conditions
			//Inverts alpha
			alpha = new Double(1 - Double.parseDouble(measure.getAttribute(XML_A_MEASURE_ALPHA)));
			precision = Double.valueOf(measure.getAttribute(XML_A_MEASURE_PRECISION));
			String verboseStr = measure.getAttribute(XML_A_MEASURE_VERBOSE);
			boolean verbose = Boolean.parseBoolean(verboseStr);

			//Adds measure to the model
			model.addMeasure(type, stationKey, classKey, alpha, precision, verbose);
		}
	}

	// -----------------------------------------------------------------------------------------------

	// --- Connection section ------------------------------------------------------------------------
	/**
	 * Parses all informations on connections to be made into model
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parseConnections(Element root, CommonModel model) {
		NodeList connections = root.getElementsByTagName(XML_E_CONNECTION);
		Object sourceKey, targetKey;
		for (int i = 0; i < connections.getLength(); i++) {
			sourceKey = stations.get(((Element) connections.item(i)).getAttribute(XML_A_CONNECTION_SOURCE));
			targetKey = stations.get(((Element) connections.item(i)).getAttribute(XML_A_CONNECTION_TARGET));
			// Adds connection to data structure
			model.setConnected(sourceKey, targetKey, true);
		}
	}

	// -----------------------------------------------------------------------------------------------

	// --- Preloading section ------------------------------------------------------------------------
	/**
	 * Parses all informations on preloading to be added to the model
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parsePreloading(Element root, CommonModel model) {
		NodeList preload = root.getElementsByTagName(XML_E_PRELOAD);
		if (preload.getLength() > 0) {
			// For every station, search for classes and initial jobs in queue
			NodeList station_pop = ((Element) preload.item(0)).getElementsByTagName(XML_E_STATIONPOPULATIONS);
			for (int i = 0; i < station_pop.getLength(); i++) {
				Object stationKey = stations.get(((Element) station_pop.item(i)).getAttribute(XML_A_PRELOADSTATION_NAME));
				NodeList class_pop = ((Element) station_pop.item(i)).getElementsByTagName(XML_E_CLASSPOPULATION);
				for (int j = 0; j < class_pop.getLength(); j++) {
					Object classKey = classes.get(((Element) class_pop.item(j)).getAttribute(XML_A_CLASSPOPULATION_NAME));
					Integer jobs = new Integer(((Element) class_pop.item(j)).getAttribute(XML_A_CLASSPOPULATION_POPULATION));
					// Sets preloading informations
					model.setPreloadedJobs(jobs, stationKey, classKey);
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------

	// --- Blocking regions section ------------------------------------------------------------------
	/**
	 * Parses all informations on blocking regions to be added to the model
	 * @param root root element of XML Document
	 * @param model data structure where all properties have to be set
	 */
	protected static void parseBlockingRegions(Element root, CommonModel model) {
		regions = new TreeMap<String, Object>();
		NodeList regionNodes = root.getElementsByTagName(XML_E_REGION);

		// Creates each region into data structure
		for (int i = 0; i < regionNodes.getLength(); i++) {
			Element region = (Element) regionNodes.item(i);
			String name = region.getAttribute(XML_A_REGION_NAME);
			String type = region.getAttribute(XML_A_REGION_TYPE);
			if (type == null || type.equals("")) {
				type = Defaults.get("blockingRegionType");
			}
			// Adds blocking region to data structure
			Object key = model.addBlockingRegion(name, type);
			regions.put(name, key);

			// Now parses all included stations
			NodeList nodes = region.getElementsByTagName(XML_E_REGIONNODE);
			for (int j = 0; j < nodes.getLength(); j++) {
				String stationName = ((Element) nodes.item(j)).getAttribute(XML_A_REGIONNODE_NAME);
				model.addRegionStation(key, stations.get(stationName));
			}

			// Now parses global constraint
			Element globalConstraint = (Element) region.getElementsByTagName(XML_E_GLOBALCONSTRAINT).item(0);
			//TODO Add support for Double global constraints
			int ivalue = Double.valueOf(globalConstraint.getAttribute(XML_A_GLOBALCONSTRAINT_MAXJOBS)).intValue();
			model.setRegionCustomerConstraint(key, Integer.valueOf(ivalue));

			Element memoryConstraint = (Element) region.getElementsByTagName(XML_E_GLOBALMEMORYCONSTRAINT).item(0);
			if (memoryConstraint == null) {
				ivalue = -1;
			} else {
				ivalue = Double.valueOf(memoryConstraint.getAttribute(XML_A_GLOBALMEMORYCONSTRAINT_MAXMEMORY)).intValue();
			}
			model.setRegionMemorySize(key, Integer.valueOf(ivalue));

			// Now parses class constraints
			NodeList classConstraints = region.getElementsByTagName(XML_E_CLASSCONSTRAINT);
			for (int j = 0; j < classConstraints.getLength(); j++) {
				Element classConstraint = (Element) classConstraints.item(j);
				//TODO Add support for Double class constraints
				ivalue = Double.valueOf(classConstraint.getAttribute(XML_A_CLASSCONSTRAINT_MAXJOBS)).intValue();
				model.setRegionClassCustomerConstraint(key, classes.get(classConstraint.getAttribute(XML_A_CLASSCONSTRAINT_CLASS)),
						Integer.valueOf(ivalue));
			}

			NodeList classWeights = region.getElementsByTagName(XML_E_CLASSWEIGHT);
			for (int j = 0; j < classWeights.getLength(); j++) {
				Element classWeight = (Element) classWeights.item(j);
				model.setRegionClassWeight(key, classes.get(classWeight.getAttribute(XML_A_CLASSWEIGHT_CLASS)),
						Integer.valueOf(classWeight.getAttribute(XML_A_CLASSWEIGHT_WEIGHT)));
			}

			NodeList classSizes = region.getElementsByTagName(XML_E_CLASSMEMORYSIZE);
			for (int j = 0; j < classSizes.getLength(); j++) {
				Element classSize = (Element) classSizes.item(j);
				model.setRegionClassSize(key, classes.get(classSize.getAttribute(XML_A_CLASSMEMORY_CLASS)),
						Integer.valueOf(classSize.getAttribute(XML_A_CLASSMEMORY_SIZE)));
			}

			NodeList dropRules = region.getElementsByTagName(XML_E_DROPRULES);
			for (int j = 0; j < dropRules.getLength(); j++) {
				Element dropRule = (Element) dropRules.item(j);
				model.setRegionClassDropRule(key, classes.get(dropRule.getAttribute(XML_A_DROPRULE_CLASS)),
						Boolean.valueOf(dropRule.getAttribute(XML_A_DROPRULE_DROP)));
			}

			// Now parses group constraints
			NodeList groupConstraints = region.getElementsByTagName(XML_E_GROUPCONSTRAINT);
			for (int j = 0; j < groupConstraints.getLength(); j++) {
				Element groupConstraint = (Element) groupConstraints.item(j);
				//TODO Add support for Double group constraints
				ivalue = Double.valueOf(groupConstraint.getAttribute(XML_A_GROUPCONSTRAINT_MAXJOBS)).intValue();
				model.addRegionGroup(key, groupConstraint.getAttribute(XML_A_GROUPCONSTRAINT_GROUP), Integer.valueOf(ivalue), "");
			}

			NodeList groupStrategies = region.getElementsByTagName(XML_E_GROUPSTRATEGY);
			for (int j = 0; j < groupStrategies.getLength(); j++) {
				Element groupStrategy = (Element) groupStrategies.item(j);
				model.setRegionGroupStrategy(key, j, groupStrategy.getAttribute(XML_A_GROUPSTRATEGY_STRATEGY));
			}

			NodeList groupClassLists = region.getElementsByTagName(XML_E_GROUPCLASSLIST);
			for (int j = 0; j < groupClassLists.getLength(); j++) {
				Element groupClassList = (Element) groupClassLists.item(j);
				NodeList groupClasses = groupClassList.getElementsByTagName(XML_E_GROUPCLASS);
				for (int k = 0; k < groupClasses.getLength(); k++) {
					Element groupClass = (Element) groupClasses.item(k);
					model.addClassIntoRegionGroup(key, j, model.getClassByName(groupClass.getAttribute(XML_A_GROUPCLASS_CLASS)));
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------

	// --- Generic XML Loader ------------------------------------------------------------------------
	/**
	 * Loads an XML file, returning the Document representation of it. This method is generic
	 * and can be used to load every xml file. Actually it is used by <code>XMLReader</code>
	 * and by <code>GuiXMLReader</code>. This method will validate input file.
	 * @param filename name of the file to be loaded
	 * @param schemaSource url of schema to be used to validate the model
	 * @return Document representation of input xml file
	 */
	public static Document loadXML(String filename, String schemaSource) {
		DOMParser parser = new DOMParser();
		try {
			// Sets validation only if needed
			if (schemaSource != null) {
				parser.setFeature(NAMESPACES_FEATURE_ID, true);
				parser.setFeature(VALIDATION_FEATURE_ID, true);
				parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
				parser.setFeature(VALIDATION_DYNAMIC_FEATURE_ID, true);
				parser.setProperty(EXTERNAL_SCHEMA_LOCATION_PROPERTY_ID, schemaSource);
			}

			FileReader fr = new FileReader(filename);
			InputSource in_source = new InputSource(fr);
			//TODO: the parser must first be created
			parser.parse(in_source);
			return parser.getDocument();
		} catch (SAXException e) {
			System.err.println("XMLLoader Error - An error occurs while attempting to parse the document \"" + e.getMessage() + "\".");

			/*JOptionPane.showMessageDialog(null,
					"An error occurs while attempting to parse the document \"" + e.getMessage() + "\".",
					"JMT - File error",
					JOptionPane.ERROR_MESSAGE);*/
			return null;
		} catch (IOException e) {
			System.err.println("XMLLoader Error - An error occurs while attempting to parse the document.");
			return null;
		}
	}

	/**
	 * Loads an XML file, returning the Document representation of it. This method is generic
	 * and can be used to load every xml file. Actually it is used by <code>XMLReader</code>
	 * and by <code>GuiXMLReader</code>. This method will <b>not</b> validate input file.
	 * @param filename name of the file to be loaded
	 * @return Document representation of input xml file
	 */
	public static Document loadXML(String filename) {
		return loadXML(filename, null);
	}

	// -----------------------------------------------------------------------------------------------

	// --- Debug -------------------------------------------------------------------------------------
	/**
	 * This method is used for debug purpose to write a portion of xml on standard output.
	 * This can be removed freely!
	 * @param node node to be written on standard output
	 */
	protected static void write(Node node) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty("indent", "yes");
			transformer.setOutputProperty("encoding", ENCODING);
			transformer.transform(new DOMSource(node), new StreamResult(System.out));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace(); //To change body of catch statement use Options | File Templates.
		} catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
			transformerFactoryConfigurationError.printStackTrace(); //To change body of catch statement use Options | File Templates.
		} catch (TransformerException e) {
			e.printStackTrace(); //To change body of catch statement use Options | File Templates.
		}

	}
	// -----------------------------------------------------------------------------------------------

}
