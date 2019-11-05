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
import java.nio.file.Paths;
import java.util.Set;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Deploys on hosts.
 */
public class DeployWorker extends HostWorker {
    /** List of directories to deploy. */
    private String[] toDeploy = new String[] {"bin", "config", "libs"};

    /** */
    private CleanRemDirWorker cleaner;

    /** {@inheritDoc} */
    public DeployWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);

        cleaner = new CleanRemDirWorker(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        cleaner.workOnHosts();
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        if ((isLocal(host) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            || host.equals(runCtx.currentHost()) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            return null;

        String createCmd = String.format("mkdir -p %s", runCtx.remoteWorkDirectory());

        try {
            runCtx.handler().runCmd(host, createCmd);

            log().info(String.format("Deploying on the host '%s'.", host));

            for (String name : toDeploy) {
                String fullPath = Paths.get(runCtx.remoteWorkDirectory(), name).toAbsolutePath().toString();

                if (runCtx.handler().checkRemFile(host, fullPath))
                    runCtx.handler().runMkdirCmd(host, fullPath);

                String locPath = String.format("%s/%s", runCtx.localeWorkDirectory(), name);

                runCtx.handler().upload(host, locPath, runCtx.remoteWorkDirectory());
            }
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to deploy on the host '%s'", host), e);
        }

        return null;
    }
}
