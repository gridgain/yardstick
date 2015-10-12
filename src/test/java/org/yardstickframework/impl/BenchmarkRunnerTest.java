/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yardstickframework.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkDriverAdapter;
import org.yardstickframework.BenchmarkProbe;

/**
 * Test.
 */
public class BenchmarkRunnerTest {
    /** */
    public static final int TIMEOUT = 3000;

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testDeadlock() throws Exception {
        final BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        cfg.warmup(3);
        cfg.output(System.out);
        cfg.error(System.err);

        final TestBenchmarkDriver driver = new TestBenchmarkDriver();

        driver.setUp(cfg);

        BenchmarkRunner runner = new BenchmarkRunner(
            cfg,
            new BenchmarkDriver[] {driver},
            new BenchmarkProbeSet[] {
                new BenchmarkProbeSet(driver, cfg, Collections.<BenchmarkProbe>emptyList(),
                    new BenchmarkLoader())},
            new int[] {0});

        runner.runBenchmark();

        // Waiting for deadlock.
        Thread.sleep(cfg.warmup() * 1000 + TIMEOUT + 2_000);

        // Check that a deadlock didn't happen.
        ThreadGroup threadGrp = Thread.currentThread().getThreadGroup();

        while (threadGrp.getParent() != null)
            threadGrp = threadGrp.getParent();

        Thread[] allThreads = new Thread[threadGrp.activeCount() + 100];

        threadGrp.enumerate(allThreads);

        for (Thread t : allThreads) {
            if (t != null && t.getName() != null && t.getName().startsWith("benchmark-worker-"))
                Assert.fail("All 'benchmark-worker' threads have to be already stopped [thread=" + t + ']');
        }
    }

    /**
     *
     */
    private static class TestBenchmarkDriver extends BenchmarkDriverAdapter {
        /** */
        private static final AtomicBoolean first = new AtomicBoolean();

        /** {@inheritDoc} */
        @Override public boolean test(Map<Object, Object> ctx) throws Exception {
            if (first.compareAndSet(false, true)) {
                // Waiting for other threads go to await().
                Thread.sleep(cfg.warmup() * 1000 + TIMEOUT);

                throw new IllegalStateException("Test exception [thread=" + Thread.currentThread() + ']');
            }

            Thread.sleep(5);

            return true;
        }
    }
}
