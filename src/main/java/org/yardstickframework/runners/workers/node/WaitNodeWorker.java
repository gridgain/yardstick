package org.yardstickframework.runners.workers.node;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.checkers.NodeChecker;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Class waiting for node to start or stop.
 */
public class WaitNodeWorker extends NodeWorker {
    /** */
    private NodeStatus expStatus;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     * @param expStatus {@code NodeStatus} status to wait for.
     */
    public WaitNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, NodeStatus expStatus) {
        super(runCtx, nodeList);
        this.expStatus = expStatus;
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        boolean unexpected = true;

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

        while (unexpected) {
            checker.checkNode(nodeInfo);

            if (nodeInfo.nodeStatus() == expStatus) {
                unexpected = false;

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
