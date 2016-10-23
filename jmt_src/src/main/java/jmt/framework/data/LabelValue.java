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

package jmt.framework.data;

/**
 * <p><b>Name:</b> LabelValue</p> 
 * <p><b>Description:</b> 
 * A simple immutable label-value pair
 * </p>
 * <p><b>Date:</b> 2014-07-20
 * @author Bertoli Marco
 * @version 1.0
 */
public class LabelValue {
	private String label;
	private String value;
	
	/**
	 * Builds a label value with the given label and value
	 * @param label the label
	 * @param value the value
	 */
	public LabelValue(String label, String value) {
		this.label = label;
		this.value = value;
	}
	
	/**
	 * Builds a label value with the given value used as label too
	 * @param value the value and label
	 */
	public LabelValue(String value) {
		this(value, value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LabelValue) {
			return ((LabelValue) obj).value.equals(value);
		}
		return false;
	}

	@Override
	public LabelValue clone() {
		// Return itself since it is immutable.
		return this;
	}

	@Override
	public String toString() {
		return label;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Return an element chosen in a list of LabelValue elements
	 * @param elements the element
	 * @param value the value to search
	 * @return the found value or null if not found
	 */
	public static LabelValue getElement(LabelValue[] elements, String value) {
		for (LabelValue e : elements) {
			if (value == e.getValue()) {
				return e;
			} else if (value != null && value.equals(e.getValue())) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Return an element chosen in a list of LabelValue elements
	 * @param elements the element
	 * @param value the value to search
	 * @return the found value or null if not found
	 */
	public static LabelValue getElement(Iterable<LabelValue> elements, String value) {
		for (LabelValue e : elements) {
			if (value == e.getValue()) {
				return e;
			} else if (value != null && value.equals(e.getValue())) {
				return e;
			}
		}
		return null;
	}
	
	
	
}
