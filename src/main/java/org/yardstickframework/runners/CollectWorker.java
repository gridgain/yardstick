package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;

public class CollectWorker extends HostWorker{

    public CollectWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
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

        log().info(String.format("Collecting data from the host %s.", host));

        CommandHandler hndl = new CommandHandler(runCtx);

        String pathRem = String.format("%s/*", nodeOutDir);

        String pathLoc = outDir.getAbsolutePath();

        try {
            hndl.download(host, pathRem, pathLoc);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }
}
