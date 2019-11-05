/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners.workers.node;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.DockerInfo;
import org.yardstickframework.runners.DockerRunner;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.DockerContext;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.DockerBuildImagesWorker;

/**
 * Starts docker containers.
 */
public class DockerStartContWorker extends NodeWorker {
    /** */
    private static final String DFLT_RUN_CMD =
        "run -d --network host --name CONT_NAME_PLACEHOLDER IMAGE_NAME_PLACEHOLDER sleep 1d";

    /** */
    private static final String CONT_NAME_PLACEHOLDER = "CONT_NAME_PLACEHOLDER";

    /** */
    private static final String IMAGE_NAME_PLACEHOLDER = "IMAGE_NAME_PLACEHOLDER";

    /** */
    private DockerContext dockerCtx;

    /** {@inheritDoc} */
    public DockerStartContWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);

        dockerCtx = runCtx.dockerContext();
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        String host = nodeInfo.host();

        String id = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String imageName = dockerCtx.getImageName(type);

        String contName = "Undefined";

        try {
            CommandExecutionResult res = CommandExecutionResult.emptyFailedResult();

            DockerBuildImagesWorker imageWorker = new DockerBuildImagesWorker(runCtx, new TreeSet<>(), nodeInfo.nodeType());

            if (!imageWorker.checkIfImageExists(host, imageName)) {
                log().error(String.format("Image '%s' does not exist on the host '%s'. Cannot start containers.",
                    imageName, host));

                runCtx.exitCode(1);

                return nodeInfo;
            }

            String contNamePref = dockerCtx.contNamePrefix(type);

            contName = String.format("%s_%s", contNamePref, id);

            DockerInfo docInfo = new DockerInfo(imageName, contName);

            nodeInfo.dockerInfo(docInfo);

            log().info(String.format("Starting the container '%s' on the host '%s'", contName, host));

            String startContCmd = getStartContCmd(imageName, contName);

            res = runCtx.handler().runDockerCmd(host, startContCmd);

            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, runCtx.remoteWorkDirectory());

            res = runCtx.handler().runDockerCmd(host, mkdirCmd);

            String remPath = runCtx.remoteWorkDirectory();

            String parentPath = new File(remPath).getParentFile().getAbsolutePath();

            String cpCmd = String.format("cp %s %s:%s", remPath, contName, parentPath);

            res = runCtx.handler().runDockerCmd(host, cpCmd);

            nodeInfo.commandExecutionResult(res);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to start container '%s' on the host '%s'",
                contName, host), e);

            runCtx.exitCode(1);

            nodeInfo.commandExecutionResult(CommandExecutionResult.emptyFailedResult());

            return nodeInfo;
        }

        return nodeInfo;
    }

    /**
     * @param imageName Image name.
     * @param contName Container name.
     * @return Command to start docker container.
     */
    private String getStartContCmd(String imageName, String contName) {
        String withPlaceHolders = dockerCtx.getDockerRunCmd() != null ? dockerCtx.getDockerRunCmd() : DFLT_RUN_CMD;

        String withImageName = withPlaceHolders.replace(IMAGE_NAME_PLACEHOLDER, imageName);

        return withImageName.replace(CONT_NAME_PLACEHOLDER, contName);
    }

    /** {@inheritDoc} */
    @Override public void afterWork() {
        if (runCtx.exitCode() != 0)
            System.exit(runCtx.exitCode());

        if (!resNodeList().isEmpty()) {
            try {
                setDockerJavaHome(resNodeList().get(0));
            }
            catch (IOException | InterruptedException e) {
                log().error(String.format("Failed to set Java home for the node type'%s'",
                    resNodeList().get(0).nodeType()), e);
            }
        }
    }

    /**
     * @param nodeInfo Node info.
     * @throws IOException if failed.
     * @throws InterruptedException if interrupted.
     */
    private void setDockerJavaHome(NodeInfo nodeInfo) throws IOException, InterruptedException {
        NodeType type = nodeInfo.nodeType();

        String contName = nodeInfo.dockerInfo().contName();

        String echoCmd = String.format("exec %s sh -c 'echo $JAVA_HOME'", contName);

        String host = nodeInfo.host();

        CommandExecutionResult res = runCtx.handler().runDockerCmd(host, echoCmd);

        if (!res.outputList().isEmpty()) {
            String javaHome = res.outputList().get(0);

            switch (type) {
                case SERVER:
                    if (dockerCtx.serverDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for server nodes: '%s'",
                            javaHome));

                        dockerCtx.serverDockerJavaHome(javaHome);
                    }
                    break;
                case DRIVER:
                    if (dockerCtx.driverDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for driver nodes: '%s'",
                            javaHome));

                        dockerCtx.driverDockerJavaHome(javaHome);
                    }
                    break;
                default:
                    log().info("Unknown node type " + type);
            }
        }
        else {
            log().info(String.format("Failed to get JAVA_HOME variable from docker container '%s'", contName));

            log().info("Will clean up docker and exit.");

            DockerRunner runner = new DockerRunner(runCtx);

            runner.cleanUp(runCtx.nodeTypes(RunMode.DOCKER), "after");

            System.exit(runCtx.exitCode());
        }

    }
}
