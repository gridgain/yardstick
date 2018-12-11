package org.yardstickframework.runners.workers.host;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public class CheckJavaWorker extends CheckWorker {
    /** */
    private String locJavaHome;

    /** */
    private static final Collection<String> checked = new HashSet<>();

    public CheckJavaWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        locJavaHome = System.getProperty("java.home");
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        if(checked.contains(host))
            return res;

        checked.add(host);

        CommandHandler hndl = new CommandHandler(runCtx);

        if (runCtx.getRemJavaHome() != null) {
            if (!hndl.checkRemJava(host, runCtx.getRemJavaHome())) {
                log().info(String.format("Failed to find %s/bin/java on the host %s.",
                    runCtx.getRemJavaHome(), host));

                res.exit(true);
            }
            else
                runCtx.getHostJavaHomeMap().put(host, runCtx.getRemJavaHome());

            return res;
        }

        if (hndl.checkRemJava(host, locJavaHome)) {
            log().info(String.format("Using JAVA_HOME '%s' on the host %s.", locJavaHome, host));

            runCtx.getHostJavaHomeMap().put(host, locJavaHome);
        }
        else {
            String hostJava = hndl.getHostJavaHome(host);

            String warn = String.format("WARNING! JAVA_HOME is not defined in property file and default JAVA_HOME " +
                    "'%s' on the host %s is different from default JAVA_HOME on this host (%s)." +
                    " Will use '%s' to start nodes on the host %s.",
                hostJava,
                host,
                locJavaHome,
                hostJava,
                host);

            log().info(warn);

            runCtx.getHostJavaHomeMap().put(host, hostJava);
        }

        return res;
    }
}
