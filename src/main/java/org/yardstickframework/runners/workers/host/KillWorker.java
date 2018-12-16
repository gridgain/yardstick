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
        CommandHandler hand = new CommandHandler(runCtx);

        try {
            String killServCmd = "pkill -9 -f \"Dyardstick.server\"";

            hand.runCmd(host, killServCmd);

            String killDrvrCmd = "pkill -9 -f \"Dyardstick.driver\"";

            hand.runCmd(host, killDrvrCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to kill nodes on the host '%s'", host), e);
        }

        return null;
    }
}
