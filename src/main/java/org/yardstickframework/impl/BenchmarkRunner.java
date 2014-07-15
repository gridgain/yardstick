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
    /** */
    public static final String INTERVAL = "BENCHMARK_BUILD_PROBE_POINT_INTERVAL";

    /** */
    public static final long DEFAULT_INTERVAL_IN_MSECS = 1_000;

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

    /** Thread building probe points. */
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private Thread buildingThread;

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

        startBuildingThread();

        final long testStart = System.currentTimeMillis();

        final long totalDuration = cfg.duration() + cfg.warmup();

        final int sumWeight = sumWeights();

        final CyclicBarrier barrier = new CyclicBarrier(threadNum, new Runnable() {
            @Override public void run() {
                for (BenchmarkDriver drv : drivers)
                    drv.onWarmupFinished();

                for (BenchmarkProbeSet set : probeSets)
                    set.onWarmupFinished();

                BenchmarkUtils.println("Starting main test (warmup finished).");
            }
        });

        final AtomicLong opsCnt = cfg.operationsCount() <= 0 ? null : new AtomicLong();

        for (int i = 0; i < threadNum; i++) {
            final int threadIdx = i;

            threads.add(new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        Random rand = new Random();

                        Map<Object, Object> ctx = new HashMap<>();

                        // To avoid CAS on each benchmark iteration.
                        boolean reset = true;

                        while (!cancelled && !Thread.currentThread().isInterrupted()) {
                            int idx = driverIndex(rand, sumWeight);

                            BenchmarkDriver drv = drivers[idx];

                            BenchmarkProbeSet probeSet = probeSets[idx];

                            probeSet.onBeforeExecute(threadIdx);

                            // Execute benchmark code.
                            boolean res = drv.test(ctx);

                            probeSet.onAfterExecute(threadIdx);

                            if (!res) {
                                for (BenchmarkProbeSet set : probeSets)
                                    set.onFinished();

                                break;
                            }

                            long now = System.currentTimeMillis();

                            long elapsed = (now - testStart) / 1_000;

                            if (reset && elapsed > cfg.warmup()) {
                                barrier.await();

                                reset = false;

                                continue;
                            }

                            if (!reset && cfg.operationsCount() > 0) {
                                long ops = opsCnt.incrementAndGet();

                                if (ops % 25000 == 0)
                                    BenchmarkUtils.println("Finished iteration: " + ops);

                                if (ops >= cfg.operationsCount()) {
                                    for (BenchmarkProbeSet set : probeSets)
                                        set.onFinished();

                                    break;
                                }
                            }
                            else if (!reset && elapsed > totalDuration) {
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
     *
     */
    private void startBuildingThread() {
        final long interval = interval(cfg);

        buildingThread = new Thread() {
            @SuppressWarnings("BusyWait")
            @Override public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        long time = System.currentTimeMillis();

                        for (BenchmarkProbeSet probeSet : probeSets)
                            probeSet.buildPoint(time);

                        Thread.sleep(interval);
                    }
                }
                catch (InterruptedException ignore) {
                    // No-op.
                }
            }
        };

        buildingThread.start();
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
     * @return Sum of weights.
     */
    private int sumWeights() {
        int sumWeight = 0;

        for (int w : weights)
            sumWeight += w;

        return sumWeight;
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
            long now = System.currentTimeMillis();

            System.out.println("Finishing main test " +
                "[ts=" + now + ", date=" + new Date(now) + ']');

            shutdownThread = new ShutdownThread();

            shutdownThread.start();
        }
    }

    /**
     * @param cfg Config.
     * @return Interval.
     */
    private static long interval(BenchmarkConfiguration cfg) {
        try {
            return Long.parseLong(cfg.customProperties().get(INTERVAL));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_INTERVAL_IN_MSECS;
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
                if (buildingThread != null) {
                    buildingThread.interrupt();
                    buildingThread.join();
                }

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
