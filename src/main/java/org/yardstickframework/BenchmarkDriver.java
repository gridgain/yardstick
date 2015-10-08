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

import java.util.Map;

/**
 * Benchmark driver.
 */
public interface BenchmarkDriver {
    /**
     * This method is invoked right before the {@link #test(Map)} method.
     * All benchmark preparation and all resource initialization should be done here.
     *
     * @param cfg Benchmark configuration.
     * @throws Exception If failed.
     */
    public void setUp(BenchmarkConfiguration cfg) throws Exception;

    /**
     * Operation or group of operations that are to be benchmarked.
     *
     * @param ctx Thread local map.
     * @throws Exception If failed.
     * @return {@code False} if some condition is fulfilled and the driver should be shutdown, {@code true} otherwise.
     */
    public boolean test(Map<Object, Object> ctx) throws Exception;

    /**
     * This method is invoked right after the {@link #test(Map)} method.
     * All necessary resources should be released here.
     *
     * @throws Exception If failed.
     */
    public void tearDown() throws Exception;

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

    /**
     * This method is invoked right after the warmup procedure is finished.
     */
    public void onWarmupFinished();

    /**
     * This method is invoked only then {@link #test(Map)} throws exception, right after the exception
     * thrown. It blocks shutting down of benchmark, so it should not have long execution.
     *
     * Any exception thrown from the method will be ignored.
     *
     * @param e Exception.
     */
    public void onException(Throwable e);
}
