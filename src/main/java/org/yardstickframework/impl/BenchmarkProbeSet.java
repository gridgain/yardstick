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
import org.yardstickframework.writers.*;

import java.util.*;
import java.util.concurrent.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Set of configured benchmark probes.
 */
public class BenchmarkProbeSet {
    /** Probe statistics dump frequency. */
    public static final int PROBE_DUMP_FREQ = 1_000;

    /** Writers. */
    private final Map<BenchmarkProbe, BenchmarkProbePointWriter> writers;

    /** Probes. */
    private final Collection<BenchmarkProbe> probes;

    /** Execution aware probes. */
    private final List<BenchmarkExecutionAwareProbe> execProbes;

    /** Writer thread. */
    private Thread fileWriterThread;

    /** Benchmark configuration. */
    private final BenchmarkConfiguration cfg;

    /** Benchmark driver. */
    private final BenchmarkDriver driver;

    /** Loader. */
    private final BenchmarkLoader ldr;

    /** Latch indicating whether warmup is finished or not. */
    private final CountDownLatch warmupFinished = new CountDownLatch(1);

    /** Flag indicating whether benchmark time is over or not. */
    private volatile boolean finished;

    /**
     * @param driver Benchmark driver.
     * @param cfg Configuration.
     * @param probes Collection of probes.
     * @param ldr Loader.
     */
    public BenchmarkProbeSet(
        BenchmarkDriver driver,
        BenchmarkConfiguration cfg,
        Collection<BenchmarkProbe> probes,
        BenchmarkLoader ldr
    ) {
        this.cfg = cfg;
        this.driver = driver;
        this.ldr = ldr;
        this.probes = probes;

        writers = new HashMap<>(probes.size());
        execProbes = new ArrayList<>(probes.size());
    }

    /**
     * @throws Exception If start failed.
     */
    @SuppressWarnings("BusyWait")
    public void start() throws Exception {
        String writerClsName = cfg.probeWriterClassName();

        if (writerClsName == null) {
            println(cfg, "Probe writer is not configured (using default CSV writer)");

            writerClsName = BenchmarkProbePointCsvWriter.class.getName();
        }

        boolean warn = true;

        long writersStartTime = System.currentTimeMillis();

        for (BenchmarkProbe probe : probes) {
            BenchmarkProbePointWriter writer = ldr.loadClass(BenchmarkProbePointWriter.class, writerClsName);

            if (writer == null) {
                if (warn) {
                    println(cfg, "Failed to load writer class (will use default CSV writer): " + writerClsName);

                    warn = false;
                }

                writer = new BenchmarkProbePointCsvWriter();
            }

            writers.put(probe, writer);

            if (probe instanceof BenchmarkExecutionAwareProbe)
                execProbes.add((BenchmarkExecutionAwareProbe)probe);

            writer.start(driver, cfg, writersStartTime);
        }

        try {
            for (BenchmarkProbe probe : writers.keySet())
                probe.start(driver, cfg);
        }
        catch (Exception e) {
            stopProbes();

            throw e;
        }

        if (!probes.isEmpty()) {
            fileWriterThread = new Thread("probe-dump-thread") {
                @Override
                public void run() {
                    try {
                        warmupFinished.await();

                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(PROBE_DUMP_FREQ);

                            for (Map.Entry<BenchmarkProbe, BenchmarkProbePointWriter> entry : writers.entrySet()) {
                                BenchmarkProbe probe = entry.getKey();

                                if (probe instanceof BenchmarkTotalsOnlyProbe)
                                    continue;

                                BenchmarkProbePointWriter writer = entry.getValue();

                                Collection<BenchmarkProbePoint> points = probe.points();

                                try {
                                    writer.writePoints(probe, points);
                                } catch (Exception e) {
                                    errorHelp(cfg, "Exception is raised during point write.", e);
                                }
                            }

                            if (finished)
                                break;
                        }
                    }
                    catch (InterruptedException ignore) {
                        // No-op.
                    }
                    finally {
                        for (Map.Entry<BenchmarkProbe, BenchmarkProbePointWriter> entry : writers.entrySet()) {
                            BenchmarkProbe probe = entry.getKey();

                            if (probe instanceof BenchmarkTotalsOnlyProbe) {
                                BenchmarkProbePointWriter writer = entry.getValue();

                                try {
                                    writer.writePoints(probe, probe.points());
                                } catch (Exception e) {
                                    errorHelp(cfg, "Exception is raised during point write.", e);
                                }
                            }

                            try {
                                entry.getValue().close();
                            } catch (Exception e) {
                                errorHelp(cfg, "Failed to gracefully close probe writer " +
                                    "[probe=" + entry.getKey() + ", writer=" + entry.getValue() +
                                    ", err=" + e.getMessage() + ']', e);
                            }
                        }
                    }
                }
            };

            fileWriterThread.start();
        }
    }

    /**
     * Before benchmark test iteration execute callback.
     *
     * @param threadIdx Executor thread index.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void onBeforeExecute(int threadIdx) {
        // Avoid iterator creation.
        for (int i = 0; i < execProbes.size(); i++)
            execProbes.get(i).beforeExecute(threadIdx);
    }

    /**
     * After benchmark test iteration execute callback.
     *
     * @param threadIdx Executor thread index.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void onAfterExecute(int threadIdx) {
        // Avoid iterator creation.
        for (int i = 0; i < execProbes.size(); i++)
            execProbes.get(i).afterExecute(threadIdx);
    }

    /**
     * Notifies probes to build a point. This method is invoked periodically with given interval.
     *
     * @param time Time of the point.
     */
    public void buildPoint(long time) {
        for (BenchmarkProbe probe : probes)
            probe.buildPoint(time);
    }

    /**
     * Warmup finished callback.
     */
    public void onWarmupFinished() {
        for (Map.Entry<BenchmarkProbe, BenchmarkProbePointWriter> entry : writers.entrySet()) {
            BenchmarkProbe probe = entry.getKey();

            probe.points();
        }

        warmupFinished.countDown();
    }

    /**
     * Benchmark time is over callback.
     */
    public void onFinished() {
        finished = true;
    }

    /**
     * @throws Exception If failed.
     */
    public void stop() throws Exception {
        if (fileWriterThread != null) {
            fileWriterThread.interrupt();

            fileWriterThread.join();
        }

        stopProbes();
    }

    /**
     *
     */
    private void stopProbes() {
        for (BenchmarkProbe probe : writers.keySet()) {
            try {
                probe.stop();
            }
            catch (Exception e) {
                errorHelp(cfg, "Failed to gracefully stop probe [probe=" + probe + ", err=" + e.getMessage() + ']', e);
            }
        }
    }
}
