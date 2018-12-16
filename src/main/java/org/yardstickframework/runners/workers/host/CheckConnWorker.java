package org.yardstickframework.runners.workers.host;

import java.util.List;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public class CheckConnWorker extends CheckWorker {
    public CheckConnWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CheckWorkResult res = new CheckWorkResult();

        CommandHandler hand = new CommandHandler(runCtx);

        log().info(String.format("Checking ssh connection to the host '%s'.", host));

        if(!hand.checkConn(host)){
            log().info(String.format("Failed to establish connection to the host '%s'.", host));

            res.exit(true);
        }

        return res;
    }
}
