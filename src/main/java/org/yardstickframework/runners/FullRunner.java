package org.yardstickframework.runners;

import org.yardstickframework.runners.context.RunContext;

/**
 * Full runner.
 */
public class FullRunner extends Runner {
    /**
     *
     * @param runCtx Run context.
     */
    FullRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        Runner runner = runner(runCtx);

        runner.run0();
    }

    /**
     *
     */
    @Override protected void printHelp(){
        System.out.println("Script for running benchmarks. Starts servers and drivers and collects results.");
        System.out.println("Usage: ./bin/run-benchmarks.sh <options>.");

        commonHelp();
    }
}
