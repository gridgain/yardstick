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

package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker for docker run.
 */
public class InDockerNodeChecker extends NodeChecker {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public InDockerNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.host();

        String checkCmd = String.format("ps -ax|grep java", nodeInfo.dockerInfo().contName());

        CommandExecutionResult res = CommandExecutionResult.emptyFailedResult();

        try {
            res = runCtx.handler().runCmd(host, checkCmd);
        }
        catch (IOException e) {
            log().error(String.format("Failed to check node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(res);
        }

        String toLook = String.format("-Dyardstick.%s ", nodeInfo.toShortStr());

        if(runCtx.handler().checkList(res.outputList(), toLook))
            nodeInfo.nodeStatus(NodeStatus.RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);

        return nodeInfo;
    }
}
