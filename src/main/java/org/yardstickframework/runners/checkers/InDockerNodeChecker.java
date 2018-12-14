package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.AbstractRunner;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker for docker run.
 */
public class InDockerNodeChecker extends AbstractRunner implements NodeChecker {
    /** */
    private CommandHandler hndl;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public InDockerNodeChecker(RunContext runCtx) {
        super(runCtx);

        hndl = new CommandHandler(runCtx);
    }

    /** {@inheritDoc} */
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
