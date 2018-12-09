package org.yardstickframework.runners;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RestartNodeWorker extends StartNodeWorker {
    public RestartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList, cfgFullStr);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers_restarted", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers_restarted", baseLogDirFullName);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        while (!Thread.interrupted()) {
            try {
                String host = nodeInfo.getHost();

                String id = nodeInfo.getId();

                NodeType type = nodeInfo.getNodeType();

                if (runCtx.getRestartContext(type) == null) {
                    log().debug(String.format("No restart schedule for %s nodes.", nodeInfo.typeLow()));

                    return nodeInfo;
                }

                RestartContext restCtx = runCtx.getRestartContext(type);

                if (restCtx.get(host) == null || restCtx.get(host).get(id) == null) {
                    log().debug(String.format("No restart schedule for '%s' node.", nodeInfo.toShortStr()));

                    return nodeInfo;
                }

                RestartInfo restartInfo = runCtx.getRestartContext(type).get(host).get(id);

                new CountDownLatch(1).await(restartInfo.delay(), TimeUnit.MILLISECONDS);

                log().info(String.format("Stopping node '%s' on the host %s", nodeInfo.toShortStr(), host));

                StopNodeWorker stopWorker = new StopNodeWorker(runCtx, Collections.singletonList(nodeInfo));

                stopWorker.stopNode(nodeInfo);

                new CountDownLatch(1).await(restartInfo.pause(), TimeUnit.MILLISECONDS);

                startNode(nodeInfo);

                new CountDownLatch(1).await(restartInfo.period(), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ignored) {
                log().debug("Restart worker stopped.");

                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            log().debug("Restart worker stopped.");
        }

        return nodeInfo;
    }
}
