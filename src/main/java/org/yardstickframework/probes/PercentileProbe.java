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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

    /** */
    private static final Object lock = new Object();

    /** Operations executed. */
    private ThreadAgent[] agents;

    /** */
    private BenchmarkConfiguration cfg;

    /** */
    private AtomicInteger initBucketsCnt = new AtomicInteger();

    /** */
    private AtomicInteger maxBucketsCnt = new AtomicInteger();

    /** */
    private ThreadLocal<Integer> bucketsCnt = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            Integer locVal = initBucketsCnt.get();

            return locVal;
        }
    };
    ;

    /** */
    private long bucketInterval;

    /** */
    private TimeUnit timeUnit;

    /** {@inheritDoc} */
    @SuppressWarnings("BusyWait")
    @Override public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {
        this.cfg = cfg;

        bucketInterval = interval(cfg);

        initBucketsCnt.getAndSet(count(cfg));

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
        long[] buckets0 = new long[maxBucketsCnt.get()];

        for (ThreadAgent agent : agents) {
            long[] b0 = agent.reset();

            for (int i = 0; i < b0.length; i++)
                buckets0[i] += b0[i];
        }

        long sum = 0;

        for (long b : buckets0)
            sum += b;

        double counted = 0;

        int currBucket = 0;

        Collection<BenchmarkProbePoint> ret = new ArrayList<>();

        for (int i = 5; i < 100; i += 5) {
            long qnt = (sum / 100) * i;

            while (counted < qnt && currBucket < buckets0.length) {
                counted += buckets0[currBucket];

                currBucket++;
            }

            ret.add(new BenchmarkProbePoint(currBucket * bucketInterval, new double[] {i}));
        }

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
        private volatile long[] buckets = new long[initBucketsCnt.get()];

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
            long afterTs = System.nanoTime();

            long latency = afterTs - beforeTs;

            long latencyNano = timeUnit.convert(latency, NANOSECONDS);

            beforeTs = 0;

            long bucketIdx = latencyNano / bucketInterval;

            int bucketIdxInt = (int)bucketIdx;

            long[] newArr;

            if (bucketIdxInt >= bucketsCnt.get()) {
                newArr = new long[(int)(bucketIdxInt * 1.2)];

                System.arraycopy(buckets, 0, newArr, 0, buckets.length);

                bucketsCnt.set(newArr.length);

                newArr[bucketIdxInt]++;

                buckets = newArr;

                synchronized (lock) {
                    if (maxBucketsCnt.get() < newArr.length)
                        maxBucketsCnt.getAndSet(newArr.length);
                }
            }
            else {
                newArr = buckets;

                newArr[bucketIdxInt]++;

                buckets = newArr;
            }
        }

        /**
         * @return Resets the agent.
         */
        public long[] reset() {
            long[] b = buckets;

            return b;
        }
    }
}
