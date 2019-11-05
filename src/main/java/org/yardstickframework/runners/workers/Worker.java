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

package org.yardstickframework.runners.workers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Parent for workers.
 */
public abstract class Worker{
    /** Run context */
    protected RunContext runCtx;

    /** Local addresses*/
    private static final Set<String> LOC_ADR_SET = Stream.of("localhost", "127.0.0.1")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public Worker(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * Executes before workOnHosts()
     */
    public void beforeWork(){};

    /**
     * Executes after workOnHosts().
     */
    public void afterWork(){};

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(workerName());
    }

    /**
     *
     * @return Worker name.
     */
    protected String workerName(){
        return getClass().getSimpleName();
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Thread name.
     */
    protected String threadName(NodeInfo nodeInfo){
        return String.format("%s-%s",
            workerName(),
            nodeInfo.host());
    }

    /**
     *
     * @param host Host.
     * @return {@code true} if host address is "localhost" or "127.0.0.1" or {@code false} otherwise.
     */
    protected boolean isLocal(String host) {
        return LOC_ADR_SET.contains(host.toLowerCase());
    }
}