package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.BenchmarkUtils;

public abstract class NodeWorker extends Worker {
    private List<NodeInfo> nodeList;

    private List<NodeInfo> resNodeList;

    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();

    private boolean runAsyncOnHost;

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
     * Executes doWork() method defined in worker class asynchronously.
     */
    protected List<NodeInfo> workForNodes() {
        beforeWork();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<NodeInfo>> futList = new ArrayList<>();

        for (final NodeInfo nodeInfo : nodeList) {

            futList.add(execServ.submit(new Callable<NodeInfo>() {
                @Override public NodeInfo call() throws Exception {
                    Thread.currentThread().setName(threadName(nodeInfo));

                    if(!runAsyncOnHost)
                        awaitOnLatch(nodeInfo);

                    final NodeInfo res = doWork(nodeInfo);

                    releaseLatch(nodeInfo);

                    return res;
                }
            }));
        }

        for (Future<NodeInfo> f : futList) {
            try {
                NodeInfo nodeInfo = f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS);

                resNodeList.add(nodeInfo);
            }
            catch (InterruptedException e) {
                for (Future<NodeInfo> f0 : futList)
                    f0.cancel(true);

                Thread.currentThread().interrupt();

                log().info(String.format("%s stopped.", getWorkerName()));

                log().debug(e.getMessage(), e);

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return new ArrayList<>(resNodeList);
    }

    private void awaitOnLatch(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.getHost();

        CountDownLatch hostLatch = latchMap.putIfAbsent(host, new CountDownLatch(1));

        if(hostLatch != null) {
//                log().info(String.format("Thread %s waiting on latch %s", Thread.currentThread().getName(), hostLatch));

                hostLatch.await();

//                log().info(String.format("Thread %s cont", Thread.currentThread().getName()));

        }
    }

    private void releaseLatch(NodeInfo nodeInfo){
        String host = nodeInfo.getHost();

        CountDownLatch hostLatch = latchMap.get(host);

        if (hostLatch != null) {
//            log().info(String.format("Thread %s releasing latch %s", Thread.currentThread().getName(), hostLatch));


            hostLatch.countDown();
        }
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
