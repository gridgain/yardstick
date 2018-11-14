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

    public abstract void doWork(String ip, String dateTime, int cnt, int total);

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
    protected void workOnHosts() {
        beforeWork();

        final String dateTime = getMainDateTime();

        final List<String> hostList = getHostList();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<?>> futList = new ArrayList<>();

        for (int cntr = 0; cntr < hostList.size(); cntr++) {
            final int cntrF = cntr;

            futList.add(execServ.submit(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Thread.currentThread().setName(String.format("Starter-%s", hostList.get(cntrF)));

                    doWork(hostList.get(cntrF), dateTime, cntrF, hostList.size());

                    return null;
                }
            }));
        }

        for (Future f : futList) {
            try {
                f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();
    }

    public void afterWork(){
        BenchmarkUtils.println(String.format("%s finished.", getClass().getSimpleName()));
    }


}
