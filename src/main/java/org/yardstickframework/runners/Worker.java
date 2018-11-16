package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.yardstickframework.BenchmarkUtils;

import static org.yardstickframework.BenchmarkUtils.dateTime;

public abstract class Worker extends AbstractRunner{
    public Worker(Properties runProps) {
        super(runProps);
    }

    public abstract WorkResult doWork(String ip, int cnt, int total, WorkContext workCtx);

    public abstract List<String> getHostList();

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
    protected Collection<WorkResult> workOnHosts(final WorkContext workCtx) {
        beforeWork();

        final List<String> hostList = getHostList();

        List<WorkResult> res = new ArrayList<>(hostList.size());

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        for (int cntr = 0; cntr < hostList.size(); cntr++) {
            final int cntrF = cntr;

            futList.add(execServ.submit(new Callable<WorkResult>() {
                @Override public WorkResult call() throws Exception {
                    Thread.currentThread().setName(String.format("Worker-%s", hostList.get(cntrF)));

                    return doWork(hostList.get(cntrF), cntrF, hostList.size(), workCtx);
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

    public void afterWork(){
        BenchmarkUtils.println(String.format("%s finished.", getClass().getSimpleName()));
    }


}
