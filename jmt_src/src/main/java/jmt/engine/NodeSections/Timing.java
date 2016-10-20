/**
 * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.engine.NodeSections;

import java.util.Arrays;
import java.util.LinkedList;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.DataPacket;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.random.engine.RandomEngine;
import jmt.engine.simEngine.RemoveToken;

/**
 * <p>Title: Timing</p>
 * <p>Description: This class implements the timing section.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class Timing extends ServiceSection {

	private int numberOfServers;
	private ServiceStrategy[] timingStrategies;
	private Integer[] firingPriorities;
	private Integer[] firingWeights;
	private int idleServers;
	private RandomEngine randomEngine;
	private LinkedList[] timingTokenQueues;
	private int[] extraEnablingSets;
	private LinkedList<JobClass> extraEnablingSetQueue;

	/**
	 * Creates a new instance of the timing section.
	 */
	public Timing(Integer numberOfServers, ServiceStrategy[] timingStrategies, Integer[] firingPriorities, Integer[] firingWeights) {
		super(true);
		this.numberOfServers = numberOfServers;
		this.timingStrategies = timingStrategies;
		this.firingPriorities = firingPriorities;
		this.firingWeights = firingWeights;
	}

	@Override
	protected void nodeLinked(NetNode node) {
		idleServers = numberOfServers;
		randomEngine = RandomEngine.makeDefault();
		JobClassList jobClasses = getJobClasses();
		timingTokenQueues = new LinkedList[jobClasses.size()];
		for (int i = 0; i < timingTokenQueues.length; i++) {
			timingTokenQueues[i] = new LinkedList<RemoveToken>();
		}
		extraEnablingSets = new int[jobClasses.size()];
		Arrays.fill(extraEnablingSets, 0);
		extraEnablingSetQueue = new LinkedList<JobClass>();
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		Object data = message.getData();

		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
		{
			break;
		}

		case NetEvent.EVENT_JOB:
			return MSG_NOT_PROCESSED;

		case NetEvent.EVENT_ACK:
			break;

		case NetEvent.EVENT_ENABLING:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			int enablingDegree = ((Integer) ((DataPacket) data).getData(DataPacket.FIELD_ENABLING_DEGREE)).intValue();
			int classIndex = jobClass.getId();
			int timingEvents = timingTokenQueues[classIndex].size();
			int deltaTimingEvents = 0;
			int deltaExtraEnablingSets = 0;
			if (numberOfServers < 0) {
				deltaTimingEvents = enablingDegree - timingEvents;
			} else {
				int availableServers = timingEvents + idleServers;
				if (enablingDegree <= availableServers) {
					deltaTimingEvents = enablingDegree - timingEvents;
					idleServers = availableServers - enablingDegree;
					deltaExtraEnablingSets = 0 - extraEnablingSets[classIndex];
					extraEnablingSets[classIndex] = 0;
				} else {
					deltaTimingEvents = idleServers;
					idleServers = 0;
					deltaExtraEnablingSets = (enablingDegree - availableServers) - extraEnablingSets[classIndex];
					extraEnablingSets[classIndex] = enablingDegree - availableServers;
				}
			}

			if (deltaTimingEvents >= 0) {
				for (int i = 0; i < deltaTimingEvents; i++) {
					double delay = timingStrategies[classIndex].wait(this);
					DataPacket packet = new DataPacket();
					packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
					packet.setData(DataPacket.FIELD_FIRING_DELAY, Double.valueOf(delay));
					packet.setData(DataPacket.FIELD_FIRING_PRIORITY, Integer.valueOf(firingPriorities[classIndex]));
					packet.setData(DataPacket.FIELD_FIRING_WEIGHT, Integer.valueOf(firingWeights[classIndex]));
					RemoveToken token = sendMe(NetEvent.EVENT_TIMING, packet, delay);
					packet.setData(DataPacket.FIELD_TIMING_TOKEN, token);
					int index = (int) (randomEngine.raw() * (timingTokenQueues[classIndex].size() + 1));
					timingTokenQueues[classIndex].add(index, token);
					//timingTokenQueues[classIndex].add(token);
				}
			} else {
				for (int i = 0; i > deltaTimingEvents; i--) {
					RemoveToken token = (RemoveToken) timingTokenQueues[classIndex].removeLast();
					removeMessage(token);
				}
			}

			if (deltaExtraEnablingSets >= 0) {
				for (int i = 0; i < deltaExtraEnablingSets; i++) {
					int index = (int) (randomEngine.raw() * (extraEnablingSetQueue.size() + 1));
					extraEnablingSetQueue.add(index, jobClass);
					//extraEnablingSetQueue.add(jobClass);
				}
			} else {
				for (int i = 0; i > deltaExtraEnablingSets; i--) {
					extraEnablingSetQueue.removeLast();
				}
			}
			break;
		}

		case NetEvent.EVENT_TIMING:
		{
			JobClass jobClass = (JobClass) ((DataPacket) data).getData(DataPacket.FIELD_REFERENCE_CLASS);
			RemoveToken token = (RemoveToken) ((DataPacket) data).getData(DataPacket.FIELD_TIMING_TOKEN);
			int classIndex = jobClass.getId();
			timingTokenQueues[classIndex].remove(token);

			DataPacket packet = new DataPacket();
			packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
			sendBackward(NetEvent.EVENT_FIRING, packet, 0.0);

			if (numberOfServers >= 0) {
				if (extraEnablingSetQueue.size() > 0) {
					jobClass = extraEnablingSetQueue.removeFirst();
					classIndex = jobClass.getId();
					extraEnablingSets[classIndex]--;
					double delay = timingStrategies[classIndex].wait(this);
					packet = new DataPacket();
					packet.setData(DataPacket.FIELD_REFERENCE_CLASS, jobClass);
					packet.setData(DataPacket.FIELD_FIRING_DELAY, Double.valueOf(delay));
					packet.setData(DataPacket.FIELD_FIRING_PRIORITY, Integer.valueOf(firingPriorities[classIndex]));
					packet.setData(DataPacket.FIELD_FIRING_WEIGHT, Integer.valueOf(firingWeights[classIndex]));
					token = sendMe(NetEvent.EVENT_TIMING, packet, delay);
					packet.setData(DataPacket.FIELD_TIMING_TOKEN, token);
					int index = (int) (randomEngine.raw() * (timingTokenQueues[classIndex].size() + 1));
					timingTokenQueues[classIndex].add(index, token);
					//timingTokenQueues[classIndex].add(token);
				} else {
					idleServers++;
				}
			}
			break;
		}

		case NetEvent.EVENT_FIRING:
		{
			sendForward(NetEvent.EVENT_FIRING, data, 0.0);
			break;
		}

		case NetEvent.EVENT_STOP:
		{
			break;
		}

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

}
