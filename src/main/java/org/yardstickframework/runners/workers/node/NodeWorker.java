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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.Worker;

/**
 * Parent class for node workers.
 */
public abstract class NodeWorker extends Worker {
    /** Main list of NodeInfo objects to work with. */
    private List<NodeInfo> nodeList;

    /** Result list */
    private List<NodeInfo> resNodeList;

    /** Flag indicating whether or not tasks should run on the same host at the same time. */
    private boolean runAsyncOnHost;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     */
    NodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx);
        this.nodeList = new ArrayList<>(nodeList);
        resNodeList = new ArrayList<>(nodeList.size());
    }

    /**
     * Executes actual work for node.
     *
     * @param nodeInfo {@code NodeInfo} object to work with.
     * @return {@code NodeInfo} result.
     * @throws InterruptedException if interrupted.
     */
    public abstract NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException;

    /**
     * @return {@code int} Number of objects in the main list.
     */
    int getNodeListSize() {
        return nodeList.size();
    }

    /**
     * Executes doWork() method defined in worker class asynchronously.
     *
     * @return List of NodeInfo objects.
     */
    public List<NodeInfo> workForNodes() {
        beforeWork();

        if (nodeList.isEmpty())
            return nodeList;

        ExecutorService exec = Executors.newFixedThreadPool(nodeList.size());

        Collection<Future<NodeInfo>> futList = new ArrayList<>(nodeList.size());

        final Map<String, Semaphore> semMap = new HashMap<>();

        for (final NodeInfo nodeInfo : nodeList)
            semMap.put(nodeInfo.host(), new Semaphore(1));

        for (final NodeInfo nodeInfo : nodeList) {
            futList.add(exec.submit(new Callable<NodeInfo>() {
                @Override public NodeInfo call() throws Exception {
                    String host = nodeInfo.host();

                    Thread.currentThread().setName(threadName(nodeInfo));

                    if (!runAsyncOnHost)
                        semMap.get(host).acquire();

                    final NodeInfo res = doWork(nodeInfo);

                    semMap.get(host).release();

                    return res;
                }
            }));
        }

        for (Future<NodeInfo> f : futList) {
            try {
                NodeInfo nodeInfo = f.get();

                resNodeList.add(nodeInfo);
            }
            catch (InterruptedException e) {
                for (Future<NodeInfo> f0 : futList)
                    f0.cancel(true);

                Thread.currentThread().interrupt();

                log().info(String.format("%s stopped.", workerName()));

                log().debug(e.getMessage(), e);

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        exec.shutdown();

        afterWork();

        for(NodeInfo nodeInfo : resNodeList){
            if(nodeInfo.commandExecutionResult() != null && nodeInfo.commandExecutionResult().exitCode() != 0)
                System.exit(nodeInfo.commandExecutionResult().exitCode());
        }

        return new ArrayList<>(resNodeList);
    }

    /**
     * @return Response node list.
     */
    public List<NodeInfo> resNodeList() {
        return new ArrayList<>(resNodeList);
    }

    /**
     * @return Node list.
     */
    public List<NodeInfo> nodeList() {
        return new ArrayList<>(nodeList);
    }

    /**
     * @return Run async on host.
     */
    public boolean runAsyncOnHost() {
        return runAsyncOnHost;
    }

    /**
     * @param runAsyncOnHost New run async on host.
     */
    public void runAsyncOnHost(boolean runAsyncOnHost) {
        this.runAsyncOnHost = runAsyncOnHost;
    }
}
