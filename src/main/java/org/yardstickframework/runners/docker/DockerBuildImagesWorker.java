package org.yardstickframework.runners.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerBuildImagesWorker extends DockerWorker {

    public DockerBuildImagesWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        NodeType type = dockerWorkCtx.getNodeType();

        String nameToUse = getImageNameToUse(type);

        if(!checkIfImageExists(host, nameToUse)  || dockerCtx.isRebuildImagesIfExist()) {
            String docFilePath = type == NodeType.SERVER ?
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

    private List<String> getIdList(String ip){
        String getListCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", ip);

        String servContNamePref = String.format("SERVER-%s", ip);
        String drvrContNamePref = String.format("DRIVER-%s", ip);

        List<String> res = new ArrayList<>();

        List<String> responseList = runCmd(getListCmd);

        for(String response : responseList)
            if(response.contains(servContNamePref) || response.contains(drvrContNamePref))
                res.add(response.split(" ")[0]);

        return res;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
