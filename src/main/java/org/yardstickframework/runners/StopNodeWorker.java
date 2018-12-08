package org.yardstickframework.runners;

import java.util.List;

public class StopNodeWorker extends NodeWorker {

    public StopNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) {
        log().info(String.format("Stopping node %s%s on the host %s.",
            nodeInfo.typeLow(),
            nodeInfo.getId(),
            nodeInfo.getHost()));

        new KillWorker(runCtx, null).killNode(nodeInfo);

        return null;
    }

    @Override public void afterWork() {
        if(!resNodeList().isEmpty()){
            NodeInfo nodeInfo = resNodeList().get(0);

            if(nodeInfo.runMode() == RunMode.DOCKER)
                log().info("Keeping docker containers running.");
        }
    }
}
