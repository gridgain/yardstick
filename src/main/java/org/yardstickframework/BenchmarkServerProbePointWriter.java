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

package org.yardstickframework;

import java.util.Collection;

/**
 * Points writer for server probes.
 */
public interface BenchmarkServerProbePointWriter extends AutoCloseable {
    /**
     * Writer preparation and all resource initialization should be done here.
     *
     * @param drv Benchmark driver.
     * @param cfg Benchmark configuration.
     * @param startTime Time when writers are initialized.
     */
    public void start(BenchmarkServer drv, BenchmarkConfiguration cfg, long startTime);

    /**
     * Writes collection of collected points to target.
     *
     * @param probe Probe for which points are collected.
     * @param points Points to write.
     * @throws Exception If write failed.
     */
    public void writePoints(BenchmarkServerProbe probe, Collection<BenchmarkProbePoint> points) throws Exception;
}
