package org.yardstickframework.runners;

import java.util.List;

public class CommonWorkContext implements WorkContext {
    private List<String> hostList;

    public CommonWorkContext(List<String> hostList) {
        this.hostList = hostList;
    }

    @Override public List<String> getHostList() {
        return hostList;
    }
}
