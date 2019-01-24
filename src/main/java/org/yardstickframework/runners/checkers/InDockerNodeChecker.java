package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker for docker run.
 */
public class InDockerNodeChecker extends NodeChecker {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public InDockerNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.host();

        String checkCmd = String.format("exec %s sh -c 'ps -a|grep java'", nodeInfo.dockerInfo().contName());

        CommandExecutionResult res = CommandExecutionResult.emptyFailedResult();

        try {
            res = runCtx.handler().runDockerCmd(host, checkCmd);
        }
        catch (IOException e) {
            log().error(String.format("Failed to check node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(res);
        }

        String toLook = String.format("-Dyardstick.%s ", nodeInfo.toShortStr());

        if(runCtx.handler().checkList(res.outputList(), toLook))
            nodeInfo.nodeStatus(NodeStatus.RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);

        return nodeInfo;
    }
}
