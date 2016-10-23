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

package jmt.engine.QueueNet;

import java.util.ListIterator;

import jmt.common.exception.NetException;
import jmt.engine.simEngine.SimSystem;

/**
 * Controls the state of the simulation and determines when to stop the simulation.
 *
 * @author Federico Granata, Stefano Omini
 */

class NetController {

	private boolean running;

	private double startTime;

	private double stopTime;

	//number of system "ticks"
	private int n;
	//check measures every refreshPeriod system ticks
	private int refreshPeriod = 12000;

	//check if some measures have not receive any sample yet
	//WARNING: this samples number must be a multiple of refreshPeriod!!
	private int reachabilityTest = refreshPeriod * 3;

	private boolean blocked = false;

	//set to true when when the first abort measures is called due to max simulated time
	//in order to prevent further calls while the system is still processing enqueued events
	private boolean aborting = false;

	//the maximum simulated time a simulation is allowed to reach before it is stopped, negative value means infinite
	private double maxSimulatedTime = -1;

	NetController() {
		running = false;
		//initializes tick counter
		n = 0;
	}

	/** This is the run method of the NetController (thread). */
	public void run() throws InterruptedException, NetException {
		SimSystem.runStart();
		startTime = NetSystem.getElapsedTime();

		while (SimSystem.runTick()) {
			synchronized (this) {
				//the presence of this "if" allows pause control
				if (blocked) {
					wait();
				}
				n++;

				if (n % refreshPeriod == 0) {
					//User may have defined measures that will not receive any sample
					if (n % reachabilityTest == 0) {
						//stop measures which have not collected samples yet
						NetSystem.stopNoSamplesMeasures();
					}
					//refresh measures
					NetSystem.checkMeasures();
				}
				//check if a positive max simulated time is set and has been reached for the first time
				if (maxSimulatedTime > 0 && SimSystem.getClock() > maxSimulatedTime && !aborting) {
					ListIterator<QueueNetwork> nets = NetSystem.getNetworkList().listIterator();
					QueueNetwork network;
					while (nets.hasNext()) {
						network = nets.next();
						network.abortAllMeasures();
						aborting = true;
					}
					NetSystem.checkMeasures(); //refresh measures, this triggers a simulation stop because all measures have been aborted
				}
			}
		}
		//sim is finished: get stop time
		stopTime = NetSystem.getElapsedTime();
		SimSystem.runStop();
		running = false;
	}

	public void start() {
		running = true;
	}

	/** Checks if the NetSystem Engine thread is running.
	 * @return True if NetSystem Engine thread is running.
	 */
	synchronized boolean isRunning() {
		return running;
	}

	/** Gets simulation time.
	 * @return Simulation time.
	 */
	synchronized double getSimulationTime() {
		return stopTime - startTime;
	}

	/**
	 * Blocks NetController for synchronized access to data.
	 */
	public synchronized void block() {
		blocked = true;
	}

	/**
	 * Unblocks the object.
	 */
	public synchronized void unblock() {
		blocked = false;
		notifyAll();
	}

	/**
	 * Sets the value of the max simulated time, when this value is reached in a simulation the run is stopped
	 * @param maxSimulatedTime the value to be set for the max simulated time
	 */
	public void setMaxSimulatedTime(double maxSimulatedTime) {
		this.maxSimulatedTime = maxSimulatedTime;
	}

}
