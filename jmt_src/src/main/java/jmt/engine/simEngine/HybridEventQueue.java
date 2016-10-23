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

package jmt.engine.simEngine;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import jmt.framework.data.CircularList;

/**
 * <p><b>Name:</b> HybridEventQueue</p> 
 * <p><b>Description:</b> 
 * An hybrid implementation for the EventQueue interface. Future events are inserted and retrieved in a sorted
 * binary heap with log(n) performances, while events for the current time interval are inserted and retrieved
 * using an unbounded circular list. 
 * </p>
 * <p><b>Date:</b> 12/mag/2009
 * <b>Time:</b> 18:40:26</p>
 * @author Bertoli Marco
 * @version 1.1
 */
public class HybridEventQueue implements EventQueue {

	private final static int DEFAULT_INITIAL_CAPACITY = 111;

	/** Current events */
	private CircularList<SimEvent> current;
	/** Future events*/
	private PriorityQueue<SimEvent> future;
	/** Current time. All events in current event queue will have this time. */
	private double currentTime;
	/** A counter used to order future events basing on event time and insertion order */
	private int order;

	public HybridEventQueue() {
		clear();
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#add(jmt.engine.simEngine.SimEvent)
	 */
	public boolean add(SimEvent event) {
		double eventTime = event.eventTime();
		if (eventTime == currentTime) {
			current.add(event);
		} else if (eventTime > currentTime) {
			addToFuture(event);
		} else {
			// Probably this condition will never happen but we are robust to it. We leave to the simulation
			// engine the choice to deal with past events.
			moveCurrentToFuture();
			current.add(event);
			currentTime = eventTime;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#clear()
	 */
	public void clear() {
		current = new CircularList<SimEvent>();
		future = new PriorityQueue<SimEvent>(DEFAULT_INITIAL_CAPACITY, new SimEventComparator());
		currentTime = 0.0;
		order = Integer.MIN_VALUE;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#iterator()
	 */
	public Iterator<SimEvent> iterator() {
		return new Iter();
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#peek()
	 */
	public SimEvent peek() {
		handleCurrent();
		if (current.size() > 0) {
			return current.getFirst();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#pop()
	 */
	public SimEvent pop() {
		handleCurrent();
		if (current.size() > 0) {
			return current.removeFirst();
		} else {
			return null;
		}
	}

	/**
	 * This method will move events from future to current queue if it is empty.
	 */
	private void handleCurrent() {
		if (current.size() == 0 && future.size() > 0) {
			SimEvent first = future.remove();
			current.add(first);
			currentTime = first.eventTime();
			while (future.size() > 0 && future.peek().eventTime() == currentTime) {
				current.add(future.remove());
			}
		}
	}

	/**
	 * This method will move the entire current queue to future. This is needed when an event 
	 * older than current one is queued. Probably will never happen because the simulator currently 
	 * avoid this.
	 */
	private void moveCurrentToFuture() {
		while (current.size() > 0) {
			addToFuture(current.removeFirst());
		}
	}

	/**
	 * This method will build a new future buffer with new order indices. As the total number of indices is
	 * 2^32 this method probably will never be called.
	 */
	private void rebuildOrderIndices() {
		Queue<SimEvent> tmp = future;
		future = new PriorityQueue<SimEvent>(DEFAULT_INITIAL_CAPACITY, new SimEventComparator());
		order = Integer.MIN_VALUE;
		while (tmp.size() > 0) {
			addToFuture(tmp.remove());
		}
	}

	/**
	 * Adds an event to future queue.
	 * @param event
	 */
	private void addToFuture(SimEvent event) {
		if (order == Integer.MAX_VALUE) {
			// Need to rebuild future events queue because order indices were finished. Probably this case will never happen
			// but we are robust to it.
			rebuildOrderIndices();
		}
		event.internalOrdering = order++;
		future.add(event);
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#remove(jmt.engine.simEngine.SimEvent)
	 */
	public boolean remove(SimEvent event) {
		if (event.eventTime() == currentTime) {
			return current.remove(event);
		} else {
			return future.remove(event);
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#size()
	 */
	public int size() {
		return current.size() + future.size();
	}

	/**
	 * Internal Iterator implementation
	 */
	private class Iter implements Iterator<SimEvent> {
		private Iterator<SimEvent> currentIter = current.iterator();
		private Iterator<SimEvent> futureIter = future.iterator();
		private boolean isCurrent = true;

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return currentIter.hasNext() || futureIter.hasNext();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public SimEvent next() {
			if (currentIter.hasNext()) {
				isCurrent = true;
				return currentIter.next();
			} else {
				isCurrent = false;
				return futureIter.next();
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			if (isCurrent) {
				currentIter.remove();
			} else {
				futureIter.remove();
			}
		}

	}

	private static class SimEventComparator implements Comparator<SimEvent> {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(SimEvent e1, SimEvent e2) {
			if (e1.eventTime() > e2.eventTime()) {
				return 1;
			} else if (e1.eventTime() < e2.eventTime()) {
				return -1;
			} else {
				if (e1.internalOrdering > e2.internalOrdering) {
					return 1;
				} else if (e1.internalOrdering < e2.internalOrdering) {
					return -1;
				} else {
					return 0;
				}
			}
		}

	}

}
