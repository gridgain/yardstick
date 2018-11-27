package org.yardstickframework.runners.docker;

import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerCleanImagesWorker extends DockerWorker{
    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerCleanImagesWorker(RunContext runCtx,
        WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }

    @Override public String getWorkerName() {
        return null;
    }
}
