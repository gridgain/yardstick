package org.yardstickframework.runners.workers.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.workers.Worker;
import org.yardstickframework.runners.context.RunContext;

/**
 * Parent for host workers.
 */
public abstract class HostWorker extends Worker {
    /** Main host list to work with. */
    private final List<String> hostList;

    /** Result list. */
    private final List<WorkResult> resList;

    /**
     * Getter for result list.
     *
     * @return Result list.
     */
    List<WorkResult> resList() {
        return new ArrayList<>(resList);
    }

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param hostList Main host list to work with.
     */
    HostWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx);
        this.hostList = new ArrayList<>(hostList);

        resList = new ArrayList<>(hostList.size());
    }

    /**
     * Actual work method which every worker must implement.
     *
     * @param host Host.
     * @param cnt Number in host list.
     * @return Work result.
     */
    public abstract WorkResult doWork(String host, int cnt);

    /**
     * Executes doWork() method defined in worker class asynchronously.
    *
     * @return List of {@code WorkResult} objects.
     */
    public List<WorkResult> workOnHosts() {
        beforeWork();

        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        for (int cnt = 0; cnt < hostList.size(); cnt++) {
            final int cntF = cnt;

            final String host = hostList.get(cntF);

            futList.add(exec.submit(new Callable<WorkResult>() {
                @Override public WorkResult call() throws Exception {
                    Thread.currentThread().setName(String.format("%s-%s", workerName(), host));

                    return doWork(host, cntF);
                }
            }));
        }

        for (Future<WorkResult> f : futList) {
            try {
                resList.add(f.get());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        exec.shutdown();

        afterWork();

        return new ArrayList<>(resList);
    }
}
