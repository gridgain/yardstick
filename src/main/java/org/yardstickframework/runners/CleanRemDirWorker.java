package org.yardstickframework.runners;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public class CleanRemDirWorker extends Worker{
    /** */
    private static final Logger LOG = LogManager.getLogger(CleanRemDirWorker.class);

    public CleanRemDirWorker(RunContext runCtx, WorkContext workCtx) {
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

        String remDir =  runCtx.getRemWorkDir();

        log().info(String.format("Cleaning up directory %s on the host %s", remDir, host));

        for(String name : toClean){
            String cleanCmd = String.format("ssh -o StrictHostKeyChecking=no %s rm -rf %s/%s",
                host, remDir, name);

            runCmd(cleanCmd);
        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
