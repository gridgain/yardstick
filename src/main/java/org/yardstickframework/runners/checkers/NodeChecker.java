package org.yardstickframework.runners.checkers;

import org.yardstickframework.runners.context.NodeInfo;

public interface NodeChecker {
    public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException;
}
