package org.yardstickframework.runners;

import java.util.List;

public class ServiceWorkContext implements WorkContext {
    private List<NodeInfo> nodeInfoList;

    public ServiceWorkContext(List<NodeInfo> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }

    @Override public List<NodeInfo> getList() {
        return nodeInfoList;
    }
}
