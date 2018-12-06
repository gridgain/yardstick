package org.yardstickframework.runners;

import java.io.File;
import org.yardstickframework.BenchmarkUtils;

public class CheckConnWorker extends Worker{

    @Override public void beforeWork() {
        //NO_OP
    }

    public CheckConnWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }
    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        CommandHandler hndl = new CommandHandler(runCtx);

        log().info(String.format("Checking ssh connection to the host %s", host));

        if(!hndl.checkConn(host)){
            log().info(String.format("Failed to establish connection to the host %s.", host));

            res.exit(true);
        }

        return res;
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
