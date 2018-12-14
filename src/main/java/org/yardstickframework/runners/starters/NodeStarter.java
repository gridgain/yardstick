package org.yardstickframework.runners.starters;

import org.yardstickframework.runners.context.NodeInfo;

/**
 * Node starter.
 */
public interface NodeStarter {
    /**
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws InterruptedException if interrupted.
     */
    public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException;
}
