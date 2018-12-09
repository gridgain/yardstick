package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class InDockerNodeChecker extends AbstractRunner implements NodeChecker {
    private CommandHandler hndl;

    public InDockerNodeChecker(RunContext runCtx) {
        super(runCtx);

        hndl = new CommandHandler(runCtx);
    }

    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.getHost();

        String nodeToCheck = String.format("Dyardstick.%s%s ",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId());

        String checkCmd = String.format("exec %s pgrep -f \"%s\"", nodeInfo.dockerInfo().contName(), nodeToCheck);

        CommandExecutionResult res = null;

        try {
            res = hndl.runDockerCmd(host, checkCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(res.getOutStream().isEmpty())
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.RUNNING);

        return nodeInfo;
    }
}
