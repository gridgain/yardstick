package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

public class DockerCheckWorker extends CheckWorker {
    public DockerCheckWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CommandHandler hand = new CommandHandler(runCtx);

//        log().info(String.format("Checking docker on the host %s.", host));

        CommandExecutionResult res = null;

        try {
            res = hand.runDockerCmd(host, "images");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        CheckWorkResult workRes = new CheckWorkResult();

        if(res.getExitCode() != 0 || !res.getErrStream().isEmpty()){
            for(String err : res.getErrStream())
                log().error(err);

            workRes.exit(true);
        }

        return workRes;
    }
}
