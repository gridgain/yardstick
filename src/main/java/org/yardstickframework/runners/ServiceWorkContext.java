package org.yardstickframework.runners;

import java.util.List;

public class ServiceWorkContext implements WorkContext {
    private List<? extends WorkResult> nodeInfoList;

    public ServiceWorkContext(List<? extends WorkResult> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }

    @Override public List<? extends WorkResult> getList() {
        return nodeInfoList;
    }
}
