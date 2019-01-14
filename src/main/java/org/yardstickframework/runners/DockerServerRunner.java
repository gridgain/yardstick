package org.yardstickframework.runners;

import java.util.ArrayList;
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
        super.run0();

        generalPrepare();

        dockerPrepare();

        String cfgStr0 = runCtx.properties().getProperty("CONFIGS").split(",")[0];

        startNodes(NodeType.SERVER, cfgStr0);

        return runCtx.exitCode();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for starting driver nodes.");
        System.out.println("Usage: ./bin/run-drivers.sh <options>.");

        commonHelp();
    }
}
