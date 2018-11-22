package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;

public class InDockerNodeChecker extends AbstractRunner implements NodeChecker {
    public InDockerNodeChecker(RunContext runCtx, StartNodeWorkContext ctx) {
        super(runCtx);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        String checkCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker exec %s pgrep -f \"Dyardstick.%s%s \"",
            nodeInfo.getHost(), nodeInfo.getDockerInfo().getContName(), nodeInfo.getNodeType().toString().toLowerCase(),
            nodeInfo.getId());

        List<String> resList = runCmd(checkCmd);

        if(!resList.isEmpty())
            return new NodeCheckResult(NodeStatus.RUNNING);
        else
            return new NodeCheckResult(NodeStatus.NOT_RUNNING);
    }
}
