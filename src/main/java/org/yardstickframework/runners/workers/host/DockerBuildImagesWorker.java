package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

public class DockerBuildImagesWorker extends DockerHostWorker {
    /** */
    private static final String IMAGE_NAME_PLACEHOLDER = "IMAGE_NAME_PLACEHOLDER";

    /** */
    private static final String DOCKERFILE_PATH_PLACEHOLDER = "DOCKERFILE_PATH_PLACEHOLDER";

    /** */
    private NodeType nodeType;

    /**
     *
     * @param runCtx
     * @param hostList
     * @param nodeType
     */
    public DockerBuildImagesWorker(RunContext runCtx, List<String> hostList,
        NodeType nodeType) {
        super(runCtx, hostList);
        this.nodeType = nodeType;
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        String nameToUse = getImageNameToUse(nodeType);

        if(!checkIfImageExists(host, nameToUse)  || dockerCtx.isRebuildImagesIfExist()) {
            String docFilePath = nodeType == NodeType.SERVER ?
                runCtx.resolveRemotePath(dockerCtx.getServerDockerfilePath()):
                runCtx.resolveRemotePath(dockerCtx.getDriverDockerfilePath());

            CommandHandler hand = new CommandHandler(runCtx);

//            log().info(String.format("Building the image '%s' on the host %s.", nameToUse, host));


//            System.out.println(buildCmd);

            String src = dockerCtx.getDockerBuildCmd();

            String buildCmd = src.replace(IMAGE_NAME_PLACEHOLDER, nameToUse)
                .replace(DOCKERFILE_PATH_PLACEHOLDER, docFilePath);

            try {
//                String buildCmd = String.format("build --no-cache -t %s -f %s .",nameToUse, docFilePath);

                hand.runDockerCmd(host, buildCmd);
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
