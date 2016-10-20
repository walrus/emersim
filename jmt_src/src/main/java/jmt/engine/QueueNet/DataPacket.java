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

package jmt.engine.QueueNet;

import java.util.HashMap;
import java.util.Set;

/**
 * <p>Title: Data Packet</p>
 * <p>Description: This class implements the data packet.</p>
 *
 * @author Lulai Zhu
 * Date: 15-07-2016
 * Time: 21.00.00
 */
public class DataPacket {

	/** FIELD ID: Reference class field.*/
	public static final int FIELD_REFERENCE_CLASS = 0x0000;
	/** FIELD ID: Job number field.*/
	public static final int FIELD_JOB_NUMBER = 0x0001;
	/** FIELD ID: Enabling degree field.*/
	public static final int FIELD_ENABLING_DEGREE = 0x0002;
	/** FIELD ID: Firing delay field.*/
	public static final int FIELD_FIRING_DELAY = 0x0003;
	/** FIELD ID: Firing priority field.*/
	public static final int FIELD_FIRING_PRIORITY = 0x0004;
	/** FIELD ID: Firing weight field.*/
	public static final int FIELD_FIRING_WEIGHT = 0x0005;
	/** FIELD ID: Timing token field.*/
	public static final int FIELD_TIMING_TOKEN = 0x0006;
	/** FIELD ID: Total time field.*/
	public static final int FIELD_TOTAL_LIFETIME = 0x0007;

	private HashMap<Integer, Object> packet;

	public DataPacket() {
		packet = new HashMap<Integer, Object>(8, 1.0f);
	}

	public int size() {
		return packet.size();
	}

	public boolean isEmpty() {
		return packet.isEmpty();
	}

	public boolean containsField(int field) {
		return packet.containsKey(Integer.valueOf(field));
	}

	public Set<Integer> fieldSet() {
		return packet.keySet();
	}

	public Object getData(int field) {
		return packet.get(Integer.valueOf(field));
	}

	public void setData(int field, Object data) {
		packet.put(Integer.valueOf(field), data);
	}

}
