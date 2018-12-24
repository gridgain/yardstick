package org.yardstickframework.runners.workers.host;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

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
            if (!runCtx.handler().checkRemJava(host, runCtx.remoteJavaHome())) {
                log().info(String.format("Failed to find %s/bin/java on the host %s.",
                    runCtx.remoteJavaHome(), host));

                res.exit(true);
            }
            else
                runCtx.hostJavaHomeMap().put(host, runCtx.remoteJavaHome());

            return res;
        }

        if (runCtx.handler().checkRemJava(host, locJavaHome)) {
            log().info(String.format("Using JAVA_HOME '%s' on the host %s.", locJavaHome, host));

            runCtx.hostJavaHomeMap().put(host, locJavaHome);
        }
        else {
            String hostJava = runCtx.handler().getHostJavaHome(host);

            String warn = String.format("WARNING! JAVA_HOME is not defined in property file and default JAVA_HOME " +
                    "'%s' on the host %s is different from default JAVA_HOME on this host (%s)." +
                    " Will use '%s' to start nodes on the host %s.",
                hostJava,
                host,
                locJavaHome,
                hostJava,
                host);

            log().info(warn);

            runCtx.putInJavaHostMap(host, hostJava);
        }

        return res;
    }
}
