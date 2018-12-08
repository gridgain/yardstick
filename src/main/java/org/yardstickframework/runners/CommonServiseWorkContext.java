package org.yardstickframework.runners;

import java.util.List;

public class CommonServiseWorkContext implements WorkContext {
    private List<NodeInfo> list;

    public CommonServiseWorkContext(List<NodeInfo> list) {
        this.list = list;
    }

    @Override public List<NodeInfo> getList() {
        return list;
    }
}
