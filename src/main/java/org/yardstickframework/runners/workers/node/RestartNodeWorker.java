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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RestartSchedule;
import org.yardstickframework.runners.context.RestartContext;
import org.yardstickframework.runners.context.RunContext;

/**
 * Restart nodes.
 */
public class RestartNodeWorker extends StartNodeWorker {
    /** */
    private long startTime;

    /** {@inheritDoc} */
    public RestartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList, cfgFullStr);
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        super.beforeWork();

        startTime = System.currentTimeMillis();

        servLogDirFullName = Paths.get(baseLogDirFullName, "log_servers_restarted").toString();

        drvrLogDirFullName = Paths.get(baseLogDirFullName, "log_drivers_restarted").toString();
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        String host = nodeInfo.host();

        String id = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        if (runCtx.restartContext(type) == null) {
            log().debug(String.format("No restart schedule for %s nodes.", nodeInfo.typeLow()));

            return nodeInfo;
        }

        RestartContext restCtx = runCtx.restartContext(type);

        if (restCtx.get(host) == null || restCtx.get(host).get(id) == null) {
            log().info(String.format("No restart schedule for '%s' node.", nodeInfo.toShortStr()));

            return nodeInfo;
        }

        RestartSchedule restartInfo = runCtx.restartContext(type).get(host).get(id);

        log().info(String.format("Restart schedule for the node '%s' on the host '%s': %s.",
            nodeInfo.toShortStr(),
            host,
            restartInfo));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                new CountDownLatch(1).await(restartInfo.delay(), TimeUnit.MILLISECONDS);

                log().info(String.format("Stopping node '%s' on the host '%s'.", nodeInfo.toShortStr(), host));

                StopNodeWorker stopWorker = new StopNodeWorker(runCtx, Collections.singletonList(nodeInfo));

                stopWorker.stopNode(nodeInfo);

                new CountDownLatch(1).await(restartInfo.pause(), TimeUnit.MILLISECONDS);

                long currTime = System.currentTimeMillis();

                // Set new duration only for DRIVER nodes to avoid stopping server restarts due to small duration.
                long newDuration = nodeInfo.nodeType() == NodeType.DRIVER ?
                    initDuration - ((currTime - startTime) / 1000) :
                    initDuration;

                if(newDuration <= 0)
                    break;

                // Set warmup to '0' for restarted node.
                warmup("0");

                duration(String.valueOf(newDuration));

                startNode(nodeInfo);

                if(newDuration * 1000L <= restartInfo.delay() + restartInfo.period())
                    break;

                new CountDownLatch(1).await(restartInfo.period(), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ignored) {
                log().debug("Restart worker stopped.");

                break;
            }
            catch (IOException e) {
                log().error(String.format("Failed to restart node '%s'", nodeInfo.toShortStr()), e);
            }

            log().debug("Restart worker stopped.");
        }

        return nodeInfo;
    }
}
