package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerCleanImagesWorker extends DockerWorker {

    private List<String> imagesToClean;

    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerCleanImagesWorker(RunContext runCtx,
        WorkContext workCtx, List<String> imagesToClean) {
        super(runCtx, workCtx);

        this.imagesToClean = imagesToClean;
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host, imagesToClean);

        return null;
    }

    @Override public String getWorkerName() {
        return this.getClass().getSimpleName();
    }
}
