package org.yardstickframework.runners.starters;

import org.yardstickframework.runners.context.NodeInfo;

public interface NodeStarter {
    public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException;
}
