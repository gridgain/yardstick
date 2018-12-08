package org.yardstickframework.runners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitNodeWorker extends NodeWorker {
    private NodeStatus expStatus;

    public WaitNodeWorker(RunContext runCtx, List<NodeInfo> nodeList,
        NodeStatus expStatus) {
        super(runCtx, nodeList);
        this.expStatus = expStatus;
    }

    private static Map<NodeStatus, String> statusMap;

    @Override public void beforeWork() {
        if(statusMap == null) {
            statusMap = new HashMap<>();

            statusMap.put(NodeStatus.RUNNING, "started");
            statusMap.put(NodeStatus.NOT_RUNNING, "stopped");
        }
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) {
        boolean unexpected = true;

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

        while (unexpected) {
            unexpected = false;

            NodeCheckResult res = (NodeCheckResult)checker.checkNode(nodeInfo);

            if (res.getNodeStatus() != expStatus) {
                unexpected = true;

                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        nodeInfo.nodeStatus(expStatus);

        log().info(String.format("Node %s%s on the host %s is %s.",
            nodeInfo.typeLow(),
            nodeInfo.getId(),
            nodeInfo.getHost(),
            statusMap.get(expStatus)));

        return nodeInfo;

    }
}
