package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;

/**
 * Collects data.
 */
public class CollectRunner extends Runner {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
     private CollectRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CollectRunner runner = new CollectRunner(runCtx);

        runner.run0();
    }

    /**
     *
     * @return Exit code.
     */
    @Override protected int run0() {
        super.run0();

        checkPlain(new CheckConnWorker(runCtx, runCtx.getHostSet()));

        List<NodeType> dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.collect(dockerList);

        new CollectWorker(runCtx, runCtx.getHostSet()).workOnHosts();

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for collecting data from remote hosts.");
        System.out.println("Usage: ./bin/collect.sh <options>.");

        commonHelp();
    }
}
