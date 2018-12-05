package org.yardstickframework.runners.docker;

import java.io.IOException;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerCheckWorker extends DockerWorker{
    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerCheckWorker(RunContext runCtx,
        WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CommandHandler hndl = new CommandHandler(runCtx);

        BenchmarkUtils.println(String.format("Checking docker on the host %s.", host));

        CommandExecutionResult res = null;

        try {
            res = hndl.runDockerCmd(host, "images");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        DockerCheckWorkResult workRes = new DockerCheckWorkResult();

        if(res.getExitCode() != 0 || !res.getErrStream().isEmpty()){
            workRes.getErrMsgs().add("Command 'docker images' returned non 0 exit code.");

            workRes.getErrMsgs().addAll(res.getErrStream());
        }

        return workRes;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
