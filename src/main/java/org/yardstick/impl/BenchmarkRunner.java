/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstick.impl;

import org.yardstick.*;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Benchmark runner. Starts benchmarking threads, manages lifecycle.
 */
public class BenchmarkRunner {
    /** Benchmark arguments. */
    private final BenchmarkConfiguration cfg;

    /** Benchmark driver. */
    private final BenchmarkDriver drv;

    /** Cancelled flag. */
    private volatile boolean cancelled;

    /** Shutdown thread. */
    private ShutdownThread shutdownThread;

    /** Started threads. */
    private volatile Collection<Thread> threads;

    /** Probes. */
    private final BenchmarkProbeSet probeSet;

    /** Execution error. */
    private volatile Throwable err;

    /**
     * @param cfg Benchmark arguments.
     * @param drv Driver.
     * @param probeSet Probe set.
     */
    public BenchmarkRunner(BenchmarkConfiguration cfg, BenchmarkDriver drv, BenchmarkProbeSet probeSet) {
        this.cfg = cfg;
        this.drv = drv;

        this.probeSet = probeSet;
    }

    /**
     * @throws Exception If failed.
     */
    public synchronized void runBenchmark() throws Exception {
        final int threadNum = cfg.threads();

        threads = new ArrayList<>(threadNum);

        final AtomicBoolean warmupFinished = new AtomicBoolean(false);

        final AtomicInteger finished = new AtomicInteger(0);

        probeSet.start();

        final long testStart = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            final int threadIdx = i;

            threads.add(new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        long totalDuration = cfg.duration() + cfg.warmup();

                        // To avoid CAS on each benchmark iteration.
                        boolean reset = true;

                        while (!cancelled && !Thread.currentThread().isInterrupted()) {
                            probeSet.onBeforeExecute(threadIdx);

                            // Execute benchmark code.
                            drv.test();

                            probeSet.onAfterExecute(threadIdx);

                            long now = System.currentTimeMillis();

                            long elapsed = (now - testStart) / 1_000;

                            if (reset && elapsed > cfg.warmup()) {
                                if (warmupFinished.compareAndSet(false, true))
                                    probeSet.onWarmupFinished();

                                reset = false;
                            }

                            if (elapsed > totalDuration)
                                break;
                        }

                        // Either interrupted, or cancelled.
                        if (finished.incrementAndGet() == threadNum)
                            shutdown();
                    }
                    catch (Throwable e) {
                        // Stop whole benchmark execution.
                        cancel(e);
                    }
                }
            }, "benchmark-worker-" + i));
        }

        for (Thread t : threads)
            t.start();
    }

    /**
     *
     */
    public void cancel() {
        cancel(null);
    }

    /**
     * @param e Throwable.
     */
    private synchronized void cancel(Throwable e) {
        if (!cancelled) {
            err = e;

            cancelled = true;

            shutdown();
        }
    }

    /**
     *
     */
    private synchronized void shutdown() {
        if (shutdownThread == null) {
            shutdownThread = new ShutdownThread();

            shutdownThread.start();
        }
    }

    /**
     *
     */
    private class ShutdownThread extends Thread {
        /** {@inheritDoc} */
        @Override public void run() {
            if (err != null) {
                cfg.error().println("ERROR: Shutting down benchmark driver to unexpected exception: ");

                err.printStackTrace(cfg.error());
            }

            try {
                for (Thread t : threads)
                    t.join();

                drv.tearDown();

                probeSet.stop();
            }
            catch (Exception e) {
                cfg.error().println("ERROR: Failed to gracefully shutdown benchmark runner.");

                e.printStackTrace(cfg.error());
            }
        }
    }
}
