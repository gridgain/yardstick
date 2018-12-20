package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Kills nodes.
 */
public class KillWorker extends HostWorker {
    /** {@inheritDoc} */
    public KillWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {


        try {
            String killServCmd = "pkill -9 -f \"Dyardstick.server\"";

            runCtx.handler().runCmd(host, killServCmd);

            String killDrvrCmd = "pkill -9 -f \"Dyardstick.driver\"";

            runCtx.handler().runCmd(host, killDrvrCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to kill nodes on the host '%s'", host), e);
        }

        return null;
    }
}
