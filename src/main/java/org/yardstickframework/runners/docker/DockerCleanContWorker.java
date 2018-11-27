package org.yardstickframework.runners.docker;

import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerCleanContWorker extends DockerWorker{
    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerCleanContWorker(RunContext runCtx,
        WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        removeContainers(host);

        return null;
    }

    @Override public String getWorkerName() {
        return null;
    }
}
