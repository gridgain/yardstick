package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class DeployWorker extends Worker{

    public DeployWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }
    @Override public WorkResult doWork(String host, int cnt) {
        if (isLocal(host) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;

        String createCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", host, runCtx.getRemWorkDir());

        runCmd(createCmd);

        BenchmarkUtils.println(String.format("Deploying on the host %s", host));

        for(String name : toClean){
            String cleanCmd = String.format("ssh -o StrictHostKeyChecking=no %s rm -rf %s/%s",
                host, runCtx.getRemWorkDir(), name);

            runCmd(cleanCmd);
        }

        for(String name : toDeploy) {
            String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s/%s %s:%s",
                runCtx.getRemWorkDir(), name, host, runCtx.getRemWorkDir());

            runCmd(cpCmd);
        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
