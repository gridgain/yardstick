package org.yardstickframework.runners;

public class NodeCheckResult implements WorkResult{

    private NodeStatus nodeStatus;

    public NodeCheckResult(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
