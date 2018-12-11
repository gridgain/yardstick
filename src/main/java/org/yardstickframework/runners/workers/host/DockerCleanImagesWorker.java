package org.yardstickframework.runners.workers.host;

import java.util.List;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

public class DockerCleanImagesWorker extends DockerHostWorker {
    public DockerCleanImagesWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }

    @Override public String getWorkerName() {
        return this.getClass().getSimpleName();
    }
}
