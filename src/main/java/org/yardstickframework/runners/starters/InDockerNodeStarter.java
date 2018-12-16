package org.yardstickframework.runners.starters;

import java.io.File;
import java.io.IOException;
import org.yardstickframework.runners.AbstractRunner;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.DockerInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Starts nodes in docker containers.
 */
public class InDockerNodeStarter extends AbstractRunner implements NodeStarter {
    /**
     *
     * @param runCtx Run context.
     */
    public InDockerNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException {
        String contName = String.format("YARDSTICK_%s_%s", nodeInfo.nodeType(), nodeInfo.id());

        String nodeLogDir = new File(nodeInfo.logPath()).getParent();

        CommandHandler hand = new CommandHandler(runCtx);

        String javaParams = nodeInfo.parameterString();

        String javaHome = runCtx.dockerContext().javaHome(nodeInfo.nodeType());

        try {
            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, nodeLogDir);

            String startNodeCmd = String.format("%s/bin/java %s", javaHome, javaParams);

            String cmd = String.format("exec %s nohup %s > %s 2>& 1 &", contName, startNodeCmd, nodeInfo.logPath());

            hand.runDockerCmd(nodeInfo.host(), mkdirCmd);

            hand.runDockerCmd(nodeInfo.host(), cmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        nodeInfo.dockerInfo(new DockerInfo(null, contName));

        return nodeInfo;
    }
}
