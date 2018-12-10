package org.yardstickframework.runners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaitNodeWorker extends NodeWorker {
    private NodeStatus expStatus;

    public WaitNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, NodeStatus expStatus) {
        super(runCtx, nodeList);
        this.expStatus = expStatus;
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        boolean unexpected = true;

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

        while (unexpected) {
            unexpected = false;

            checker.checkNode(nodeInfo);

            if (nodeInfo.nodeStatus() != expStatus) {
                unexpected = true;

                new CountDownLatch(1).await(1000L, TimeUnit.MILLISECONDS);
            }
        }

        nodeInfo.nodeStatus(expStatus);

        String status = expStatus.toString().toLowerCase().replace("_", " ");

        log().info(String.format("Node '%s' on the host '%s' is %s.",
            nodeInfo.toShortStr(),
            nodeInfo.getHost(),
            status));

        return nodeInfo;
    }
}
