package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.context.RunContext;

public class StopNodeWorker extends NodeWorker {

    public StopNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        log().info(String.format("Stopping node '%s' on the host '%s'.",
            nodeInfo.toShortStr(),
            nodeInfo.host()));

        try {
            return stopNode(nodeInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }

    public NodeInfo stopNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        CommandHandler hand = new CommandHandler(runCtx);

        nodeInfo = hand.killNode(nodeInfo);

        return nodeInfo;
    }

    @Override public void afterWork() {
        if (!nodeList().isEmpty()) {
            NodeInfo nodeInfo = nodeList().get(0);

            if (nodeInfo.runMode() == RunMode.DOCKER)
                log().info("Keeping docker containers running.");
        }
    }
}
