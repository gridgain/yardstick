package org.yardstickframework.runners.docker;

import java.io.IOException;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerCollectWorker extends DockerWorker{
    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerCollectWorker(RunContext runCtx,
        WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        NodeType type = dockerWorkCtx.getNodeType();

        String contName = String.format("YARDSTICK_%s_%d", type, cnt);

        String nodeOutDir = String.format("%s/output", runCtx.getRemWorkDir());

        String mkdirCmd = String.format("mkdir -p %s", nodeOutDir);

        String cpCmd = String.format("cp %s:%s/output %s", contName, runCtx.getRemWorkDir(), runCtx.getRemWorkDir());

        CommandHandler hndl = new CommandHandler(runCtx);

//        System.out.println(cpCmd);

        log().info(String.format("Collecting data from the container %s on the host %s.", contName, host));

        try {
//            hndl.runDockerCmd(host, mkdirCmd);

            hndl.runDockerCmd(host, cpCmd);
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
