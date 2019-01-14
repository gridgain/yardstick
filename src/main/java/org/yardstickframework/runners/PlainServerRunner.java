 package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

/**
 * Driver runner.
 */
public class PlainServerRunner extends Runner {
    /**
     * @param runCtx Run context.
     */
    PlainServerRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        super.run0();

        generalPrepare();

        startServers();

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
