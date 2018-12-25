package org.yardstickframework.runners;

import java.util.ArrayList;
import org.yardstickframework.runners.context.RunContext;

/**
 * Driver runner.
 */
public class PlainDriverRunner extends Runner {
    /**
     * @param runCtx Run context.
     */
    PlainDriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        super.run0();

        driverPrepare();

        for (String cfgStr : runCtx.configList())
            iterationBody(cfgStr, new ArrayList<>());

        afterExecution();

        return 0;
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
