package org.yardstickframework.runners;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public class DeployWorker extends HostWorker{
    /** */
    private static final Logger LOG = LogManager.getLogger(DeployWorker.class);

    public DeployWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if ((isLocal(host) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            || host.equals(runCtx.getCurrentHost()) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;


        String createCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", host, runCtx.getRemWorkDir());

        runCmd(createCmd);

        log().info(String.format("Deploying on the host %s", host));

        for(String name : toClean){
            String cleanCmd = String.format("ssh -o StrictHostKeyChecking=no %s rm -rf %s/%s",
                host, runCtx.getRemWorkDir(), name);

            runCmd(cleanCmd);
        }

        CommandHandler hndl = new CommandHandler(runCtx);


        for(String name : toDeploy) {
            String fullPath = Paths.get(runCtx.getRemWorkDir(), name).toAbsolutePath().toString();

            if(hndl.checkRemFile(host, fullPath)) {
                try {
                    hndl.runMkdirCmd(host, fullPath);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s/%s %s:%s",
                runCtx.getLocWorkDir(), name, host, runCtx.getRemWorkDir());

            runCmd(cpCmd);
        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
