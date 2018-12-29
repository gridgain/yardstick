package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Set;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Builds docker images on hosts.
 */
public class DockerBuildImagesWorker extends DockerHostWorker {
    /** */
    private static final String DFLT_BUILD_CMD = "build --no-cache -t IMAGE_NAME_PLACEHOLDER -f " +
        "DOCKERFILE_PATH_PLACEHOLDER .";

    /** */
    private static final String IMAGE_NAME_PLACEHOLDER = "IMAGE_NAME_PLACEHOLDER";

    /** */
    private static final String DOCKERFILE_PATH_PLACEHOLDER = "DOCKERFILE_PATH_PLACEHOLDER";

    /** */
    private NodeType nodeType;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param hostSet Main host list to work with.
     * @param nodeType Node type
     */
    public DockerBuildImagesWorker(RunContext runCtx, Set<String> hostSet,
        NodeType nodeType) {
        super(runCtx, hostSet);
        this.nodeType = nodeType;
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        String nameToUse = getImageNameToUse(nodeType);

        CheckWorkResult res = new CheckWorkResult();

        if (!checkIfImageExists(host, nameToUse)) {
            log().info(String.format("Building docker image '%s' on the host '%s'.", nameToUse,
                host));

            String docFilePath = runCtx.resolveRemotePath(dockerCtx.getNodeContext(nodeType).getDockerfilePath());

            if(!runCtx.handler().checkRemFile(host, docFilePath)){
                log().error(String.format("Failed to find file '%s' on the host '%s'. Cannot build docker image.",
                    docFilePath, host));

                res.exit(true);

                return res;
            }

            String src = dockerCtx.getDockerBuildCmd() != null ? dockerCtx.getDockerBuildCmd() :
                DFLT_BUILD_CMD;

            String buildCmd = src.replace(IMAGE_NAME_PLACEHOLDER, nameToUse)
                .replace(DOCKERFILE_PATH_PLACEHOLDER, docFilePath);

            try {
                runCtx.handler().runDockerCmd(host, buildCmd);

                if(checkIfImageExists(host, nameToUse))
                    log().info(String.format("Successfully built image '%s' on the host '%s'.", nameToUse,
                        host));
//                else {
//                    log().error(String.format("Failed to build image '%s' on the host '%s'.", nameToUse, host));
//
//                    runCtx.exitCode(1);
//
//                    res.exit(true);
//                }
            }
            catch (IOException | InterruptedException e) {
                log().error(String.format("Failed to build image '%s' on the host '%s'.", nameToUse, host), e);
            }
        }
        else
            log().info(String.format("Found image '%s' on the host '%s'.", nameToUse, host));

        return res;
    }
}
