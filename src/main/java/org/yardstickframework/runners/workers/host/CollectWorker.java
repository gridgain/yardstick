package org.yardstickframework.runners.workers.host;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public class CollectWorker extends HostWorker {
    private File outDir;

    public CollectWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);

        outDir = new File(String.format("%s/output", runCtx.localeWorkDirectory()));
    }

    @Override public void beforeWork() {
        if (!outDir.exists())
            outDir.mkdirs();
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if (isLocal(host) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            return null;

        String nodeOutDir = String.format("%s/output", runCtx.remoteWorkDirectory());

        log().info(String.format("Collecting data from the host '%s'.", host));

        CommandHandler hand = new CommandHandler(runCtx);

        String pathRem = String.format("%s/*", nodeOutDir);

        String pathLoc = outDir.getAbsolutePath();

        try {
            hand.download(host, pathRem, pathLoc);
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
