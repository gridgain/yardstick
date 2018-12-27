package org.yardstickframework.runners.starters;

import java.io.File;
import java.io.IOException;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.DockerInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Starts nodes in docker containers.
 */
public class InDockerNodeStarter extends NodeStarter {
    /**
     *
     * @param runCtx Run context.
     */
    public InDockerNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException {
        String contName = String.format("%s_%s", runCtx.dockerContext().contNamePrefix(nodeInfo.nodeType()),
            nodeInfo.id());

        String nodeLogDir = new File(nodeInfo.logPath()).getParent();

        String javaParams = nodeInfo.parameterString();

        String javaHome = runCtx.dockerContext().javaHome(nodeInfo.nodeType());

        try {
            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, nodeLogDir);

            String startNodeCmd = String.format("%s/bin/java %s", javaHome, javaParams);

            String cmd = String.format("exec %s nohup %s > %s 2>& 1 &", contName, startNodeCmd, nodeInfo.logPath());

            runCtx.handler().runDockerCmd(nodeInfo.host(), mkdirCmd);

            runCtx.handler().runDockerCmd(nodeInfo.host(), cmd);
        }
        catch (IOException e) {
            log().error(String.format("Failed to start node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(CommandExecutionResult.emptyFailedResult());
        }

        nodeInfo.dockerInfo(new DockerInfo(null, contName));

        return nodeInfo;
    }
}
