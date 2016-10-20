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

package jmt.gui.common.joinStrategies;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class JoinStrategy {

	protected static JoinStrategy[] all = null;
	protected static JoinStrategy[] allForSource = null;

	protected String description;

	public abstract String getName();

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return getName();
	}

	public abstract int getRequiredNum();

	public abstract void setRequiredNum(int i);

	@Override
	public abstract JoinStrategy clone();

	@Override
	public boolean equals(Object o) {
		if (o instanceof JoinStrategy) {
			return this.getName().equals(((JoinStrategy) o).getName());
		} else {
			return false;
		}
	}

	public abstract String getClassPath();

	public static JoinStrategy[] findAll() {
		if (all != null) {
			return all;
		}
		ArrayList<JoinStrategy> strategies = new ArrayList<JoinStrategy>();
		Field[] fields = jmt.gui.jsimgraph.JSimGraphConstants.class.getFields();
		try {
			for (Field field : fields) {
				if (field.getName().startsWith("JOINING_")) {
					strategies.add((JoinStrategy) field.get(null));
				}
			}
		} catch (IllegalAccessException ex) {
			System.err.println("A security manager has blocked reflection");
			ex.printStackTrace();
		}
		JoinStrategy[] ret = new JoinStrategy[strategies.size()];
		for (int i = 0; i < strategies.size(); i++) {
			ret[i] = strategies.get(i);
		}
		all = ret;
		return ret;
	}

	//TODO:modify this piece of code
	public static JoinStrategy[] findAllForSource() {
		if (allForSource != null) {
			return allForSource;
		}
		ArrayList<JoinStrategy> strategies = new ArrayList<JoinStrategy>();
		Field[] fields = jmt.gui.jsimgraph.JSimGraphConstants.class.getFields();
		try {
			for (Field field : fields) {
				if (field.getName().startsWith("Join_") && !field.getName().endsWith("LOADDEPENDANT") ) {
					strategies.add((JoinStrategy) field.get(null));
				}
			}
		} catch (IllegalAccessException ex) {
			System.err.println("A security manager has blocked reflection");
			ex.printStackTrace();
		}
		JoinStrategy[] ret = new JoinStrategy[strategies.size()];
		for (int i = 0; i < strategies.size(); i++) {
			ret[i] = strategies.get(i);
		}
		allForSource = ret;
		return ret;
	}

	public abstract boolean isModelStateDependent();

}
