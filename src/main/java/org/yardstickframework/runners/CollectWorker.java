package org.yardstickframework.runners;

import java.io.File;
import org.yardstickframework.BenchmarkUtils;

public class CollectWorker extends Worker{

    public CollectWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if (isLocal(host) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;

        File outDir = new File(String.format("%s/output", runCtx.getLocWorkDir()));

        synchronized (this) {
            if (!outDir.exists())
                outDir.mkdirs();
        }

        String nodeOutDir = String.format("%s/output", runCtx.getRemWorkDir());


        String collectCmd = String.format("scp -r -o StrictHostKeyChecking=no %s:%s/* %s",
            host, nodeOutDir, outDir.getAbsolutePath());

        log().info(String.format("Collecting data from the host %s.", host));

        runCmd(collectCmd);

        return null;

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
