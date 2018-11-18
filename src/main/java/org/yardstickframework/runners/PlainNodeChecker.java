package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;

public class PlainNodeChecker extends AbstractRunner implements NodeChecker {
    public PlainNodeChecker(Properties runProps) {
        super(runProps);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        String checkCmd = String.format("ssh -o StrictHostKeyChecking=no %s pgrep -f \"Dyardstick.%s%s \"",
            nodeInfo.getHost(), nodeInfo.getNodeType().toString().toLowerCase(),
            nodeInfo.getId());

        List<String> resList = runCmd(checkCmd);

        if(!resList.isEmpty())
            return new NodeCheckResult(NodeStatus.ACTIVE);
        else
            return new NodeCheckResult(NodeStatus.NOT_EXIST);
    }
}
