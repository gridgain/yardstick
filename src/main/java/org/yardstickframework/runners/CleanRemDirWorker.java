package org.yardstickframework.runners;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

public class CleanRemDirWorker extends HostWorker{

    protected String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};


    public CleanRemDirWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        if ((isLocal(host) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            || host.equals(runCtx.getCurrentHost()) && runCtx.getLocWorkDir().equals(runCtx.getRemWorkDir()))
            return null;

        String remDir =  runCtx.getRemWorkDir();

        log().info(String.format("Cleaning up directory '%s' on the host '%s'", remDir, host));

        CommandHandler hndl = new CommandHandler(runCtx);

        try {
            for(String name : toClean){
                String cleanCmd = String.format("rm -rf %s/%s",
                    runCtx.getRemWorkDir(), name);

                hndl.runCmd(host, cleanCmd);
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
}
