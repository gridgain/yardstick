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

package org.yardstick;

/**
 * Benchmark server.
 */
public interface BenchmarkServer {
    /**
     * All benchmark preparation and all resource initialization should be done here.
     *
     * @param cfg Benchmark configuration.
     * @throws Exception If failed.
     */
    public void start(BenchmarkConfiguration cfg) throws Exception;

    /**
     * All necessary resources should be released here.
     *
     * @throws Exception If failed.
     */
    public void stop() throws Exception;

    /**
     * Gets benchmark usage.
     *
     * @return Benchmark usage.
     */
    public String usage();
}
