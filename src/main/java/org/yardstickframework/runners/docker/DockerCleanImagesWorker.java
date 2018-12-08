package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.RunContext;

import org.yardstickframework.runners.WorkResult;

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
