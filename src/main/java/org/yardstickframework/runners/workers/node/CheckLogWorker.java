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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.checkers.NodeChecker;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Checks node log.
 */
public class CheckLogWorker extends NodeWorker {
    /** {@inheritDoc} */
    public CheckLogWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    /** */
    private static final String[] keyWords = new String[]{"Exception", "failed", "Error"};

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        String host = nodeInfo.host();

        String logPath = nodeInfo.logPath();

        boolean fileExists = false;

        int cnt = 10;

        while (!fileExists && cnt-- > 0) {
            fileExists = runCtx.handler().checkRemFile(host, logPath);

            new CountDownLatch(1).await(1000L, TimeUnit.MILLISECONDS);
        }

        NodeChecker checker = runCtx.nodeChecker(nodeInfo);

        checker.checkNode(nodeInfo);

        if (nodeInfo.nodeStatus() == NodeStatus.NOT_RUNNING)
            log().info(String.format("Node '%s' on the host '%s' in not running. Will check log file and exit.",
                nodeInfo.toShortStr(),
                host));

        if (!fileExists) {
            log().info(String.format("No log file '%s' on the host '%s'.", logPath, host));

            return nodeInfo;
        }

        try {
            CommandExecutionResult res = runCtx.handler().runGrepCmd(host, logPath, Arrays.asList(keyWords));

            if (!res.outputList().isEmpty()) {
                nodeInfo.errorMessages().addAll(res.outputList());

                log().warn(String.format("Log file '%s' contains following error messages:",
                    logPath));

                for (String msg : res.outputList())
                    log().error(msg);

                return nodeInfo;
            }
        }
        catch (IOException e) {
            log().error(String.format("Failed to check log for the node '%s' on the host '%s'",
                nodeInfo.toShortStr(),
                host), e);
        }

        return nodeInfo;
    }
}
