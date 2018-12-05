package org.yardstickframework.runners;

import java.util.List;

public class WaitNodeWorkContext extends CommonWorkContext{

    private NodeStatus expStatus;

    public WaitNodeWorkContext(List<?> nodeInfoList, NodeStatus expStatus) {
        super(nodeInfoList);

        this.expStatus = expStatus;
    }

    public NodeStatus getExpStatus() {
        return expStatus;
    }
}
