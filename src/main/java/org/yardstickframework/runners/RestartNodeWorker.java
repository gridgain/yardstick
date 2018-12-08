package org.yardstickframework.runners;

import java.util.List;

public class RestartNodeWorker extends StartNodeWorker {
    public RestartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList, cfgFullStr);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers_restarted", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers_restarted", baseLogDirFullName);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) {
        String host = nodeInfo.getHost();

        String id = nodeInfo.getId();

        NodeType type = nodeInfo.getNodeType();

        if (runCtx.getRestartContext(type) == null) {
            log().debug(String.format("No restarter scheduled for %s nodes.", nodeInfo.typeLow()));

            return nodeInfo;
        }

        RestartContext restCtx = runCtx.getRestartContext(type);

        if (restCtx.get(host) == null || restCtx.get(host).get(id) == null) {
            log().debug(String.format("No restarter scheduled for %s.%s node.", nodeInfo.typeLow(), nodeInfo.getId()));

            return nodeInfo;
        }

        RestartInfo restartInfo = runCtx.getRestartContext(type).get(host).get(id);

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(restartInfo.delay());

                new KillWorker(runCtx, null).killNode(nodeInfo);

                Thread.sleep(restartInfo.pause());

                startNode(nodeInfo);
            }
            catch (InterruptedException e) {

            }

        }

        return null;

    }
}
