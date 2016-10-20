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

package jmt.engine.jaba;

import java.util.Vector;

import Jama.Matrix;

/**
 * Created by IntelliJ IDEA.
 * User: PoliMi
 * Date: 25-lug-2005
 * Time: 10.48.18
 * To change this template use File | Settings | File Templates.
 */
public class Beta2D {

	double EPSYLON = 0.0001; //costant for the approximations
        
	/**
	 * Given two points p1 and p2 the method return the betas of the saturation
         * sector by means of solving a system of linear equations
	 * @param p1
	 * @param p2
	 * @return a sector Sector2D with the coordinates in the space of the betas and the associated service demands
         */
	public Sector2D BetaCalc2D(newPoint p1, newPoint p2) {
		double[][] arraya = { { 1, 1, 0, 0 }, { 0, 0, 1, 1 }, { -p2.getX(), 0, p1.getX(), 0 }, { 0, p2.getY(), 0, -p1.getY() } };
		double[][] arrayb = { { 1 }, { 1 }, { 0 }, { 0 } };
		Matrix A = new Matrix(arraya);
		Matrix b = new Matrix(arrayb);
		Matrix x = A.solve(b);
		// Creates a new sector with the associates solutions and points
                Sector2D out = new Sector2D(x.get(0, 0), x.get(1, 0), x.get(2, 0), x.get(3, 0), p1, p2);
		return out;
	}

	/**
         * Given a vector of vertices returns a vector double[] representing
         * the betas and the points in the associated sector
	 * @param verticesnp    the vector of vertices
         * @return              a vector with the beta with the data structures in BetaCal2D
	 */
	public Vector<Sector2D> BetaVector(Vector<newPoint> verticesnp) {
		Vector<Sector2D> sector = new Vector<Sector2D>();
		for (int i = 0; i < (verticesnp.size() - 1); i++) {
			Sector2D out = new Sector2D();
			out = BetaCalc2D(verticesnp.get(i), verticesnp.get(i + 1));
			sector.addElement(out);
		}
		return sector;
	}

	/**
         * The method associates a vector passed from BetaVector to each sector 
         * a station, exploding in such a way to obtain also the sectors where
         * a single station saturates
	 * @param sector        a vettore passed by BetaVector
	 * @return              {b1,1-b1,b2,1-b2,xa,ya,xb,yb}
	 *                      if in the sector saturates a single station=>xb,yb=-1
	 */
	public Vector<Sector2D> StatAss(Vector<Sector2D> sector) {
		//Util2d uti = new Util2d();
		Vector<Sector2D> out = new Vector<Sector2D>();
		// Suppose to have 2 intervals [b1,1-b1]->[b2,1-b2] and [b3,1-b3]->[b4,1-b4]

		// if b1!=0 associate to station A the interval [0,1]->[b1,1-b1]
		if (sector.get(0).getBeta1() > EPSYLON) {
			Sector2D sss = new Sector2D(0, 1, sector.get(0).getBeta1(), sector.get(0).getBeta11(), sector.get(0).getP1());
			out.addElement(sss);
		}

		// Now proceed to scanning all intervals
                for (int i = 0; i < (sector.size()); i++) {

			// Normal sectors

			Sector2D sss = new Sector2D(sector.get(i).getBeta1(), sector.get(i).getBeta11(), sector.get(i).getBeta2(), sector.get(i).getBeta22(),
					sector.get(i).getP1(), sector.get(i).getP2());
			out.addElement(sss);

			// if the extremes b2 and b3 do not coincide (except EPSYLON) associate b2-->b3 to the station b2
			if (i != (sector.size() - 1) //TODO: check extension
					&& (sector.get(i + 1).getBeta1() - sector.get(i).getBeta2()) > EPSYLON) {
				Sector2D ss1 = new Sector2D(sector.get(i).getBeta2(), sector.get(i).getBeta22(), sector.get(i + 1).getBeta1(), sector.get(i + 1)
						.getBeta11(), sector.get(i).getP2());
				out.addElement(ss1);
			}

			//Associate the last station to the interval b4->1 if b4!=0
			else if (i == (sector.size() - 1) && sector.get(i).getBeta2() != 1) {
				Sector2D ssf = new Sector2D(sector.get(i).getBeta2(), sector.get(i).getBeta22(), 1, 0, sector.get(i).getP2()); //aggiungere ,end
				out.addElement(ssf);
			}

		}
		return out;
	}

}
