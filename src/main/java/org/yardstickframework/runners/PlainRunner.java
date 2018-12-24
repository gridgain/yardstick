package org.yardstickframework.runners;

import org.yardstickframework.runners.context.RunContext;

/**
 * Plain runner.
 */
public class PlainRunner extends Runner {
    /**
     *
     * @param runCtx Run context.
     */
    PlainRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        generalPrepare();

        execute();

        afterExecution();

        return 0;
    }
}
