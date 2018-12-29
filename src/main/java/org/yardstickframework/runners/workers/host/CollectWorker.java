package org.yardstickframework.runners.workers.host;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Collects data from remote hosts.
 */
public class CollectWorker extends HostWorker {
    /** Main output directory */
    private File outDir;

    /** {@inheritDoc} */
    public CollectWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);

        outDir = new File(String.format("%s/output", runCtx.localeWorkDirectory()));
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        if (!outDir.exists())
            outDir.mkdirs();
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        if (isLocal(host) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            return null;

        String nodeOutDir = String.format("%s/output", runCtx.remoteWorkDirectory());

        log().info(String.format("Collecting data from the host '%s'.", host));

        String pathLoc = outDir.getAbsolutePath();

        try {
            String pathRem = String.format("%s/*", nodeOutDir);

            runCtx.handler().download(host, pathLoc, pathRem);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to collect data from the host '%s'", host), e);

            runCtx.exitCode(1);
        }

        return null;
    }
}
