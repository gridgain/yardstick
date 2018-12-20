package org.yardstickframework.runners.workers.host;

import java.util.Set;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Checks ssh connection.
 */
public class CheckConnWorker extends CheckWorker {
    /** {@inheritDoc} */
    public CheckConnWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        log().info(String.format("Checking ssh connection to the host '%s'.", host));

        if (!runCtx.handler().checkConn(host)) {
            log().info(String.format("Failed to establish connection to the host '%s'.", host));

            res.exit(true);
        }

        return res;
    }
}
