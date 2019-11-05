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

import java.util.ArrayList;
import org.yardstickframework.runners.context.RunContext;

/**
 * Docker driver runner.
 */
public class DockerDriverRunner extends DockerRunner {
    /**
     * @param runCtx Run context.
     */
    DockerDriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        if (runCtx.config().help()) {
            printHelp();

            System.exit(0);
        }

        driverPrepare();

        dockerPrepare();

        for (String cfgStr : runCtx.configList())
            iterationBody(cfgStr, new ArrayList<>());

        dockerAfterExecution();

        afterExecution();

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for starting driver nodes.");
        System.out.println("Usage: ./bin/run-drivers.sh <options>.");

        commonHelp();
    }
}
