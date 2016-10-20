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

import java.util.AbstractMap.SimpleEntry;

/**
 * <p>Title: Transition Entry</p>
 * <p>Description: This class implements the transition entry.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class TransitionEntry {

	private SimpleEntry<String, Integer> entry;

	public TransitionEntry(String key, Integer value) {
		if (value.intValue() < 0) {
			value = Integer.valueOf(0);
		}
		entry = new SimpleEntry<String, Integer>(key, value);
	}

	public String getKey() {
		return entry.getKey();
	}

	public Integer getValue() {
		return entry.getValue();
	}

	public void setValue(Integer value) {
		if (value.intValue() < 0) {
			value = Integer.valueOf(0);
		}
		entry.setValue(value);
	}

}
