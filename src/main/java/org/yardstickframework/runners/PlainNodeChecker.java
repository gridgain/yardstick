package org.yardstickframework.runners;

import java.util.Properties;

public class PlainNodeChecker extends AbstractRunner implements NodeChecker {
    public PlainNodeChecker(Properties runProps) {
        super(runProps);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        return null;
    }
}
