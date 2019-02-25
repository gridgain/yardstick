/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners.workers.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.TreeSet;
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
    private final Set<String> hostSet;

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
     * @param hostSet Main host list to work with.
     */
    HostWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx);
        this.hostSet = new TreeSet<>(hostSet);

        resList = new ArrayList<>(hostSet.size());
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
        if (hostSet.isEmpty())
            return new ArrayList<>();

        beforeWork();

        ExecutorService exec = Executors.newFixedThreadPool(Math.min(hostSet.size(),
            Runtime.getRuntime().availableProcessors()));

        Collection<Future<WorkResult>> futList = new ArrayList<>();

        List<String> hostList = new ArrayList<>(hostSet);

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
