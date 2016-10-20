/**
 * Copyright (C) 2009, 2010, Michail Makaronidis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmt.jmva.analytical.solvers.Utilities;

/**
 *
 * The Timer object can receive "start" and "pause" commands. It can also
 * return the time interval in various formats.
 *
 *  @author Michail Makaronidis, 2009, 2010
 *
 */
public class Timer {

    /**
     *  Stores the respective timestamp.
     */
    private long startTime, stopTime, interval;

    /**
     * Creates and initialises a Timer object.
     */
    public Timer() {
        super();
        this.interval = 0;
    }

    /**
     * Creates and initialises a Timer object. The initial time interval is set
     * equal to the argument interval.
     * @param interval The desired interval in msecs
     */
    public Timer(long interval) {
        super();
        this.interval = interval;
    }

    /**
     * Starts the timer, i.e. sets the current time as the beginning of the
     * interval.
     *
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Stops the timer, i.e. sets the current time as the end of the interval.
     *
     */
    public void pause() {
        stopTime = System.currentTimeMillis();
        interval += stopTime - startTime;
    }

    /**
     * This method returns the sum of time intervals in msecs between every 
     * start and pause of the timer. The result is valid only if each start call
     * is followed by a subsequent pause call.
     *
     * @return  Time interval in msecs
     */
    public long getInterval() {
        return interval;
    }

    /**
     * This method returns the sum of time intervals between every start and
     * pause of the timer in a pretty form. The result is valid only if each
     * start call is followed by a subsequent pause call.
     *
     * @return  String representing the time interval in pretty form.
     */
    public String getPrettyInterval() {

        if (interval < 1000) {
            return (interval + " msec");
        } else if (interval < 60000) {
            return (interval / 1000.0 + " sec");
        } else {
            return ((interval / 60000) + " min " + ((interval - (interval / 60000) * 60000) / 1000.0) + " sec");
        }
    }
}
