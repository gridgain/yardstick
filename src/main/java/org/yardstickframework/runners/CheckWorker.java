package org.yardstickframework.runners;

import java.io.File;
import org.yardstickframework.BenchmarkUtils;

public class CheckWorker extends Worker{

    public CheckWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        CommandHandler hndl = new CommandHandler(runCtx);

        if(!hndl.checkConn(host)){
            res.getErrMsgs().add(String.format("Failed to establish connection to the host %s.", host));

            return res;
        }

        if(runCtx.getRemJavaHome() != null && !hndl.checkRemJava(host, runCtx.getRemJavaHome())){
            res.getErrMsgs().add(String.format("Failed find %s/bin/java on the host %s.",
                runCtx.getRemJavaHome(), host));

            return res;
        }


        return res;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
