package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Set;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

/**
 * Builds docker images on hosts.
 */
public class DockerBuildImagesWorker extends DockerHostWorker {
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

        if (!checkIfImageExists(host, nameToUse) || dockerCtx.isRebuildImagesIfExist()) {
            String docFilePath = dockerCtx.getNodeContext(nodeType).getDockerfilePath();

            String src = dockerCtx.getDockerBuildCmd();

            try {
                String buildCmd = src.replace(IMAGE_NAME_PLACEHOLDER, nameToUse)
                    .replace(DOCKERFILE_PATH_PLACEHOLDER, docFilePath);

                runCtx.handler().runDockerCmd(host, buildCmd);
            }
            catch (IOException | InterruptedException e) {
                log().error(String.format("Failed to build images on the host '%s'", host), e);
            }
        }

        return null;
    }
}
