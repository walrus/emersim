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

package jmt.engine.NetStrategies.TransitionStrategies;

import java.util.HashMap;
import java.util.Set;

/**
 * <p>Title: Transition Vector</p>
 * <p>Description: This class implements the transition vector.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class TransitionVector {

	private HashMap<String, Integer> vector;

	public TransitionVector() {
		vector = new HashMap<String, Integer>();
	}

	public TransitionVector(TransitionEntry[] entries) {
		this();
		for (int i = 0; i < entries.length; i++) {
			vector.put(entries[i].getKey(), entries[i].getValue());
		}
	}

	public int size() {
		return vector.size();
	}

	public boolean isEmpty() {
		return vector.isEmpty();
	}

	public boolean containsKey(String key) {
		return vector.containsKey(key);
	}

	public Set<String> keySet() {
		return vector.keySet();
	}

	public Integer getValue(String key) {
		return vector.get(key);
	}

	public void setValue(String key, Integer value) {
		if (value.intValue() < 0) {
			value = Integer.valueOf(0);
		}
		vector.put(key, value);
	}

}
