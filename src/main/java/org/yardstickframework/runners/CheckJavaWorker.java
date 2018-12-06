package org.yardstickframework.runners;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public class CheckJavaWorker extends Worker {
    /** */
    private static final Logger LOG = LogManager.getLogger(CheckJavaWorker.class);

    private String locJavaHome;

    public CheckJavaWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        locJavaHome = System.getProperty("java.home");
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

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
            LOG.info(String.format("Using JAVA_HOME '%s' on the host %s.", locJavaHome, host));

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

            LOG.info(warn);

            runCtx.getHostJavaHomeMap().put(host, hostJava);
        }

        return res;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
