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
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Cleans remote directory.
 */
public class CleanRemDirWorker extends HostWorker {
    /** */
    private String[] toClean = new String[] {"bin", "config", "libs", "output", "work"};

    /** {@inheritDoc} */
    public CleanRemDirWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        return clean(host);
    }

    /**
     * @param host Host
     * @return Work result.
     */
    private WorkResult clean(String host) {
        CheckWorkResult res = new CheckWorkResult();

        if ((isLocal(host) && runCtx.dirsEquals()) || host.equals(runCtx.currentHost()) && runCtx.dirsEquals())
            return res;

        String remDir = runCtx.remoteWorkDirectory();

        log().info(String.format("Cleaning up directory '%s' on the host '%s'.", remDir, host));

        try {
            for (String name : toClean) {
                String cleanCmd = String.format("rm -rf %s/%s",
                    runCtx.remoteWorkDirectory(), name);

                runCtx.handler().runCmd(host, cleanCmd);
            }
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to clean remote directory on the host '%s'", host), e);

            res.exit(true);
        }

        return res;
    }
}
