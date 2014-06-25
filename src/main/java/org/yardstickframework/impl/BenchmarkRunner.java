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

package org.yardstickframework.impl;

import org.yardstickframework.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Benchmark runner. Starts benchmarking threads, manages lifecycle.
 */
public class BenchmarkRunner {
    /** Benchmark arguments. */
    private final BenchmarkConfiguration cfg;

    /** Benchmark drivers. */
    private final BenchmarkDriver[] drivers;

    /** Cancelled flag. */
    private volatile boolean cancelled;

    /** Shutdown thread. */
    private ShutdownThread shutdownThread;

    /** Started threads. */
    private volatile Collection<Thread> threads;

    /** List of probe sets. */
    private final BenchmarkProbeSet[] probeSets;

    /** List of weights. */
    private final int[] weights;

    /** Execution error. */
    private volatile Throwable err;

    /**
     * @param cfg Benchmark arguments.
     * @param drivers Drivers.
     * @param probeSets Probe sets.
     * @param weights Driver run weights.
     */
    public BenchmarkRunner(
        BenchmarkConfiguration cfg,
        BenchmarkDriver[] drivers,
        BenchmarkProbeSet[] probeSets,
        int[] weights
    ) {
        this.cfg = cfg;
        this.drivers = drivers;
        this.probeSets = probeSets;
        this.weights = weights;
    }

    /**
     * @throws Exception If failed.
     */
    public synchronized void runBenchmark() throws Exception {
        final int threadNum = cfg.threads();

        threads = new ArrayList<>(threadNum);

        final AtomicInteger finished = new AtomicInteger(0);

        for (BenchmarkProbeSet probeSet : probeSets)
            probeSet.start();

        final long testStart = System.currentTimeMillis();

        final long totalDuration = cfg.duration() + cfg.warmup();

        final CountDownLatch barrierActionFinished = new CountDownLatch(1);

        final CyclicBarrier barrier = new CyclicBarrier(threadNum, new Runnable() {
            @Override public void run() {
                for (BenchmarkDriver drv : drivers)
                    drv.onWarmupFinished();

                for (BenchmarkProbeSet set : probeSets)
                    set.onWarmupFinished();

                barrierActionFinished.countDown();
            }
        });

        for (int i = 0; i < threadNum; i++) {
            final int threadIdx = i;

            threads.add(new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        Random rand = new Random();

                        int sumWeight = 0;

                        for (Integer w : weights)
                            sumWeight += w;

                        Map<Object, Object> ctx = new HashMap<>();

                        // To avoid CAS on each benchmark iteration.
                        boolean reset = true;

                        while (!cancelled && !Thread.currentThread().isInterrupted()) {
                            int idx = driverIndex(rand, sumWeight);

                            BenchmarkDriver drv = drivers[idx];

                            BenchmarkProbeSet probeSet = probeSets[idx];

                            probeSet.onBeforeExecute(threadIdx);

                            // Execute benchmark code.
                            if (!drv.test(ctx)) {
                                for (BenchmarkProbeSet set : probeSets)
                                    set.onFinished();

                                break;
                            }

                            probeSet.onAfterExecute(threadIdx);

                            long now = System.currentTimeMillis();

                            long elapsed = (now - testStart) / 1_000;

                            if (reset && elapsed > cfg.warmup()) {
                                barrier.await();

                                barrierActionFinished.await();

                                reset = false;
                            }

                            if (elapsed > totalDuration) {
                                for (BenchmarkProbeSet set : probeSets)
                                    set.onFinished();

                                break;
                            }
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
     * @param rand Random.
     * @param sumWeight Sum of weights.
     * @return Driver index.
     * @throws Exception If failed.
     */
    private int driverIndex(Random rand, int sumWeight) throws Exception {
        int len = weights.length;

        if (len == 1)
            return 0;

        int w = rand.nextInt(sumWeight);

        int sum = 0;

        for (int i = 0; i < len; i++) {
            sum += weights[i];

            if (sum > w)
                return i;
        }

        throw new Exception("Can not reach here.");
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
            if (err != null)
                errorHelp(cfg, "Shutting down benchmark driver to unexpected exception.", err);

            try {
                for (Thread t : threads)
                    t.join();

                for (int i = 0; i < drivers.length; i++) {
                    drivers[i].tearDown();
                    probeSets[i].stop();
                }
            }
            catch (Exception e) {
                errorHelp(cfg, "Failed to gracefully shutdown benchmark runner.", e);
            }
        }
    }
}
