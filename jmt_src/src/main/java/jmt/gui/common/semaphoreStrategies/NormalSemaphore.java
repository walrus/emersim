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

/**
 *
 * @author Vitor S. Lopes
 */
public class NormalSemaphore extends SemaphoreStrategy {

	private int semaphoreThres = 1;

	public NormalSemaphore() {
		description = "The semaphore stops a percentage of forked tasks in each class "
				+ "until they all reach the semaphore. Then it lets them pass through "
				+ "altogether. Tasks originated from the same job that arrive later are "
				+ "not blocked.";
	}

	@Override
	public String getName() {
		return "Standard Semaphore";
	}

	@Override
	public int getSemaphoreThres() {
		return semaphoreThres;
	}

	@Override
	public void setSemaphoreThres(int i) {
		semaphoreThres = i;
	}

	@Override
	public SemaphoreStrategy clone() {
		NormalSemaphore s = new NormalSemaphore();
		s.semaphoreThres = semaphoreThres;
		return s;
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.SemaphoreStrategies.NormalSemaphore";
	}

	@Override
	public boolean isModelStateDependent() {
		return false;
	}

}
