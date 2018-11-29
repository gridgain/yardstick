package org.yardstickframework.runners;

import java.io.IOException;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class KillWorker extends Worker{

    public KillWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        CommandHandler hndl = new CommandHandler(runCtx);

        String killServCmd = "pkill -9 -f \"Dyardstick.server\"";
        String killDrvrCmd = "pkill -9 -f \"Dyardstick.driver\"";

        try {
            hndl.runCmd(ip, killServCmd, "");

            hndl.runCmd(ip, killDrvrCmd, "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    public WorkResult killNode(NodeInfo nodeInfo){
        String actualKillCmd = String.format("pkill -9 -f \"Dyardstick.%s%s \"",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId());

        String killCmd = nodeInfo.getStartCtx().getRunMode() == RunMode.DOCKER ?
            String.format("ssh -o StrictHostKeyChecking=no %s docker exec %s %s",
                nodeInfo.getHost(), nodeInfo.getDockerInfo().getContName(), actualKillCmd):
            String.format("ssh -o StrictHostKeyChecking=no %s %s",
                nodeInfo.getHost(), actualKillCmd);

        BenchmarkUtils.println(String.format("Killing node -Dyardstick.%s-%s",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId()));
//        BenchmarkUtils.println(String.format("Running kill cmd: %s", killCmd));

        runCmd(killCmd);

        return nodeInfo;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
