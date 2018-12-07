package org.yardstickframework.runners;

import java.util.HashMap;
import java.util.Map;

public class RestartNodeWorker extends NodeServiceWorker {

    public RestartNodeWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {

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

        return null;

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
