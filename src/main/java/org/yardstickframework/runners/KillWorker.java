package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class KillWorker extends Worker{

    public KillWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {

        String killServCmd = String.format("ssh -o StrictHostKeyChecking=no %s pkill -9 -f \"Dyardstick.server\"", ip);

        runCmd(killServCmd);

        String killDrvrCmd = String.format("ssh -o StrictHostKeyChecking=no %s pkill -9 -f \"Dyardstick.driver\"", ip);

        runCmd(killDrvrCmd);

        return null;
    }

    public WorkResult killNode(NodeInfo nodeInfo){
        String killCmd = nodeInfo.getDockerInfo() != null ?
            String.format("ssh -o StrictHostKeyChecking=no %s docker stop %s",
                nodeInfo.getHost(), nodeInfo.getDockerInfo().getContName()):
            String.format("ssh -o StrictHostKeyChecking=no %s pkill -9 -f \"Dyardstick.%s%s \"",
                nodeInfo.getHost(), nodeInfo.getNodeType(), nodeInfo.getId());

        System.out.println(String.format("Runing kill cmd: %s", killCmd));

        runCmd(killCmd);

        return nodeInfo;
    }
}
