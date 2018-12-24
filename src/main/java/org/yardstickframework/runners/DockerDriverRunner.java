package org.yardstickframework.runners;

import java.util.ArrayList;
import org.yardstickframework.runners.context.RunContext;

/**
 * Docker driver runner.
 */
public class DockerDriverRunner extends DockerRunner {
    /**
     * @param runCtx Run context.
     */
    DockerDriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        driverPrepare();

        dockerPrepare();

        for (String cfgStr : runCtx.configList())
            iterationBody(cfgStr, new ArrayList<>());

        dockerAfterExecution();

        afterExecution();

        return 0;
    }
}
