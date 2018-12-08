package org.yardstickframework.runners.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;

import org.yardstickframework.runners.WorkResult;

public class DockerBuildImagesWorker extends DockerHostWorker {

    private NodeType nodeType;

    public DockerBuildImagesWorker(RunContext runCtx, List<String> hostList,
        NodeType nodeType) {
        super(runCtx, hostList);
        this.nodeType = nodeType;
    }

    @Override public WorkResult doWork(String host, int cnt) {
        String nameToUse = getImageNameToUse(nodeType);

        if(!checkIfImageExists(host, nameToUse)  || dockerCtx.isRebuildImagesIfExist()) {
            String docFilePath = nodeType == NodeType.SERVER ?
                RunContext.resolveRemotePath(dockerCtx.getServerDockerfilePath()):
                RunContext.resolveRemotePath(dockerCtx.getDriverDockerfilePath());

            CommandHandler hndl = new CommandHandler(runCtx);

//            log().info(String.format("Building the image '%s' on the host %s.", nameToUse, host));


//            System.out.println(buildCmd);

            try {
                String buildCmd = String.format("build --no-cache -t %s -f %s .",nameToUse, docFilePath);

                hndl.runDockerCmd(host, buildCmd);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
