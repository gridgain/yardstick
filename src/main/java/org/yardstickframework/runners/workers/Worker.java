package org.yardstickframework.runners.workers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

public abstract class Worker{
    protected static final long DFLT_TIMEOUT = 300_000L;

    protected RunContext runCtx;

    /** */
    public Worker(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * Executes before workOnHosts()
     */
    public void beforeWork(){
//        log().info(String.format("%s started.", getClass().getSimpleName()));
    }


    protected Logger log(){
        return LogManager.getLogger(getWorkerName());
    }

    protected String getWorkerName(){
        return getClass().getSimpleName();
    }

    protected String threadName(NodeInfo nodeInfo){
        StringBuilder sb = new StringBuilder(String.format("%s-%s",
            getWorkerName(),
            nodeInfo.host()));

        if (getWorkerName().startsWith("Restart"))
            sb.append("-").append(BenchmarkUtils.hms());

        return sb.toString();
    }

    protected boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    public void afterWork(){
//        log().info(String.format("%s finished.", getClass().getSimpleName()));
    }


}
