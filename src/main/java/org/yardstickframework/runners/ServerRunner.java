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

import org.yardstickframework.runners.context.RunContext;

/**
 * Driver runner.
 */
public class ServerRunner extends Runner {
    /**
     * @param runCtx Run context.
     */
    private ServerRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        runCtx.startServersEndExit(true);

        Runner runner = serverRunner(runCtx);

        runner.run0();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for starting server nodes.");
        System.out.println("Usage: ./bin/run-servers.sh <options>.");

        commonHelp();
    }
}
