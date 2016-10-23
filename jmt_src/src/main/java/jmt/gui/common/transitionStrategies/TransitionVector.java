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

package jmt.gui.common.transitionStrategies;

import java.util.HashMap;

/**
 * <p>Title: Transition Vector</p>
 * <p>Description: This class implements the transition vector.</p>
 *
 * @author Lulai Zhu
 * Date: 24-06-2016
 * Time: 16.00.00
 */
public class TransitionVector {

	private HashMap<Object, Integer> vector;

	public TransitionVector() {
		vector = new HashMap<Object, Integer>();
	}

	public Integer getValue(Object key) {
		if (!vector.containsKey(key)) {
			return Integer.valueOf(0);
		}
		return vector.get(key);
	}

	public void setValue(Object key, Integer value) {
		if (value.intValue() <= 0) {
			vector.remove(key);
			return;
		}
		vector.put(key, value);
	}

}
