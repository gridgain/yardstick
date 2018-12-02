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

public abstract class Worker extends AbstractRunner{
    protected WorkContext workCtx;

    /** */
    public Worker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx);
        this.workCtx = workCtx;
    }

    public abstract WorkResult doWork(String ip, int cnt);

    public abstract String getWorkerName();

    public WorkContext getWorkCtx(){
        return workCtx;
    }

    /**
     * Executes before workOnHosts()
     */
    public void beforeWork(){
        BenchmarkUtils.println(String.format("%s started.", getClass().getSimpleName()));
    }

    /**
     * Executes start method defined in worker class asynchronously.
     *
     */
    protected List<WorkResult> workOnHosts() {
        beforeWork();

        final List<String> hostList = workCtx.getHostList();

        List<WorkResult> res = new ArrayList<>(hostList.size());

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        String lastHost = null;

        for (int cntr = 0; cntr < hostList.size(); cntr++) {
            final int cntrF = cntr;

            final String host = hostList.get(cntrF);

            if(host.equals(lastHost)){
                try {
                    Thread.sleep(1000L);
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
        BenchmarkUtils.println(String.format("%s finished.", getClass().getSimpleName()));
    }


}
