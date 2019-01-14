package org.yardstickframework.runners;

import org.yardstickframework.runners.context.RunContext;

/**
 * Driver runner.
 */
public class ServerRunner extends Runner {
    /**
     * @param runCtx Run context.
     */
    private ServerRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        Runner runner = serverRunner(runCtx);

        runner.run0();
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
