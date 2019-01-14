package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

/**
 * Docker driver runner.
 */
public class DockerServerRunner extends DockerRunner {
    /**
     * @param runCtx Run context.
     */
    DockerServerRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        if (runCtx.config().help()) {
            printHelp();

            System.exit(0);
        }

        generalPrepare();

        dockerPrepare();

        startServers();

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for starting server nodes.");
        System.out.println("Usage: ./bin/run-servers.sh <options>.");

        commonHelp();
    }
}
