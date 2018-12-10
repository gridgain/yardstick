package org.yardstickframework.runners;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public class DeployWorker extends HostWorker{
    protected String[] toDeploy = new String[]{"bin", "config", "libs"};

    protected String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};

    public DeployWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if ((isLocal(host) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            || host.equals(runCtx.getCurrentHost()) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;


        String createCmd = String.format("mkdir -p %s", runCtx.getRemWorkDir());

        CommandHandler hndl = new CommandHandler(runCtx);

        try {
            hndl.runCmd(host, createCmd);

            for(String name : toClean){
                String cleanCmd = String.format("rm -rf %s/%s",
                    runCtx.getRemWorkDir(), name);

                hndl.runCmd(host, cleanCmd);
            }

            log().info(String.format("Deploying on the host '%s'.", host));

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

                String locPath = String.format("%s/%s", runCtx.getLocWorkDir(), name);

                hndl.upload(host, locPath, runCtx.getRemWorkDir());
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
