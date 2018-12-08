package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.RunContext;

import org.yardstickframework.runners.WorkResult;

public class DockerCleanContWorker extends DockerHostWorker{
    public DockerCleanContWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        removeContainers(host);

        return null;
    }

    @Override public String getWorkerName() {
        return this.getClass().getSimpleName();
    }
}
