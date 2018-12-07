package org.yardstickframework.runners;

import java.util.List;

public class RestartNodeWorkContext extends CommonWorkContext{

    private NodeStatus expStatus;

    public RestartNodeWorkContext(List<?> nodeInfoList, NodeStatus expStatus) {
        super(nodeInfoList);

        this.expStatus = expStatus;
    }

    public NodeStatus getExpStatus() {
        return expStatus;
    }
}
