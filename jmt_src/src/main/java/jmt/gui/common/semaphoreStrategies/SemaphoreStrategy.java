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

package jmt.gui.common.semaphoreStrategies;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 *
 * @author Vitor S. Lopes
 */
public abstract class SemaphoreStrategy {

	protected static SemaphoreStrategy[] all = null;
	protected static SemaphoreStrategy[] allForSource = null;

	protected String description;

	public abstract String getName();

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return getName();
	}

	public abstract int getSemaphoreThres();

	public abstract void setSemaphoreThres(int i);

	@Override
	public abstract SemaphoreStrategy clone();

	@Override
	public boolean equals(Object o) {
		if (o instanceof SemaphoreStrategy) {
			return this.getName().equals(((SemaphoreStrategy) o).getName());
		} else {
			return false;
		}
	}

	public abstract String getClassPath();

	public static SemaphoreStrategy[] findAll() {
		if (all != null) {
			return all;
		}
		ArrayList<SemaphoreStrategy> strategies = new ArrayList<SemaphoreStrategy>();
		Field[] fields = jmt.gui.jsimgraph.JSimGraphConstants.class.getFields();
		try {
			for (Field field : fields) {
				if (field.getName().startsWith("SEMAPHORE_")) {
					strategies.add((SemaphoreStrategy) field.get(null));
				}
			}
		} catch (IllegalAccessException ex) {
			System.err.println("A security manager has blocked reflection");
			ex.printStackTrace();
		}
		SemaphoreStrategy[] ret = new SemaphoreStrategy[strategies.size()];
		for (int i = 0; i < strategies.size(); i++) {
			ret[i] = strategies.get(i);
		}
		all = ret;
		return ret;
	}

	//TODO:modify this piece of code
	public static SemaphoreStrategy[] findAllForSource() {
		if (allForSource != null) {
			return allForSource;
		}
		ArrayList<SemaphoreStrategy> strategies = new ArrayList<SemaphoreStrategy>();
		Field[] fields = jmt.gui.jsimgraph.JSimGraphConstants.class.getFields();
		try {
			for (Field field : fields) {
				if (field.getName().startsWith("Semaphore_") && !field.getName().endsWith("LOADDEPENDANT") ) {
					strategies.add((SemaphoreStrategy) field.get(null));
				} 
			}
		} catch (IllegalAccessException ex) {
			System.err.println("A security manager has blocked reflection");
			ex.printStackTrace();
		}
		SemaphoreStrategy[] ret = new SemaphoreStrategy[strategies.size()];
		for (int i = 0; i < strategies.size(); i++) {
			ret[i] = strategies.get(i);
		}
		allForSource = ret;
		return ret;
	}

	public abstract boolean isModelStateDependent();

}
