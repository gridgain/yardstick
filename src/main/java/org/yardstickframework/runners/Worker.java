package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public abstract class Worker extends AbstractRunner{
    /** */
    public Worker(RunContext runCtx) {
        super(runCtx);
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
