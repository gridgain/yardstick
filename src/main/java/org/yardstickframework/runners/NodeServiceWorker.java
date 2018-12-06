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

public abstract class NodeServiceWorker extends AbstractRunner{
    protected WorkContext workCtx;

    /** */
    public NodeServiceWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx);
        this.workCtx = workCtx;
    }

    public abstract WorkResult doWork(NodeInfo nodeInfo);

    public abstract String getWorkerName();

    public WorkContext getWorkCtx(){
        return workCtx;
    }

    /**
     * Executes before workOnHosts()
     */
    public void beforeWork(){
        log().info(String.format("%s started.", getClass().getSimpleName()));
    }

    /**
     * Executes start method defined in worker class asynchronously.
     *
     */
    protected List<WorkResult> workForNodes() {
        beforeWork();

        final List<?> nodeInfoList = workCtx.getList();

        List<WorkResult> res = new ArrayList<>(nodeInfoList.size());

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        String lastHost = null;

        for (Object nodeInfoObj : nodeInfoList) {
            NodeInfo nodeInfo = (NodeInfo) nodeInfoObj;

            futList.add(execServ.submit(new Callable<WorkResult>() {
                @Override public WorkResult call() throws Exception {
                    Thread.currentThread().setName(String.format("%s-%s",
                        getWorkerName(), nodeInfo.getHost()));

                    return doWork(nodeInfo);
                }
            }));
        }

        for (Future<WorkResult> f : futList) {
            try {
                res.add(f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return res;
    }

    protected boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    public void afterWork(){
        log().info(String.format("%s finished.", getClass().getSimpleName()));
    }


}
