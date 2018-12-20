package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Cleans remote directory.
 */
public class CleanRemDirWorker extends HostWorker {
    /** */
    private String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};

    /** {@inheritDoc} */
    public CleanRemDirWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        return clean(host);
    }

    /**
     *
     * @param host Host
     * @return Work result.
     */
    private WorkResult clean(String host){
        if ((isLocal(host) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            || host.equals(runCtx.currentHost()) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            return null;

        String remDir =  runCtx.remoteWorkDirectory();

        log().info(String.format("Cleaning up directory '%s' on the host '%s'", remDir, host));



        try {
            for(String name : toClean){
                String cleanCmd = String.format("rm -rf %s/%s",
                    runCtx.remoteWorkDirectory(), name);

                runCtx.handler().runCmd(host, cleanCmd);
            }

        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to clean remote directory on the host '%s'", host), e);
        }

        return null;
    }
}
