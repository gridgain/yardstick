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

import java.util.*;

/**
 * Benchmark driver.
 */
public interface BenchmarkDriver {
    /**
     * This method is invoked right before the {@link #test()} method.
     * All benchmark preparation and all resource initialization should be done here.
     *
     * @param cfg Benchmark configuration.
     * @throws Exception If failed.
     */
    public void setUp(BenchmarkConfiguration cfg) throws Exception;

    /**
     * Operation or group of operations that are to be benchmarked.
     *
     * @throws Exception If failed.
     */
    public void test() throws Exception;

    /**
     * This method is invoked right after the {@link #test()} method.
     * All necessary resources should be released here.
     *
     * @throws Exception If failed.
     */
    public void tearDown() throws Exception;

    /**
     * Gets collection of custom probes that this benchmark provides.
     *
     * @return Collection of custom probes or {@code null} if set of default probes should be used.
     */
    public Collection<BenchmarkProbe> probes();

    /**
     * Gets benchmark description.
     *
     * @return Benchmark description.
     */
    public String description();

    /**
     * Gets benchmark usage.
     *
     * @return Benchmark usage.
     */
    public String usage();
}
