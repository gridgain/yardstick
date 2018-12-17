package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RestartSchedule;
import org.yardstickframework.runners.context.RestartContext;
import org.yardstickframework.runners.context.RunContext;

/**
 * Restart nodes.
 */
public class RestartNodeWorker extends StartNodeWorker {
    /** {@inheritDoc} */
    public RestartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList, cfgFullStr);
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers_restarted", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers_restarted", baseLogDirFullName);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        String host = nodeInfo.host();

        String id = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        if (runCtx.restartContext(type) == null) {
            log().debug(String.format("No restart schedule for %s nodes.", nodeInfo.typeLow()));

            return nodeInfo;
        }

        RestartContext restCtx = runCtx.restartContext(type);

        if (restCtx.get(host) == null || restCtx.get(host).get(id) == null) {
            log().debug(String.format("No restart schedule for '%s' node.", nodeInfo.toShortStr()));

            return nodeInfo;
        }

        RestartSchedule restartInfo = runCtx.restartContext(type).get(host).get(id);

        log().info(String.format("Restart schedule for the node '%s' on the host '%s': %s.",
            nodeInfo.toShortStr(),
            host,
            restartInfo));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                new CountDownLatch(1).await(restartInfo.delay(), TimeUnit.MILLISECONDS);

                log().info(String.format("Stopping node '%s' on the host '%s'.", nodeInfo.toShortStr(), host));

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
                log().error(String.format("Failed to restart node '%s'", nodeInfo.toShortStr()), e);
            }

            log().debug("Restart worker stopped.");
        }

        return nodeInfo;
    }
}
