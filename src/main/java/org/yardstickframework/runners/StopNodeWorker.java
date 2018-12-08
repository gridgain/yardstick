package org.yardstickframework.runners;

public class StopNodeWorker extends NodeWorker {

    public StopNodeWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(NodeInfo nodeInfo) {
        log().info(String.format("Stopping node %s%s on the host %s.",
            nodeInfo.typeLow(),
            nodeInfo.getId(),
            nodeInfo.getHost()));

        new KillWorker(runCtx, null).killNode(nodeInfo);


        return null;
    }

    @Override public void afterWork() {
        if(!getWorkCtx().getList().isEmpty()){
            NodeInfo nodeInfo = (NodeInfo) getWorkCtx().getList().get(0);

            if(nodeInfo.getStartCtx().getRunMode() == RunMode.DOCKER)
                log().info("Keeping docker containers running.");
        }
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
