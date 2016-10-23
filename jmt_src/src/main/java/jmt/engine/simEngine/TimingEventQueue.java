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

package jmt.engine.simEngine;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import jmt.engine.QueueNet.DataPacket;
import jmt.engine.random.engine.RandomEngine;
import jmt.framework.data.CircularList;

/**
 * <p>Title: Timing Event Queue</p>
 * <p>Description: This class implements the timing event queue.</p>
 *
 * @author Lulai Zhu
 * Date: 10-09-2016
 * Time: 14.00.00
 */
public class TimingEventQueue implements EventQueue {

	private final static int DEFAULT_INITIAL_CAPACITY = 111;

	private TimingEventComparator comparator;
	private CircularList<SimEvent> current;
	private PriorityQueue<SimEvent> future;
	private RandomEngine randomEngine;

	public TimingEventQueue() {
		comparator = new TimingEventComparator();
		current = new CircularList<SimEvent>();
		future = new PriorityQueue<SimEvent>(DEFAULT_INITIAL_CAPACITY, comparator);
		randomEngine = RandomEngine.makeDefault();
	}

	@Override
	public int size() {
		return current.size() + future.size();
	}

	@Override
	public boolean add(SimEvent event) {
		handleCurrent();
		if (current.size() == 0) {
			current.add(event);
			return true;
		}

		int result = comparator.compare(event, current.getFirst());
		if (result == 0) {
			current.add(event);
		} else if (result > 0) {
			future.add(event);
		} else {
			moveCurrentToFuture();
			current.add(event);
		}
		return true;
	}

	@Override
	public SimEvent pop() {
		handleCurrent();
		if (current.size() == 0) {
			return null;
		}

		long totalWeight = 0;
		Iterator<SimEvent> it = current.iterator();
		DataPacket packet = null;
		while (it.hasNext()) {
			packet = (DataPacket) it.next().getData();
			totalWeight += ((Integer) packet.getData(DataPacket.FIELD_FIRING_WEIGHT)).intValue();
		}
		SimEvent event = null;
		long summedWeight = 0;
		long randomIndex = (long) (randomEngine.nextDouble() * totalWeight);
		it = current.iterator();
		while (summedWeight <= randomIndex) {
			event = it.next();
			packet = (DataPacket) event.getData();
			summedWeight += ((Integer) packet.getData(DataPacket.FIELD_FIRING_WEIGHT)).intValue();
		}
		it.remove();
		return event;
	}

	@Override
	public SimEvent peek() {
		handleCurrent();
		if (current.size() > 0) {
			return current.getFirst();
		} else {
			return null;
		}
	}

	@Override
	public boolean remove(SimEvent event) {
		handleCurrent();
		if (current.size() == 0) {
			return false;
		}

		if (comparator.compare(event, current.getFirst()) == 0) {
			return current.remove(event);
		} else {
			return future.remove(event);
		}
	}

	@Override
	public void clear() {
		current.clear();
		future.clear();
	}

	@Override
	public Iterator<SimEvent> iterator() {
		return new Iter();
	}

	private void moveCurrentToFuture() {
		while (current.size() > 0) {
			future.add(current.removeFirst());
		}
	}

	private void handleCurrent() {
		if (current.size() == 0 && future.size() > 0) {
			SimEvent first = future.remove();
			current.add(first);
			while (future.size() > 0 && comparator.compare(future.peek(), first) == 0) {
				current.add(future.remove());
			}
		}
	}

	private static class TimingEventComparator implements Comparator<SimEvent> {

		public int compare(SimEvent e1, SimEvent e2) {
			double time1 = e1.eventTime();
			double time2 = e2.eventTime();
			if (time1 > time2) {
				return 1;
			} else if (time1 < time2) {
				return -1;
			}
			DataPacket packet1 = (DataPacket) e1.getData();
			DataPacket packet2 = (DataPacket) e2.getData();
			double delay1 = ((Double) packet1.getData(DataPacket.FIELD_FIRING_DELAY)).doubleValue();
			double delay2 = ((Double) packet2.getData(DataPacket.FIELD_FIRING_DELAY)).doubleValue();
			if (delay1 > delay2) {
				return 1;
			} else if (delay1 < delay2) {
				return -1;
			}
			int priority1 = ((Integer) packet1.getData(DataPacket.FIELD_FIRING_PRIORITY)).intValue();
			int priority2 = ((Integer) packet2.getData(DataPacket.FIELD_FIRING_PRIORITY)).intValue();
			if (priority1 > priority2) {
				return -1;
			} else if (priority1 < priority2) {
				return 1;
			} else {
				return 0;
			}
		}

	}

	private class Iter implements Iterator<SimEvent> {

		private Iterator<SimEvent> currentIter = current.iterator();
		private Iterator<SimEvent> futureIter = future.iterator();
		private boolean isCurrent = true;

		public boolean hasNext() {
			return currentIter.hasNext() || futureIter.hasNext();
		}

		public SimEvent next() {
			if (currentIter.hasNext()) {
				isCurrent = true;
				return currentIter.next();
			} else {
				isCurrent = false;
				return futureIter.next();
			}
		}

		public void remove() {
			if (isCurrent) {
				currentIter.remove();
			} else {
				futureIter.remove();
			}
		}

	}

}
