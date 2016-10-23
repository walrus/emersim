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

package jmt.gui.common;

import java.awt.Dimension;

import jmt.framework.data.ConstMap;
import jmt.gui.common.forkStrategies.MultiBranchClassSwitchFork;
import jmt.gui.common.forkStrategies.ClassSwitchFork;
import jmt.gui.common.forkStrategies.CombFork;
import jmt.gui.common.forkStrategies.ForkStrategy;
import jmt.gui.common.forkStrategies.ProbabilitiesFork;
import jmt.gui.common.joinStrategies.GuardJoin;
import jmt.gui.common.joinStrategies.JoinStrategy;
import jmt.gui.common.joinStrategies.NormalJoin;
import jmt.gui.common.joinStrategies.PartialJoin;
import jmt.gui.common.routingStrategies.FastestServiceRouting;
import jmt.gui.common.routingStrategies.LeastUtilizationRouting;
import jmt.gui.common.routingStrategies.LoadDependentRouting;
import jmt.gui.common.routingStrategies.ProbabilityRouting;
import jmt.gui.common.routingStrategies.RandomRouting;
import jmt.gui.common.routingStrategies.RoundRobinRouting;
import jmt.gui.common.routingStrategies.RoutingStrategy;
import jmt.gui.common.routingStrategies.ShortestQueueLengthRouting;
import jmt.gui.common.routingStrategies.ShortestResponseTimeRouting;
import jmt.gui.common.semaphoreStrategies.NormalSemaphore;
import jmt.gui.common.semaphoreStrategies.SemaphoreStrategy;

/**
 * Created by IntelliJ IDEA.
 * User: OrsotronIII
 * Date: 17-mag-2005
 * Time: 11.15.51
 * Modified by Bertoli Marco
 *
 * Modified by Ashanka (May 2010):
 * Desc: Renamed the default of Queueing Station to Queue Stat.
 *       in the inner class : ConstMap function: fill
 *
 * Modified by Ashanka (Nov 2012)
 * Desc: Added new LoadDependent Routing
 *
 * Modified by Vitor S. Lopes (Jul 2016)
 * Desc: Added Semaphore
 *
 * Modified by Lulai Zhu (Jul 2016)
 * Desc: Added Place and Transition
 */
public interface CommonConstants {

	public final static int CLASS_TYPE_OPEN = 0;
	public final static int CLASS_TYPE_CLOSED = 1;

	/** Table row height */
	public final static int ROW_HEIGHT = 30;

	public final static String STATION_TYPE_SOURCE = "Source";
	public final static String STATION_TYPE_SINK = "Sink";
	public final static String STATION_TYPE_TERMINAL = "Terminal";
	public final static String STATION_TYPE_ROUTER = "RoutingStation";
	public final static String STATION_TYPE_DELAY = "Delay";
	public final static String STATION_TYPE_SERVER = "Server";
	public final static String STATION_TYPE_FORK = "Fork";
	public final static String STATION_TYPE_JOIN = "Join";
	public final static String STATION_TYPE_LOGGER = "Logger";
	public final static String STATION_TYPE_CLASSSWITCH = "ClassSwitch";
	public final static String STATION_TYPE_SEMAPHORE = "Semaphore";
	public final static String STATION_TYPE_SCALER = "Scaler";
	public final static String STATION_TYPE_PLACE = "Place";
	public final static String STATION_TYPE_TRANSITION = "Transition";

	//For BPMN
	//public final static String STATION_TYPE_INGATEWAY = "InGateWay";
	//public final static String STATION_TYPE_OUTGATEWAY = "OutGateWay";

	/** A map that decodes from station type to station name for the GUI */

	public static final ConstMap<String, String> STATION_NAMES = new ConstMap<String, String>() {
		@Override
		protected void fill() {
			putConst(STATION_TYPE_SOURCE, STATION_TYPE_SOURCE);
			putConst(STATION_TYPE_SINK, STATION_TYPE_SINK);
			putConst(STATION_TYPE_TERMINAL, STATION_TYPE_TERMINAL);
			putConst(STATION_TYPE_ROUTER, "Router");
			putConst(STATION_TYPE_DELAY, STATION_TYPE_DELAY);
			putConst(STATION_TYPE_SERVER, "Queue");
			putConst(STATION_TYPE_FORK, STATION_TYPE_FORK);
			putConst(STATION_TYPE_JOIN, STATION_TYPE_JOIN);
			putConst(STATION_TYPE_LOGGER, STATION_TYPE_LOGGER);
			putConst(STATION_TYPE_CLASSSWITCH, STATION_TYPE_CLASSSWITCH);
			putConst(STATION_TYPE_SEMAPHORE, STATION_TYPE_SEMAPHORE);
			putConst(STATION_TYPE_SCALER, STATION_TYPE_SCALER);
			putConst(STATION_TYPE_PLACE, STATION_TYPE_PLACE);
			putConst(STATION_TYPE_TRANSITION, STATION_TYPE_TRANSITION);

			//For BPMN
			//putConst(STATION_TYPE_INGATEWAY, STATION_TYPE_INGATEWAY);
			//putConst(STATION_TYPE_OUTGATEWAY, STATION_TYPE_OUTGATEWAY);
		}
	};

	/**Constants for GUI scaling*/
	public final static int MAX_GUI_WIDTH_STARTSCREEN =  800;
	public final static int MAX_GUI_HEIGHT_STARTSCREEN = 600;
	public final static int MAX_GUI_WIDTH_ABOUT =  800;
	public final static int MAX_GUI_HEIGHT_ABOUT = 600;
	public final static int MAX_GUI_WIDTH_QUICKHTML =  800;
	public final static int MAX_GUI_HEIGHT_QUICKHTML = 600;

	//public final static float MIN_GUI_DIALOG_RESIZE_RATIO = 0.8f;
	public final static int MAX_GUI_WIDTH_DIALOG_DEFAULT =  780;
	public final static int MAX_GUI_HEIGHT_DIALOG_DEFAULT = 520;
	public final static int MAX_GUI_WIDTH_COMMON =  1024;
	public final static int MAX_GUI_HEIGHT_COMMON = 768;

	public final static int MAX_GUI_WIDTH_JSIM_DISTRIB =  480;
	public final static int MAX_GUI_HEIGHT_JSIM_DISTRIB = 600;
	public final static int MAX_GUI_WIDTH_JSIM_PROBLEMS =  450;
	public final static int MAX_GUI_HEIGHT_JSIM_PROBLEMS = 435;
	public final static int MAX_GUI_WIDTH_JSIM_RESULTS =  1024;
	public final static int MAX_GUI_HEIGHT_JSIM_RESULTS = 768;
	public final static int MAX_GUI_WIDTH_JSIM_STAT_OUTPUT =  1152;
	public final static int MAX_GUI_HEIGHT_JSIM_STAT_OUTPUT = 864;

	public final static int MAX_GUI_WIDTH_JMVA =  1024;
	public final static int MAX_GUI_HEIGHT_JMVA = 768;

	public final static int MAX_GUI_WIDTH_JMCH =  800;
	public final static int MAX_GUI_HEIGHT_JMCH = 600;

	public final static int MAX_GUI_WIDTH_JABA =  1024;
	public final static int MAX_GUI_HEIGHT_JABA = 768;

	public final static int MAX_GUI_WIDTH_JWAT =  1024;
	public final static int MAX_GUI_HEIGHT_JWAT = 768;
	public final static int MAX_GUI_WIDTH_JWAT_STARTSCREEN =  520;
	public final static int MAX_GUI_HEIGHT_JWAT_STARTSCREEN = 400;
	public final static int MAX_GUI_WIDTH_JWAT_COLOR =  1024;
	public final static int MAX_GUI_HEIGHT_JWAT_COLOR = 768;

	public final static int MAX_GUI_WIDTH_LDSTRATEGY =  800;
	public final static int MAX_GUI_HEIGHT_LDSTRATEGY = 600;
	public final static int MAX_GUI_WIDTH_LDHELP =  600;
	public final static int MAX_GUI_HEIGHT_LDHELP = 400;
	public final static int MAX_GUI_WIDTH_LDROUTING =  800;
	public final static int MAX_GUI_HEIGHT_LDROUTING = 600;
	public final static int MAX_GUI_WIDTH_LDEDITING =  400;
	public final static int MAX_GUI_HEIGHT_LDEDITING = 400;

	public final static int MAX_GUI_WIDTH_WHATIF_PROGRESS =  640;
	public final static int MAX_GUI_HEIGHT_WHATIF_PROGRESS = 480;
	public final static int MAX_GUI_WIDTH_WHATIF_RESULTS =  1024;
	public final static int MAX_GUI_HEIGHT_WHATIF_RESULTS = 768;

	final static Dimension DIM_BUTTON_XS = new Dimension(60, 20);
	final static Dimension DIM_BUTTON_S = new Dimension(80, 20);
	final static Dimension DIM_BUTTON_M = new Dimension(110, 20);
	final static Dimension DIM_BUTTON_L = new Dimension(140, 20);

	/**Constants for selection of queueing strategy*/
	public final static String QUEUE_STRATEGY_FCFS = "FCFS";
	public final static String QUEUE_STRATEGY_LCFS = "LCFS";
	public final static String QUEUE_STRATEGY_RAND = "Random";
	public final static String QUEUE_STRATEGY_SJF = "SJF";
	public final static String QUEUE_STRATEGY_LJF = "LJF";
	public final static String QUEUE_STRATEGY_STATION_PS = "Processor Sharing";
	public final static String QUEUE_STRATEGY_STATION_SRPT = "Shortest Remaining Processing Time";
	public final static String QUEUE_STRATEGY_STATION_QUEUE = "Non-preemptive Scheduling";
	public final static String QUEUE_STRATEGY_STATION_QUEUE_PRIORITY = "Non-preemptive Scheduling (Priority)";

	/**Constants for service time distributions*/
	public static final String SERVICE_LOAD_INDEPENDENT = "Load Independent";
	public static final String SERVICE_LOAD_DEPENDENT = "Load Dependent";
	public static final String SERVICE_ZERO = "Zero Service Time";

	/**Constants for selection of distributions*/
	public final static String DISTRIBUTION_DETERMINISTIC = "Deterministic";
	public final static String DISTRIBUTION_EXPONENTIAL = "Exponential";
	public final static String DISTRIBUTION_NORMAL = "Normal";
	public final static String DISTRIBUTION_PARETO = "Pareto";
	public final static String DISTRIBUTION_ERLANG = "Erlang";
	public final static String DISTRIBUTION_HYPEREXPONENTIAL = "Hyperexponential";
	public final static String DISTRIBUTION_BURST = "Burst";
	//public final static String DISTRIBUTION_MAP= "MAP";
	public final static String DISTRIBUTION_MMPP2 = "MMPP2";
	public final static String DISTRIBUTION_GAMMA = "Gamma";
	public final static String DISTRIBUTION_UNIFORM = "Uniform";
	//public final static String DISTRIBUTION_STUDENTT = "StudentT";
	//public final static String DISTRIBUTION_POISSON = "Poisson";
	public final static String DISTRIBUTION_REPLAYER = "Replayer";

	/**Constants for selection of timing strategy*/
	public final static String TIMING_STRATEGY_TIMED = "Timed";
	public final static String TIMING_STRATEGY_IMMEDIATE = "Immediate";

	/**Constants for selection of region group strategy*/
	public final static String REGION_GROUP_STRATEGY_FCFS = "FCFS";
	public final static String REGION_GROUP_STRATEGY_HPS = "HPS";
	public final static String REGION_GROUP_STRATEGY_HPS_LCFS = "HPS_LCFS";
	public final static String REGION_GROUP_STRATEGY_HPS_FCFS = "HPS_FCFS";

	public final static RoutingStrategy ROUTING_RANDOM = new RandomRouting();
	public final static RoutingStrategy ROUTING_ROUNDROBIN = new RoundRobinRouting();
	public final static RoutingStrategy ROUTING_PROBABILITIES = new ProbabilityRouting();
	public final static RoutingStrategy ROUTING_SHORTESTQL = new ShortestQueueLengthRouting();
	public final static RoutingStrategy ROUTING_SHORTESTRT = new ShortestResponseTimeRouting();
	public final static RoutingStrategy ROUTING_LEASTUTILIZATION = new LeastUtilizationRouting();
	public final static RoutingStrategy ROUTING_FASTESTSERVICE = new FastestServiceRouting();
	public final static RoutingStrategy ROUTING_LOADDEPENDENT = new LoadDependentRouting();

	public final static ForkStrategy FORKING_PROBABILITIES = new ProbabilitiesFork();
	public final static ForkStrategy FORKING_COMBINATION = new CombFork();
	public final static ForkStrategy FORKING_CLASS_SWITCH = new ClassSwitchFork();
	public final static ForkStrategy FORKING_MULTI_BRANCH_CLASS_SWITCH = new MultiBranchClassSwitchFork();

	public final static JoinStrategy JOINING_NORMAL = new NormalJoin();
	public final static JoinStrategy JOINING_PARTIAL = new PartialJoin();
	public final static JoinStrategy JOINING_MIX = new GuardJoin();

	public final static SemaphoreStrategy SEMAPHORE_NORMAL = new NormalSemaphore();

	/**HTML formats for panels descriptions*/
	final static String HTML_START = "<html><body align=\"left\">";
	final static String HTML_END = "</body></html>";
	final static String HTML_FONT_TITLE = "<font size=\"4\"><b>";
	final static String HTML_FONT_NORM = "<font size=\"3\">";
	final static String HTML_FONT_TIT_END = "</b></font><br>";
	final static String HTML_FONT_NOR_END = "</font>";

	public static final String FINITE_DROP = "Drop";
	public static final String FINITE_BLOCK = "BAS blocking";
	public static final String FINITE_WAITING = "Waiting Queue (no drop)";

	public final static String CLASSES_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Classes Characteristics" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define type (Open or Closed), name and parameters for each customer " + "class."
			+ "<br/>" + "<b>Closed Classes:</b> If a ClassSwitch is in the model, then <b>all</b> classes must have the <b>same</b> Reference Station."
			+ "<br/>" + "<b>Open Classes:</b> A class that has ClassSwitch as Reference Station cannot be generated by a Source Station."
			+ "<br/>" + "<b>Priorities:</b> A larger value implies a higher priority." + HTML_FONT_NOR_END + HTML_END;

	public final static String STATIONS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Characteristics" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define type and name for each station" + HTML_FONT_NOR_END + HTML_END;

	public final static String CONNECTIONS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Connections" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Click on table entry (i,j) to connect station i to station j." + HTML_FONT_NOR_END + HTML_END;

	public final static String STATIONS_PAR_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Parameters" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "For each station in the list, define the requested parameters" + HTML_FONT_NOR_END + HTML_END;

	public final static String MEASURES_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Performance Indices" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define system performance indices to be collected and " + "plotted by the simulation engine." + HTML_FONT_NOR_END + HTML_END;

	public final static String SIMULATION_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Simulation Parameters" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define simulation parameters and initial customer locations." + HTML_FONT_NOR_END + HTML_END;

	public final static String BATCH_SIMULATION_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "What-if analysis" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Enable parametric analysis and customize it." + HTML_FONT_NOR_END + HTML_END;

	public final static String REFSOURCE_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Reference Station" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define reference station for each closed class. This is used " + "to calculate system throughput for that class." + HTML_FONT_NOR_END
			+ HTML_END;

	public final static String LDSERVICE_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Load Dependent Service Time Distribution" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define the distribution and the values of service times for a range of number of jobs inside "
			+ "the station. Mean value of the distribution can be specified with an arithmetic "
			+ "expression, as a function of the current value of 'n' (see Help for operators)." + HTML_FONT_NOR_END + HTML_END;

	public final static String PARAMETRIC_ANALYSIS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "What-if analysis" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define the type of what-if analysis to be performed and modify parameter options." + HTML_FONT_NOR_END + HTML_END;

	public final static String BLOCKING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Finite Capacity Region Characteristics" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define number, name, composition, global and class specific constraints for finite capacity regions."
			+ HTML_FONT_NOR_END + HTML_END;

	public final static String CONVERSION_WARNING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Conversion performed" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Input model was automatically converted from <b>%PAR1%</b> to <b>%PAR2%</b>. "
			+ "The conversion was completed with the warnings shown below" + HTML_FONT_NOR_END + HTML_END;

	public final static String LDROUTING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Description" + HTML_FONT_TIT_END +
			HTML_FONT_NORM + "Customers of each class are routed depending on their number on the current station. " +
			"The outgoing paths must have associated probabilities that should sum to 1 for each range."
			+ HTML_FONT_NOR_END + HTML_END;

	public final static String MEASURE_LOG_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Statistical Results CSV file" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Check the 'Stat.Res.' button to collect samples in a CSV file for additional statistical analysis. This option may produce a file with a large size." + HTML_FONT_NOR_END + HTML_END;

	// String tokens
	public static final String PAR1 = "%PAR1%";
	public static final String PAR2 = "%PAR2%";

	//ARIF: number of maximum job can be 64 for JSIMgraph
	public static final int MAX_NUMBER_OF_CLASSES = 64;
	public static final int MAX_NUMBER_OF_STATIONS = 1000;
	public static final int MAX_NUMBER_OF_REGIONS = 1000;
	public static final int MAX_NUMBER_OF_GROUPS = 64;

	public static final String ALL_CLASSES = "--- All Classes ---";
	public static final String INFINITE_CAPACITY = "Infinite Capacity";

	// Warnings for missing resources
	public static final String WARNING_CLASS_STATION = "User classes and stations have to be defined first";
	public static final String WARNING_CLASS = "User classes have to be defined first";
	public static final String WARNING_STATION = "Stations have to be defined first";
	public static final String WARNING_INCOMING_ROUTING = "Station incoming connections undefined";
	public static final String WARNING_OUTGOING_ROUTING = "Station outgoing connections undefined";
	public static final String WARNING_CLASS_INCOMING_ROUTING = "User classes and station incoming connections have to be defined first";
	public static final String WARNING_CLASS_OUTGOING_ROUTING = "User classes and station outgoing connections have to be defined first";

	// Application names (used for input file format conversion messages)
	public static final String JMVA = "JMVA";
	public static final String JSIM = "JSIM";
	public static final String JABA = "JABA";
	public static final String JWAT = "JWAT";
	public static final String SIMENGINE = "Simulation Engine XML data";

}
