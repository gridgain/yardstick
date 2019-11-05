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

package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;

/**
 * Collects data.
 */
public class CollectRunner extends Runner {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
     private CollectRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CollectRunner runner = new CollectRunner(runCtx);

        runner.run0();
    }

    /**
     *
     * @return Exit code.
     */
    @Override protected int run0() {
        super.run0();

        checkPlain(new CheckConnWorker(runCtx, runCtx.getHostSet()));

        List<NodeType> dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.collect(dockerList);

        new CollectWorker(runCtx, runCtx.getHostSet()).workOnHosts();

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for collecting data from remote hosts.");
        System.out.println("Usage: ./bin/collect.sh <options>.");

        commonHelp();
    }
}
