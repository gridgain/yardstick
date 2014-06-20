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

import org.yardstickframework.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.TimeUnit.*;
import static org.yardstickframework.BenchmarkUtils.*;

/**
* Probe that calculates throughput and average latency.
*/
public class PercentileProbe implements BenchmarkExecutionAwareProbe, BenchmarkTotalsOnlyProbe {
    /** */
    public static final String BUCKET_INTERVAL = "BENCHMARK_PROBE_PERCENTILE_BUCKET_INTERVAL";

    /** */
    public static final String BUCKETS_CNT = "BENCHMARK_PROBE_PERCENTILE_BUCKETS_CNT";

    /** */
    public static final String TIME_UNIT = "BENCHMARK_PROBE_PERCENTILE_TIME_UNIT";

    /** */
    public static final long DEFAULT_BUCKET_INTERVAL = 200;

    /** */
    public static final int DEFAULT_BUCKETS_CNT = 5_000;

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

    /** */
    private AtomicLong[] buckets;

    /** */
    private final AtomicBoolean pointsGuard = new AtomicBoolean();

    /** {@inheritDoc} */
    @SuppressWarnings("BusyWait")
    @Override public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {
        this.cfg = cfg;

        bucketInterval = interval(cfg);
        bucketsCnt = count(cfg);
        timeUnit = timeUnit(cfg);

        buckets = new AtomicLong[bucketsCnt];

        for (int i = 0; i < bucketsCnt; i++)
            buckets[i] = new AtomicLong();

        agents = new ThreadAgent[cfg.threads()];

        for (int i = 0; i < agents.length; i++)
            agents[i] = new ThreadAgent();

        println(cfg, PercentileProbe.class.getSimpleName() + " is started.");
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        println(cfg, PercentileProbe.class.getSimpleName() + " is stopped.");
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
        if (!pointsGuard.compareAndSet(false, true))
            throw new IllegalStateException();

        Collection<BenchmarkProbePoint> ret = new ArrayList<>(bucketsCnt);

        long sum = 0;

        for (AtomicLong b : buckets)
            sum += b.get();

        for (int i = 0; i < buckets.length; i++) {
            long cnt = buckets[i].get();

            ret.add(new BenchmarkProbePoint((i + 1) * bucketInterval, new double[] {((double)cnt)/sum}));
        }

        return ret;
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

            int idx = bucketIdx >= bucketsCnt ? bucketsCnt - 1 : (int)bucketIdx;

            buckets[idx].incrementAndGet();
        }
    }
}
