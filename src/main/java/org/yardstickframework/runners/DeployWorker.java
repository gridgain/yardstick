package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class DeployWorker extends Worker{

    public DeployWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {

        String createCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, getMainDir());

        runCmd(createCmd);

        BenchmarkUtils.println(String.format("Deploying on the host %s", ip));

        for(String name : toClean){
            String cleanCmd = String.format("ssh -o StrictHostKeyChecking=no %s rm -rf %s/%s",
                ip, getMainDir(), name);

            runCmd(cleanCmd);
        }

        for(String name : toDeploy) {
            String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s/%s %s:%s",
                getMainDir(), name, ip, getMainDir());

            runCmd(cpCmd);
        }

        return null;

    }
}
