package org.yardstickframework.runners;

import java.util.HashMap;
import java.util.Map;

public class WaitNodeWorker extends NodeWorker {

    public WaitNodeWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    private static Map<NodeStatus, String> statusMap;

    @Override public void beforeWork() {
        if(statusMap == null) {
            statusMap = new HashMap<>();

            statusMap.put(NodeStatus.RUNNING, "started");
            statusMap.put(NodeStatus.NOT_RUNNING, "stopped");
        }
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(NodeInfo nodeInfo) {
        boolean unexpected = true;

        WaitNodeWorkContext workCtx = (WaitNodeWorkContext)getWorkCtx();

        NodeStatus expStatus = workCtx.getExpStatus();

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

//        log().info(String.format("Waiting for node %s%s on the host %s to be %s.",
//            nodeInfo.typeLow(),
//            nodeInfo.getId(),
//            nodeInfo.getHost(),
//            statusMap.get(expStatus)));

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

        log().info(String.format("Node %s%s on the host %s is %s.",
            nodeInfo.typeLow(),
            nodeInfo.getId(),
            nodeInfo.getHost(),
            statusMap.get(expStatus)));

        return null;

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
