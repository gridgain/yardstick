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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkProbePoint;
import org.yardstickframework.BenchmarkServer;
import org.yardstickframework.BenchmarkServerProbe;
import org.yardstickframework.BenchmarkServerProbePointWriter;
import org.yardstickframework.writers.BenchmarkProbePointCsvWriter;

import static org.yardstickframework.BenchmarkUtils.errorHelp;
import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Set of configured benchmark server probes.
 */
public class BenchmarkServerProbeSet {
    /** Probe statistics dump frequency. */
    public static final int PROBE_DUMP_FREQ = 1_000;

    /** Writers. */
    private final Map<BenchmarkServerProbe, BenchmarkServerProbePointWriter> writers;

    /** Probes. */
    private final Collection<BenchmarkServerProbe> probes;

    /** Writer thread. */
    private Thread fileWriterThread;

    /** Benchmark configuration. */
    private final BenchmarkConfiguration cfg;

    /** Benchmark driver. */
    private final BenchmarkServer server;

    /** Loader. */
    private final BenchmarkLoader ldr;

    /**
     * @param server Benchmark server.
     * @param cfg Configuration.
     * @param probes Collection of probes.
     * @param ldr Loader.
     */
    public BenchmarkServerProbeSet(
        BenchmarkServer server,
        BenchmarkConfiguration cfg,
        Collection<BenchmarkServerProbe> probes,
        BenchmarkLoader ldr
    ) {
        this.cfg = cfg;
        this.server = server;
        this.ldr = ldr;
        this.probes = probes;

        writers = new HashMap<>(probes.size());
    }

    /**
     * @throws Exception If start failed.
     */
    public void start() throws Exception {
        String writerClsName = cfg.probeWriterClassName();

        if (writerClsName == null) {
            println(cfg, "Probe writer is not configured (using default CSV writer)");

            writerClsName = BenchmarkProbePointCsvWriter.class.getName();
        }

        boolean warn = true;

        long writersStartTime = System.currentTimeMillis();

        for (BenchmarkServerProbe probe : probes) {
            BenchmarkServerProbePointWriter writer =
                ldr.loadClass(BenchmarkServerProbePointWriter.class, writerClsName);

            if (writer == null) {
                if (warn) {
                    println(cfg, "Failed to load server writer class (will use default CSV writer): " + writerClsName);

                    warn = false;
                }

                writer = new BenchmarkProbePointCsvWriter();
            }

            writers.put(probe, writer);

            writer.start(server, cfg, writersStartTime);
        }

        try {
            for (BenchmarkServerProbe probe : writers.keySet())
                probe.start(server, cfg);
        }
        catch (Exception e) {
            stopProbes();

            throw e;
        }

        if (!probes.isEmpty()) {
            fileWriterThread = new Thread("probe-dump-thread") {
                @Override public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(PROBE_DUMP_FREQ);

                            for (Map.Entry<BenchmarkServerProbe, BenchmarkServerProbePointWriter> entry
                                : writers.entrySet()) {
                                BenchmarkServerProbe probe = entry.getKey();

                                BenchmarkServerProbePointWriter writer = entry.getValue();

                                Collection<BenchmarkProbePoint> points = probe.points();

                                try {
                                    writer.writePoints(probe, points);
                                }
                                catch (Exception e) {
                                    errorHelp(cfg, "Exception is raised during point write.", e);
                                }
                            }
                        }
                    }
                    catch (InterruptedException ignore) {
                        // No-op.
                    }
                    finally {
                        for (BenchmarkServerProbePointWriter writer : writers.values()) {
                            try {
                                writer.close();
                            }
                            catch (Exception e) {
                                errorHelp(cfg, "Failed to gracefully close probe writer " +
                                    "[writer=" + writer + ", err=" + e.getMessage() + ']', e);
                            }
                        }
                    }
                }
            };

            fileWriterThread.start();
        }
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
        for (BenchmarkServerProbe probe : writers.keySet()) {
            try {
                probe.stop();
            }
            catch (Exception e) {
                errorHelp(cfg, "Failed to gracefully stop probe [probe=" + probe + ", err=" + e.getMessage() + ']', e);
            }
        }
    }
}
