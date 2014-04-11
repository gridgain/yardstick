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

package org.yardstick.probes;

import org.yardstick.*;

import java.util.*;

/**
* Probe that calculates throughput and average latency.
*/
public class ThroughputLatencyProbe implements BenchmarkExecutionAwareProbe {
    /** */
    public static final String THROUGHPUT_LATENCY_PROBE_INVERVAL = "benchmark.probe.throughput.interval";

    /** */
    public static final long THROUGHPUT_LATENCY_PROBE_DEFAULT_INVERVAL_IN_MSECS = 1_000;

    /** Operations executed. */
    private ThreadAgent[] agents;

    /** Collected points. */
    private Collection<BenchmarkProbePoint> collected = new ArrayList<>();

    /** Timer thread. */
    private Thread timerThread;

    /** {@inheritDoc} */
    @SuppressWarnings("BusyWait")
    @Override public void start(BenchmarkConfiguration cfg) throws Exception {
        agents = new ThreadAgent[cfg.threads()];

        for (int i = 0; i < agents.length; i++)
            agents[i] = new ThreadAgent();

        final long interval = interval(cfg);

        timerThread = new Thread() {
            @Override public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(interval);

                        ThreadAgent collector = new ThreadAgent();

                        for (ThreadAgent agent : agents)
                            agent.collect(collector);

                        BenchmarkProbePoint pnt = new BenchmarkProbePoint(System.currentTimeMillis(),
                            new float[] {collector.execCnt, (float)collector.totalLatency / collector.execCnt});

                        collectPoint(pnt);
                    }
                }
                catch (InterruptedException ignore) {
                    // No-op, exit probe thread.
                }
            }
        };

        timerThread.start();
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        timerThread.interrupt();

        timerThread.join();
    }

    /** {@inheritDoc} */
    @Override public Collection<String> metaInfo() {
        return Arrays.asList("Time, ms", "Operations/sec", "Latency, ms");
    }

    /** {@inheritDoc} */
    @Override public synchronized Collection<BenchmarkProbePoint> points() {
        Collection<BenchmarkProbePoint> ret = collected;

        collected = new ArrayList<>(ret.size() + 5);

        return ret;
    }

    /**
     * @param pnt Probe point.
     */
    private synchronized void collectPoint(BenchmarkProbePoint pnt) {
        collected.add(pnt);
    }

    /** {@inheritDoc} */
    @Override public void beforeExecute(int threadIdx) {
        agents[threadIdx].beforeExecute();
    }

    /** {@inheritDoc} */
    @Override public void afterExecute(int threadIdx) {
        agents[threadIdx].afterExecute();
    }

    /**
     * @param cfg Config.
     * @return Interval.
     */
    private static long interval(BenchmarkConfiguration cfg) {
        try {
            return Long.parseLong(cfg.customProperties().get(THROUGHPUT_LATENCY_PROBE_INVERVAL));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return THROUGHPUT_LATENCY_PROBE_DEFAULT_INVERVAL_IN_MSECS;
        }
    }

    /**
     *
     */
    private static class ThreadAgent {
        /** Total execution count by thread. */
        private long execCnt;

        /** Total latency by  */
        private long totalLatency;

        /** Last before execute timestamp. */
        private long beforeTs;

        /**
         *
         */
        public void beforeExecute() {
            beforeTs = System.nanoTime();
        }

        /**
         *
         */
        public void afterExecute() {
            long latency = System.nanoTime() - beforeTs;

            beforeTs = 0;

            synchronized (this) {
                execCnt++;
                totalLatency += latency;
            }
        }

        /**
         * @param other Thread agent.
         */
        public synchronized void collect(ThreadAgent other) {
            other.execCnt += execCnt;
            other.totalLatency += totalLatency;

            execCnt = 0;
            totalLatency = 0;
        }
    }
}
