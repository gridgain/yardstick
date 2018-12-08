package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.BenchmarkUtils;

public abstract class NodeWorker extends HostWorker{
    private List<NodeInfo> nodeList;

    private List<NodeInfo> resNodeList;

    /** */
    NodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx);
        this.nodeList = new ArrayList<>(nodeList);
        resNodeList = new ArrayList<>(nodeList.size());
    }

    public abstract NodeInfo doWork(NodeInfo nodeInfo);

    /**
     * Executes start method defined in worker class asynchronously.
     *
     */
    protected List<NodeInfo> workForNodes() {
        beforeWork();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<NodeInfo>> futList = new ArrayList<>();

        for (Object nodeInfoObj : nodeList) {
            NodeInfo nodeInfo = (NodeInfo) nodeInfoObj;

            futList.add(execServ.submit(new Callable<NodeInfo>() {
                @Override public NodeInfo call() throws Exception {
                    Thread.currentThread().setName(String.format("%s-%s",
                        getWorkerName(), nodeInfo.getHost()));

                    return doWork(nodeInfo);
                }
            }));
        }

        for (Future<NodeInfo> f : futList) {
            try {
                resNodeList.add(f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return new ArrayList<>(resNodeList);
    }
}
