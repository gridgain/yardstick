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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.yardstickframework.runners.Runner;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Starts nodes.
 */
public class PlainNodeStarter extends NodeStarter {
    /** */
    private static Map<String, String> hostJavaHomeMap = new HashMap<>();

    /**
     *
     * @param runCtx Run context.
     */
    public PlainNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.host();

        String param = nodeInfo.parameterString();

        String javaHome = runCtx.getHostJava(host);

        CommandExecutionResult res = null;

        try {
            String withJavaHome = String.format("%s/bin/java %s", javaHome, param);

            res = runCtx.handler().startNode(host, withJavaHome, nodeInfo.logPath());

            nodeInfo.commandExecutionResult(res);
        }
        catch (IOException e) {
            log().error(String.format("Failed to start node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(CommandExecutionResult.emptyFailedResult());
        }

        return nodeInfo;
    }

    /**
     *
     * @param host Host.
     * @return Host Java home path.
     */
    private String getHostJavaHome(String host) {
        return runCtx.handler().getHostJavaHome(host);
    }

    /**
     *
     * @param host Host.
     */
    private void printNoJavaError(String host) {
        log().info(String.format("Failed to get default JAVA_HOME variable from the host %s", host));
        log().info(String.format("Will not start node on the host %s", host));

    }

    /**
     *
     * @param host Host.
     * @return {@code boolean} true if host already been checked or {@code false} otherwise.
     */
    private boolean checked(String host) {
        return hostJavaHomeMap.containsKey(host);
    }
}
