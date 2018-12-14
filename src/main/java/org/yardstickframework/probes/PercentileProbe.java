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

package org.yardstickframework.probes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkExecutionAwareProbe;
import org.yardstickframework.BenchmarkProbePoint;
import org.yardstickframework.BenchmarkTotalsOnlyProbe;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Probe that tracks the latency of each individual request and collects the time frame bucket.
 */
public class PercentileProbe implements BenchmarkExecutionAwareProbe, BenchmarkTotalsOnlyProbe {
    /** */
    public static final String BUCKET_INTERVAL = "BENCHMARK_PROBE_PERCENTILE_BUCKET_INTERVAL";

    /** */
    public static final String BUCKETS_CNT = "BENCHMARK_PROBE_PERCENTILE_BUCKETS_CNT";

    /** */
    public static final String TIME_UNIT = "BENCHMARK_PROBE_PERCENTILE_TIME_UNIT";

    /** */
    public static final long DEFAULT_BUCKET_INTERVAL = 100;

    /** */
    public static final int DEFAULT_BUCKETS_CNT = 200;

    /** */
    public static final TimeUnit DEFAULT_TIME_UNIT = MICROSECONDS;

    /** Operations executed. */
    private ThreadAgent[] agents;

    /** */
    private BenchmarkConfiguration cfg;

    /** */
    private int bucketsCnt;

    /** */
    private long bucketInterval;

    /** */
    private TimeUnit timeUnit;

    /** {@inheritDoc} */
    @SuppressWarnings("BusyWait")
    @Override public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {
        this.cfg = cfg;

        bucketInterval = interval(cfg);
        bucketsCnt = count(cfg);
        timeUnit = timeUnit(cfg);

        agents = new ThreadAgent[cfg.threads()];

        for (int i = 0; i < agents.length; i++)
            agents[i] = new ThreadAgent();

        println(cfg, getClass().getSimpleName() + " is started.");
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        println(cfg, getClass().getSimpleName() + " is stopped.");
    }

    /** {@inheritDoc} */
    @Override public Collection<String> metaInfo() {
        return Arrays.asList("Latency, " + unitAsString(), "Operations, %");
    }

    /**
     * @return Time unit as string.
     */
    private String unitAsString() {
        return timeUnit != null ? timeUnit.name().toLowerCase() : "n/a";
    }

    /** {@inheritDoc} */
    @Override public Collection<BenchmarkProbePoint> points() {
        int maxLen = 0;

        for (ThreadAgent agent : agents)
            maxLen = Math.max(maxLen, agent.buckets.length);

        long[] buckets0 = new long[maxLen];

        for (ThreadAgent agent : agents) {
            long[] b0 = agent.reset();

            for (int i = 0; i < b0.length; i++)
                buckets0[i] += b0[i];
        }

        Collection<BenchmarkProbePoint> ret = new ArrayList<>(bucketsCnt + 1);

        if (bucketsCnt > 0)
            ret.add(new BenchmarkProbePoint(0, new double[] {0}));

        // Total sum of operations from all thread agents;
        long totalSum = 0;

        for (long b : buckets0)
            totalSum += b;

        int currBucket = 0;

        // Counted operations.
        double counted = buckets0[currBucket];

        double p;

        do{
            p = (counted * 100) / totalSum;

            ret.add(new BenchmarkProbePoint(currBucket * bucketInterval, new double[] {p}));
            ret.add(new BenchmarkProbePoint((currBucket + 1) * bucketInterval, new double[] {p}));

            currBucket++;

            counted += buckets0[currBucket];
        }
        while (p < 99);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public void buildPoint(long time) {
        // No-op.
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
            return Long.parseLong(cfg.customProperties().get(BUCKET_INTERVAL));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_BUCKET_INTERVAL;
        }
    }

    /**
     * @param cfg Config.
     * @return Interval.
     */
    private static int count(BenchmarkConfiguration cfg) {
        try {
            return Integer.parseInt(cfg.customProperties().get(BUCKETS_CNT));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_BUCKETS_CNT;
        }
    }

    /**
     * @param cfg Config.
     * @return Interval.
     */
    private static TimeUnit timeUnit(BenchmarkConfiguration cfg) {
        try {
            return TimeUnit.valueOf(cfg.customProperties().get(TIME_UNIT));
        }
        catch (IllegalArgumentException | NullPointerException ignored) {
            return DEFAULT_TIME_UNIT;
        }
    }

    /**
     *
     */
    private class ThreadAgent {
        /** Last before execute timestamp. */
        private long beforeTs;

        /** */
        private volatile long[] buckets = new long[bucketsCnt];

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

            long bucketIdx = timeUnit.convert(latency, NANOSECONDS) / bucketInterval;

            int bucketIdxInt = (int)bucketIdx;

            if (bucketIdxInt >= buckets.length) {
                long[] newArr = new long[(int)(bucketIdxInt * 1.2)];

                System.arraycopy(buckets, 0, newArr, 0, buckets.length);

                newArr[bucketIdxInt]++;

                buckets = newArr;
            }
            else
                buckets[bucketIdxInt]++;
        }

        /**
         * @return Resets the agent.
         */
        public long[] reset() {
            long[] b = buckets;

            buckets = new long[bucketsCnt];

            return b;
        }
    }
}
