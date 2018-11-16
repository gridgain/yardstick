package org.yardstickframework.runners;

import java.util.Properties;
import org.yardstickframework.BenchmarkConfiguration;

import static org.yardstickframework.BenchmarkUtils.jcommander;

public abstract class StartNodeWorker extends Worker {
    /** */
    protected String dateTime;

    /** */
    protected String resDirName;

    /** */
    protected String logDirName;

    /** */
    protected String baseLogDirFullName;

    /** */
    protected String cfgFullStr;

    /** */
    private BenchmarkConfiguration iterCfg;


    public StartNodeWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    public String getCfgFullStr() {
        return cfgFullStr;
    }

    @Override public void beforeWork() {
        super.beforeWork();

        dateTime = runProps.getProperty("MAIN_DATE_TIME");

        resDirName = String.format("results-%s", dateTime);

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", getMainDir(), logDirName);
    }
}
