package org.yardstickframework.runners;

public class RestartNodeWorker extends NodeWorker {

    public RestartNodeWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {

    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(NodeInfo nodeInfo) {
        String host = nodeInfo.getHost();

        String id = nodeInfo.getId();

        NodeType type = nodeInfo.getNodeType();

        RestartNodeWorkContext workCtx = (RestartNodeWorkContext)getWorkCtx();

        RestartInfo restartInfo = runCtx.getRestartContext(type).get(host).get(id);

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(restartInfo.delay());

                new KillWorker(runCtx, null).killNode(nodeInfo);

                Thread.sleep(restartInfo.pause());
            }
            catch (InterruptedException e){

            }


        }

        return null;

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
