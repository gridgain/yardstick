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

package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Set;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

/**
 * Checks docker on remote hosts.
 */
public class DockerCheckWorker extends CheckWorker {
    /** {@inheritDoc} */
    public DockerCheckWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult workRes = new CheckWorkResult();

        try {
            CommandExecutionResult res = runCtx.handler().runDockerCmd(host, "images");

            if(res.exitCode() != 0 || !res.errorList().isEmpty()){
                for(String err : res.errorList())
                    log().error(err);

                workRes.exit(true);
            }
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to check docker on the host '%s'", host), e);

            workRes.exit(true);
        }

        return workRes;
    }
}
