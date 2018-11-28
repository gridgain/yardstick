package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PlainNodeChecker extends AbstractRunner implements NodeChecker {
    public PlainNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        String checkCmd = String.format("pgrep -f \"Dyardstick.%s%s \"",
            nodeInfo.getNodeType().toString().toLowerCase(),
            nodeInfo.getId());

        CommandHandler hndl = new CommandHandler(runCtx);

        String host = nodeInfo.getHost();

        CommandExecutionResult res = null;

        try {
            res = hndl.runCmd(host, checkCmd, "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!res.getOutStream().isEmpty())
            return new NodeCheckResult(NodeStatus.RUNNING);
        else
            return new NodeCheckResult(NodeStatus.NOT_RUNNING);
    }
}
