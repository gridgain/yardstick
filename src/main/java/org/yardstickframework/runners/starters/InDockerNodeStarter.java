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

package org.yardstickframework.runners.starters;

import java.io.File;
import java.io.IOException;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.DockerInfo;
import org.yardstickframework.runners.context.RunContext;

import static org.yardstickframework.BenchmarkUtils.getJava;

/**
 * Starts nodes in docker containers.
 */
public class InDockerNodeStarter extends NodeStarter {
    /**
     *
     * @param runCtx Run context.
     */
    public InDockerNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException {
        String contName = String.format("%s_%s", runCtx.dockerContext().contNamePrefix(nodeInfo.nodeType()),
            nodeInfo.id());

        String nodeLogDir = new File(nodeInfo.logPath()).getParent();

        String javaParams = nodeInfo.parameterString();

        String javaHome = runCtx.dockerContext().javaHome(nodeInfo.nodeType());

        try {
            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, nodeLogDir);

            String startNodeCmd = String.format("%s %s", getJava(javaHome), javaParams);

            String cmd = String.format("exec --workdir %s %s nohup %s > %s 2>& 1 &",
                runCtx.remoteWorkDirectory(), contName,  startNodeCmd, nodeInfo.logPath());

            runCtx.handler().runDockerCmd(nodeInfo.host(), mkdirCmd);

            runCtx.handler().runDockerCmd(nodeInfo.host(), cmd);
        }
        catch (IOException e) {
            log().error(String.format("Failed to start node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(CommandExecutionResult.emptyFailedResult());
        }

        nodeInfo.dockerInfo(new DockerInfo(null, contName));

        return nodeInfo;
    }
}
