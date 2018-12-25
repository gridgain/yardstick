package org.yardstickframework.runners;

import org.yardstickframework.runners.context.RunContext;

/**
 * Driver runner.
 */
public class DriverRunner extends Runner {
    /**
     * @param runCtx Run context.
     */
    private DriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        Runner runner = driverRunner(runCtx);

        runner.run0();
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
