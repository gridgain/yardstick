package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class InDockerNodeChecker extends AbstractRunner implements NodeChecker {
    public InDockerNodeChecker(RunContext runCtx, StartNodeWorkContext ctx) {
        super(runCtx);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        String host = nodeInfo.getHost();

        String nodeToCheck = String.format("Dyardstick.%s%s ",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId());

        String checkCmd = String.format("exec %s pgrep -f \"%s\"", nodeInfo.getDockerInfo().getContName(), nodeToCheck);

        CommandHandler hndl = new CommandHandler(runCtx);

        CommandExecutionResult res = null;

        try {
//            System.out.println(checkCmd);

            res = hndl.runDockerCmd(host, checkCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res.getOutStream().isEmpty() ?
            new NodeCheckResult(NodeStatus.NOT_RUNNING) :
            new NodeCheckResult(NodeStatus.RUNNING);
    }
}
