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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

import static org.yardstickframework.BenchmarkUtils.getJava;

/**
 * Checks java on remote hosts and set host map.
 */
public class CheckJavaWorker extends CheckWorker {
    /** */
    private String locJavaHome;

    /** */
    private static final Collection<String> checked = new HashSet<>();

    /** {@inheritDoc} */
    public CheckJavaWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        super.beforeWork();

        locJavaHome = System.getProperty("java.home");

        if (!runCtx.onlyLocal() && runCtx.properties().getProperty("JAVA_HOME") == null)
            log().warn("'JAVA_HOME' is not defined in property file.");
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        synchronized (this) {
            if (checked.contains(host))
                return res;

            checked.add(host);
        }

        if (runCtx.remoteJavaHome() != null) {
            if (!runCtx.handler().checkJava(host, runCtx.remoteJavaHome())) {
                log().info(String.format("Failed to find %s on the host %s.", getJava(runCtx.remoteJavaHome()), host));

                res.exit(true);
            }
            else
                runCtx.putInJavaHostMap(host, runCtx.remoteJavaHome());

            return res;
        }

        if (runCtx.handler().checkJava(host, locJavaHome)) {
            log().info(String.format("Using JAVA_HOME '%s' on the host '%s'.", locJavaHome, host));

            runCtx.putInJavaHostMap(host, locJavaHome);
        }
        else {
            String hostJava = runCtx.handler().getHostJavaHome(host);

            String warn1 = String.format("Default JAVA_HOME " +
                    "'%s' on the host '%s' is different from default JAVA_HOME on the current host '%s'.",
                hostJava,
                host,
                locJavaHome);

            String warn2 = String.format("Will use '%s' to start nodes on the host '%s'.",
                hostJava,
                host);

            log().warn(warn1);
            log().warn(warn2);

            runCtx.putInJavaHostMap(host, hostJava);
        }

        return res;
    }
}
