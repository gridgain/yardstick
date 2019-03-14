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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Collects data from remote hosts.
 */
public class CollectWorker extends HostWorker {
    /** Main output directory */
    private File outDir;

    /** {@inheritDoc} */
    public CollectWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);

        outDir = Paths.get(runCtx.localeWorkDirectory(), "output").toFile();
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        if (!outDir.exists())
            outDir.mkdirs();
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        if (isLocal(host) && runCtx.dirsEquals())
            return res;

        String nodeOutDir = Paths.get(runCtx.remoteWorkDirectory(), "output").toString();

        log().info(String.format("Collecting data from the host '%s'.", host));

        String pathLoc = outDir.getAbsolutePath();

        try {
            String pathRem = String.format("%s%s%s", nodeOutDir, File.separator, "*");

            runCtx.handler().download(host, pathLoc, pathRem);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to collect data from the host '%s'", host), e);

            runCtx.exitCode(1);

            res.exit(true);
        }

        return res;
    }
}
