package org.yardstickframework.runners.checkers;

import org.yardstickframework.runners.context.NodeInfo;

/**
 * Node checker.
 */
public interface NodeChecker {
    /**
     * Sets {@code NodeInfo.nodeStatus} to NodeStatus.RUNNING if node is running or NodeStatus.NOT_RUNNING otherwise.
     *
     * @param nodeInfo {@code Nodeinfo} object.
     * @return {@code Nodeinfo} object.
     * @throws InterruptedException
     */
    public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException;
}
