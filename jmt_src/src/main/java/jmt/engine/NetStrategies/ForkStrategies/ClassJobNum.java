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

package jmt.engine.NetStrategies.ForkStrategies;

/**
 *
 * @author mattia
 */
public class ClassJobNum {

	private String name;
	private Integer[] numbers;
	private String[] classes;

	public ClassJobNum(String name, String[] classes, Integer[] numbers) {
		this.name = name;
		this.numbers = numbers;
		this.classes = classes;
	}

	public ClassJobNum(String name, String[] classes, String[] numbers) {
		this.name = name;
		this.numbers = new Integer[numbers.length];
		for (int i = 0; i < numbers.length; i++)
			this.numbers[i] = Integer.parseInt(numbers[i]);
		this.classes = classes;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the numbers
	 */
	public Integer[] getNumbers() {
		return numbers.clone();
	}

	/**
	 * @return the classes
	 */
	public String[] getClasses() {
		return classes.clone();
	}

}
