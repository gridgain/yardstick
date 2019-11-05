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
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.context.RunContext;

/**
 * Terminates nodes.
 */
public class StopNodeWorker extends NodeWorker {
    /** {@inheritDoc} */
    public StopNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        log().info(String.format("Stopping node '%s' on the host '%s'.",
            nodeInfo.toShortStr(),
            nodeInfo.host()));

        try {
            return stopNode(nodeInfo);
        }
        catch (IOException e) {
            log().error(String.format("Failed to stop node '%s' on the host '%s'",
                nodeInfo.toShortStr(),
                nodeInfo.host()), e);
        }

        return nodeInfo;
    }

    /**
     * @param nodeInfo Node info.
     * @return Node  info.
     * @throws IOException if failed.
     * @throws InterruptedException if interrupted.
     */
    public NodeInfo stopNode(NodeInfo nodeInfo) throws IOException, InterruptedException {

        nodeInfo = runCtx.handler().killNode(nodeInfo);

        return nodeInfo;
    }
}
