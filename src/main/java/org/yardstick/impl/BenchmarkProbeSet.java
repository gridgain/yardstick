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
import org.yardstick.writers.*;

import java.util.*;

import static org.yardstick.BenchmarkUtils.*;

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

    /** Benchmark context. */
    private final BenchmarkConfiguration cfg;

    /** Loader. */
    private final BenchmarkLoader ldr;

    /** Flag indicating whether warmup is finished or not. */
    private volatile boolean warmupFinished;

    /**
     * @param cfg Context.
     * @param probes Collection of probes.
     * @param ldr Loader.
     */
    public BenchmarkProbeSet(BenchmarkConfiguration cfg, Collection<BenchmarkProbe> probes, BenchmarkLoader ldr) {
        this.cfg = cfg;
        this.ldr = ldr;
        this.probes = probes;
        this.writers = new HashMap<>(probes.size());
        this.execProbes = new ArrayList<>(probes.size());
    }

    /**
     * @throws Exception If start failed.
     */
    @SuppressWarnings("BusyWait")
    public void start() throws Exception {
        String writerClsName = cfg.probeWriterClassName();

        if (writerClsName == null) {
            println(cfg, "Probe writer is not configured, using default CSV writer.");

            writerClsName = BenchmarkProbePointCsvWriter.class.getName();
        }

        boolean warn = true;

        long writersStartTime = System.currentTimeMillis();

        for (BenchmarkProbe probe : probes) {
            BenchmarkProbePointWriter writer = ldr.loadBenchmarkClass(BenchmarkProbePointWriter.class, writerClsName);

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

            writer.start(cfg, writersStartTime);
        }

        try {
            for (BenchmarkProbe probe : writers.keySet())
                probe.start(cfg);
        }
        catch (Exception e) {
            stopProbes();

            throw e;
        }

        fileWriterThread = new Thread("probe-dump-thread") {
            @Override public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(PROBE_DUMP_FREQ);

                        boolean warmupFinished0 = warmupFinished;

                        for (Map.Entry<BenchmarkProbe, BenchmarkProbePointWriter> entry : writers.entrySet()) {
                            BenchmarkProbe probe = entry.getKey();
                            BenchmarkProbePointWriter writer = entry.getValue();

                            Collection<BenchmarkProbePoint> points = probe.points();

                            if (warmupFinished0) {
                                try {
                                    writer.writePoints(probe, points);
                                }
                                catch (Exception e) {
                                    errorHelp(cfg, "Exception is raised during point write.", e);
                                }
                            }
                        }
                    }
                }
                catch (InterruptedException ignore) {
                    // No-op.
                }
                finally {
                    for (Map.Entry<BenchmarkProbe, BenchmarkProbePointWriter> entry : writers.entrySet()) {
                        try {
                            entry.getValue().close();
                        }
                        catch (Exception e) {
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
     * Warmup finished callback.
     */
    public void onWarmupFinished() {
        warmupFinished = true;
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
