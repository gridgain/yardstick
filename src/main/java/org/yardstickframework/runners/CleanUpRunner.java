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
import org.yardstickframework.runners.workers.host.CleanRemDirWorker;
import org.yardstickframework.runners.workers.host.HostWorker;
import org.yardstickframework.runners.workers.host.KillWorker;

/**
 * Cleans up docker on hosts.
 */
public class CleanUpRunner  extends Runner {
    /** {@inheritDoc} */
    private CleanUpRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CleanUpRunner runner = new CleanUpRunner(runCtx);

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

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }

        HostWorker killWorker = new KillWorker(runCtx, runCtx.getHostSet());

        killWorker.workOnHosts();

        HostWorker cleanWorker = new CleanRemDirWorker(runCtx, runCtx.getHostSet());

        cleanWorker.workOnHosts();

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for cleaning up remote work directory.");
        System.out.println("Usage: ./bin/clean-up.sh <options>.");

        commonHelp();
    }
}
