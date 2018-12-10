package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;

public class StopNodeWorker extends NodeWorker {

    public StopNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        log().info(String.format("Stopping node '%s' on the host '%s'.",
            nodeInfo.toShortStr(),
            nodeInfo.getHost()));

        try {
            return stopNode(nodeInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }

    public NodeInfo stopNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        CommandHandler hndl = new CommandHandler(runCtx);

        nodeInfo = hndl.killNode(nodeInfo);

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
