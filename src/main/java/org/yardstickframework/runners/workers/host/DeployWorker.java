package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public class DeployWorker extends HostWorker {
    protected String[] toDeploy = new String[]{"bin", "config", "libs"};

    protected String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};

    public DeployWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if ((isLocal(host) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            || host.equals(runCtx.currentHost()) && runCtx.localeWorkDirectory().equals(runCtx.remoteWorkDirectory()))
            return null;


        String createCmd = String.format("mkdir -p %s", runCtx.remoteWorkDirectory());

        CommandHandler hand = new CommandHandler(runCtx);

        try {
            hand.runCmd(host, createCmd);

            for(String name : toClean){
                String cleanCmd = String.format("rm -rf %s/%s",
                    runCtx.remoteWorkDirectory(), name);

                hand.runCmd(host, cleanCmd);
            }

            log().info(String.format("Deploying on the host '%s'.", host));

            for(String name : toDeploy) {
                String fullPath = Paths.get(runCtx.remoteWorkDirectory(), name).toAbsolutePath().toString();

                if(hand.checkRemFile(host, fullPath)) {
                    try {
                        hand.runMkdirCmd(host, fullPath);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String locPath = String.format("%s/%s", runCtx.localeWorkDirectory(), name);

                hand.upload(host, locPath, runCtx.remoteWorkDirectory());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
