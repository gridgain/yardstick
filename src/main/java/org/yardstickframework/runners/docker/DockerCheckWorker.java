package org.yardstickframework.runners.docker;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;

import org.yardstickframework.runners.WorkResult;

public class DockerCheckWorker extends DockerHostWorker{
    public DockerCheckWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CommandHandler hndl = new CommandHandler(runCtx);

//        log().info(String.format("Checking docker on the host %s.", host));

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
