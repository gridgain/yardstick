package org.yardstickframework.runners;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

    protected boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    public void afterWork(){
//        log().info(String.format("%s finished.", getClass().getSimpleName()));
    }


}
