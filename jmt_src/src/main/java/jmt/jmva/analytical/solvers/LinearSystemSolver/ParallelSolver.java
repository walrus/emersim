/**
 * Copyright (C) 2010, Michail Makaronidis
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

package jmt.jmva.analytical.solvers.LinearSystemSolver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class extends the Solver one and adds to it all useful fields and
 * methods a parallel solver should support, such as thread management code,
 * shutdown routines, etc.
 *
 * @author Michail Makaronidis, 2010
 */
public class ParallelSolver extends Solver {

    /**
     * The number of threads the ParallelSolver must use.
     */
    protected int nThreads;
    /**
     * The pool of worker threads where the parallel tasks are given for execution.
     */
    protected ExecutorService pool;

    /**
     * Initialises the ParallelSolver object and creates the worker threads.
     * @param nThreads The number of threads the ParallelSolver must use
     */
    public ParallelSolver(int nThreads) {
        this.nThreads = (nThreads <= 1) ? 1 : nThreads;
        pool = Executors.newFixedThreadPool(this.nThreads); // If no relevant argument is given by the user, nThreads = Runtime.getRuntime().availableProcessors()
    }

    /**
     * Shutsdown the solver, terminating all pools of threads and child processes
     * if any exist.
     */
    @Override
    public void shutdown() {
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(ParallelSolver.class.getName()).log(Level.SEVERE, null, ex);
            pool.shutdownNow();
        }
    }
}
