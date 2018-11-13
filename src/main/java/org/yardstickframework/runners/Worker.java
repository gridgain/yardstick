package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.yardstickframework.BenchmarkUtils.dateTime;

public abstract class Worker extends AbstractRunner{
    public abstract void doWork(int cnt, String ip);

    public abstract List<String> getHostList();

    /**
     * Executes start method defined in worker class asynchronously.
     *
     */
    private void workOnHosts() {
        final String dateTime = dateTime();

        final List<String> hostList = getHostList();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<?>> futList = new ArrayList<>();

        for (int cntr = 0; cntr < hostList.size(); cntr++) {
            if (!checkHost(hostList.get(cntr))) {
                printer(String.format("Host ip '%s' does not match ip address pattern.", hostList.get(cntr)));

                continue;
            }

            final int cntrF = cntr;

            futList.add(execServ.submit(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Thread.currentThread().setName(String.format("Starter-%s", hostList.get(cntrF)));

                    start(args, hostList.get(cntrF), dateTime, cntrF, hostList.size(), null);

                    return null;
                }
            }));
        }

        for (Future f : futList) {
            try {
                f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            catch (TimeoutException e) {
                LOG.error("Abstract start worker: timeout is exceeded.", e);
            }
            catch (Exception e) {
                LOG.error("Failed to start worker.", e);
            }
        }

        execServ.shutdown();
    }


}
