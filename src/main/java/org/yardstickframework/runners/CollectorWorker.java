package org.yardstickframework.runners;

import java.io.File;
import org.yardstickframework.BenchmarkUtils;

public class CollectorWorker extends Worker{

    public CollectorWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }
    @Override public WorkResult doWork(String ip, int cnt) {
        if (ip.equals("localhost") && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;

        File outDir = new File(String.format("%s/output", runCtx.getRemWorkDir()));

        synchronized (this) {
            if (!outDir.exists())
                outDir.mkdirs();
        }

        String nodeOutDir = String.format("%s/output", runCtx.getRemWorkDir());


        String collectCmd = String.format("scp -r -o StrictHostKeyChecking=no %s:%s/* %s",
            ip, nodeOutDir, outDir.getAbsolutePath());

        BenchmarkUtils.println(String.format("Running cp from host cmd: %s", collectCmd));

        runCmd(collectCmd);

        return null;

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
