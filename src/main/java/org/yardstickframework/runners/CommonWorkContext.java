package org.yardstickframework.runners;

import java.util.List;

public class CommonWorkContext implements WorkContext {
    private List<?> list;

    public CommonWorkContext(List<?> list) {
        this.list = list;
    }

    @Override public List<?> getList() {
        return list;
    }
}
