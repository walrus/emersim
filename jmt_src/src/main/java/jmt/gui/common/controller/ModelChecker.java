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
package jmt.gui.common.controller;

import java.util.*;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.BlockingRegionDefinition;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.definitions.parametric.ParametricAnalysisChecker;
import jmt.gui.common.definitions.parametric.ParametricAnalysisDefinition;
import jmt.gui.common.distributions.Distribution;
import jmt.gui.common.joinStrategies.GuardJoin;
import jmt.gui.common.routingStrategies.LoadDependentRouting;
import jmt.gui.common.routingStrategies.ProbabilityRouting;
import jmt.gui.common.routingStrategies.RandomRouting;
import jmt.gui.common.routingStrategies.RoutingStrategy;
import jmt.gui.common.serviceStrategies.LDStrategy;

/**
 * <p>
 * Title: ModelChecker</p>
 * <p>
 * Description: Checks the model correctness</p>
 *
 * @author Francesco D'Aquino Date: 6-ott-2005 Time: 16.12.50
 *
 * @author Bertoli Marco (new errors)
 *
 * Modified by Ashanka (May 2010): Patch: Multi-Sink Perf. Index Description:
 * Added new Performance index for the capturing the 1. global response time
 * (ResponseTime per Sink) 2. global throughput (Throughput per Sink) each sink
 * per class. Hence new validations are required to check the Performance
 * Indices of response per sink and throughput per sink follow the model
 * validations. 1. Response Time per Sink and Throughput per Sink should have a
 * sink in the model. 2. Response Time per Sink and Throughput per Sink should
 * not be selected with a closed class as for a closed model till now in JMT no
 * jobs are routed to the sink and apparently sink is allowed to choose only
 * when a open class is present.
 *
 * Modified by Ashanka (June 2010): Added Sink Probability for Closed class
 * update to 0.0 warning message.
 */
public class ModelChecker implements CommonConstants {

	private ClassDefinition class_def;
	private StationDefinition station_def;
	private SimulationDefinition simulation_def;
	private BlockingRegionDefinition blocking_def;

	//sets the behavior of the checker
	private boolean isToJMVAChecker;

	//Variable that contains information about problems
	private boolean[] errors;
	//Variable that contains information about warnings
	private boolean[] warnings;
	//Vector containing the keys of the classes without a reference station
	private Vector<Object> classesWithoutRefStation;
	//Vector containing only the keys of the open classes without a reference station
	private Vector<Object> openClassesWithoutRefStation;
	//Vector containing the keys of the sources without a class associated to
	private Vector<Object> sourceWithoutClasses;
	//Vector containing the keys of stations with link problems
	private Vector<Object> stationsWithLinkErrors;
	// HashMap containing the vector of keys of the stations where
	// routing problems for this close class occur. The key of the
	// Hash Map is the key of the closed class considered
	private HashMap<Object, Vector<Object>> routingErrors;
	// HashMap containing the vector of keys of the stations reachable by a close class
	// The key of the Hash Map is the key of the closed class considered
	private HashMap<Object, Vector<Object>> allForwardStationsAreSinkErrors;
	//Vector containing the keys of measures defined more than once
	private Vector<Object> redundantMeasure;
	//Vector containing the inconsistent performance indices
	private Vector<Object> inconsistentMeasures;
	//Vector containing the keys of servers with per class different
	//queue strategies
	private Vector<Object> BCMPserversWithDifferentQueueStrategy;
	//Vector containing the keys of servers with FCFS queueing strategy
	//but mixed types of service
	private Vector<Object> BCMPserversWithDifferentServiceTypes;
	//Vector containing the keys of servers with FCFS queue strategy
	//but a non exponential distribution
	private Vector<Object> BCMPserversFCFSWithoutExponential;
	//Vector containing the keys of servers with FCFS, the same
	//type of service for each class, but with different mean service
	//times
	private Vector<Object> BCMPFcfsServersWithDifferentServiceTimes;
	//Vector containing the keys of delays with at least a service time
	//distribution with a non rational Laplace transform
	private Vector<Object> BCMPdelaysWithNonRationalServiceDistribution;
	//Vector containing the keys of the stations where a non random routing is used.
	//Used only in JMVA conversion
	private Vector<Object> BCMPserverWithNonSymmetricSchedulingPolicy;
	//Vector containing the keys of the stations where a non BCMP scheduling policy is used.
	//Used only in JMVA conversion
	private Vector<Object> BCMPnonStateIndependentRoutingStations;
	//Vector containing the stations (not Sinks or Sources) not backward connected. Used for
	// warnings
	private Vector<Object> stationWithoutBackwardLinks;
	/**
	 * Empty blocking regions
	 */
	private Vector<Object> emptyBlockingRegions;
	//Vector containing the keys of cs with wrong matrix(sum by rows less than 1)
	private Vector<Object> csWithWrongMatrix;
	//Vector containing the keys of cs followed by queue with drop bas strategy
	private Vector<Object> csFollowedByBas;
	//Vector containing the keys of cs between fork/join
	private Vector<Object> csBetweenForkJoin;
	//Vector containing the keys of classes which is different then the first closed type class if class_switch exists
	//private Vector<Object> csReferenceStation;
	//Vector containing the classes of the system throughput
	private Vector<Object> csSystemThroughputClass;
	//Vector containing the closed classes of zero population
	private Vector<Object> closedZeroPopulation;
	//List of all FiringMixJoin station and keys with zero tasks
	private HashMap<Object, List<Object>> GuardStrategyZeroTasks;

	private Map<Object, HashMap<Object, Object>> invalidLoadDependentRouting;

	//Vector containing the keys of semaphores not between fork/join
	private Vector<Object> semaphoreNotBetweenForkJoin;
	//Vector containing the keys of places between fork/join
	private Vector<Object> placeBetweenForkJoin;
	//Vector containing the keys of transitions between fork/join
	private Vector<Object> transitionBetweenForkJoin;
	//Vector containing the keys of transitions without valid enabling or inhibiting conditions for all classes
	private Vector<Object> transitionWithoutValidEnablingOrInhibitingCondition;
	//Vector containing the keys of transitions without valid firing outcomes for all classes
	private Vector<Object> transitionWithoutValidFiringOutcome;
	//Vector containing the keys of transitions with invalid enabling or inhibiting conditions for some classes
	private Vector<Object> transitionWithInvalidEnablingOrInhibitingCondition;
	//Vector containing the keys of transitions with invalid firing outcomes for some classes
	private Vector<Object> transitionWithInvalidFiringOutcome;

	//Vector containing the keys of the stations with a queue strategy different from FCFS.
	//Used only in JMVA conversion
	//private Vector nonFCFSStations;
	//constant used to define an error
	public static final int ERROR_PROBLEM = 0;
	//constant used to define a warning
	public static final int WARNING_PROBLEM = 1;

	//it occurs when no classes have been defined
	public static final int NO_CLASSES_ERROR = 0;
	//it occurs when no station have been defined
	public static final int NO_STATION_ERROR = 1;
	//it occurs when no measure have been defined
	public static final int SIMULATION_ERROR = 2;
	//it occurs when there is a sink but no open classes have been defined
	public static final int SINK_BUT_NO_OPEN_CLASSES_ERROR = 3;
	//it occurs when there is an open class but no sources have been defined
	public static final int OPEN_CLASS_BUT_NO_SOURCE_ERROR = 4;
	//it occurs when no open classes have been associated to a source
	public static final int SOURCE_WITH_NO_OPEN_CLASSES_ERROR = 5;
	//it occurs when a class has not a reference station
	public static final int REFERENCE_STATION_ERROR = 6;
	//it occurs when there is an open class but no sinks have been defined
	public static final int NO_SINK_WITH_OPEN_CLASSES_ERROR = 7;
	//it occurs when a station is not properly linked
	public static final int STATION_LINK_ERROR = 8;
	//it occurs when a close class is routed with probability 1 into a sink
	public static final int ROUTING_ERROR = 9;
	//it occurs when a close class is routed into a station whose forward stations are all sinks
	public static final int ALL_FORWARD_STATION_ARE_SINK_ERROR = 10;
	//it occurs if the same measure is defined more than once
	public static final int DUPLICATE_MEASURE_ERROR = 11;
	//it occurs if one or performance index has one or more null field
	public static final int INCONSISTENT_MEASURE_ERROR = 12;
	//Thrown only when trying to switch from JSim or JModel to JMVA, if there is at least
	//one open class without a reference station
	//public static final int OPEN_CLASS_REFERENCE_STATION_ERROR = 13;
	//it occurs when there is at least one join but not a fork;
	public static final int JOIN_WITHOUT_FORK_ERROR = 13;
	//All jobs split by a fork must be routed to the same sink, or
	//to the same join
	public static final int FORK_JOIN_ROUTING_ERROR = 14;
	/**
	 * Empty blocking region
	 */
	public static final int EMPTY_BLOCKING_REGION = 15;
	/**
	 * Preloaded station in a blocking region
	 */
	public static final int PRELOADING_WITH_BLOCKING = 16;

	//Checks if XperSink and RperSink is selected with any sink in the model.
	public static final int SINK_PERF_IND_WITH_NO_SINK_ERROR = 17;
	//Checks if XperSink and RperSink is selected with closed classes.
	public static final int SINK_PERF_WITH_CLOSED_CLASS_ERROR = 18;
	//Checks if exist some rows of a cs matrix that sum to a number less than one.
	public static final int CS_MATRIX_HAS_ROWS_LESS_THAN_ONE = 19;
	//Checks if exist some cs followed by a queue with bas drop strategy.
	public static final int CS_FOLLOWED_BY_A_BAS = 20;
	//Checks if exist some cs between a fork and a join.
	public static final int CS_BETWEEN_FORK_JOIN = 21;

	public static final int LOAD_DEPENDENT_ROUTING_INVALID = 22;
	//Checks the different reference station of closed class if there exists class_switch.
	public static final int CS_REFERENCE_STATION = 23;
	//Checks existence of the global throughput per class with class switch 
	public static final int CS_SYSTEM_THROUGHPUT_CLASS = 24;
	//Checks if all the closed classes population is zero with class switch 
	public static final int CS_ALL_CLOSED_ZERO_POPULATION = 25;

	//Check if FiringForkStrategy has 0 tasks
	public static final int GUARD_STRATEGY_ZERO_TASKS = 26;
	//Check if FiringForkStrategy has 0 tasks
	public static final int GUARD_STRATEGY_CLOSED_ZERO_POPULATION = 27;

	//Checks if there are any semaphores not between a fork and a join
	public static final int SEMAPHORE_NOT_BETWEEN_FORK_JOIN = 28;
	//Checks if there are any places between a fork and a join.
	public static final int PLACE_BETWEEN_FORK_JOIN = 29;
	//Checks if there are any transitions between a fork and a join.
	public static final int TRANSITION_BETWEEN_FORK_JOIN = 30;
	//Checks if any transitions have no valid enabling conditions for all classes.
	public static final int TRANSITION_WITHOUT_VALID_ENABLING_OR_INHIBITING_CONDITION = 31;
	//Checks if any transitions have no valid firing outcomes for all classes.
	public static final int TRANSITION_WITHOUT_VALID_FIRING_OUTCOME = 32;

	//it occurs when more than one sink have been defined
	public static final int MORE_THAN_ONE_SINK_WARNING = 0;
	//it occurs when a station (not sink or source) is not backward linked
	public static final int NO_BACKWARD_LINK_WARNING = 1;
	//it occurs if there is a fork but not a join
	public static final int FORK_WITHOUT_JOIN_WARNING = 2;
	//Thrown if the parametric analysis model has become inconsistent
	//with the model definition. This warning advises user to recheck the
	//parametric model definition, since it had been modified to make it
	//consistent back again.
	public static final int PARAMETRIC_ANALYSIS_MODEL_MODIFIED_WARNING = 3;
	//Thrown if a no PA are available, but a PA model was previously defined
	public static final int PARAMETRIC_ANALYSIS_NO_MORE_AVAILABLE_WARNING = 4;
	//Thrown if the station has different per class queueing
	//strategy. (only when trying to convert to JMVA)
	public static final int BCMP_DIFFERENT_QUEUEING_STRATEGIES_WARNING = 5;
	//Thrown if the service policy inside a station with FCFS
	//queueing strategy, differs from class to class. In fact,
	//according to the BCMP theorem hypothesis, the service times
	//in an FCFS station must be all load independent or independent,
	//mixtures are not allowed
	public static final int BCMP_FCFS_DIFFERENT_SERVICE_TYPES_WARNING = 6;
	//Thrown if the station has a FCFS queueing strategy, but
	//the service time distribution is not exponential
	public static final int BCMP_FCFS_EXPONENTIAL_WARNING = 7;
	//Thrown if the station has a FCFS queueing strategy, but
	//the per class mean values of service time differ
	// (only when trying to convert to JMVA)
	public static final int BCMP_FCFS_DIFFERENT_SERVICE_TIMES_WARNING = 8;
	//Thrown if the station has a Processor Sharing service strategy
	//but it is not possible to define a Laplace rational transform
	//for the service time distribution
	// (only when trying to convert to JMVA)
	public static final int BCMP_PROCESSOR_SHARING_WARNING = 9;
	//Thrown if the station has a delay behavior but it is not
	//possible to define a Laplace rational transform for a
	//service time distribution
	// (only when trying to convert to JMVA)
	public static final int BCMP_DELAY_WARNING = 10;
	//Thrown if the station has a LCFS - Preemptive Resume
	//queueing strategy but it is not possible to define a
	//Laplace rational transform for the service time distribution
	// (only when trying to convert to JMVA)
	public static final int BCMP_LCFS_PR_WARNING = 11;
	//Thrown if the routing strategy is state dependent
	// (only when trying to convert to JMVA)
	public static final int BCMP_NON_STATE_INDEPENDENT_ROUTING_WARNING = 12;

	public static final int SINK_PROBABILITY_UPDATE_WARNING = 13;

	//Checks if the closed classes population is zero. 
	public static final int CLOSED_ZERO_POPULATION = 14;

	//Thrown if the scheduling policies other than PS, FCFS, or RAND
	//are used
	// (only when trying to convert to JMVA)
	public static final int BCMP_NON_SYMMETRIC_SCHEDULING = 15;

	//Checks if CS with FCR OR FORK-JOIN
	public static final int CS_WITH_FORK_JOIN = 16;

	//Throws a warning if FCFS Priorities is used but all classes have the same priorities
	public static final int SLOW_FCFS_SAME_PRIORITIES = 17;

	//Checks if any transitions have invalid enabling or inhibiting conditions for some classes.
	public static final int TRANSITION_WITH_INVALID_ENABLING_OR_INHIBITING_CONDITION = 18;
	//Checks if any transitions have invalid firing outcomes for some classes.
	public static final int TRANSITION_WITH_INVALID_FIRING_OUTCOME = 19;

	/*
	//Thrown only when trying to switch from JSim or JModel to JMVA, if there is at least
	//one non exponential distribution
	public static int NO_EXP_FOUND_WARNING = 2;
	//it occurs only when trying to convert to JMVA
	public static int DIFFERENT_SERVICE_TIME_WARNING = 3;
	//it occurs only when trying to convert to JMVA
	public static int NON_FCFS_WARNING = 4;
	//it it occurs only when trying to convert to JMVA
	public static int NON_STATE_INDEPENDENT_ROUTING_WARNING = 5;
	 */
	private static final int NUMBER_OF_ERROR_TYPES = 33;
	private static final int NUMBER_OF_NORMAL_ERROR_TYPES = 33;
	private static final int NUMBER_OF_WARNING_TYPES = 20;
	private static final int NUMBER_OF_NORMAL_WARNING_TYPES = 5;

	/**
	 * Creates a new modelchecker
	 *
	 * @param class_def reference to class definition data structure
	 * @param station_def reference to station definition data structure
	 * @param simulation_def reference to simulation definition data structure
	 * @param bd reference to blocking region definition data structure
	 * @param isToJMVA true if model must be converted to jmva, false otherwise
	 */
	public ModelChecker(ClassDefinition class_def, StationDefinition station_def, SimulationDefinition simulation_def, BlockingRegionDefinition bd,
			boolean isToJMVA) {
		this.class_def = class_def;
		this.station_def = station_def;
		this.simulation_def = simulation_def;
		this.blocking_def = bd;
		errors = new boolean[NUMBER_OF_ERROR_TYPES];
		warnings = new boolean[NUMBER_OF_WARNING_TYPES];
		classesWithoutRefStation = new Vector<Object>(0, 1);
		openClassesWithoutRefStation = new Vector<Object>(0, 1);
		sourceWithoutClasses = new Vector<Object>(0, 1);
		stationsWithLinkErrors = new Vector<Object>(0, 1);
		routingErrors = new HashMap<Object, Vector<Object>>(0, 1);
		allForwardStationsAreSinkErrors = new HashMap<Object, Vector<Object>>(0, 1);
		stationWithoutBackwardLinks = new Vector<Object>(0, 1);
		redundantMeasure = new Vector<Object>(0, 1);
		inconsistentMeasures = new Vector<Object>(0, 1);
		BCMPserversWithDifferentQueueStrategy = new Vector<Object>(0, 1);
		BCMPserversWithDifferentServiceTypes = new Vector<Object>(0, 1);
		BCMPserversFCFSWithoutExponential = new Vector<Object>(0, 1);
		BCMPFcfsServersWithDifferentServiceTimes = new Vector<Object>(0, 1);
		BCMPdelaysWithNonRationalServiceDistribution = new Vector<Object>(0, 1);
		BCMPserverWithNonSymmetricSchedulingPolicy = new Vector<Object>(0, 1);
		invalidLoadDependentRouting = new HashMap<Object, HashMap<Object, Object>>(0);
		csWithWrongMatrix = new Vector<Object>(0, 1);
		csFollowedByBas = new Vector<Object>(0, 1);
		csBetweenForkJoin = new Vector<Object>(0, 1);
		//csReferenceStation = new Vector<Object>(0, 1);
		csSystemThroughputClass = new Vector<Object>(0, 1);
		closedZeroPopulation = new Vector<Object>(0, 1);
		GuardStrategyZeroTasks = new HashMap<Object, List<Object>>(0, 1);
		//nonFCFSStations = new Vector(0,1);
		BCMPnonStateIndependentRoutingStations = new Vector<Object>(0, 1);
		emptyBlockingRegions = new Vector<Object>(0, 1);
		semaphoreNotBetweenForkJoin = new Vector<Object>(0, 1);
		placeBetweenForkJoin = new Vector<Object>(0, 1);
		transitionBetweenForkJoin = new Vector<Object>(0, 1);
		transitionWithoutValidEnablingOrInhibitingCondition = new Vector<Object>(0, 1);
		transitionWithoutValidFiringOutcome = new Vector<Object>(0, 1);
		transitionWithInvalidEnablingOrInhibitingCondition = new Vector<Object>(0, 1);
		transitionWithInvalidFiringOutcome = new Vector<Object>(0, 1);
		isToJMVAChecker = isToJMVA;
		checkModel();
	}

	//resets all the variables of the model checker
	private void reset() {
		openClassesWithoutRefStation.removeAllElements();
		classesWithoutRefStation.removeAllElements();
		sourceWithoutClasses.removeAllElements();
		stationsWithLinkErrors.removeAllElements();
		routingErrors.clear();
		allForwardStationsAreSinkErrors.clear();
		stationWithoutBackwardLinks.removeAllElements();
		redundantMeasure.removeAllElements();
		inconsistentMeasures.clear();
		//nonFCFSStations.removeAllElements();
		BCMPserversWithDifferentQueueStrategy.removeAllElements();
		BCMPserversWithDifferentServiceTypes.removeAllElements();
		BCMPserversFCFSWithoutExponential.removeAllElements();
		BCMPFcfsServersWithDifferentServiceTimes.removeAllElements();
		BCMPdelaysWithNonRationalServiceDistribution.removeAllElements();
		BCMPnonStateIndependentRoutingStations.removeAllElements();
		BCMPserverWithNonSymmetricSchedulingPolicy.removeAllElements();
		csWithWrongMatrix.removeAllElements();
		csFollowedByBas.removeAllElements();
		csBetweenForkJoin.removeAllElements();
		//csReferenceStation.removeAllElements();
		csSystemThroughputClass.removeAllElements();
		closedZeroPopulation.removeAllElements();
		GuardStrategyZeroTasks.clear();
		emptyBlockingRegions.removeAllElements();
		invalidLoadDependentRouting.clear();
		semaphoreNotBetweenForkJoin.removeAllElements();
		placeBetweenForkJoin.removeAllElements();
		transitionBetweenForkJoin.removeAllElements();
		transitionWithoutValidEnablingOrInhibitingCondition.removeAllElements();
		transitionWithoutValidFiringOutcome.removeAllElements();
		transitionWithInvalidEnablingOrInhibitingCondition.removeAllElements();
		transitionWithInvalidFiringOutcome.removeAllElements();
		for (int i = 0; i < NUMBER_OF_ERROR_TYPES; i++) {
			errors[i] = false;
		}
		for (int i = 0; i < NUMBER_OF_WARNING_TYPES; i++) {
			warnings[i] = false;
		}
	}

	//checks for problems of the model
	public void checkModel() {
		reset();

		if (isToJMVAChecker) {
			checkForNoClassError();
			checkForNoStationError();
			checkForOpenClassButNoSourceError();
			checkForRefStationError();
			//checkForOpenClassReferenceStationError();
			//checkForNoExpFoundWarning();
			//checkForDifferentServiceTimesWarnings();
			//checkForNonFCFSWarning();
			checkForBCMPDifferentQueueingStrategyWarning();
			checkForBCMPFcfsDifferentServiceTypesWarning();
			checkForBCMPFcfsNonExponentialWarning();
			checkForBCMPFcfsDifferentServiceTimesWarning();
			checkForBCMPProcessorSharingWarning();
			checkForBCMPDelayWarning();
			checkForBCMPLcfsPrWarning();
			checkForBCMPNonStateIndependentRoutingWarning();
			checkForBCMPNonSymmetricSchedulingPolicies();
		} else {
			station_def.manageProbabilities();
			checkForNoClassError();
			checkForNoStationError();
			checkForRefStationError();
			checkForOpenClassButNoSourceError();
			checkForSourcesWithNoClassesError();
			checkForNoSinkWithOpenClassError();
			checkForSinkButNoOpenClassError();
			checkForStationLinkError();
			checkForRoutingError();
			checkForAllForwardStationsAreSinkError();
			checkForSimulationError();
			checkForMeasureError();
			checkForInconsistentMeasureError();
			checkForLoadDependentRoutingError();
			//checkForMoreThanOneSinkWarning();
			checkForNoBacwardLinkWarning();
			checkForParametricAnalysisModelModifiedWarning();
			checkForParametricAnalysisNoMoreAvailableWarning();
			checkForJoinWithoutForkErrors();
			checkForForkWithoutJoinWarnings();
			checkForEmptyBlockingRegions();
			checkForPreloadingInBlockingRegions();
			checkForSinkPerfIndicesWithNoSink();
			checkForSinkPerfIndicesWithClosedClass();
			checkForSinkProbabilityUpdateWarning();
			checkForCsMatrixProbabilities();
			checkForCsFollowedByBAS();
			checkForCsBetweenForkJoin();
			checkCsReferenceStation();
			checkCsSystemThroughputClass();
			checkZeroPopulation();
			checkForkOrFcr();
			checkGuardStrategyZeroTasks();
			checkSlowFCFSPriorities();
			checkForSemaphoreNotBetweenForkJoin();
			checkForPlaceBetweenForkJoin();
			checkForTransitionBetweenForkJoin();
			checkForTransitionWithInvalidEnablingOrInhibitingCondition();
			checkForTransitionWithInvalidFiringOutcome();
		}
	}

	public StationDefinition getStationModel() {
		return station_def;
	}

	public ClassDefinition getClassModel() {
		return class_def;
	}

	public BlockingRegionDefinition getBlockingModel() {
		return blocking_def;
	}

	public boolean isToJMVA() {
		return isToJMVAChecker;
	}

	public boolean isEverythingOkNormal() {
		boolean ok = false;
		if (isErrorFreeNormal() && isWarningFreeNormal()) {
			ok = true;
		}
		return ok;
	}

	public boolean isEverythingOkToJMVA() {
		boolean ok = false;
		if (isErrorFreeToJMVA() && isWarningFreeToJMVA()) {
			ok = true;
		}
		return ok;
	}

	public boolean isErrorFreeNormal() {
		boolean ok = true;
		for (int i = 0; i < NUMBER_OF_NORMAL_ERROR_TYPES; i++) {
			if (errors[i]) {
				ok = false;
			}
		}
		return ok;
	}

	public boolean isWarningFreeNormal() {
		boolean ok = true;
		for (int i = 0; i < NUMBER_OF_NORMAL_WARNING_TYPES; i++) {
			if (warnings[i]) {
				ok = false;
			}
		}
		//Added this separately otherwise I had to redo the numbering
		if (warnings[SINK_PROBABILITY_UPDATE_WARNING]) {
			ok = false;
		}
		if (warnings[CLOSED_ZERO_POPULATION]) {
			ok = false;
		}
		if (warnings[CS_WITH_FORK_JOIN]) {
			ok = false;
		}
		if (warnings[SLOW_FCFS_SAME_PRIORITIES]) {
			ok = false;
		}
		if (warnings[TRANSITION_WITH_INVALID_ENABLING_OR_INHIBITING_CONDITION]) {
			ok = false;
		}
		if (warnings[TRANSITION_WITH_INVALID_FIRING_OUTCOME]) {
			ok = false;
		}
		return ok;
	}

	public boolean isErrorFreeToJMVA() {
		boolean ok = true;
		if ((errors[NO_CLASSES_ERROR]) || (errors[NO_STATION_ERROR]) || (errors[OPEN_CLASS_BUT_NO_SOURCE_ERROR]) || (errors[REFERENCE_STATION_ERROR])) {
			ok = false;
		}
		return ok;
	}

	public boolean isWarningFreeToJMVA() {
		boolean ok = true;
		if ((warnings[BCMP_DIFFERENT_QUEUEING_STRATEGIES_WARNING]) || (warnings[BCMP_FCFS_DIFFERENT_SERVICE_TYPES_WARNING])
				|| (warnings[BCMP_FCFS_EXPONENTIAL_WARNING]) || (warnings[BCMP_NON_SYMMETRIC_SCHEDULING]) || (warnings[BCMP_FCFS_DIFFERENT_SERVICE_TIMES_WARNING])
				|| (warnings[BCMP_PROCESSOR_SHARING_WARNING]) || (warnings[BCMP_DELAY_WARNING]) || (warnings[BCMP_LCFS_PR_WARNING])
				|| (warnings[BCMP_NON_STATE_INDEPENDENT_ROUTING_WARNING])) {
			ok = false;
		}
		return ok;
	}

	/**
	 * Returns true if no classes have been defined.
	 *
	 * @return false, if there is at least one class
	 * <br>true, if no classes have been defined
	 */
	public boolean isThereNoClassesError() {
		return errors[NO_CLASSES_ERROR];
	}

	/**
	 * Returns true if no stations have been defined.
	 *
	 * @return false, if there is at least one station
	 */
	public boolean isThereNoStationError() {
		return errors[NO_STATION_ERROR];
	}

	/**
	 * Returns true if a reference station for each class have been defined.
	 *
	 * @return false, if each class has a reference station
	 */
	public boolean isThereClassesWithoutRefStationError() {
		return errors[REFERENCE_STATION_ERROR];
	}

	/**
	 * Returns true if each source has an open class associated to.
	 *
	 * @return false, if source has an open class associated to.
	 */
	public boolean isThereSourceWithNoClassesError() {
		return errors[SOURCE_WITH_NO_OPEN_CLASSES_ERROR];
	}

	/**
	 * Returns true if there is at least one open class but no sinks have been
	 * defined
	 *
	 * @return true if the problem occurs.
	 */
	public boolean isThereNoSinkWithOpenClassesError() {
		return errors[NO_SINK_WITH_OPEN_CLASSES_ERROR];
	}

	/**
	 * Returns true if there is at least a sink but no open classes have been
	 * defined
	 *
	 * @return true if the problem occurs
	 */
	public boolean isThereSinkButNoOpenClassError() {
		return errors[SINK_BUT_NO_OPEN_CLASSES_ERROR];
	}

	/**
	 * Returns true if there is at least one open class but no source has been
	 * defined
	 *
	 * @return true if the problem occurs
	 */
	public boolean isThereOpenClassButNoSourceError() {
		return errors[OPEN_CLASS_BUT_NO_SOURCE_ERROR];
	}

	/**
	 * Returns true if there is at least one station link error
	 *
	 * @return true if the problem occurs.
	 */
	public boolean isThereStationLinkError() {
		return errors[STATION_LINK_ERROR];
	}

	/**
	 * Returns true if there is at least one closed class routed into a station
	 * whose forward stations are all sink
	 *
	 * @return true if at least one routing problem was found
	 */
	public boolean isThereAllForwardStationsAreSinkErrors() {
		return errors[ALL_FORWARD_STATION_ARE_SINK_ERROR];
	}

	/**
	 * Returns true if there is at least one routing error (a closed class
	 * routed into a sink)
	 *
	 * @return true if at least one routing problem was found
	 */
	public boolean isThereRoutingError() {
		return errors[ROUTING_ERROR];
	}

	/**
	 * Returns true if no measures have been defined
	 *
	 * @return true if the problem occurs
	 */
	public boolean isThereSimulationError() {
		return errors[SIMULATION_ERROR];
	}

	/**
	 * Returns true if the same measure is defined more than one time
	 *
	 * @return true if the same measure is defined more than one time
	 */
	public boolean isThereMeasureError() {
		return errors[DUPLICATE_MEASURE_ERROR];
	}

	/**
	 * Returns true if there is at least one measure with at least one 'null'
	 * field
	 *
	 * @return true if the same measure is defined more than one time
	 */
	public boolean isThereInconsistentMeasureError() {
		return errors[INCONSISTENT_MEASURE_ERROR];
	}

	/**
	 * Returns true if there is at least one join but not a fork, false
	 * otherwise.
	 *
	 * @return true if there is at least one join but not a fork, false
	 * otherwise.
	 */
	public boolean isTherejoinWithoutForkError() {
		return errors[JOIN_WITHOUT_FORK_ERROR];
	}

	/**
	 * Returns if a blocking region without stations error occurred
	 *
	 * @return if a blocking region without stations error occurred
	 * <br>Author: Bertoli Marco
	 */
	public boolean isThereEmptyBlockingRegionError() {
		return errors[EMPTY_BLOCKING_REGION];
	}

	/**
	 * Returns if a station in a blocking region is preloaded
	 *
	 * @return if a station in a blocking region is preloaded
	 * <br>Author: Bertoli Marco
	 */
	public boolean isTherePreloadingInBlockingRegionError() {
		return errors[PRELOADING_WITH_BLOCKING];
	}

	public boolean isThereSinkPerfIndicesWithNoSinkError() {
		return errors[SINK_PERF_IND_WITH_NO_SINK_ERROR];
	}

	public boolean isSinkPerfIndicesWithClosedClassError() {
		return errors[SINK_PERF_WITH_CLOSED_CLASS_ERROR];
	}

	/**
	 * Returns true if exist some class_switch components with a invalid matrix
	 * (e.g. some rows do not sum to a value grater or equals to one.), returns
	 * false otherwise.
	 *
	 * @return
	 */
	public boolean isThereCsMatrixRowSumError() {
		return errors[CS_MATRIX_HAS_ROWS_LESS_THAN_ONE];
	}

	/**
	 * Returns true if exist some class_switch components followed by a queue
	 * implementing the BAS strategy, returns false otherwise.
	 *
	 * @return
	 */
	public boolean isThereCsFollowedByBasError() {
		return errors[CS_FOLLOWED_BY_A_BAS];
	}

	public boolean isThereLoadDependentRoutingError() {
		return errors[LOAD_DEPENDENT_ROUTING_INVALID];
	}

	/**
	 * Returns true if exist some class_switch components between a fork and a
	 * join, returns false otherwise.
	 *
	 * @return true if exist some class_switch components between a fork and a
	 * join, returns false otherwise.
	 */
	public boolean isThereCsBetweenForkJoin() {
		return errors[CS_BETWEEN_FORK_JOIN];
	}

	/**
	 * Returns true if exist at least class_switch and different reference
	 * station for the closed classes, false otherwise.
	 *
	 * @return true if exist at least class_switch and different reference
	 * station for the closed classes, false otherwise.
	 */
	public boolean isThereCsReferenceStation() {
		return errors[CS_REFERENCE_STATION];
	}

	/**
	 * Returns true if exist at least system throughput for a class performance
	 * index when there exists at least one class switch in the model, false
	 * otherwise.
	 *
	 * @return true if exist at least system throughput for a class performance
	 * index when there exists at least one class switch in the model, false
	 * otherwise.
	 */
	public boolean isThereCsSystemThroughputClass() {
		return errors[CS_SYSTEM_THROUGHPUT_CLASS];
	}

	/**
	 * Returns true if there exists at least one closed class with zero
	 * population, false otherwise.
	 *
	 * @return true if there exists at least one closed class with zero
	 * population, false otherwise.
	 */
	public boolean isThereClosedClassZeroPopulation() {
		return warnings[CLOSED_ZERO_POPULATION];
	}

	/**
	 * Returns true if all the closed classes has zero population with class
	 * switch, false otherwise.
	 *
	 * @return true if all the closed classes has zero population with class
	 * switch, false otherwise.
	 */
	public boolean isThereCsAllClosedZeroPopulation() {
		return errors[CS_ALL_CLOSED_ZERO_POPULATION];
	}

	/**
	 * Returns true if all the closed classes has zero population with class
	 * switch, false otherwise.
	 *
	 * @return true if all the closed classes has zero population with class
	 * switch, false otherwise.
	 */
	public boolean isThereCsWithForkJoin() {
		return warnings[CS_WITH_FORK_JOIN];
	}

	/**
	 * Returns true if exist a FiringForkStrategy with a mix of 0 tasks for each
	 * class, false otherwise.
	 *
	 * @return f exist a FiringForkStrategy with a mix of 0 tasks for each class,
	 * false otherwise.
	 */
	public boolean isThereGuardStrategyZeroTasks() {
		return errors[GUARD_STRATEGY_ZERO_TASKS];
	}

	/**
	 * Returns true if exist a FiringForkStrategy and at least a closed class
	 * has zero population, false otherwise.
	 *
	 * @return f exist a FiringForkStrategy with a mix of 0 tasks for each class,
	 * false otherwise.
	 */
	public boolean isThereGuardStrategyClosedZeroPopulation() {
		return errors[GUARD_STRATEGY_CLOSED_ZERO_POPULATION];
	}

	/**
	 * Returns true if there are any semaphores not between a fork and a join,
	 * false otherwise.
	 *
	 * @return true if there are any semaphores not between a fork and a join,
	 * false otherwise.
	 */
	public boolean isThereSemaphoreNotBetweenForkJoin() {
		return errors[SEMAPHORE_NOT_BETWEEN_FORK_JOIN];
	}

	/**
	 * Returns true if there are any places between a fork and a join,
	 * false otherwise.
	 *
	 * @return true if there are any places between a fork and a join,
	 * false otherwise.
	 */
	public boolean isTherePlaceBetweenForkJoin() {
		return errors[PLACE_BETWEEN_FORK_JOIN];
	}

	/**
	 * Returns true if there are any transitions between a fork and a join,
	 * false otherwise.
	 *
	 * @return true if there are any transitions between a fork and a join,
	 * false otherwise.
	 */
	public boolean isThereTransitionBetweenForkJoin() {
		return errors[TRANSITION_BETWEEN_FORK_JOIN];
	}

	/**
	 * Returns true if any transitions have no valid enabling or inhibiting
	 * conditions for all classes, false otherwise.
	 *
	 * @return true if any transitions have no valid enabling or inhibiting
	 * conditions for all classes, false otherwise.
	 */
	public boolean isThereTransitionWithoutValidEnablingOrInhibitingCondition() {
		return errors[TRANSITION_WITHOUT_VALID_ENABLING_OR_INHIBITING_CONDITION];
	}

	/**
	 * Returns true if any transitions have no valid firing outcomes for
	 * all classes, false otherwise.
	 *
	 * @return true if any transitions have no valid firing outcomes for
	 * all classes, false otherwise.
	 */
	public boolean isThereTransitionWithoutValidFiringOutcome() {
		return errors[TRANSITION_WITHOUT_VALID_FIRING_OUTCOME];
	}

	/**
	 * Checks the presence in the model of a non exponential time distribution.
	 * This is an error only if user is trying to convert the model to JMVA
	 *
	 * @return true if at least one non-exponential time distribution is found
	 */
	/*public boolean isThereNoExpFoundWarning() {
		return warnings[NO_EXP_FOUND_WARNING];
	}*/

	/**
	 * To be used only for the JSim/JModel to JMVA conversion
	 * @return true if there is at least one open class without a reference station
	 */
	/*public boolean isThereOpenClassReferenceStationError() {
		return errors[OPEN_CLASS_REFERENCE_STATION_ERROR];
	}*/

	/**
	 * use it to check if there is at least one non FCFS queue policy in the
	 * model
	 *
	 * @return true if at least one non FCFS queue policy is found in the model
	 */
	/*public boolean isThereNonFCFSWarning() {
		return warnings[NON_FCFS_WARNING];
	}*/

	/**
	 * Checks if there are delays in the model
	 * @return true if at least a delay is found
	 */
	/*public boolean isThereDelaysFoundError() {
		return errors[DELAYS_FOUND_ERROR];
	}*/

	/**
	 * Returns true if more than one Sink have been defined. In order to
	 * maintain the accuracy of the measured throughput.
	 *
	 * @return true if more than one Sink have been defined
	 */
	public boolean isThereMoreThanOneSinkWarning() {
		return warnings[MORE_THAN_ONE_SINK_WARNING];
	}

	/**
	 * Checks if there is a station (not a sink or a source) no backward linked
	 *
	 * @return true if there is at least a station (not a sink or a source) no
	 * backward linked
	 */
	public boolean isThereNoBackwardLinkWarning() {
		return warnings[NO_BACKWARD_LINK_WARNING];
	}

	/**
	 * Returns true if the PARAMETRIC_ANALYSIS_MODEL_MODIFIED_WARNING occurs
	 *
	 * @return true if the parametric analysis model had become inconsistent
	 * consequently it was modified
	 */
	public boolean isThereParametricAnalysisModelModifiedWarning() {
		return warnings[PARAMETRIC_ANALYSIS_MODEL_MODIFIED_WARNING];
	}

	/**
	 * Returns true if PARAMETRIC_ANALYSIS_NO_MORE_AVAILABLE_WARNING occurs
	 *
	 * @return true if no PA are available but a PA model was previously defined
	 */
	public boolean isThereParametricAnalysisNoMoreAvailableWarning() {
		return warnings[PARAMETRIC_ANALYSIS_NO_MORE_AVAILABLE_WARNING];
	}

	/**
	 * Checks if the service times for different classes are different inside
	 * the same station
	 *
	 * @return true if the problem occurs
	 */
	/*public boolean isThereDifferentServiceTimeWarning() {
		return warnings[DIFFERENT_SERVICE_TIME_WARNING];
	}*/

	/**
	 * Checks if there are servers with per class different queueing strategy
	 *
	 * @return true if servers with per class different queueing strategy are
	 * found
	 */
	public boolean isThereBCMPDifferentQueueingStrategyWarning() {
		return warnings[BCMP_DIFFERENT_QUEUEING_STRATEGIES_WARNING];
	}

	/**
	 * Checks if there are FCFS servers with a mixed service type
	 *
	 * @return true if FCFS servers with with a mixed service type are found
	 */
	public boolean isThereBCMPDifferentServiceTypeWarning() {
		return warnings[BCMP_FCFS_DIFFERENT_SERVICE_TYPES_WARNING];
	}

	/**
	 * Checks if there are FCFS servers with homogeneous but non exponential
	 * service type
	 *
	 * @return true if FCFS servers with homogeneous but non exponential service
	 * type are found
	 */
	public boolean isThereBCMPFcfsNonExponentialWarning() {
		return warnings[BCMP_FCFS_EXPONENTIAL_WARNING];
	}

	/**
	 * Checks if there are FCFS servers with homogeneous exponential service
	 * type, but with different per class service times mean values. If the
	 * service type is Load Dependent it checks that for each class the load
	 * dependent strategies are the same, i.e. :
	 * <br>1) have the same number of ranges</br>
	 * <br>2) have the same distribution in each range (Exponential)</br>
	 * <br>3) have the same mean value in each range</br>
	 * <br>4) have the same 'from' and 'to' values in each range</br>
	 *
	 * @return true if FCFS servers with homogeneous exponential service type
	 * with different per class service times mean values are found
	 */
	public boolean isThereBCMPFcfsDifferentServiceTimesWarning() {
		return warnings[BCMP_FCFS_DIFFERENT_SERVICE_TIMES_WARNING];
	}

	/**
	 * Checks if the scheduling policies other than PS, FCFS, or RAND are used
	 *
	 * @return true if the scheduling policies other than PS, FCFS, or RAND are
	 * used
	 */
	public boolean isThereBCMPNonSymmetricSchedulingPolicies() {
		return warnings[BCMP_NON_SYMMETRIC_SCHEDULING];

	}

	/**
	 * Checks if there are delays with a service time distribution whose Laplace
	 * transform is not rational
	 *
	 * @return true if delays with a service time distribution with a non
	 * rational Laplace transform are found
	 */
	public boolean isThereBCMPDelayWarning() {
		return warnings[BCMP_DELAY_WARNING];
	}

	/**
	 * Checks if there are LCFS-PR stations with service time distribution whose
	 * Laplace transform is not rational
	 *
	 * @return true if LCFS-PR stations with service time distribution whose
	 * Laplace transform is not rational
	 */
	public boolean isThereBCMPLcfsPrWarning() {
		return warnings[BCMP_LCFS_PR_WARNING];
	}

	public boolean isThereForkWithoutJoinWarnings() {
		return warnings[FORK_WITHOUT_JOIN_WARNING];
	}

	public boolean isThereSlowFCFSSamePriorities() {
		return warnings[SLOW_FCFS_SAME_PRIORITIES];
	}    

	/**
	 * Checks if there are stations with Processor Sharing service strategy with
	 * a service time distribution whose Laplace transform is not rational
	 *
	 * @return true if stations with Processor Sharing service strategy with a
	 * service time distribution whose Laplace transform is not rational are
	 * found
	 */
	public boolean isThereBCMPProcessorSharingWarning() {
		return warnings[BCMP_PROCESSOR_SHARING_WARNING];
	}

	/**
	 * Use it to check if a non random routing is used
	 *
	 * @return true if a non random routing is used
	 */
	public boolean isThereBCMPNonStateIndependentRoutingWarning() {
		return warnings[BCMP_NON_STATE_INDEPENDENT_ROUTING_WARNING];
	}

	/**
	 * Returns true if any transitions have invalid enabling or inhibiting
	 * conditions for some classes, false otherwise.
	 *
	 * @return true if any transitions have invalid enabling or inhibiting
	 * conditions for some classes, false otherwise.
	 */
	public boolean isThereTransitionWithInvalidEnablingOrInhibitingCondition() {
		return warnings[TRANSITION_WITH_INVALID_ENABLING_OR_INHIBITING_CONDITION];
	}

	/**
	 * Returns true if any transitions have invalid firing outcomes for
	 * some classes, false otherwise.
	 *
	 * @return true if any transitions have invalid firing outcomes for
	 * some classes, false otherwise.
	 */
	public boolean isThereTransitionWithInvalidFiringOutcome() {
		return warnings[TRANSITION_WITH_INVALID_FIRING_OUTCOME];
	}

	/**
	 * Returns a Vector<Object> containing the keys of the classes without a
	 * reference station. If every has a reference station returns null.
	 *
	 * @return a Vector<Object> containing the keys of the classes without a
	 * reference station.
	 */
	public Vector<Object> getKeysOfClassesWithoutRefStation() {
		if (errors[REFERENCE_STATION_ERROR]) {
			return classesWithoutRefStation;
		} else {
			return null;
		}
	}

	/**
	 * Returns a Vector<Object> containing the keys of OPEN classes without a
	 * reference station. If each open class has a reference station returns
	 * null.
	 *
	 * @return a Vector<Object> containing the keys of the classes without a
	 * reference station.
	 */
	/*public Vector<Object> getKeysOfOpenClassesWithoutRefStation() {
		if (errors[OPEN_CLASS_REFERENCE_STATION_ERROR]) return openClassesWithoutRefStation;
		else return null;
	}*/

	/**
	 * Returns a Vector<Object> containing the keys of the sources witch have no
	 * classes associated to. If every source has a reference station or no
	 * source are defined returns null.
	 *
	 * @return a Vector<Object> containing the keys of the sources with no
	 * classes associated to.
	 */
	public Vector<Object> getKeysOfSourceWithoutClasses() {
		if (errors[SOURCE_WITH_NO_OPEN_CLASSES_ERROR]) {
			return sourceWithoutClasses;
		} else {
			return null;
		}
	}

	/**
	 * Returns a Vector<Object> containing the keys of the stations with link
	 * problems, such as:
	 * <br>- a Source not forward linked.
	 * <br>- a Server not forward linked.
	 * <br>- a Delay not forward linked.
	 * <br>- a Sink not backward linked.
	 * <br><br>If there is not any problem it returns null.
	 *
	 * @return a Vector<Object> containing the keys of the stations with link
	 * problems.
	 */
	public Vector<Object> getKeysOfStationsWithLinkProblems() {
		if (errors[STATION_LINK_ERROR]) {
			return stationsWithLinkErrors;
		} else {
			return null;
		}
	}

	/**
	 * Returns a vector with all empty blocking regions
	 *
	 * @return a vector with all empty blocking regions keys.
	 */
	public Vector<Object> getKeysOfEmptyBlockingRegions() {
		if (errors[EMPTY_BLOCKING_REGION]) {
			return emptyBlockingRegions;
		} else {
			return null;
		}
	}

	/**
	 * Returns a HashMap where the key is the key of a close class. For each
	 * close class that may be routed into a station whose forward stations are
	 * all sink it contains a Vector<Object> with the keys of the stations where
	 * the problems occurs. If there is not any problem or no close classes are
	 * defined it returns null.
	 *
	 * @return a HashMap where the key is the key of a close class.
	 */
	public HashMap<Object, Vector<Object>> getKeysOfAllForwardStationsAreSinkErrors() {
		if (errors[ALL_FORWARD_STATION_ARE_SINK_ERROR]) {
			return allForwardStationsAreSinkErrors;
		} else {
			return null;
		}
	}

	/**
	 * Returns a HashMap where the key is the key of a close class. For each
	 * close class with a routing problem it contains a Vector<Object> with the
	 * keys of the stations where the routing problems occurs. If there is not
	 * any problem or no close classes are defined it returns null.
	 *
	 * @return a HashMap where the key is the key of a close class.
	 */
	public HashMap<Object, Vector<Object>> getKeysOfRoutingProblems() {
		if (errors[ROUTING_ERROR]) {
			return routingErrors;
		} else {
			return null;
		}
	}

	/**
	 * Get the Vector<Object> with station (no Sinks or Sources) not backward
	 * connected. Used for warnings.
	 *
	 * @return the Vector<Object> with station (no Sinks or Sources) not
	 * backward connected.
	 */
	public Vector<Object> getKeysOfStationWithoutBackwardLinks() {
		if (warnings[NO_BACKWARD_LINK_WARNING]) {
			return stationWithoutBackwardLinks;
		} else {
			return null;
		}
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of servers with per
	 * class different queue strategies
	 *
	 * @return a Vector<Object> containing the keys of servers with per class
	 * different queue strategies
	 */
	public Vector<Object> getBCMPserversWithDifferentQueueStrategy() {
		return BCMPserversWithDifferentQueueStrategy;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of servers with per
	 * class different queue strategies
	 *
	 * @return a Vector<Object> containing the keys of servers with per class
	 * different queue strategies
	 */
	public Vector<Object> getBCMPserversWithNonSymmetricScheduling() {
		return BCMPserverWithNonSymmetricSchedulingPolicy;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy but mixed types of service
	 *
	 * @return a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy but mixed types of service
	 */
	public Vector<Object> getBCMPserversWithDifferentServiceTypes() {
		return BCMPserversWithDifferentServiceTypes;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of css with a matrix
	 * in which some rows sum to a value less than one
	 *
	 * @return a Vector<Object> containing the keys of css with a matrix in
	 * which some rows sum to a value less than one
	 */
	public Vector<Object> getCsWithWrongMatrix() {
		return csWithWrongMatrix;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of css followed by a
	 * queue implementing a drop bas strategy.
	 *
	 * @return a Vector<Object> containing the keys of css followed by a queue
	 * implementing a drop bas strategy.
	 */
	public Vector<Object> getCsFolloweByBas() {
		return csFollowedByBas;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of css between
	 * fork/join.
	 *
	 * @return a Vector<Object> containing the keys of css between fork/join.
	 */
	public Vector<Object> getCsBetweenForkJoin() {
		return csBetweenForkJoin;
	}

	/**
	 * Use it to get a Vector<Object> containing closed classes with different
	 * reference station other than the first closed class
	 * @return a Vector<Object> containing closed classes with different
	 * reference station other than the first closed class
	 */
	/*public Vector<Object> getCsReferenceStation() {
		return csReferenceStation;
	}*/

	/**
	 * Use it to get a Vector<Object> containing classes that are defined global
	 * throughput when class switch exists in model
	 *
	 * @return a Vector<Object> class names that are defined global throughput
	 * when class switch exists in model
	 */
	public Vector<Object> getCsSystemThroughputClass() {
		return csSystemThroughputClass;
	}

	/**
	 * Use it to get a Vector<Object> containing closed classes that has zero
	 * populations
	 *
	 * @return a Vector<Object> closed class keys that has zero populations
	 */
	public Vector<Object> getClosedZeroPopulation() {
		return closedZeroPopulation;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy but a non exponential distribution
	 *
	 * @return a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy but a non exponential distribution
	 */
	public Vector<Object> getBCMPserversFCFSWithoutExponential() {
		return BCMPserversFCFSWithoutExponential;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy, exponential distribution but different per class
	 * service times
	 *
	 * @return a Vector<Object> containing the keys of servers with FCFS
	 * queueing strategy, exponential distribution but different per class
	 * service times
	 */
	public Vector<Object> getBCMPFcfsServersWithDifferentServiceTimes() {
		return BCMPFcfsServersWithDifferentServiceTimes;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of delays with at
	 * least a service time distribution with a non rational Laplace transform
	 *
	 * @return a Vector<Object> containing the keys of delays with at least a
	 * service time distribution with a non rational Laplace transform
	 */
	public Vector<Object> getBCMPdelaysWithNonRationalServiceDistribution() {
		return BCMPdelaysWithNonRationalServiceDistribution;
	}

	public Map<Object, HashMap<Object, Object>> getInvalidLoadDependentRoutingStations() {
		return invalidLoadDependentRouting;
	}

	/**
	 * Returns a HashMap where the key is the key of a join station. For each
	 * fork station that contains a FiringMixStrategy with zero tasks it
	 * contains a List<Object> with the keys of the classes where the problems
	 * occurs. If there is not any problem it returns null.
	 *
	 * @return a HashMap where the key is the key of a fork station.
	 */
	public HashMap<Object, List<Object>> getKeysOfAllGuardStrategyZeroTask() {
		if (errors[GUARD_STRATEGY_ZERO_TASKS]) {
			return GuardStrategyZeroTasks;
		} else {
			return null;
		}
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of semaphores not between
	 * fork/join.
	 *
	 * @return a Vector<Object> containing the keys of semaphores not between fork/join.
	 */
	public Vector<Object> getSemaphoreNotBetweenForkJoin() {
		return semaphoreNotBetweenForkJoin;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of places between
	 * fork/join.
	 *
	 * @return a Vector<Object> containing the keys of places between fork/join.
	 */
	public Vector<Object> getPlaceBetweenForkJoin() {
		return placeBetweenForkJoin;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of transitions between
	 * fork/join.
	 *
	 * @return a Vector<Object> containing the keys of transitions between fork/join.
	 */
	public Vector<Object> getTransitionBetweenForkJoin() {
		return transitionBetweenForkJoin;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of transitions have no
	 * valid enabling or inhibiting conditions for all classes.
	 *
	 * @return a Vector<Object> containing the keys of transitions have no valid
	 * enabling or inhibiting conditions for all classes.
	 */
	public Vector<Object> getTransitionWithoutValidEnablingOrInhibitingCondition() {
		return transitionWithoutValidEnablingOrInhibitingCondition;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of transitions have no
	 * valid firing outcomes for all classes.
	 *
	 * @return a Vector<Object> containing the keys of transitions have no valid
	 * firing outcomes for all classes.
	 */
	public Vector<Object> getTransitionWithoutValidFiringOutcome() {
		return transitionWithoutValidFiringOutcome;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of transitions have
	 * invalid enabling or inhibiting conditions for some classes.
	 *
	 * @return a Vector<Object> containing the keys of transitions have invalid
	 * enabling or inhibiting conditions for some classes.
	 */
	public Vector<Object> getTransitionWithInvalidEnablingOrInhibitingCondition() {
		return transitionWithInvalidEnablingOrInhibitingCondition;
	}

	/**
	 * Use it to get a Vector<Object> containing the keys of transitions have
	 * invalid firing outcomes for some classes.
	 *
	 * @return a Vector<Object> containing the keys of transitions have invalid
	 * firing outcomes for some classes.
	 */
	public Vector<Object> getTransitionWithInvalidFiringOutcome() {
		return transitionWithInvalidFiringOutcome;
	}

	/**
	 * Check that there is at least one class in the model. If a problem is
	 * found it raises to "true" the corresponding position inside the problems
	 * array.
	 */
	private void checkForNoClassError() {
		// get the vector of the keys of the classes
		Vector<Object> classKeys = class_def.getClassKeys();
		//if the vector is empty there are no classes defined in the model
		if (classKeys.size() == 0) {
			errors[NO_CLASSES_ERROR] = true;
		}
	}

	/**
	 * Check that there is at least one station in the model. If a problem is
	 * found it raises to "true" the corresponding position inside the problems
	 * array.
	 */
	private void checkForNoStationError() {
		boolean noStation = true;
		// get the vector of the keys of the elements (stations, sinks, delays, servers)
		Vector<Object> elements = station_def.getStationKeys();
		//check each element...
		for (int i = 0; i < elements.size(); i++) {
			Object thisElement = elements.get(i);
			String type = station_def.getStationType(thisElement);
			if ((!type.equals(STATION_TYPE_SOURCE)) && (!type.equals(STATION_TYPE_SINK))) {
				noStation = false;
			}
		}
		if (noStation) {
			errors[NO_STATION_ERROR] = true;
		}
	}

	/**
	 * Checks that every class has a reference station. If a problem is found it
	 * raises to "true" the corresponding position inside the problems array.
	 */
	private void checkForRefStationError() {
		// get the vector of the keys of the classes
		Vector<Object> classKeys = class_def.getClassKeys();

		for (int i = 0; i < classKeys.size(); i++) {
			//get the key at i
			Object thisKey = classKeys.get(i);
			//get the Reference Station of the class
			Object thisRefStation = class_def.getClassRefStation(thisKey);
			//if the class has no reference station there is a class problem
			if (thisRefStation == null) {
				errors[REFERENCE_STATION_ERROR] = true;
				classesWithoutRefStation.add(thisKey);
			}
		}
	}

	/**
	 * Checks that there are no sources without a class associated to. If a
	 * problem is found it raises to "true" the corresponding position inside
	 * the problems array.
	 */
	private void checkForSourcesWithNoClassesError() {
		// get the vector of the keys of the stations
		Vector<Object> stationKeys = station_def.getStationKeys();

		for (int i = 0; i < stationKeys.size(); i++) {
			//get the sourceKey at i
			Object thisStationKey = stationKeys.get(i);
			//get the type of the station
			String type = station_def.getStationType(thisStationKey);
			if (type.equals(STATION_TYPE_SOURCE)) {
				//this variable is true if no classes are associated to this source
				boolean noClassesAssociated = true;
				//get the vector of the class keys....
				Vector<Object> classKeys = class_def.getClassKeys();
				for (int j = 0; j < classKeys.size(); j++) {
					//get the key of the class at j
					Object thisClassKey = classKeys.get(j);
					//get the key of the reference station of this class
					Object refStationKeyForThisClass = class_def.getClassRefStation(thisClassKey);
					//if the key of the reference station and the key of the source considered
					//are the same it means that there is at least one class associated to the source
					if (refStationKeyForThisClass == thisStationKey) {
						noClassesAssociated = false;
					}
				}
				//if the variable noClassesAssociated is still true it means that this source has
				//no classes associated to
				if (noClassesAssociated) {
					errors[SOURCE_WITH_NO_OPEN_CLASSES_ERROR] = true;
					sourceWithoutClasses.add(thisStationKey);
				}
			}
		}
	}

	/**
	 * Check if there is at least one open class but there are no sinks. If a
	 * problem is found it raises to "true" the corresponding position inside
	 * the problems array.
	 */
	private void checkForNoSinkWithOpenClassError() {
		//vector used to contain the set of open class keys
		Vector<Object> openClasses = new Vector<Object>(0, 1);
		//vector used to contain the complete set class keys
		Vector<Object> classes = class_def.getClassKeys();
		//for cicle used to collect the open class keys from the classes vector
		for (int i = 0; i < classes.size(); i++) {
			Object thisClassKey = classes.get(i);
			if (class_def.getClassType(thisClassKey) == CLASS_TYPE_OPEN) {
				openClasses.add(thisClassKey);
			}
		}
		//if there is at least one open class
		if (openClasses.size() > 0) {
			//variable counting the number of sink
			int nSink = 0;
			//vector containing the complete set of class keys
			Vector<Object> stations = station_def.getStationKeys();
			//for each station ...
			for (int i = 0; i < stations.size(); i++) {
				Object thisStationKey = stations.get(i);
				// ... check if it is a sink, in that case increase the nSink variable
				if (station_def.getStationType(thisStationKey).equals(STATION_TYPE_SINK)) {
					nSink++;
				}
			}
			//if no sink was found there is an error
			errors[NO_SINK_WITH_OPEN_CLASSES_ERROR] = nSink == 0;
		} else {
			errors[NO_SINK_WITH_OPEN_CLASSES_ERROR] = false;
		}
	}

	/**
	 * Checks if there is at least an open class but no source have been defined
	 */
	private void checkForOpenClassButNoSourceError() {
		//variable used to count the number of open classes
		int openClasses = 0;
		//vector used to contain the complete set class keys
		Vector<Object> classes = class_def.getClassKeys();
		//for cicle used to collect the open class keys from the classes vector
		for (int i = 0; i < classes.size(); i++) {
			Object thisClassKey = classes.get(i);
			if (class_def.getClassType(thisClassKey) == CLASS_TYPE_OPEN) {
				openClasses++;
			}
		}
		if (openClasses == 0) {
			errors[OPEN_CLASS_BUT_NO_SOURCE_ERROR] = false;
		} else {
			//Vector<Object> containing the entire station key set
			Vector<Object> stations = station_def.getStationKeys();
			//variable counting the number of source
			int nSource = 0;
			for (int i = 0; i < stations.size(); i++) {
				Object thisStation = stations.get(i);
				String type = station_def.getStationType(thisStation);
				if (type.equals(STATION_TYPE_SOURCE)) {
					nSource++;
				}
			}
			//if no sources have been defined there is an error
			errors[OPEN_CLASS_BUT_NO_SOURCE_ERROR] = nSource == 0;
		}
	}

	/**
	 * Checks if there is at least a sink but no open classes have been defined
	 */
	private void checkForSinkButNoOpenClassError() {
		//vector used to contain the complete set of station keys
		Vector<Object> stationSet = station_def.getStationKeys();
		//variable used to count the number of sink station
		int nSink = 0;
		//for cycle used to count the number of sink
		for (int i = 0; i < stationSet.size(); i++) {
			Object thisStation = stationSet.get(i);
			String stationType = station_def.getStationType(thisStation);
			if (stationType.equals(STATION_TYPE_SINK)) {
				nSink++;
			}
		}
		if (nSink > 0) {
			//variable used to count the number of open class keys
			int nOpenClasses = 0;
			//vector used to contain the complete set class keys
			Vector<Object> classes = class_def.getClassKeys();
			//for cicle used to count the number of open class keys
			for (int i = 0; i < classes.size(); i++) {
				Object thisClassKey = classes.get(i);
				if (class_def.getClassType(thisClassKey) == CLASS_TYPE_OPEN) {
					nOpenClasses++;
				}
			}
			//if there is at least one open class
			errors[SINK_BUT_NO_OPEN_CLASSES_ERROR] = nOpenClasses <= 0;
		} else {
			errors[SINK_BUT_NO_OPEN_CLASSES_ERROR] = false;
		}
	}

	/**
	 * Checks: 1) Each Source is forward linked to something. 2) Each Server is
	 * forward linked to something. 3) Each Delay is forward linked to
	 * something. 4) Each Sink is backward linked to something. If a problem is
	 * found it raises to "true" the corresponding position inside the problems
	 * array.
	 */
	private void checkForStationLinkError() {
		// get the vector of the keys of the stations
		Vector<Object> stationKeys = station_def.getStationKeys();

		for (int i = 0; i < stationKeys.size(); i++) {
			//get the key of the station at i
			Object thisStationKey = stationKeys.get(i);
			//get the type of the station
			String stationType = station_def.getStationType(thisStationKey);

			if (stationType.equals(STATION_TYPE_SOURCE)) {
				//get the vector of forward links
				Vector<Object> connections = station_def.getForwardConnections(thisStationKey);
				//if the vector is empty the source is not forward connected
				if (connections.isEmpty()) {
					errors[STATION_LINK_ERROR] = true;
					stationsWithLinkErrors.add(thisStationKey);
				}

			} else if (stationType.equals(STATION_TYPE_SINK)) {
				//get the vector of backward links
				Vector<Object> connections = station_def.getBackwardConnections(thisStationKey);
				//if the vector is empty the Sink is not backward connected
				if (connections.isEmpty()) {
					errors[STATION_LINK_ERROR] = true;
					stationsWithLinkErrors.add(thisStationKey);
				}
			} else {
				//get the vector of forward links
				Vector<Object> connections = station_def.getForwardConnections(thisStationKey);
				//if the vector is empty the Server is not forward connected
				if (connections.isEmpty()) {
					errors[STATION_LINK_ERROR] = true;
					stationsWithLinkErrors.add(thisStationKey);
				}
			}

		}
	}

	/**
	 * Checks for the existence of empty blocking regions
	 * <br>Author: Bertoli Marco
	 */
	private void checkForEmptyBlockingRegions() {
		Iterator<Object> regionKeys = blocking_def.getRegionKeys().iterator();
		while (regionKeys.hasNext()) {
			Object key = regionKeys.next();
			if (blocking_def.getBlockingRegionStations(key).size() == 0) {
				// Blocking region 'key' is empty
				emptyBlockingRegions.add(key);
				errors[EMPTY_BLOCKING_REGION] = true;
			}
		}
	}

	/**
	 * Checks if a station in a blocking region is preloaded
	 * <br>Author: Bertoli Marco
	 */
	private void checkForPreloadingInBlockingRegions() {
		Iterator<Object> regionKeys = blocking_def.getRegionKeys().iterator();
		Vector<Object> classes = class_def.getClassKeys();
		while (regionKeys.hasNext()) {
			Object key = regionKeys.next();
			Set<Object> stations = blocking_def.getBlockingRegionStations(key);
			Iterator<Object> st = stations.iterator();
			Integer totalPreloaded = new Integer(0);
			Integer[] totalPreloadedClass = new Integer[classes.size()];
			for (int i = 0; i < classes.size(); i++) {
				totalPreloadedClass[i] = new Integer(0);
			}

			while (st.hasNext()) {
				Object stationKey = st.next();
				for (int i = 0; i < classes.size(); i++) {
					totalPreloaded += simulation_def.getPreloadedJobs(stationKey, classes.get(i)).intValue();
					totalPreloadedClass[i] += simulation_def.getPreloadedJobs(stationKey, classes.get(i)).intValue();
				}
			}
			errors[PRELOADING_WITH_BLOCKING] = false;

			if (blocking_def.getRegionCustomerConstraint(key).intValue() > 0) {
				if (totalPreloaded.intValue() > blocking_def.getRegionCustomerConstraint(key).intValue()) {
					errors[PRELOADING_WITH_BLOCKING] = true;
				}
			}

			for (int i = 0; i < classes.size(); i++) {
				if (blocking_def.getRegionClassCustomerConstraint(key, classes.get(i)).intValue() > 0) {
					if (totalPreloadedClass[i].intValue() > blocking_def.getRegionClassCustomerConstraint(key, classes.get(i)).intValue()) {
						errors[PRELOADING_WITH_BLOCKING] = true;
					}
				}
			}

			if (blocking_def.getBlockingRegionStations(key).size() == 0) {
				// Blocking region 'key' is empty
				emptyBlockingRegions.add(key);
				errors[EMPTY_BLOCKING_REGION] = true;
			}
		}
	}

	/**
	 * Checks that no closed classes may be routed into a Sink. If a problem is
	 * found it raises to "true" the corresponding position inside the errors
	 * array.
	 */
	@SuppressWarnings("unchecked")
	private void checkForRoutingError() {
		//vector collecting all the station keys where routing problems occur
		// for this close class
		Vector<Object> problemsPerClass = new Vector<Object>(0, 1);
		Vector<Object> closedClassKeys = class_def.getClosedClassKeys();
		Vector<Object> startingPoints = new Vector<Object>(0, 1);
		//get the vector of the possible starting points
		Vector<Object> noSourceSink = station_def.getStationKeysNoSourceSink();
		for (int i = 0; i < closedClassKeys.size(); i++) {
			//remove all elements from the problemsPerClass vector
			problemsPerClass.removeAllElements();
			//create the vector containing the already visited station keys
			Vector<Object> alreadyVisited = new Vector<Object>(0, 1);
			//get the class at i
			Object thisClassKey = closedClassKeys.get(i);
			//checks for each possible starting point if there are preloaded jobs for the class i.
			//In that case put it into the startingPoints vector
			for (int j = 0; j < noSourceSink.size(); j++) {
				Object thisElementKey = noSourceSink.get(j);
				int preloadedJobs = simulation_def.getPreloadedJobs(thisElementKey, thisClassKey).intValue();
				if (preloadedJobs > 0) {
					startingPoints.add(thisElementKey);
				}
			}
			//start the explore algorithm for each of the station containing some preloaded jobs for class i
			for (int j = 0; j < startingPoints.size(); j++) {
				//get the element at j
				Object thisStartingPoint = startingPoints.get(j);
				//if this possible starting point has not been visited yet
				if (!alreadyVisited.contains(thisStartingPoint)) {
					alreadyVisited.add(thisStartingPoint);
					Vector<Object> problemsForThisStartingPoint = exploreForRoutingProblems(thisClassKey, thisStartingPoint, alreadyVisited);
					for (int k = 0; k < problemsForThisStartingPoint.size(); k++) {
						problemsPerClass.add(problemsForThisStartingPoint.get(k));
					}
				}
			}
			if (!problemsPerClass.isEmpty()) {
				routingErrors.put(thisClassKey, (Vector<Object>) problemsPerClass.clone());
				errors[ROUTING_ERROR] = true;
			}
		}
	}

	/**
	 * Checks that no closed classes may be routed into a station whose forward
	 * station(s) are all sink. If a problem is found it raises to "true" the
	 * corresponding position inside the errors array.
	 */
	@SuppressWarnings("unchecked")
	private void checkForAllForwardStationsAreSinkError() {
		//vector collecting all the station keys where errors occur
		// for this close class
		Vector<Object> problemsPerClass = new Vector<Object>(0, 1);
		Vector<Object> closedClassKeys = class_def.getClosedClassKeys();
		Vector<Object> startingPoints = new Vector<Object>(0, 1);
		//get the vector of the possible starting points
		Vector<Object> noSourceSink = station_def.getStationKeysNoSourceSink();
		for (int i = 0; i < closedClassKeys.size(); i++) {
			//remove all elements from the problemsPerClass vector
			problemsPerClass.removeAllElements();
			//create the vector containing the already visited station keys
			Vector<Object> alreadyVisited = new Vector<Object>(0, 1);
			//get the class at i
			Object thisClassKey = closedClassKeys.get(i);
			//checks for each possible starting point if there are preloaded jobs for the class i.
			//In that case put it into the startingPoints vector
			for (int j = 0; j < noSourceSink.size(); j++) {
				Object thisElementKey = noSourceSink.get(j);
				int preloadedJobs = simulation_def.getPreloadedJobs(thisElementKey, thisClassKey).intValue();
				if (preloadedJobs > 0) {
					startingPoints.add(thisElementKey);
				}
			}
			//start the explore algorithm for each of the station containing some preloaded jobs for class i
			for (int j = 0; j < startingPoints.size(); j++) {
				//get the element at j
				Object thisStartingPoint = startingPoints.get(j);
				//if this possible starting point has not been visited yet
				if (!alreadyVisited.contains(thisStartingPoint)) {
					alreadyVisited.add(thisStartingPoint);
					Vector<Object> problemsForThisStartingPoint = this.exploreForAllForwardStationAreSink(thisClassKey, thisStartingPoint,
							alreadyVisited);
					for (int k = 0; k < problemsForThisStartingPoint.size(); k++) {
						problemsPerClass.add(problemsForThisStartingPoint.get(k));
					}
				}
			}
			if (!problemsPerClass.isEmpty()) {
				allForwardStationsAreSinkErrors.put(thisClassKey, (Vector<Object>) problemsPerClass.clone());
				errors[ALL_FORWARD_STATION_ARE_SINK_ERROR] = true;
			}
		}
	}

	/**
	 * Checks for NO_EXP_FOUND_ERROR
	 */
	/*private void checkForNoExpFoundWarning() {
		Vector<Object> classes = class_def.getClassKeys();
		//first search for open classes with non exponential arrival time distribution
		for (int i=0; i<classes.size();i++) {
			Object thisKey = classes.get(i);
			if (class_def.getClassType(thisKey) == CLASS_TYPE_OPEN) {
				Object distribution = class_def.getClassDistribution(thisKey);
				if ((distribution != null)&&(!(distribution instanceof Exponential))) {
					warnings[NO_EXP_FOUND_WARNING] = true;
					return;
				}
			}
		}
		//than search for non exponential service time distribution
		Vector<Object> stations = station_def.getStationKeys();
		for (int i=0; i<stations.size();i++) {
			Object thisStation = stations.get(i);
			for (int j=0; j<classes.size();j++) {
				Object thisClass = classes.get(j);
				Object temp = station_def.getServiceTimeDistribution(thisStation,thisClass);
				if (temp instanceof Distribution) {
					Distribution distribution = (Distribution) temp;
					if (!(distribution instanceof Exponential)) {
						warnings[NO_EXP_FOUND_WARNING] = true;
						return;
					}
				}
			}
		}
	}*/

	/**
	 * checks for delays inside the model
	 */
	/*public void checkForDelaysFoundError() {
		//get the vector of delays and servers
		Vector<Object> stations = station_def.getStationKeysNoSourceSink();
		//for each station...
		for (int i=0;i<stations.size();i++) {
			//get the type of the station
			String stationType = station_def.getStationType(stations.get(i));
			//if it is a delay there is an error
			if (stationType.equals(STATION_TYPE_DELAY)) {
				errors[DELAYS_FOUND_ERROR] = true;
				return;
			}
		}
	}*/

	/**
	 * Checks that there is at least one measure defined. If a problem is found
	 * it raises to "true" the corresponding position inside the problems array.
	 */
	private void checkForSimulationError() {
		Vector<Object> measures = simulation_def.getMeasureKeys();
		if (measures.size() == 0) {
			errors[SIMULATION_ERROR] = true;
		}
	}

	/**
	 * Checks that each measure is defined only one time
	 * <br> Fixed by Bertoli Marco to support global measures
	 */
	private void checkForMeasureError() {
		Vector<Object> measures = simulation_def.getMeasureKeys();
		Vector<String> measuresAlreadyChecked = new Vector<String>(0, 1);
		for (int i = 0; i < measures.size(); i++) {
			Object thisMeasure = measures.get(i);
			String thisMeasureType = simulation_def.getMeasureType(thisMeasure);
			String thisMeasureClass;
			if (simulation_def.getMeasureClass(thisMeasure) == null) {
				thisMeasureClass = "ALL";
			} else {
				thisMeasureClass = class_def.getClassName(simulation_def.getMeasureClass(thisMeasure));
			}
			String thisMeasureStation;
			if (simulation_def.getMeasureStation(thisMeasure) == null) {
				thisMeasureStation = "ALL";
			} else {
				thisMeasureStation = station_def.getStationName(simulation_def.getMeasureStation(thisMeasure));
			}
			String thisMeasureDescription = thisMeasureType + thisMeasureClass + thisMeasureStation;
			if (!measuresAlreadyChecked.contains(thisMeasureDescription)) {
				measuresAlreadyChecked.add(thisMeasureDescription);
			} else {
				errors[DUPLICATE_MEASURE_ERROR] = true;
				redundantMeasure.add(thisMeasure);
			}
		}
	}

	/**
	 * Checks that each performance index is consistent, i.e. it has no 'null'
	 * field in reference station.
	 * <br> Fixed by Bertoli Marco to support global measures
	 */
	private void checkForInconsistentMeasureError() {
		Vector<Object> measures = simulation_def.getMeasureKeys();
		//Vector<Object> measuresAlreadyChecked = new Vector<Object>(0,1);
		for (int i = 0; i < measures.size(); i++) {
			Object thisMeasure = measures.get(i);
			String thisMeasureType = simulation_def.getMeasureType(thisMeasure);
			Object thisMeasureStationKey = simulation_def.getMeasureStation(thisMeasure);
			if ((thisMeasureType == null) || (thisMeasureStationKey == null && !simulation_def.isGlobalMeasure(thisMeasure))) {
				inconsistentMeasures.add(thisMeasure);
				errors[INCONSISTENT_MEASURE_ERROR] = true;
			}
		}
	}

	private void checkForLoadDependentRoutingError() {
		//Step 1 : Find all stations.
		for (Object stationKey : station_def.getStationKeys()) {
			//Step 2: check Routing for each class of the station
			for (Object classKey : class_def.getClassKeys()) {
				RoutingStrategy rs = (RoutingStrategy) station_def.getRoutingStrategy(stationKey, classKey);
				if (rs instanceof LoadDependentRouting) {
					List<String> errorMsgs = ((LoadDependentRouting) rs).validate();
					if (errorMsgs != null && errorMsgs.size() > 0) {
						errors[LOAD_DEPENDENT_ROUTING_INVALID] = true;
						HashMap<Object, Object> innerVal = new HashMap<Object, Object>();
						innerVal.put(stationKey, classKey);
						invalidLoadDependentRouting.put(rs, innerVal);
					}
				}
			}
		}
	}

	/**
	 * This method is the same of checkForReferenceStationError but it checks
	 * only open classes
	 */
	/*private void checkForOpenClassReferenceStationError() {
		// get the vector of the keys of the classes
		Vector<Object> classKeys = class_def.getClassKeys();
		for (int i=0; i<classKeys.size();i++) {
			//get the key at i
			Object thisKey = classKeys.get(i);
			if (class_def.getClassType(thisKey) == CommonConstants.CLASS_TYPE_OPEN) {
				//get the Reference Station of the class
				Object thisRefStation = class_def.getClassRefStation(thisKey);
				//if the class has no reference station there is a class problem
				if (thisRefStation == null) {
					errors[OPEN_CLASS_REFERENCE_STATION_ERROR] = true;
					openClassesWithoutRefStation.add(thisKey);
				}
			}
		}
	}*/

	/**
	 * Checks if the queue strategy for each class is FCFS. Used only in toJMVA
	 * Conversion
	 */
	/*private void checkForNonFCFSWarning() {
		Vector<Object> stations = station_def.getStationKeysNoSourceSink();
		Vector<Object> classes = class_def.getClassKeys();
		for (int i=0;i<stations.size();i++) {
			Object thisStation = stations.get(i);
			for (int j=0;j<classes.size();j++) {
				Object thisClass = classes.get(j);
				String qStrategy = station_def.getQueueStrategy(thisStation,thisClass);
				if (qStrategy != null) {
					if (!qStrategy.equals(JSIMConstants.QUEUE_STRATEGY_FCFS)) {
						nonFCFSStations.add(thisStation);
						warnings[NON_FCFS_WARNING] = true;
						break;
					}
				}
			}
		}
	}*/

	/**
	 * Checks if a non random routing is used inside a station
	 */
	private void checkForBCMPNonStateIndependentRoutingWarning() {
		Vector<Object> stations = station_def.getStationKeysNoSourceSink();
		Vector<Object> classes = class_def.getClassKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStation = stations.get(i);
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				RoutingStrategy thisRoutingStrategy = (RoutingStrategy) station_def.getRoutingStrategy(thisStation, thisClass);
				if (thisRoutingStrategy != null) {
					if (thisRoutingStrategy.isModelStateDependent()) {
						BCMPnonStateIndependentRoutingStations.add(thisStation);
						warnings[BCMP_NON_STATE_INDEPENDENT_ROUTING_WARNING] = true;
						break;
					}
				}
			}
		}
	}

	/**
	 * Explores the model, searching for routing problems for the specified
	 * close class
	 *
	 * @param classKey the key of the class to be analyzed
	 * @param startingStationKey the key of the station where we start analyzing
	 * the routing
	 * @param alreadyVisited vector containing the keys of the already visited
	 * stations
	 * @return the Vector<Object> containing the keys of the stations where the
	 * routing problem occurs
	 */
	private Vector<Object> exploreForRoutingProblems(Object classKey, Object startingStationKey, Vector<Object> alreadyVisited) {
		//Vector<Object> containing the keys to be returned
		Vector<Object> toBeReturned = new Vector<Object>(0, 1);
		//Vector<Object> containing the keys of the forward stations
		Vector<Object> forwardStations = station_def.getForwardConnections(startingStationKey);
		/*//if there is one only forward station ...
		if (forwardStations.size() == 1) {
			//... get the only forward station ...
			Object uniqueForwardElement = forwardStations.get(0);
			//... if it has not been explored yet ...
			if (!alreadyVisited.contains(uniqueForwardElement)) {
				//... add the element to the alreadyVisited vector ...
				alreadyVisited.add(uniqueForwardElement);
				// ... if the forward station is a Sink there is a problem, add the
				// startingStationKey to the toBeReturned vector ...
				if (station_def.getStationType(uniqueForwardElement).equals(STATION_TYPE_SINK)) toBeReturned.add(startingStationKey);
				//... else ...
				else {
					// ... explore starting from uniqueForwardElement and collect the
					// results into the temp vector ...
					Vector<Object> temp = exploreForRoutingProblems(classKey,uniqueForwardElement,alreadyVisited);
					// ... put the elements of the temp vector into the toBeReturned Vector<Object> ...
					for (int i=0; i<temp.size(); i++) {
						toBeReturned.add(temp.get(i));
					}
				}
			}
		}
		// if there is more than one forward station ...*/
		//else {
		for (int i = 0; i < forwardStations.size(); i++) {
			// ... get the forward station at i ...
			Object thisForwardStation = forwardStations.get(i);
			//... if it has not been explored yet ...
			if (!alreadyVisited.contains(thisForwardStation)) {
				//... add the element to the alreadyVisited vector ...
				alreadyVisited.add(thisForwardStation);
				// ... get the routing strategy for the class in the starting station ...
				RoutingStrategy strategy = (RoutingStrategy) station_def.getRoutingStrategy(startingStationKey, classKey);
				// ... if thisForwardStation is a Sink ...
				if (station_def.getStationType(thisForwardStation).equals(STATION_TYPE_SINK)) {
					// ... if the routing strategy is the Probability Routing ...
					if (strategy instanceof ProbabilityRouting) {
						Map<Object, Double> probabilities = strategy.getValues();
						// ... get the routing probability toward thisForwardStation ...
						double p = probabilities.get(thisForwardStation).doubleValue();
						// ... if p = 1 there is an error, put startingStationKey into the
						// toBeReturned vector ...
						if (p == 1) {
							toBeReturned.add(startingStationKey);
						}
					}
				} // ... else thisForwardStation is not a Sink ...
				else {
					Vector<Object> temp;
					// ... if the routing policy is ProbabilityRouting ...
					if (strategy instanceof ProbabilityRouting) {
						Map<Object, Double> probabilities = strategy.getValues();
						// ... get the routing probability toward thisForwardStation ...
						double p = probabilities.get(thisForwardStation).doubleValue();
						// ... if p != 0 start exploring from thisForwardStation and collect
						// the returned vector into temp ...
						if (p != 0) {
							temp = exploreForRoutingProblems(classKey, thisForwardStation, alreadyVisited);
						} else {
							temp = null;
						}
					} else {
						temp = exploreForRoutingProblems(classKey, thisForwardStation, alreadyVisited);
					}
					// ... if temp have been used ...
					if (temp != null) {
						// ... put its elements into the toBeReturned vector
						for (int j = 0; j < temp.size(); j++) {
							toBeReturned.add(temp.get(j));
						}
					}
				}
			}
		}
		//}
		// return the vector containing the keys of the stations where problems occur
		return toBeReturned;
	}

	/**
	 * Explores the model, searching for routing problems for the specified
	 * close class
	 *
	 * @param classKey the key of the class to be analyzed
	 * @param startingStationKey the key of the station where we start analyzing
	 * the routing
	 * @param alreadyVisited vector containing the keys of the already visited
	 * stations
	 * @return the Vector<Object> containing the keys of the stations where the
	 * error occurs
	 */
	private Vector<Object> exploreForAllForwardStationAreSink(Object classKey, Object startingStationKey, Vector<Object> alreadyVisited) {
		boolean allSink = true;
		//Vector<Object> containing the keys to be returned
		Vector<Object> toBeReturned = new Vector<Object>(0, 1);
		//Vector<Object> containing the keys of the forward stations
		Vector<Object> forwardStations = station_def.getForwardConnections(startingStationKey);
		//Check if forwardStations are all sink
		if (forwardStations.size() == 0) {
			allSink = false;
		} else {
			for (int i = 0; i < forwardStations.size(); i++) {
				Object thisStationKey = forwardStations.get(i);
				if (!station_def.getStationType(thisStationKey).equals(STATION_TYPE_SINK)) {
					allSink = false;
				}
			}
		}
		//if forward station are all sink ...
		if (allSink) {
			//... add this startingStationKey to toBeReturned vector
			toBeReturned.add(startingStationKey);
		} else {
			for (int i = 0; i < forwardStations.size(); i++) {
				// ... get the forward station at i ...
				Object thisForwardStation = forwardStations.get(i);
				//... if it has not been explored yet ...
				if (!alreadyVisited.contains(thisForwardStation)) {
					Vector<Object> temp = null;
					//... add the element to the alreadyVisited vector ...
					alreadyVisited.add(thisForwardStation);
					// ... if thisForwardStation is not a Sink ...
					if (!station_def.getStationType(thisForwardStation).equals(STATION_TYPE_SINK)) {
						// ... get the routing strategy for the class in the starting station ...
						RoutingStrategy strategy = (RoutingStrategy) station_def.getRoutingStrategy(startingStationKey, classKey);
						if (strategy instanceof ProbabilityRouting) {
							Map<Object, Double> probabilities = strategy.getValues();
							// ... get the routing probability toward thisForwardStation ...
							double p = probabilities.get(thisForwardStation).doubleValue();
							// ... if p != 0 start exploring from thisForwardStation and collect
							// the returned vector into temp ...
							if (p != 0) {
								//... start exploring from thisForwardStation
								temp = this.exploreForAllForwardStationAreSink(classKey, thisForwardStation, alreadyVisited);
							}
						} else if (strategy instanceof LoadDependentRouting) {
							String stationName = station_def.getStationName(thisForwardStation);
							LoadDependentRouting loadDependentRouting = (LoadDependentRouting) strategy;
							Map<Integer, LoadDependentRouting.EmpiricalEntry[]> empiricalEntries = loadDependentRouting.getEmpiricalEntries();
							Iterator<LoadDependentRouting.EmpiricalEntry[]> rows = empiricalEntries.values().iterator();
							Collection<LoadDependentRouting.EmpiricalEntry> allEntries = new ArrayList<LoadDependentRouting.EmpiricalEntry>(5);
							while (rows.hasNext()) {
								LoadDependentRouting.EmpiricalEntry[] entries = rows.next();
								allEntries.addAll(Arrays.asList(entries));
							}
							for (Iterator<LoadDependentRouting.EmpiricalEntry> itEntries = allEntries.iterator(); itEntries.hasNext();) {
								LoadDependentRouting.EmpiricalEntry entry = itEntries.next();
								if (entry.getStationName().equals(stationName)) {
									double p = entry.getProbability();
									if (p != 0) {
										//... start exploring from thisForwardStation
										temp = this.exploreForAllForwardStationAreSink(classKey, thisForwardStation, alreadyVisited);
									}
								}
							}
						} else {
							temp = this.exploreForAllForwardStationAreSink(classKey, thisForwardStation, alreadyVisited);
						}
					}
					//if temp != null insert its elements into toBeReturned vector
					if (temp != null) {
						for (int j = 0; j < temp.size(); j++) {
							toBeReturned.add(temp.get(j));
						}
					}
				}
			}
		}
		return toBeReturned;
	}

	/**
	 * Checks for DIFFERENT_SERVICE_TIME_ERROR. It checks the mean value for
	 * each service time distribution inside a station, if it has not a mean
	 * value the error occurs
	 */
	/*private void checkForDifferentServiceTimesWarnings() {
		Vector<Object> stations = station_def.getStationKeysNoSourceSink();
		Vector<Object> classes = class_def.getClassKeys();
		//if no classes have been defined
		if (classes.isEmpty()) {
			//the problem can not occur -> exit
			warnings[DIFFERENT_SERVICE_TIME_WARNING] = false;
			return;
		}
		//for each server/delay...
		for (int i=0; i<stations.size();i++) {
			Object thisStation = stations.get(i);
			//get the distribution of the first class...
			Object temp = station_def.getServiceTimeDistribution(thisStation,classes.get(0));
			if (temp instanceof Distribution) {
				Distribution d = (Distribution)temp;
				//if d does not have a mean value
				if (!d.hasMean()) {
					warnings[DIFFERENT_SERVICE_TIME_WARNING] = true;
					return;
				}
				else {
					//else get the mean value of the distribution
					double thisStationMean = d.getMean();
					//for each class...
					for (int j=0;j<classes.size();j++) {
						Object thisClass = classes.get(j);
						temp = station_def.getServiceTimeDistribution(thisStation,thisClass);
						if (temp instanceof Distribution) {
							d = (Distribution) temp;
							//if d does not have a mean value...
							if (!d.hasMean()) {
								//the problem occurs -> exit
								warnings[DIFFERENT_SERVICE_TIME_WARNING] = true;
								return;
							}
							else {
								double thisMean = d.getMean();
								//if a mean differs from the others
								if (thisMean != thisStationMean) {
									//the problem occurs -> exit
									warnings[DIFFERENT_SERVICE_TIME_WARNING] = true;
									return;
								}
							}
						}
					}
				}
			}
		}
	}*/

	/**
	 * To be implemented
	 *
	 * @param startingStation
	 * @return null
	 */
	private Vector<Object> exploreForForkJoinError(Object startingStation) {
		return null;
	}

	/**
	 * Checks if there are more than one sink. If a problem is found it raises
	 * to "true" the corresponding position inside the warnings array.
	 */
	private void checkForMoreThanOneSinkWarning() {
		int nSink = 0;
		//get the vector of the station keys
		Vector<Object> stations = station_def.getStationKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStationKey = stations.get(i);
			if (station_def.getStationType(thisStationKey).equals(STATION_TYPE_SINK)) {
				nSink++;
			}
		}
		if (nSink > 1) {
			warnings[MORE_THAN_ONE_SINK_WARNING] = true;
		}
	}

	/**
	 * Checks if there are stations not backward linked
	 */
	private void checkForNoBacwardLinkWarning() {
		Vector<Object> stations = station_def.getStationKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStationKey = stations.get(i);
			if ((!station_def.getStationType(thisStationKey).equals(STATION_TYPE_SOURCE))
					&& (!station_def.getStationType(thisStationKey).equals(STATION_TYPE_SINK))) {
				Vector<Object> backwardConnections = station_def.getBackwardConnections(thisStationKey);
				if (backwardConnections.size() == 0) {
					warnings[NO_BACKWARD_LINK_WARNING] = true;
					stationWithoutBackwardLinks.add(thisStationKey);
				}
			}
		}
	}

	/**
	 * Checks the consistence of the parametric analysis model (if one was
	 * defined). If it is inconsistent with simulation model definition it will
	 * be automatically changed when possible.
	 */
	private void checkForParametricAnalysisModelModifiedWarning() {
		if (simulation_def.isParametricAnalysisEnabled()) {
			ParametricAnalysisDefinition pad = simulation_def.getParametricAnalysisModel();
			int code = pad.checkCorrectness(false);
			if (code != 0) {
				if (code == 1) {
					warnings[PARAMETRIC_ANALYSIS_MODEL_MODIFIED_WARNING] = true;
				}
				if (code == 2) {
					ParametricAnalysisChecker pac = new ParametricAnalysisChecker(class_def, station_def, simulation_def);
					if (pac.canBeEnabled()) {
						//String[] available = pac.getRunnableParametricAnalysis();
						//ParametricAnalysisDefinition newPAD = ParametricAnalysisModelFactory.createParametricAnalysisModel(available[0],class_def,station_def,simulation_def);
						//simulation_def.setParametricAnalysisModel(newPAD);
						warnings[PARAMETRIC_ANALYSIS_MODEL_MODIFIED_WARNING] = true;
					}
				}
			}
		}
	}

	/**
	 * Checks if PA is no more available, but a PA model was previously defined.
	 */
	private void checkForParametricAnalysisNoMoreAvailableWarning() {
		ParametricAnalysisChecker pac = new ParametricAnalysisChecker(class_def, station_def, simulation_def);
		if ((simulation_def.isParametricAnalysisEnabled()) && (!pac.canBeEnabled())) {
			warnings[PARAMETRIC_ANALYSIS_NO_MORE_AVAILABLE_WARNING] = true;
		}
	}

	private void checkForForkWithoutJoinWarnings() {
		boolean fork = false;
		boolean join = false;
		Vector<Object> stations = station_def.getStationKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisKey = stations.get(i);
			if (station_def.getStationType(thisKey).equals(STATION_TYPE_FORK)) {
				fork = true;
			}
			if (station_def.getStationType(thisKey).equals(STATION_TYPE_JOIN)) {
				join = true;
			}
		}
		if ((fork) && (!join)) {
			warnings[FORK_WITHOUT_JOIN_WARNING] = true;
		}
	}

	private void checkForJoinWithoutForkErrors() {
		boolean fork = false;
		boolean join = false;
		Vector<Object> stations = station_def.getStationKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisKey = stations.get(i);
			if (station_def.getStationType(thisKey).equals(STATION_TYPE_FORK)) {
				fork = true;
			}
			if (station_def.getStationType(thisKey).equals(STATION_TYPE_JOIN)) {
				join = true;
			}
		}
		if ((!fork) && (join)) {
			errors[JOIN_WITHOUT_FORK_ERROR] = true;
		}
	}

	/**
	 * To be implemented
	 */
	private void checkForForkJoinRoutingError() {
		return;
	}

	public void checkSlowFCFSPriorities() {
		Vector<Object> classKeys = class_def.getClassKeys();

		boolean isSamePrios = true;
		if (classKeys.size() > 0) {
			for (int i = 1; i < classKeys.size(); i++) {
				//get the key at i
				Object lastKey = classKeys.get(i - 1);
				Object thisKey = classKeys.get(i);
				//get the Reference Station of the class
				int lastPriority = class_def.getClassPriority(lastKey);
				int thisPriority = class_def.getClassPriority(thisKey);
				if (thisPriority != lastPriority) {
					isSamePrios = false;
				}
			}

			Vector<Object> servers = station_def.getStationKeysServer();
			for (int i = 0; i < servers.size(); i++) {
				Object thisServer = servers.get(i);
				if (CommonConstants.QUEUE_STRATEGY_STATION_QUEUE_PRIORITY.equals(station_def.getStationQueueStrategy(thisServer))) {
					if (isSamePrios) {
						warnings[SLOW_FCFS_SAME_PRIORITIES] = true;
						break;
					}
				}                
			}
		}
	}

	private void checkForBCMPDifferentQueueingStrategyWarning() {
		Vector<Object> servers = station_def.getStationKeysServer();
		Vector<Object> classes = class_def.getClassKeys();
		for (int i = 0; i < servers.size(); i++) {
			Object thisServer = servers.get(i);
			// Processor sharing stations are okay.
			if (CommonConstants.QUEUE_STRATEGY_STATION_PS.equals(station_def.getStationQueueStrategy(thisServer))) {
				continue;
			}
			if (CommonConstants.QUEUE_STRATEGY_STATION_SRPT.equals(station_def.getStationQueueStrategy(thisServer))) {
				continue;
			}
			for (int j = 1; j < classes.size(); j++) {
				//Object thisClass = classes.get(j);
				String s0 = station_def.getQueueStrategy(thisServer, classes.get(j - 1));
				String s1 = station_def.getQueueStrategy(thisServer, classes.get(j));
				if (!s0.equals(s1)) {
					warnings[BCMP_DIFFERENT_QUEUEING_STRATEGIES_WARNING] = true;
					BCMPserversWithDifferentQueueStrategy.add(thisServer);
					break;
				}
			}
		}
	}

	private void checkForBCMPFcfsDifferentServiceTypesWarning() {
		Vector<Object> servers = station_def.getStationKeysServer();
		for (int i = 0; i < servers.size(); i++) {
			Object thisServer = servers.get(i);
			if (this.isAllFCFSQueueingStrategy(thisServer)) {
				if (!this.isTypeUniformServiceStrategy(thisServer)) {
					warnings[BCMP_FCFS_DIFFERENT_SERVICE_TYPES_WARNING] = true;
					BCMPserversWithDifferentServiceTypes.add(thisServer);
				}
			}
		}
	}

	private void checkForBCMPFcfsNonExponentialWarning() {
		Vector<Object> servers = station_def.getStationKeysServer();
		Vector<Object> classes = class_def.getClassKeys();
		for (int i = 0; i < servers.size(); i++) {
			Object thisServer = servers.get(i);
			// Processor sharing stations are okay.
			if (CommonConstants.QUEUE_STRATEGY_STATION_PS.equals(station_def.getStationQueueStrategy(thisServer))) {
				continue;
			}
			if (CommonConstants.QUEUE_STRATEGY_STATION_SRPT.equals(station_def.getStationQueueStrategy(thisServer))) {
				continue;
			}
			if ((this.isAllFCFSQueueingStrategy(thisServer)) && (this.isTypeUniformServiceStrategy(thisServer))) {
				boolean ok = true;
				for (int j = 0; j < classes.size(); j++) {
					Object thisClass = classes.get(j);
					Object service = station_def.getServiceTimeDistribution(thisServer, thisClass);
					if (service instanceof Distribution) {
						Distribution d = (Distribution) service;
						if (!d.getName().equals(CommonConstants.DISTRIBUTION_EXPONENTIAL)) {
							warnings[BCMP_FCFS_EXPONENTIAL_WARNING] = true;
							BCMPserversFCFSWithoutExponential.add(thisServer);
							break;
						}
					} else if (service instanceof LDStrategy) {
						LDStrategy ld = (LDStrategy) service;
						Object[] ranges = ld.getAllRanges();
						for (Object range : ranges) {
							Distribution d = ld.getRangeDistribution(range);
							if (!d.getName().equals(CommonConstants.DISTRIBUTION_EXPONENTIAL)) {
								warnings[BCMP_FCFS_EXPONENTIAL_WARNING] = true;
								BCMPserversFCFSWithoutExponential.add(thisServer);
								ok = false;
								break;
							}
						}
						if (!ok) {
							break;
						}
					}
				}
			}
		}
	}

	private void checkForBCMPFcfsDifferentServiceTimesWarning() {
		Vector<Object> servers = station_def.getStationKeysServer();
		Vector<Object> classes = class_def.getClassKeys();
		if ((!servers.isEmpty()) && (!classes.isEmpty())) {
			for (int i = 0; i < servers.size(); i++) {
				Object thisServer = servers.get(i);
				// Processor sharing stations are okay.
				if (CommonConstants.QUEUE_STRATEGY_STATION_PS.equals(station_def.getStationQueueStrategy(thisServer))) {
					continue;
				}
				if ((this.mustHaveSameExponentialServiceTimes(thisServer)) && (!BCMPserversFCFSWithoutExponential.contains(thisServer))) {
					Object service = station_def.getServiceTimeDistribution(thisServer, classes.get(0));
					if (service instanceof Distribution) {
						double mean = ((Distribution) service).getMean();
						for (int j = 0; j < classes.size(); j++) {
							Object thisClass = classes.get(j);
							Distribution d = (Distribution) station_def.getServiceTimeDistribution(thisServer, thisClass);
							double thisMean = d.getMean();
							if (thisMean != mean) {
								warnings[BCMP_FCFS_DIFFERENT_SERVICE_TIMES_WARNING] = true;
								BCMPFcfsServersWithDifferentServiceTimes.add(thisServer);
								break;
							}
						}
					} else if (service instanceof LDStrategy) {
						LDStrategy ld = (LDStrategy) service;
						for (int j = 0; j < classes.size(); j++) {
							Object thisClass = classes.get(j);
							LDStrategy thisLd = (LDStrategy) station_def.getServiceTimeDistribution(thisServer, thisClass);
							if (!ld.isEquivalent(thisLd)) {
								warnings[BCMP_FCFS_DIFFERENT_SERVICE_TIMES_WARNING] = true;
								BCMPFcfsServersWithDifferentServiceTimes.add(thisServer);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void checkForBCMPProcessorSharingWarning() {
		//TODO:implementation
	}

	private void checkForBCMPDelayWarning() {
		Vector<Object> delays = station_def.getStationKeysDelay();
		Vector<Object> classes = class_def.getClassKeys();
		if (!delays.isEmpty()) {
			for (int i = 0; i < delays.size(); i++) {
				Object thisDelay = delays.get(i);
				for (int j = 0; j < classes.size(); j++) {
					Object thisClass = classes.get(j);
					Object service = station_def.getServiceTimeDistribution(thisDelay, thisClass);
					if (service instanceof Distribution) {
						Distribution d = (Distribution) service;
						if (d.getName().equals(CommonConstants.DISTRIBUTION_PARETO)) {
							warnings[BCMP_DELAY_WARNING] = true;
							BCMPdelaysWithNonRationalServiceDistribution.add(thisDelay);
							break;
						}
					} else if (service instanceof LDStrategy) {
						LDStrategy ld = (LDStrategy) service;
						Object[] ranges = ld.getAllRanges();
						for (Object range : ranges) {
							if (ld.getRangeDistribution(range).getName().equals(CommonConstants.DISTRIBUTION_PARETO)) {
								warnings[BCMP_DELAY_WARNING] = true;
								BCMPdelaysWithNonRationalServiceDistribution.add(thisDelay);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void checkForBCMPLcfsPrWarning() {
		//TODO:implementation
	}

	private void checkForBCMPNonSymmetricSchedulingPolicies() {
		Vector<Object> servers = station_def.getStationKeysServer();
		if (!servers.isEmpty()) {
			for (int i = 0; i < servers.size(); i++) {
				Object thisServer = servers.get(i);
				Vector<Object> classes = class_def.getClassKeys();
				for (int j = 0; j < classes.size(); j++) {
					if (CommonConstants.QUEUE_STRATEGY_STATION_PS.equals(station_def.getStationQueueStrategy(thisServer))) {
						continue;
					} else if (CommonConstants.QUEUE_STRATEGY_FCFS.equals(station_def.getQueueStrategy(thisServer, classes.get(j)))) {
						continue;
					} else if (CommonConstants.QUEUE_STRATEGY_RAND.equals(station_def.getQueueStrategy(thisServer, classes.get(j)))) {
						continue;
					} else {
						warnings[BCMP_NON_SYMMETRIC_SCHEDULING] = true;
						if (!BCMPserverWithNonSymmetricSchedulingPolicy.contains(thisServer)) {
							BCMPserverWithNonSymmetricSchedulingPolicy.add(thisServer);
						}
					}
				}
			}
		}
	}

	private boolean isTypeUniformServiceStrategy(Object stationKey) {
		boolean value = true;
		Vector<Object> classes = class_def.getClassKeys();
		if (!classes.isEmpty()) {
			int ldFound = 0;
			int distrFound = 0;
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				Object service = station_def.getServiceTimeDistribution(stationKey, thisClass);
				if (service instanceof LDStrategy) {
					ldFound = 1;
				} else if (service instanceof Distribution) {
					distrFound = 1;
				}
			}
			if ((ldFound * distrFound) == 1) {
				value = false;
			}
		}
		return value;
	}

	private boolean isAllFCFSQueueingStrategy(Object serverKey) {
		boolean value = true;
		Vector<Object> classes = class_def.getClassKeys();
		for (int j = 0; j < classes.size(); j++) {
			String s = station_def.getQueueStrategy(serverKey, classes.get(j));
			if (!s.equals(CommonConstants.QUEUE_STRATEGY_FCFS)) {
				value = false;
				break;
			}
		}
		return value;
	}

	private boolean mustHaveSameExponentialServiceTimes(Object station) {
		boolean result = false;
		if ((isAllFCFSQueueingStrategy(station)) && (isTypeUniformServiceStrategy(station))
				&& (station_def.getStationType(station).equals(STATION_TYPE_SERVER))) {
			result = true;
		}
		return result;
	}

	/**
	 * Changes all time distributions to exponential maintaining (when possible)
	 * the same mean value. Since T-Student and Replayer do not have a mean
	 * value the mean value 1 is used.
	 */
	/*public void changeDistributionsToExp() {
		Vector<Object> classes = class_def.getClassKeys();
		//first search for open classes with non exponential arrival time distribution
		for (int i=0; i<classes.size();i++) {
			Object thisKey = classes.get(i);
			//if this class is an open class
			if (class_def.getClassType(thisKey) == CLASS_TYPE_OPEN) {
				//get the distribution for this open class
				Distribution distribution = (Distribution)class_def.getClassDistribution(thisKey);
				if ((distribution != null)&&(!(distribution instanceof Exponential))) {
					double meanToSet;
					//if does not have a mean value
					if (!distribution.hasMean()) {
						//set the mean to 1
						meanToSet = 1;
					}
					//else get the mean from the old distribution
					else {
						meanToSet = distribution.getMean();
					}
					//create a new exponential distribution
					Exponential newDistribution = new Exponential();
					//with meanToSet as mean value
					newDistribution.setMean(meanToSet);
					//set the new distribution
					class_def.setClassDistribution(newDistribution,thisKey);
				}
			}
		}
		//than search for non exponential service time distribution
		Vector<Object> stations = station_def.getStationKeys();
		//Vector<Object> serverDelay = new Vector<Object>(0,1);
		for (int i=0; i<stations.size();i++) {
			//get the station at i
			Object thisStation = stations.get(i);
			for (int j=0; j<classes.size();j++) {
				//get the class at j
				Object thisClass = classes.get(j);
				//get the service time distribution for thisClass at thisStation
				Object temp = station_def.getServiceTimeDistribution(thisStation,thisClass);
				if (temp instanceof Distribution) {
					Distribution distribution = (Distribution) temp;
					if (!(distribution instanceof Exponential)) {
						double meanToSet;
						//if the distribution is a Replayer or a T-Student
						if (!distribution.hasMean()) {
							//set the mean to 1
							meanToSet = 1;
						}
						//else get the mean from the old distribution
						else {
							meanToSet = distribution.getMean();
						}
						//create a new exponential distribution
						Exponential newDistribution = new Exponential();
						//with meanToSet as mean value
						newDistribution.setMean(meanToSet);
						//set the new distribution
						station_def.setServiceTimeDistribution(thisStation,thisClass,newDistribution);
					}
				}
			}
		}
	}*/

	/*public void flattenServiceTimes() {
		Vector<Object> classes = class_def.getClassKeys();
		//than search for non exponential service time distribution
		Vector<Object> stations = station_def.getStationKeysNoSourceSink();
		for (int i=0; i<stations.size();i++) {
			//get the station at i
			Object thisStation = stations.get(i);
			double minMean = Double.MAX_VALUE;
			boolean atLeastOneValidDistribution = false;
			for (int j=0; j<classes.size();j++) {
				//get the class at j
				Object thisClass = classes.get(j);
				//get the service time distribution for thisClass at thisStation
				Object temp = station_def.getServiceTimeDistribution(thisStation,thisClass);
				if (temp instanceof Distribution) {
					Distribution distribution = (Distribution) temp;
					if (distribution.hasMean()) {
						double thisMean = distribution.getMean();
						if (thisMean < minMean) minMean = thisMean;
						atLeastOneValidDistribution = true;
					}
					else {
						Exponential exp = new Exponential();
						station_def.setServiceTimeDistribution(thisStation,thisClass,exp);
					}
				}
			}
			//if all service time distributions does not have a mean value
			if (!atLeastOneValidDistribution) {
				minMean = 1;
			}
			for (int j=0; j<classes.size();j++) {
				//get the class at j
				Object thisClass = classes.get(j);
				//get the service time distribution for thisClass at thisStation
				Object temp = station_def.getServiceTimeDistribution(thisStation,thisClass);
				if (temp instanceof Distribution)  {
					Distribution distribution = (Distribution) temp;
					//set the mean value to minMean
					distribution.setMean(minMean);
				}
			}
		}
	}*/

	/**
	 * Deletes all measure already defined
	 */
	public void deleteRedundantMeasure() {
		for (int i = 0; i < redundantMeasure.size(); i++) {
			Object thisRedundantMeasure = redundantMeasure.get(i);
			simulation_def.removeMeasure(thisRedundantMeasure);
		}
	}

	/**
	 * Deletes all empty blocking regions
	 * <br>Author: Bertoli Marco
	 */
	public void deleteEmptyBlockingRegions() {
		for (int i = 0; i < emptyBlockingRegions.size(); i++) {
			blocking_def.deleteBlockingRegion(emptyBlockingRegions.get(i));
		}
	}

	/**
	 * Converts all non FCFS queueing strategies to FCFS (only used in JMVA
	 * conversion)
	 */
	/*public void flattenQueueingStrategies(Object serverKey) {
		Vector<Object> classes = class_def.getClassKeys();

		//initialize the hashmap containing the occourrences for each
		//queueing strategy
		HashMap strategiesOcc = new HashMap(0,1);
		Vector<Object> strategies = new Vector<Object>(0,1);
		for (int i=0;i<classes.size();i++) {
			Object thisClass = classes.get(i);
			String thisStrategy = station_def.getQueueStrategy(serverKey,thisClass);
			Object occ = strategiesOcc.get(thisStrategy);
			if (occ == null) {
				strategiesOcc.put(thisStrategy,new Integer(1));
				strategies.add(thisStrategy);
			}
			else {
				int prevOcc = ((Integer)strategiesOcc.get(thisStrategy)).intValue();
				int newOcc = prevOcc+1;
				strategiesOcc.put(thisStrategy,new Integer(newOcc));
			}
		}

		//Find the q. strategy with more occorrences
		int max = Integer.MIN_VALUE;
		int indexOfMax = -1;
		for (int i=0;i<strategies.size();i++) {
			String thisStrategy = (String)strategies.get(i);
			int thisOcc = ((Integer)strategiesOcc.get(thisStrategy)).intValue();
			if (thisOcc > max) {
				max = thisOcc;
				indexOfMax = i;
			}
		}
		String strategyToSet = (String)strategies.get(indexOfMax);

		//Set the queue strategy for each class
		for (int i=0;i<classes.size();i++) {
			Object thisClass = classes.get(i);
			station_def.setQueueStrategy(serverKey,thisClass,strategyToSet);
		}
	}*/

	/**
	 * Converts all state dependent routing strategies to Random Routing
	 * (only used in JMVA conversion)
	 */
	public void setAllStateDependentRoutingStrategyToRandomRouting() {
		Vector<Object> classes = class_def.getClassKeys();
		for (int i = 0; i < BCMPnonStateIndependentRoutingStations.size(); i++) {
			Object thisStation = BCMPnonStateIndependentRoutingStations.get(i);
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				RoutingStrategy rs = (RoutingStrategy) station_def.getRoutingStrategy(thisStation, thisClass);
				if (rs.isModelStateDependent()) {
					station_def.setRoutingStrategy(thisStation, thisClass, new RandomRouting());
				}
			}
		}
	}

	private void checkForSinkPerfIndicesWithNoSink() {
		boolean sinkFound = false;
		Vector<Object> stations = station_def.getStationKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStationKey = stations.get(i);
			if (station_def.getStationType(thisStationKey).equals(STATION_TYPE_SINK)) {
				sinkFound = true;
				break;
			}
		}
		Vector<Object> measures = simulation_def.getMeasureKeys();
		for (int i = 0; i < measures.size(); i++) {
			Object thisMeasure = measures.get(i);
			String thisMeasureType = simulation_def.getMeasureType(thisMeasure);
			boolean sinkPerfIndex = false;
			sinkPerfIndex = (thisMeasureType.equalsIgnoreCase(SimulationDefinition.MEASURE_R_PER_SINK)
					|| thisMeasureType.equalsIgnoreCase(SimulationDefinition.MEASURE_X_PER_SINK)) ? true : false;
			if (sinkPerfIndex && !sinkFound) {
				errors[SINK_PERF_IND_WITH_NO_SINK_ERROR] = true;
				break;
			}
		}
	}

	public void checkForSinkPerfIndicesWithClosedClass() {
		Vector<Object> closedClassKeys = class_def.getClosedClassKeys();
		Vector<Object> measures = simulation_def.getMeasureKeys();
		for (int i = 0; i < measures.size(); i++) {
			Object thisMeasure = measures.get(i);
			boolean sinkPerfIndex = false;
			Object measureClassKey = simulation_def.getMeasureClass(thisMeasure);
			String thisMeasureType = simulation_def.getMeasureType(thisMeasure);
			sinkPerfIndex = (thisMeasureType.equalsIgnoreCase(SimulationDefinition.MEASURE_R_PER_SINK)
					|| thisMeasureType.equalsIgnoreCase(SimulationDefinition.MEASURE_X_PER_SINK)) ? true : false;
			if (sinkPerfIndex && closedClassKeys.contains(measureClassKey)) {
				errors[SINK_PERF_WITH_CLOSED_CLASS_ERROR] = true;
				break;
			}
		}
	}

	public void checkForSinkProbabilityUpdateWarning() {
		if (station_def.isSinkProbabilityUpdated()) {
			warnings[SINK_PROBABILITY_UPDATE_WARNING] = true;
			station_def.setSinkProbabilityUpdatedVar(false);//reset the value.
		}
	}

	/**
	 * It checks if exist some class_switch components between a fork and a
	 * join.
	 */
	public void checkForCsBetweenForkJoin() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_FORK)) {
				continue;
			}
			Vector<Object> result = searchForStationBetweenForkJoin(STATION_TYPE_CLASSSWITCH, stationKey);
			if (result != null) {
				csBetweenForkJoin.addAll(result);
			}
		}
		if (!csBetweenForkJoin.isEmpty()) {
			errors[CS_BETWEEN_FORK_JOIN] = true;
		}
	}

	/**
	 * It checks the different reference station of closed class if there exists
	 * class_switch.
	 */
	public void checkCsReferenceStation() {
		if (csExists()) {
			Vector<Object> classKeys = class_def.getClassKeys();
			Long closedReferenceStation = null;

			for (int i = 0; i < classKeys.size(); i++) {
				//get the key at i
				Object thisKey = classKeys.get(i);
				//get the Reference Station of the class
				Object thisRefStation = class_def.getClassRefStation(thisKey);
				if (thisRefStation == null) {
					continue;
				}
				if (class_def.getClassType(thisKey) == CommonConstants.CLASS_TYPE_CLOSED) {
					if (closedReferenceStation == null) {
						closedReferenceStation = (Long) thisRefStation;
					} else if (!thisRefStation.equals(closedReferenceStation)) {
						//csReferenceStation.add(closedReferenceStation);
						errors[CS_REFERENCE_STATION] = true;
					}
				}
			}
		}
	}

	public void checkCsSystemThroughputClass() {
		if (csExists()) {
			for (Object measure : simulation_def.getMeasureKeys()) {
				Object measureClassKey = simulation_def.getMeasureClass(measure);
				String thisMeasureType = simulation_def.getMeasureType(measure);
				if (thisMeasureType.equalsIgnoreCase(SimulationDefinition.MEASURE_S_X) && measureClassKey != null) {
					csSystemThroughputClass.add(measureClassKey);
					errors[CS_SYSTEM_THROUGHPUT_CLASS] = true;
				}
			}
		}
	}

	public void checkZeroPopulation() {
		if (csExists()) {
			if (class_def.getClosedClassKeys().size() > 0 && class_def.getTotalCloseClassPopulation() == 0) {
				errors[CS_ALL_CLOSED_ZERO_POPULATION] = true;
			}
		} else if (isTereFiringMixStrategy()) {
			for (Object closedClassKey : class_def.getClosedClassKeys()) {
				if (class_def.getClassPopulation(closedClassKey) == 0) {
					closedZeroPopulation.add(closedClassKey);
					errors[GUARD_STRATEGY_CLOSED_ZERO_POPULATION] = true;
				}
			}
		} else {
			for (Object closedClassKey : class_def.getClosedClassKeys()) {
				if (class_def.getClassPopulation(closedClassKey) == 0) {
					closedZeroPopulation.add(closedClassKey);
					warnings[CLOSED_ZERO_POPULATION] = true;
				}
			}
		}
	}

	public void checkForkOrFcr() {
		if (csExists() && forkExists()) {
			warnings[CS_WITH_FORK_JOIN] = true;
		}
	}

	private boolean csExists() {
		boolean csExists = false;
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (station_def.getStationType(stationKey).equals(STATION_TYPE_CLASSSWITCH)) {
				csExists = true;
				break;
			}
		}
		return csExists;
	}

	private boolean forkExists() {
		boolean forkExists = false;
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (station_def.getStationType(stationKey).equals(STATION_TYPE_FORK)) {
				forkExists = true;
				break;
			}
		}
		return forkExists;
	}

	private boolean fcrExists() {
		boolean fcrExists = false;
		if (blocking_def.getRegionKeys().size() > 0) {
			fcrExists = true;
		}
		return fcrExists;
	}

	public void checkGuardStrategyZeroTasks() {
		for (Object station : station_def.getStationKeys()) {
			List<Object> classes = new ArrayList<Object>();
			for (Object c : class_def.getClassKeys()) {
				Object strategy = station_def.getJoinStrategy(station, c);
				if (strategy instanceof GuardJoin) {
					int sum = 0;
					for (Integer i : ((GuardJoin) strategy).getGuard().values()) {
						sum += i;
					}
					if (sum <= 0) {
						errors[GUARD_STRATEGY_ZERO_TASKS] = true;
						classes.add(c);
					}
				}
			}
			if (classes.size() > 0) {
				GuardStrategyZeroTasks.put(station, classes);
			}
		}
	}

	private boolean isTereFiringMixStrategy() {
		for (Object station : station_def.getStationKeys()) {
			for (Object c : class_def.getClassKeys()) {
				Object strategy = station_def.getJoinStrategy(station, c);
				if (strategy instanceof GuardJoin) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * It checks if exist some class_switch components followed by a queue
	 * implementing the BAS strategy.
	 */
	public void checkForCsFollowedByBAS() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_CLASSSWITCH)) {
				continue;
			}
			Vector<Object> forward = station_def.getForwardConnections(stationKey);
			for (int j = 0; j < forward.size(); j++) {
				Object forwardStation = forward.get(j);
				for (int k = 0; k < class_def.getClassKeys().size(); k++) {
					String queueStrat = station_def.getDropRule(forwardStation, class_def.getClassKeys().get(k));
					if (queueStrat != null && queueStrat.equals(CommonConstants.FINITE_BLOCK)) {
						errors[CS_FOLLOWED_BY_A_BAS] = true;
						csFollowedByBas.add(stationKey);
					}
				}
			}
		}
	}

	/**
	 * It checks if exist some class_switch components with a wrong matrix (e.g.
	 * some rows do not sum to a value grater or equals to one.)
	 */
	public void checkForCsMatrixProbabilities() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_CLASSSWITCH)) {
				continue;
			}
			for (int j = 0; j < class_def.getClassKeys().size(); j++) {
				float row = 0;
				for (int k = 0; k < class_def.getClassKeys().size(); k++) {
					row += station_def.getClassSwitchMatrix(stationKey,
							class_def.getClassKeys().get(j), class_def.getClassKeys().get(k));
				}
				if (row < 1) {
					errors[CS_MATRIX_HAS_ROWS_LESS_THAN_ONE] = true;
					csWithWrongMatrix.add(stationKey);
					return;
				}
			}
		}
	}

	public boolean isThereSinkProbabilityUpdateWarning() {
		return warnings[SINK_PROBABILITY_UPDATE_WARNING];
	}

	/**
	 * Search for all the stations of a given type between a fork and a
	 * join.
	 * @param type: the type of the stations to be searched for.
	 * @param start: the fork at which the search begins.
	 * @return all the stations of the given type found between the fork
	 * and the join, or null if source is not a fork.
	 */
	private Vector<Object> searchForStationBetweenForkJoin(String type, Object start) {
		if (!station_def.getStationType(start).equals(STATION_TYPE_FORK)) {
			return null;
		}
		Vector<Object> found = new Vector<Object>(0, 1);
		Map<Object, Boolean> reachable = new HashMap<Object, Boolean>(0, 1);
		searchForStationBetweenForkJoinAux(type, start, start, reachable, found);
		return found;
	}

	private void searchForStationBetweenForkJoinAux(String type, Object start, Object source,
			Map<Object, Boolean> reachable, Vector<Object> found) {
		Vector<Object> forwardStations = station_def.getForwardConnections(source);
		reachable.put(source, false);
		for (Object next : forwardStations) {
			if (station_def.getStationType(next).equals(STATION_TYPE_JOIN)) {
				reachable.put(source, true);
				continue;
			}
			if (next.equals(start)) {
				continue;
			}
			if (!reachable.containsKey(next)) {
				searchForStationBetweenForkJoinAux(type, start, next, reachable, found);
			}
			// The reachable state may be missed when the next station has not been finished.
			if (reachable.get(next)) {
				reachable.put(source, true);
			}
		}
		if (station_def.getStationType(source).equals(type) && reachable.get(source)) {
			found.add(source);
		}
		return;
	}

	/**
	 * It checks if there are any semaphores not between a fork and a join.
	 */
	public void checkForSemaphoreNotBetweenForkJoin() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (station_def.getStationType(stationKey).equals(STATION_TYPE_SEMAPHORE)) {
				semaphoreNotBetweenForkJoin.add(stationKey);
			}
		}
		Vector<Object> semaphoreBetweenForkJoin = new Vector<Object>();
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_FORK)) {
				continue;
			}
			Vector<Object> result = searchForStationBetweenForkJoin(STATION_TYPE_SEMAPHORE, stationKey);
			if (result != null && !result.isEmpty()) {
				semaphoreBetweenForkJoin.addAll(result);
			}
		}
		semaphoreNotBetweenForkJoin.removeAll(semaphoreBetweenForkJoin);
		if (!semaphoreNotBetweenForkJoin.isEmpty()) {
			errors[SEMAPHORE_NOT_BETWEEN_FORK_JOIN] = true;
		}
	}

	/**
	 * It checks if there are any places between a fork and a join.
	 */
	public void checkForPlaceBetweenForkJoin() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_FORK)) {
				continue;
			}
			Vector<Object> result = searchForStationBetweenForkJoin(STATION_TYPE_PLACE, stationKey);
			if (result != null) {
				placeBetweenForkJoin.addAll(result);
			}
		}
		if (!placeBetweenForkJoin.isEmpty()) {
			errors[PLACE_BETWEEN_FORK_JOIN] = true;
		}
	}

	/**
	 * It checks if there are any transitions between a fork and a join.
	 */
	public void checkForTransitionBetweenForkJoin() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_FORK)) {
				continue;
			}
			Vector<Object> result = searchForStationBetweenForkJoin(STATION_TYPE_TRANSITION, stationKey);
			if (result != null) {
				transitionBetweenForkJoin.addAll(result);
			}
		}
		if (!transitionBetweenForkJoin.isEmpty()) {
			errors[TRANSITION_BETWEEN_FORK_JOIN] = true;
		}
	}

	/**
	 * It checks if any transitions have invalid enabling or inhibiting conditions for some/all classes.
	 */
	public void checkForTransitionWithInvalidEnablingOrInhibitingCondition() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_TRANSITION)) {
				continue;
			}

			Vector<Object> classes = class_def.getClassKeys();
			Vector<Object> stationsIn = station_def.getBackwardConnections(stationKey);
			if (classes.isEmpty() || stationsIn.isEmpty()) {
				continue;
			}

			int count = 0;
			for (Object classKey : classes) {
				boolean isZeroRow = true;
				boolean isRowWithAllValuesValid = true;
				for (Object stationInKey : stationsIn) {
					int enablingValue = station_def.getEnablingCondition(stationKey, classKey, stationInKey).intValue();
					int inhibitingValue = station_def.getInhibitingCondition(stationKey, classKey, stationInKey).intValue();
					if (enablingValue > 0) {
						isZeroRow = false;
					}
					if (enablingValue > 0 && inhibitingValue > 0 && enablingValue >= inhibitingValue) {
						isRowWithAllValuesValid = false;
					}
				}
				if (isZeroRow || !isRowWithAllValuesValid) {
					count++;
				}
			}

			if (count == 0) {
				// Enabling and inhibiting conditions are valid for all classes
			} else if (count < classes.size()) {
				// Enabling or inhibiting conditions are invalid for some classes
				transitionWithInvalidEnablingOrInhibitingCondition.add(stationKey);
			} else {
				// Enabling or inhibiting conditions are invalid for all classes
				transitionWithoutValidEnablingOrInhibitingCondition.add(stationKey);
			}
		}
		if (!transitionWithInvalidEnablingOrInhibitingCondition.isEmpty()) {
			warnings[TRANSITION_WITH_INVALID_ENABLING_OR_INHIBITING_CONDITION] = true;
		}
		if (!transitionWithoutValidEnablingOrInhibitingCondition.isEmpty()) {
			errors[TRANSITION_WITHOUT_VALID_ENABLING_OR_INHIBITING_CONDITION] = true;
		}
	}

	/**
	 * It checks if any transitions have invalid firing outcomes for some/all classes.
	 */
	public void checkForTransitionWithInvalidFiringOutcome() {
		for (int i = 0; i < station_def.getStationKeys().size(); i++) {
			Object stationKey = station_def.getStationKeys().get(i);
			if (!station_def.getStationType(stationKey).equals(STATION_TYPE_TRANSITION)) {
				continue;
			}

			Vector<Object> classes = class_def.getClassKeys();
			Vector<Object> stationsOut = station_def.getForwardConnections(stationKey);
			if (classes.isEmpty() || stationsOut.isEmpty()) {
				continue;
			}

			int count = 0;
			for (Object classKey : classes) {
				boolean isZeroRow = true;
				for (Object stationOutKey : stationsOut) {
					int firingValue = station_def.getFiringOutcome(stationKey, classKey, stationOutKey).intValue();
					if (firingValue > 0) {
						isZeroRow = false;
					}
				}
				if (isZeroRow) {
					count++;
				}
			}

			if (count == 0) {
				// Firing outcomes are valid for all classes
			} else if (count < classes.size()) {
				// Firing outcomes are invalid for some classes
				transitionWithInvalidFiringOutcome.add(stationKey);
			} else {
				// Firing outcomes are invalid for all classes
				transitionWithoutValidFiringOutcome.add(stationKey);
			}
		}
		if (!transitionWithInvalidFiringOutcome.isEmpty()) {
			warnings[TRANSITION_WITH_INVALID_FIRING_OUTCOME] = true;
		}
		if (!transitionWithoutValidFiringOutcome.isEmpty()) {
			errors[TRANSITION_WITHOUT_VALID_FIRING_OUTCOME] = true;
		}
	}

}
