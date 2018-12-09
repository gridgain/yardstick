package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class HostWorker extends Worker{
    private final List<String> hostList;

    private final List<WorkResult> resList;

    /** */
    protected HostWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx);
        this.hostList = new ArrayList<>(hostList);

        resList = new ArrayList<>(hostList.size());
    }

    public abstract WorkResult doWork(String host, int cnt);

    /**
     * Executes start method defined in worker class asynchronously.
     *
     */
    protected List<WorkResult> workOnHosts() {
        beforeWork();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        String lastHost = null;

        for (int cntr = 0; cntr < hostList.size(); cntr++) {
            final int cntrF = cntr;

            final String host = hostList.get(cntrF);

            if(host.equals(lastHost)){
                try {
                    new CountDownLatch(1).await(1000L, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lastHost = host;

            futList.add(execServ.submit(new Callable<WorkResult>() {
                @Override public WorkResult call() throws Exception {
                    Thread.currentThread().setName(String.format("%s-%s",
                        getWorkerName(), host));

                    return doWork(host, cntrF);
                }
            }));
        }

        for (Future<WorkResult> f : futList) {
            try {
                resList.add(f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return resList;
    }
}
