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

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

/**
 * Collects data from docker containers/
 */
public class DockerCollectWorker extends NodeWorker {
    /** {@inheritDoc} */
    public DockerCollectWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        NodeType type = nodeInfo.nodeType();

        String host = nodeInfo.host();

        String id = nodeInfo.id();

        String contNamePref = runCtx.dockerContext().contNamePrefix(type);

        String contName = String.format("%s_%s", contNamePref, id);

        String nodeOutDir = String.format("%s/output", runCtx.remoteWorkDirectory());

        String cpCmd = String.format("cp %s:%s/output %s", contName, runCtx.remoteWorkDirectory(), runCtx.remoteWorkDirectory());

        log().info(String.format("Collecting data from the container '%s' on the host '%s'.", contName, host));

        try {
            String mkdirCmd = String.format("mkdir -p %s", nodeOutDir);

            runCtx.handler().runCmd(host, mkdirCmd);

            runCtx.handler().runDockerCmd(host, cpCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to collect data from container '%s' on the host '%s'",
                contName, host), e);

            runCtx.exitCode(1);
        }

        return nodeInfo;
    }
}
