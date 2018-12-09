package org.yardstickframework.runners;

public interface NodeChecker {
    public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException;
}
