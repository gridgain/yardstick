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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.checkers.NodeChecker;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;

/**
 * Class waiting for node to start or stop.
 */
public class WaitNodeWorker extends NodeWorker {
    /** */
    private NodeStatus expStatus;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     * @param expStatus {@code NodeStatus} status to wait for.
     */
    public WaitNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, NodeStatus expStatus) {
        super(runCtx, nodeList);
        this.expStatus = expStatus;
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        boolean exp = false;

        NodeChecker checker = runCtx.nodeChecker(nodeInfo);

        int waitCnt = 0;

        boolean noMoreWaiting = false;

        int fullTime = (int) (Integer.valueOf(runCtx.warmup()) + Integer.valueOf(runCtx.duration()));

        double warnTime = fullTime * 1.25;

        while (!exp && !noMoreWaiting) {
            checker.checkNode(nodeInfo);

            if (nodeInfo.nodeStatus() == expStatus) {
                exp = true;

                noMoreWaiting = true;
            }
            else{
                waitCnt++;

                if (expStatus == NodeStatus.RUNNING){
                    if (waitCnt > 10){
                        log().error(String.format("Node '%s' is not running after %d seconds.",
                            nodeInfo.toShortStr(), 10));

                        noMoreWaiting = true;
                    }
                }
                else{
                    if (nodeInfo.nodeType() == NodeType.DRIVER){
                        if (waitCnt > warnTime && waitCnt % 30 == 0){
                            log().info(String.format("Combined warmup and duration time for node '%s' is %d but " +
                                "node still running after %d seconds.", nodeInfo.toShortStr(), fullTime, waitCnt));
                        }
                    }
                }

                new CountDownLatch(1).await(1000L, TimeUnit.MILLISECONDS);
            }
        }

        String status = nodeInfo.nodeStatus().toString().toLowerCase().replace("_", " ");

        log().info(String.format("Node '%s' on the host '%s' is %s.",
            nodeInfo.toShortStr(),
            nodeInfo.host(),
            status));

        return nodeInfo;
    }

    /** {@inheritDoc} */
    @Override public void afterWork() {
        if (!nodeList().isEmpty()) {
            NodeInfo nodeInfo = nodeList().get(0);

            if (nodeInfo.runMode() == RunMode.DOCKER
                && expStatus == NodeStatus.NOT_RUNNING
                && nodeInfo.nodeType() == NodeType.SERVER)
                log().info("Keeping docker containers running.");
        }
    }
}
