package org.yardstickframework.runners.workers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Parent for workers.
 */
public abstract class Worker{
    /** Run context */
    protected RunContext runCtx;

    /** Local addresses*/
    private static final Set<String> LOC_ADR_SET = Stream.of("localhost", "127.0.0.1")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public Worker(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * Executes before workOnHosts()
     */
    public void beforeWork(){
        //NO_OP
    }

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(workerName());
    }

    /**
     *
     * @return Worker name.
     */
    protected String workerName(){
        return getClass().getSimpleName();
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Thread name.
     */
    protected String threadName(NodeInfo nodeInfo){
        StringBuilder sb = new StringBuilder(String.format("%s-%s",
            workerName(),
            nodeInfo.host()));

        if (workerName().startsWith("Restart"))
            sb.append("-").append(BenchmarkUtils.hms());

        return sb.toString();
    }

    /**
     *
     * @param host Host.
     * @return {@code true} if host address is "localhost" or "127.0.0.1" or {@code false} otherwise.
     */
    protected boolean isLocal(String host) {
        return LOC_ADR_SET.contains(host.toLowerCase());
    }

    /**
     * Executes after workOnHosts().
     */
    public void afterWork(){
        //NO_OP
    }
}
