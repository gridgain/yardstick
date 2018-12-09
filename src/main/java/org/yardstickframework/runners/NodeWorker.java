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

public abstract class NodeWorker extends Worker {
    private List<NodeInfo> nodeList;

    private List<NodeInfo> resNodeList;

    /** */
    public NodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx);
        this.nodeList = new ArrayList<>(nodeList);
        resNodeList = new ArrayList<>(nodeList.size());
    }

    public abstract NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException;

    protected int getNodeListSize() {
        return nodeList.size();
    }

    /**
     * Executes start method defined in worker class asynchronously.
     */
    protected List<NodeInfo> workForNodes() {
        beforeWork();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        ExecutorService execServ = Executors.newSingleThreadExecutor();

        Collection<Future<NodeInfo>> futList = new ArrayList<>();

        for (final NodeInfo nodeInfo : nodeList) {
            futList.add(execServ.submit(new Callable<NodeInfo>() {
                @Override public NodeInfo call() throws Exception {
                    String threadName = String.format("%s-%s",
                        getWorkerName(),
                        nodeInfo.getHost());

                    if (getWorkerName().startsWith("Restart"))
                        threadName = threadName + "-" + BenchmarkUtils.hms();

                    Thread.currentThread().setName(threadName);

                    return doWork(nodeInfo);
                }
            }));
        }

        for (Future<NodeInfo> f : futList) {
            try {
                resNodeList.add(f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS));
            }
            catch (InterruptedException e) {
                f.cancel(true);

                log().info(String.format("%s stopped.", getWorkerName()));

                log().debug(e.getMessage(), e);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return new ArrayList<>(resNodeList);
    }

    /**
     * @return Response node list.
     */
    public List<NodeInfo> resNodeList() {
        return resNodeList;
    }

    /**
     * @return Node list.
     */
    public List<NodeInfo> nodeList() {
        return nodeList;
    }
}
